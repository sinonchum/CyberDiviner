package com.cyberdiviner.ui.muyu

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.R
import com.cyberdiviner.data.dao.MuyuDao
import com.cyberdiviner.data.model.MuyuHit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the 电子木鱼 (electronic wooden fish) meditation screen.
 *
 * Features:
 * - SoundPool audio playback with USAGE_ASSISTANCE_SONIFICATION / CONTENT_TYPE_SONIFICATION
 * - Light haptic feedback on each hit
 * - Room-backed totalHits persistence (via MuyuDao)
 * - SharedPreferences fast-cache for instant totalHits display on launch
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MuyuViewModel @Inject constructor(
    private val muyuDao: MuyuDao,
    application: Application
) : ViewModel() {

    companion object {
        private const val TAG = "MuyuViewModel"
        private const val PREFS_NAME = "muyu_prefs"
        private const val KEY_TOTAL_HITS_CACHE = "total_hits_cache"
    }

    // ── SoundPool ────────────────────────────────────────────────
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var soundLoaded = false

    // ── Haptic ───────────────────────────────────────────────────
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // ── SharedPreferences fast-cache ─────────────────────────────
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Fast-cached totalHits loaded from SharedPreferences on init.
     * Gets superseded by the Room Flow once it emits, but provides
     * instant display on screen load.
     */
    private val _totalHitsCached = MutableStateFlow(prefs.getInt(KEY_TOTAL_HITS_CACHE, 0))

    init {
        // Initialize SoundPool for percussion
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            soundLoaded = (status == 0)
            Log.d(TAG, "Sound loaded: sampleId=$sampleId, status=$status, loaded=$soundLoaded")
        }

        try {
            soundId = soundPool!!.load(application, R.raw.muyu, 1)
            Log.d(TAG, "Loading muyu.wav, soundId=$soundId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load muyu sound: ${e.message}", e)
        }
    }

    // ── Session state ────────────────────────────────────────────

    private val _sessionId = MutableStateFlow(UUID.randomUUID().toString())
    /** Current session ID */
    val sessionId: StateFlow<String> = _sessionId.asStateFlow()

    /**
     * Total hits across all sessions (from Room).
     * Falls back to the SharedPreferences cache until Room emits.
     */
    val totalHits: StateFlow<Int> = muyuDao.getTotalHits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _totalHitsCached.value)

    /** Hits in the current session — re-subscribes when sessionId changes */
    val sessionHits: StateFlow<Int> = _sessionId
        .flatMapLatest { id -> muyuDao.getHitCountForSession(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Animation trigger counter — incremented on each hit to trigger UI animation */
    private val _hitTrigger = MutableStateFlow(0L)
    val hitTrigger: StateFlow<Long> = _hitTrigger.asStateFlow()

    /** Sound duration hint (ms) for the current hit sound */
    private val _lastDurationMs = MutableStateFlow(0)
    val lastDurationMs: StateFlow<Int> = _lastDurationMs.asStateFlow()

    // ── Hit action ───────────────────────────────────────────────

    /**
     * Register a wooden fish hit.
     * - Plays the wooden fish sound via SoundPool
     * - Triggers a light haptic vibration
     * - Persists to Room and SharedPreferences cache
     * - Fires animation trigger
     *
     * @param durationMs Duration of the hit sound in milliseconds
     */
    fun hit(durationMs: Int = 800) {
        _lastDurationMs.value = durationMs
        _hitTrigger.value++

        // Play sound
        playSound()

        // Haptic feedback — light tap
        triggerHaptic()

        // Persist to Room
        viewModelScope.launch {
            muyuDao.insert(
                MuyuHit(
                    timestamp = System.currentTimeMillis(),
                    durationMs = durationMs,
                    sessionId = _sessionId.value
                )
            )
        }

        // Update SharedPreferences cache for instant display on next launch
        val newTotal = _totalHitsCached.value + 1
        _totalHitsCached.value = newTotal
        prefs.edit().putInt(KEY_TOTAL_HITS_CACHE, newTotal).apply()
    }

    /** Start a new meditation session */
    fun newSession() {
        _sessionId.value = UUID.randomUUID().toString()
    }

    /** Delete the current session's hit history */
    fun clearSession() {
        viewModelScope.launch {
            muyuDao.deleteSession(_sessionId.value)
        }
        newSession()
    }

    /** Delete all hit history */
    fun clearAll() {
        viewModelScope.launch {
            muyuDao.deleteAll()
        }
        // Reset SharedPreferences cache too
        prefs.edit().putInt(KEY_TOTAL_HITS_CACHE, 0).apply()
        _totalHitsCached.value = 0
        newSession()
    }

    // ── Internal helpers ─────────────────────────────────────────

    /**
     * Play the wooden fish strike sound.
     *
     * NOTE: The bundled muyu.wav (44KB, mono, 44100Hz, 16-bit PCM) is a very short/simple
     * WAV that doesn't fully capture the resonant, wooden tone of a real wooden fish.
     * For best results, replace it with a higher-quality recording of an actual wooden fish
     * strike that includes the natural decay and overtones.
     *
     * The playback rate is set to 0.9 to lower the pitch slightly, giving a deeper and
     * more resonant/wooden quality to the sound.
     */
    private fun playSound() {
        if (soundLoaded && soundId != 0) {
            try {
                soundPool?.play(
                    soundId,
                    0.95f,  // leftVolume — full resonance
                    0.95f,  // rightVolume
                    1,      // priority
                    0,      // loop (0 = no loop)
                    0.85f   // rate — slower for deeper, more wooden/resonant tone
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play sound: ${e.message}")
            }
        }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, 60))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic feedback failed: ${e.message}")
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        try {
            soundPool?.release()
            soundPool = null
            Log.d(TAG, "SoundPool released")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release SoundPool: ${e.message}")
        }
    }
}
