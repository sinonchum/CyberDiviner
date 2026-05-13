package com.cyberdiviner.engine

import java.time.LocalDate

/**
 * LunarCalendar — 农历转换器
 *
 * Converts solar (Gregorian) dates to lunar dates using the standard
 * algorithm based on the 1900 reference point. Encodes lunar calendar
 * data from 1900–2100 in a compact bitmask lookup table.
 *
 * Each long encodes 1 year of lunar data:
 *   bits 0-3:    leap month number (0 = no leap month)
 *   bits 4-15:   each bit = 0 for 29-day month, 1 for 30-day month (12 months)
 *   bit 16-19:   leap month days (0 = 29, 1 = 30)
 *
 * Reference: Jan 31, 1900 = 农历庚子年正月初一
 */
object LunarCalendar {

    // ─────────────────────────── Data Structures ───────────────────────────

    data class LunarDate(
        val year: Int,            // 农历年
        val month: Int,           // 农历月 (1-12)
        val day: Int,             // 农历日 (1-30)
        val isLeapMonth: Boolean, // 是否闰月
        val monthName: String,    // "正月", "二月", ...
        val dayName: String,      // "初一", "初二", ...
    )

    data class GanzhiDate(
        val yearStem: String,
        val yearBranch: String,
        val monthStem: String,
        val monthBranch: String,
        val dayStem: String,
        val dayBranch: String,
        val zodiac: String,
    ) {
        val yearGanzhi: String get() = "$yearStem$yearBranch"
        val monthGanzhi: String get() = "$monthStem$monthBranch"
        val dayGanzhi: String get() = "$dayStem$dayBranch"
    }

    // ─────────────────────────── Lunar Data Table ───────────────────────────

    // Lunar calendar data from 1900 to 2100 (201 years).
    // Encoded as described in the class doc.
    private val LUNAR_INFO = longArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, // 1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06aa0, 0x1a6c4, 0x0aae0, // 2050-2059
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252, // 2090-2099
        0x0d520, // 2100
    )

    // ─────────────────────────── Constants ───────────────────────────

    // Lunar month names (Chinese)
    private val LUNAR_MONTHS = listOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    // Lunar day names (Chinese)
    private val LUNAR_DAYS = listOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    // Heavenly Stems (天干)
    private val HEAVENLY_STEMS = listOf(
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    )

    // Earthly Branches (地支)
    private val EARTHLY_BRANCHES = listOf(
        "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    )

    // Chinese Zodiac Animals
    private val ZODIAC_ANIMALS = listOf(
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    )

    // ─────────────────────────── Solar to Lunar Conversion ───────────────────────────

    /**
     * Convert a solar (Gregorian) date to a lunar date.
     *
     * Algorithm:
     * 1. Find the base date: Jan 31, 1900 = 农历庚子年正月初一
     * 2. Count days from base date to target date
     * 3. Iterate through lunar years/months to find the matching lunar date
     *
     * @param solarDate The Gregorian date to convert
     * @return LunarDate containing the lunar calendar information
     */
    fun solarToLunar(solarDate: LocalDate): LunarDate {
        // Base date: Jan 31, 1900 (lunar 1900-01-01)
        val baseDate = LocalDate.of(1900, 1, 31)

        // Days from base
        val dayCount = (solarDate.toEpochDay() - baseDate.toEpochDay()).toInt()

        if (dayCount < 0) {
            // Before our data range, return solar date as-is with Chinese formatting
            return LunarDate(
                year = solarDate.year,
                month = solarDate.monthValue,
                day = solarDate.dayOfMonth,
                isLeapMonth = false,
                monthName = "未知月",
                dayName = "未知日"
            )
        }

        // Find the lunar year
        var remainingDays = dayCount
        var lunarYear = 1900
        var yearDays: Int

        while (lunarYear <= 2100 && remainingDays > 0) {
            yearDays = daysInLunarYear(lunarYear)
            if (remainingDays < yearDays) break
            remainingDays -= yearDays
            lunarYear++
        }

        if (lunarYear > 2100) {
            return LunarDate(lunarYear, 1, 1, false, "正月", "初一")
        }

        // Find the lunar month
        val leapMonth = leapMonthOf(lunarYear)
        var lunarMonth = 1
        var isLeapMonth = false
        var monthDays: Int

        val totalMonths = if (leapMonth > 0) 13 else 12

        for (i in 1..totalMonths) {
            if (leapMonth > 0 && i > leapMonth) {
                // After leap month, the leap month comes next
                val actualMonth = i - 1
                monthDays = if (i - 1 == leapMonth) {
                    leapMonthDays(lunarYear)
                } else {
                    daysInLunarMonth(lunarYear, actualMonth)
                }
                isLeapMonth = (i - 1 == leapMonth)
                if (isLeapMonth) {
                    lunarMonth = leapMonth
                } else {
                    lunarMonth = i - 1
                }
            } else {
                monthDays = daysInLunarMonth(lunarYear, i)
                lunarMonth = i
                isLeapMonth = false
            }

            if (remainingDays < monthDays) break
            remainingDays -= monthDays
        }

        val lunarDay = remainingDays + 1

        return LunarDate(
            year = lunarYear,
            month = lunarMonth,
            day = lunarDay,
            isLeapMonth = isLeapMonth,
            monthName = if (isLeapMonth) "闰${LUNAR_MONTHS[lunarMonth - 1]}" else LUNAR_MONTHS[lunarMonth - 1],
            dayName = LUNAR_DAYS[(lunarDay - 1).coerceIn(0, 29)]
        )
    }

    // ─────────────────────────── Ganzhi Calculations ───────────────────────────

    /**
     * Calculate full Ganzhi date information for a solar date.
     * Returns year, month, day Ganzhi and zodiac animal.
     */
    fun calculateGanzhi(solarDate: LocalDate): GanzhiDate {
        val lunar = solarToLunar(solarDate)

        // Year Ganzhi (based on lunar year)
        // 甲子年 starts from a reference: year 1984 = 甲子
        val yearStemIdx = ((lunar.year - 4) % 10 + 10) % 10
        val yearBranchIdx = ((lunar.year - 4) % 12 + 12) % 12

        // Month Ganzhi (based on lunar month)
        // Month stem depends on year stem: 甲己年起丙寅, 乙庚年起戊寅, etc.
        val yearStem = yearStemIdx
        val baseStem = when (yearStem) {
            0, 5 -> 2  // 甲/己 → 丙寅
            1, 6 -> 4  // 乙/庚 → 戊寅
            2, 7 -> 6  // 丙/辛 → 庚寅
            3, 8 -> 8  // 丁/壬 → 壬寅
            4, 9 -> 0  // 戊/癸 → 甲寅
            else -> 2
        }
        // 寅=2 is month 1 (正月)
        val monthBranchIdx = (lunar.month + 1) % 12
        val monthStemIdx = (baseStem + (monthBranchIdx - 2 + 12) % 12) % 10

        // Day Ganzhi (based on Julian Day Number)
        val jdn = solarDate.toEpochDay() + 2440587L
        val dayStemIdx = ((jdn + 9) % 10).toInt()
        val dayBranchIdx = ((jdn + 1) % 12).toInt()

        // Zodiac
        val zodiacIdx = ((lunar.year - 4) % 12 + 12) % 12

        return GanzhiDate(
            yearStem = HEAVENLY_STEMS[yearStemIdx],
            yearBranch = EARTHLY_BRANCHES[yearBranchIdx],
            monthStem = HEAVENLY_STEMS[monthStemIdx],
            monthBranch = EARTHLY_BRANCHES[monthBranchIdx],
            dayStem = HEAVENLY_STEMS[dayStemIdx],
            dayBranch = EARTHLY_BRANCHES[dayBranchIdx],
            zodiac = ZODIAC_ANIMALS[zodiacIdx]
        )
    }

    /**
     * Get the Chinese zodiac animal for a given year.
     */
    fun zodiacAnimal(year: Int): String {
        val idx = ((year - 4) % 12 + 12) % 12
        return ZODIAC_ANIMALS[idx]
    }

    /**
     * Get the Ganzhi name for a year.
     */
    fun yearGanzhi(year: Int): String {
        val stemIdx = ((year - 4) % 10 + 10) % 10
        val branchIdx = ((year - 4) % 12 + 12) % 12
        return "${HEAVENLY_STEMS[stemIdx]}${EARTHLY_BRANCHES[branchIdx]}"
    }

    // ─────────────────────────── Lunar Calendar Helpers ───────────────────────────

    /**
     * Get the number of days in a lunar year.
     */
    private fun daysInLunarYear(year: Int): Int {
        val info = LUNAR_INFO[year - 1900]
        var sum = 348  // 29 * 12 months

        // Add days for 30-day months
        var i = 0x8000
        while (i > 0x8) {
            if (info.toInt() and i != 0) sum++
            i = i shr 1
        }

        // Add leap month days
        val leap = leapMonthOf(year)
        if (leap > 0) {
            sum += leapMonthDays(year)
        }

        return sum
    }

    /**
     * Get the number of days in a specific lunar month.
     * @return 29 or 30
     */
    private fun daysInLunarMonth(year: Int, month: Int): Int {
        val info = LUNAR_INFO[year - 1900]
        val bit = 1 shl (16 - month)
        return if (info.toInt() and bit != 0) 30 else 29
    }

    /**
     * Get the leap month number for a lunar year (0 = no leap month).
     */
    private fun leapMonthOf(year: Int): Int {
        return (LUNAR_INFO[year - 1900] and 0xf).toInt()
    }

    /**
     * Get the number of days in the leap month (0 if no leap month).
     */
    private fun leapMonthDays(year: Int): Int {
        return if (leapMonthOf(year) > 0) {
            if (LUNAR_INFO[year - 1900].toInt() and 0x10000 != 0) 30 else 29
        } else 0
    }

    // ─────────────────────────── Formatting Helpers ───────────────────────────

    /**
     * Format a lunar date as a Chinese string.
     * Example: "农历丙辰年三月十五"
     */
    fun formatLunarDate(lunar: LunarDate): String {
        val yearGz = yearGanzhi(lunar.year)
        val dayStr = LUNAR_DAYS[(lunar.day - 1).coerceIn(0, 29)]
        return "农历${yearGz}年${lunar.monthName}$dayStr"
    }

    /**
     * Format the full Ganzhi date as a readable string.
     * Example: "丙辰年 三月十五日 | 龙年"
     */
    fun formatFullDate(solarDate: LocalDate): String {
        val lunar = solarToLunar(solarDate)
        val ganzhi = calculateGanzhi(solarDate)
        val dayStr = LUNAR_DAYS[(lunar.day - 1).coerceIn(0, 29)]
        return "${ganzhi.yearGanzhi}年 ${lunar.monthName}${dayStr}日 | ${ganzhi.zodiac}年"
    }
}
