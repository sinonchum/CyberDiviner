package com.cyberdiviner.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LiuyaoDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.model.DivinationReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val divinationDao: DivinationDao,
    private val liuyaoDao: LiuyaoDao,
    private val tarotDao: TarotDao
) : ViewModel() {

    val readings: StateFlow<List<DivinationReading>> = divinationDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Get hexagram name from liuyao_readings table */
    suspend fun getHexagramName(readingId: Long): String? {
        return try {
            liuyaoDao.getByReadingId(readingId)?.hexagramName
        } catch (e: Exception) { null }
    }

    /** Get first tarot card name from tarot_readings table */
    suspend fun getTarotCardSummary(readingId: Long): String? {
        return try {
            val reading = tarotDao.getByReadingId(readingId) ?: return null
            val json = reading.cardsJson
            // Extract first card_zh + isReversed
            val cardZh = Regex("\"card_zh\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            val reversed = Regex("\"isReversed\"\\s*:\\s*\"true\"").containsMatchIn(json)
            if (cardZh != null) cardZh + if (reversed) "逆位" else "正位"
            else null
        } catch (e: Exception) { null }
    }

    /** Get all tarot card names from tarot_readings table */
    suspend fun getTarotCardNames(readingId: Long): String? {
        return try {
            val reading = tarotDao.getByReadingId(readingId) ?: return null
            val names = Regex("\"card_zh\"\\s*:\\s*\"([^\"]+)\"").findAll(reading.cardsJson)
                .map { it.groupValues[1] }.toList()
            if (names.size > 1) names.joinToString(" · ") else null
        } catch (e: Exception) { null }
    }

    fun deleteReading(reading: DivinationReading) {
        viewModelScope.launch {
            divinationDao.delete(reading)
        }
    }
}
