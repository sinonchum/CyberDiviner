package com.cyberdiviner.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.engine.AlmanacEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * HomeViewModel — 首屏 ViewModel for 赛博黄历
 *
 * Loads the daily almanac reading from [AlmanacEngine] and exposes
 * recent divination readings from the database. Also provides the
 * current Ganzhi date/time info and persona greeting.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val divinationDao: DivinationDao
) : ViewModel() {

    // ── Day Reading State ──────────────────────────────────────────────────

    private val _dayReading = MutableStateFlow<AlmanacEngine.DayReading?>(null)
    val dayReading: StateFlow<AlmanacEngine.DayReading?> = _dayReading.asStateFlow()

    private val _ganzhiDate = MutableStateFlow("")
    val ganzhiDate: StateFlow<String> = _ganzhiDate.asStateFlow()

    private val _shichenName = MutableStateFlow("")
    val shichenName: StateFlow<String> = _shichenName.asStateFlow()

    private val _currentDateFormatted = MutableStateFlow("")
    val currentDateFormatted: StateFlow<String> = _currentDateFormatted.asStateFlow()

    // ── Recent Readings ────────────────────────────────────────────────────

    val recentReadings: StateFlow<List<DivinationReading>> = divinationDao
        .getRecent(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Quick Stats ────────────────────────────────────────────────────────

    val totalCount: StateFlow<Int> = divinationDao
        .getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Greeting / Flavor ──────────────────────────────────────────────────

    private val _greeting = MutableStateFlow("")
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    // ── Init ───────────────────────────────────────────────────────────────

    init {
        loadDailyData()
    }

    // ── Actions ────────────────────────────────────────────────────────────

    fun refreshDayReading() {
        loadDailyData()
    }

    // ── Private ────────────────────────────────────────────────────────────

    private fun loadDailyData() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val time = LocalTime.now()

            // Almanac reading
            val reading = AlmanacEngine.dailyReading(now)
            _dayReading.value = reading

            // Date formatting
            val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINESE)
            _currentDateFormatted.value = now.format(formatter)

            // Ganzhi date string
            _ganzhiDate.value = "${reading.yearGanzhi.combined}年 " +
                "${reading.monthGanzhi.combined}月 " +
                "${reading.dayGanzhi.combined}日"

            // Current 时辰
            _shichenName.value = AlmanacEngine.shichenName(time.hour)

            // Generate greeting
            _greeting.value = generateCyberGreeting(reading)
        }
    }

    private fun generateCyberGreeting(reading: AlmanacEngine.DayReading): String {
        val dayGz = reading.dayGanzhi
        val energy = reading.dailyEnergy
        val zodiac = reading.zodiac
        val solarTerm = reading.currentSolarTerm

        val sb = StringBuilder()
        sb.appendLine("⚡ 赛博黄历初始化完毕")
        sb.appendLine()
        sb.appendLine("\uD83D\uDCE1 今日干支信号: ${dayGz.combined} | ${dayGz.branchElementCn}气当令")
        sb.appendLine("🐲 当前生肖频段: $zodiac")
        if (solarTerm != null) {
            sb.appendLine("🌀 节气节点: ${solarTerm.name} (${solarTerm.englishName})")
        }
        sb.appendLine("🔮 能量状态: $energy")
        sb.appendLine()
        sb.appendLine("▸ 选择你的占卜协议, 开启今日的赛博问卜之旅")

        return sb.toString()
    }
}
