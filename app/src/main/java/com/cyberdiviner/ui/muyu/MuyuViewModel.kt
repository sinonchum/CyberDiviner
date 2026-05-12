package com.cyberdiviner.ui.muyu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * Tracks hits, manages sessions, and persists data via Room.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MuyuViewModel @Inject constructor(
    private val muyuDao: MuyuDao
) : ViewModel() {

    private val _sessionId = MutableStateFlow(UUID.randomUUID().toString())
    /** Current session ID */
    val sessionId: StateFlow<String> = _sessionId.asStateFlow()

    /** Total hits across all sessions */
    val totalHits: StateFlow<Int> = muyuDao.getTotalHits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    /**
     * Register a wooden fish hit.
     * @param durationMs Duration of the hit sound in milliseconds
     */
    fun hit(durationMs: Int = 800) {
        _lastDurationMs.value = durationMs
        _hitTrigger.value++
        viewModelScope.launch {
            muyuDao.insert(
                MuyuHit(
                    timestamp = System.currentTimeMillis(),
                    durationMs = durationMs,
                    sessionId = _sessionId.value
                )
            )
        }
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
        newSession()
    }
}
