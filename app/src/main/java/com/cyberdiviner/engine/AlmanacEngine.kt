package com.cyberdiviner.engine

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

/**
 * AlmanacEngine — 通书算法 / 黄历引擎 (Chinese Almanac / Tong Shu Engine)
 *
 * Provides:
 *   1. Heavenly Stems (天干) & Earthly Branches (地支) for any date
 *   2. Ganzhi (干支) date/time calculation
 *   3. Chinese Zodiac (生肖) from birth year
 *   4. Daily auspicious/inauspicious activities (宜/忌)
 *   5. 24 Solar Terms (二十四节气)
 *   6. Monthly/Yearly guidance
 *   7. Five Elements interaction for date energy
 *   8. Quick daily reading (今日运势)
 *   9. Lunar calendar conversion (农历) via LunarCalendar
 *  10. 365 cyberpunk-philosophical daily quotes via AlmanacQuotes
 *
 * All calculations are performed locally with no network calls.
 */
object AlmanacEngine {

    // ─────────────────────────── Constants ───────────────────────────

    val HEAVENLY_STEMS = listOf(
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    )

    val EARTHLY_BRANCHES = listOf(
        "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    )

    val ZODIAC_ANIMALS = listOf(
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    )

    val ZODIAC_ENGLISH = listOf(
        "Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake",
        "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig"
    )

    val STEM_WUXING = mapOf(
        "甲" to "Wood", "乙" to "Wood",
        "丙" to "Fire", "丁" to "Fire",
        "戊" to "Earth", "己" to "Earth",
        "庚" to "Metal", "辛" to "Metal",
        "壬" to "Water", "癸" to "Water",
    )

    val BRANCH_WUXING = mapOf(
        "子" to "Water", "丑" to "Earth", "寅" to "Wood", "卯" to "Wood",
        "辰" to "Earth", "巳" to "Fire", "午" to "Fire", "未" to "Earth",
        "申" to "Metal", "酉" to "Metal", "戌" to "Earth", "亥" to "Water",
    )

    private val BRANCH_WUXING_LIST = BRANCH_WUXING.values.toList()

    val STEM_YIN_YANG = mapOf(
        "甲" to "Yang", "乙" to "Yin",
        "丙" to "Yang", "丁" to "Yin",
        "戊" to "Yang", "己" to "Yin",
        "庚" to "Yang", "辛" to "Yin",
        "壬" to "Yang", "癸" to "Yin",
    )

    val ELEMENT_CN = mapOf(
        "Wood" to "\u6728", "Fire" to "\u706B", "Earth" to "\u571F",
        "Metal" to "\u91D1", "Water" to "\u6C34"
    )

    val BRANCH_ANIMAL_MAP = mapOf(
        "子" to "Rat", "丑" to "Ox", "寅" to "Tiger", "卯" to "Rabbit",
        "辰" to "Dragon", "巳" to "Snake", "午" to "Horse", "未" to "Goat",
        "申" to "Monkey", "酉" to "Rooster", "戌" to "Dog", "亥" to "Pig",
    )

    // ─────────────────────────── 24 Solar Terms (二十四节气) ───────────────────────────

    data class SolarTerm(
        val name: String,
        val englishName: String,
        val month: Int,      // approximate month (1-12)
        val day: Int,        // approximate day
        val element: String, // Wu Xing energy
    )

    val SOLAR_TERMS = listOf(
        SolarTerm("小寒", "Minor Cold", 1, 6, "Water"),
        SolarTerm("大寒", "Major Cold", 1, 20, "Water"),
        SolarTerm("立春", "Start of Spring", 2, 4, "Wood"),
        SolarTerm("雨水", "Rain Water", 2, 19, "Water"),
        SolarTerm("惊蛰", "Awakening of Insects", 3, 6, "Wood"),
        SolarTerm("春分", "Spring Equinox", 3, 21, "Wood"),
        SolarTerm("清明", "Clear and Bright", 4, 5, "Wood"),
        SolarTerm("谷雨", "Grain Rain", 4, 20, "Wood"),
        SolarTerm("立夏", "Start of Summer", 5, 6, "Fire"),
        SolarTerm("小满", "Grain Buds", 5, 21, "Fire"),
        SolarTerm("芒种", "Grain in Ear", 6, 6, "Fire"),
        SolarTerm("夏至", "Summer Solstice", 6, 21, "Fire"),
        SolarTerm("小暑", "Minor Heat", 7, 7, "Fire"),
        SolarTerm("大暑", "Major Heat", 7, 23, "Fire"),
        SolarTerm("立秋", "Start of Autumn", 8, 7, "Metal"),
        SolarTerm("处暑", "End of Heat", 8, 23, "Metal"),
        SolarTerm("白露", "White Dew", 9, 8, "Metal"),
        SolarTerm("秋分", "Autumnal Equinox", 9, 23, "Metal"),
        SolarTerm("寒露", "Cold Dew", 10, 8, "Metal"),
        SolarTerm("霜降", "Frost Descent", 10, 23, "Metal"),
        SolarTerm("立冬", "Start of Winter", 11, 7, "Water"),
        SolarTerm("小雪", "Minor Snow", 11, 22, "Water"),
        SolarTerm("大雪", "Major Snow", 12, 7, "Water"),
        SolarTerm("冬至", "Winter Solstice", 12, 22, "Water"),
    )

    // ─────────────────────────── Auspicious Activities (宜/忌) ───────────────────────────

    data class DailyActivity(
        val name: String,       // 活动名
        val englishName: String,
        val category: String,   // 分类
    )

    // Full list of traditional activities
    val ALL_ACTIVITIES = listOf(
        DailyActivity("嫁娶", "Marriage", "关系"),
        DailyActivity("订盟", "Engagement", "关系"),
        DailyActivity("纳采", "Betrothal Gifts", "关系"),
        DailyActivity("开光", "Consecration", "宗教"),
        DailyActivity("求嗣", "Seeking Heir", "家庭"),
        DailyActivity("出行", "Travel", "出行"),
        DailyActivity("解除", "Release", "生活"),
        DailyActivity("剃头", "Haircut", "生活"),
        DailyActivity("整手足甲", "Manicure/Pedicure", "生活"),
        DailyActivity("沐浴", "Bathing Ritual", "生活"),
        DailyActivity("栽种", "Planting", "农业"),
        DailyActivity("牧养", "Herding", "农业"),
        DailyActivity("纳畜", "Keeping Livestock", "农业"),
        DailyActivity("会亲友", "Meeting Friends", "社交"),
        DailyActivity("裁衣", "Making Clothes", "生活"),
        DailyActivity("经络", "Acupuncture", "健康"),
        DailyActivity("安机械", "Installing Machinery", "工作"),
        DailyActivity("开市", "Opening Business", "商业"),
        DailyActivity("交易", "Trading", "商业"),
        DailyActivity("立券", "Signing Contracts", "商业"),
        DailyActivity("挂匾", "Hanging Signs", "商业"),
        DailyActivity("纳财", "Collecting Wealth", "商业"),
        DailyActivity("造仓", "Building Storehouse", "建筑"),
        DailyActivity("盖屋", "Building Roof", "建筑"),
        DailyActivity("修造", "Construction/Repair", "建筑"),
        DailyActivity("动土", "Breaking Ground", "建筑"),
        DailyActivity("上梁", "Raising Beams", "建筑"),
        DailyActivity("竖柱", "Erecting Pillars", "建筑"),
        DailyActivity("开池", "Digging Ponds", "建筑"),
        DailyActivity("开厕", "Building Toilets", "建筑"),
        DailyActivity("作灶", "Building Stove", "建筑"),
        DailyActivity("安门", "Installing Doors", "建筑"),
        DailyActivity("造桥", "Building Bridges", "建筑"),
        DailyActivity("造屋", "Building Houses", "建筑"),
        DailyActivity("安葬", "Burial", "丧葬"),
        DailyActivity("破土", "Breaking Ground for Burial", "丧葬"),
        DailyActivity("启攒", "Exhumation", "丧葬"),
        DailyActivity("修坟", "Tomb Repair", "丧葬"),
        DailyActivity("入棺", "Placing in Coffin", "丧葬"),
        DailyActivity("成服", "Wearing Mourning Clothes", "丧葬"),
        DailyActivity("移柩", "Moving Coffin", "丧葬"),
        DailyActivity("祭祀", "Worship/Sacrifice", "宗教"),
        DailyActivity("祈福", "Praying for Blessings", "宗教"),
        DailyActivity("酬神", "Thanking Gods", "宗教"),
        DailyActivity("造庙", "Building Temples", "宗教"),
        DailyActivity("安香", "Incense Offering", "宗教"),
        DailyActivity("赴任", "Taking Office", "工作"),
        DailyActivity("求医", "Seeking Medical Help", "健康"),
        DailyActivity("治病", "Treating Illness", "健康"),
        DailyActivity("裁衣", "Cutting Clothes", "生活"),
        DailyActivity("入学", "Entering School", "学业"),
        DailyActivity("习艺", "Learning Arts", "学业"),
        DailyActivity("置产", "Buying Property", "商业"),
        DailyActivity("出货财", "Distributing Wealth", "商业"),
        DailyActivity("分居", "Living Apart", "关系"),
        DailyActivity("词讼", "Lawsuit", "法律"),
        DailyActivity("行丧", "Funeral Procession", "丧葬"),
        DailyActivity("远行", "Long Distance Travel", "出行"),
    )

    // ─────────────────────────── Inauspicious Markers ───────────────────────────

    val BAD_DAYS = mapOf(
        // 月破 (Month Break)
        "月破" to "月破 — 凡事不宜，诸事大凶",
        // 四废 (Four Wastes)
        "四废" to "四废 — 无气之日，百事不宜",
        // 彭祖百忌 (Pengzu's Taboos)
        "往亡" to "往亡 — 不利出行、嫁娶",
    )

    // ─────────────────────────── Data Structures ───────────────────────────

    data class Ganzhi(
        val stem: String,          // 天干
        val branch: String,        // 地支
        val stemIndex: Int,        // 0-9
        val branchIndex: Int,      // 0-11
    ) {
        val combined: String get() = "$stem$branch"
        val stemElement: String get() = STEM_WUXING[stem] ?: "Earth"
        val branchElement: String get() = BRANCH_WUXING[branch] ?: "Earth"
        val stemElementCn: String get() = ELEMENT_CN[stemElement] ?: "\u571F"
        val branchElementCn: String get() = ELEMENT_CN[branchElement] ?: "\u571F"
        val yinYang: String get() = STEM_YIN_YANG[stem] ?: "Yang"
    }

    data class DayReading(
        val date: LocalDate,
        val yearGanzhi: Ganzhi,
        val monthGanzhi: Ganzhi,
        val dayGanzhi: Ganzhi,
        val hourGanzhi: Ganzhi?,
        val zodiac: String,
        val zodiacEnglish: String,
        val currentSolarTerm: SolarTerm?,
        val auspiciousActivities: List<DailyActivity>,
        val inauspiciousActivities: List<DailyActivity>,
        val dailyEnergy: String,
        val elementAdvice: String,
        val luckyColors: List<String>,
        val luckyNumbers: List<Int>,
        val overview: String,
        val warnings: List<String>,
        // ── Lunar Calendar & Quotes ────────────────────────────
        val lunarDate: LunarCalendar.LunarDate,
        val lunarGanzhi: LunarCalendar.GanzhiDate,
        val dailyQuote: AlmanacQuotes.DailyQuote,
    ) {
        fun summary(): String = buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("黄历 — Chinese Almanac")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("阳历: ${date.year}年${date.monthValue}月${date.dayOfMonth}日")
            appendLine("   农历: ${lunarDate.monthName}${lunarDate.dayName}")
            appendLine("   干支: ${yearGanzhi.combined}年 ${monthGanzhi.combined}月 ${dayGanzhi.combined}日")
            if (hourGanzhi != null) {
                appendLine("   时辰: ${hourGanzhi.combined}")
            }
            appendLine("   生肖: $zodiac ($zodiacEnglish)")
            if (currentSolarTerm != null) {
                appendLine("   节气: ${currentSolarTerm.name} (${currentSolarTerm.englishName})")
            }
            appendLine()
            appendLine("━━━ 签语 (Daily Quote) ━━━")
            appendLine("  「${dailyQuote.text}」")
            appendLine()
            appendLine("━━━ 今日宜 (Auspicious) ━━━")
            auspiciousActivities.forEach { act ->
                appendLine("  ${act.name} (${act.englishName})")
            }
            appendLine()
            appendLine("━━━ 今日忌 (Inauspicious) ━━━")
            inauspiciousActivities.forEach { act ->
                appendLine("  ${act.name} (${act.englishName})")
            }
            appendLine()
            appendLine("━━━ 运势概览 ━━━")
            appendLine("  今日能量: $dailyEnergy")
            appendLine("  五行建议: $elementAdvice")
            appendLine("  吉祥色: ${luckyColors.joinToString("、")}")
            appendLine("  吉祥数: ${luckyNumbers.joinToString("、")}")
            if (warnings.isNotEmpty()) {
                appendLine("  注意:")
                warnings.forEach { w -> appendLine("    • $w") }
            }
            appendLine()
            appendLine("━━━ 综合运势 ━━━")
            appendLine("  $overview")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    // ─────────────────────────── Ganzhi Calculation ───────────────────────────

    /**
     * Calculate the Ganzhi (干支) for a given date.
     * Uses the standard formula: Ganzhi = (year offset from reference) mod 60
     */
    fun calculateGanzhi(date: LocalDate): Ganzhi {
        // Reference: Jan 1, 1 CE was 甲子 day (Julian Day 1721426)
        // More practical: Jan 1, 1900 = 甲戌 (stem=0, branch=10) → index 10
        // Actually, use the standard Julian Day calculation:
        val jdn = date.toEpochDay() + 2440587L  // Julian Day Number
        val stemIdx = ((jdn + 9) % 10).toInt()   // +9 to align: JDN 0 → 甲
        val branchIdx = ((jdn + 1) % 12).toInt()  // +1 to align
        return Ganzhi(
            stem = HEAVENLY_STEMS[stemIdx],
            branch = EARTHLY_BRANCHES[branchIdx],
            stemIndex = stemIdx,
            branchIndex = branchIdx,
        )
    }

    /**
     * Year Ganzhi — traditional Chinese year calculation.
     * The Chinese year starts at 立春 (Start of Spring, ~Feb 4).
     */
    fun calculateYearGanzhi(date: LocalDate): Ganzhi {
        // Simplified: use the year number directly
        val year = date.year
        // Year stem = (year - 4) mod 10
        val stemIdx = ((year - 4) % 10 + 10) % 10
        // Year branch = (year - 4) mod 12
        val branchIdx = ((year - 4) % 12 + 12) % 12
        return Ganzhi(
            stem = HEAVENLY_STEMS[stemIdx],
            branch = EARTHLY_BRANCHES[branchIdx],
            stemIndex = stemIdx,
            branchIndex = branchIdx,
        )
    }

    /**
     * Month Ganzhi — based on solar term months.
     * Month changes at 节 (not 气).
     */
    fun calculateMonthGanzhi(date: LocalDate): Ganzhi {
        val month = date.monthValue
        val yearStem = calculateYearGanzhi(date).stemIndex

        // Month branch index: 寅=2 is month 1 (正月)
        val monthBranchIdx = (month + 1) % 12

        // Month stem is derived from year stem:
        // 甲己年起丙寅, 乙庚年起戊寅, 丙辛年起庚寅, 丁壬年起壬寅, 戊癸年起甲寅
        val baseStem = when (yearStem) {
            0, 5 -> 2  // 甲/己 → 丙寅
            1, 6 -> 4  // 乙/庚 → 戊寅
            2, 7 -> 6  // 丙/辛 → 庚寅
            3, 8 -> 8  // 丁/壬 → 壬寅
            4, 9 -> 0  // 戊/癸 → 甲寅
            else -> 2
        }
        val stemIdx = (baseStem + (monthBranchIdx - 2 + 12) % 12) % 10

        return Ganzhi(
            stem = HEAVENLY_STEMS[stemIdx],
            branch = EARTHLY_BRANCHES[monthBranchIdx],
            stemIndex = stemIdx,
            branchIndex = monthBranchIdx,
        )
    }

    /**
     * Hour Ganzhi — based on the two-hour period (时辰).
     * 子时 (23:00-01:00) = first, 亥时 (21:00-23:00) = last.
     */
    fun calculateHourGanzhi(date: LocalDate, time: LocalTime): Ganzhi {
        val hour = time.hour
        // Convert to 子时-based index: 23:00-01:00 = 0, 01:00-03:00 = 1, etc.
        val branchIdx = ((hour + 1) / 2) % 12

        // Hour stem derived from day stem:
        // 甲己日起甲子, 乙庚日起丙子, 丙辛日起戊子, 丁壬日起庚子, 戊癸日起壬子
        val dayStem = calculateGanzhi(date).stemIndex
        val baseStem = when (dayStem) {
            0, 5 -> 0  // 甲/己
            1, 6 -> 2  // 乙/庚
            2, 7 -> 4  // 丙/辛
            3, 8 -> 6  // 丁/壬
            4, 9 -> 8  // 戊/癸
            else -> 0
        }
        val stemIdx = (baseStem + branchIdx) % 10

        return Ganzhi(
            stem = HEAVENLY_STEMS[stemIdx],
            branch = EARTHLY_BRANCHES[branchIdx],
            stemIndex = stemIdx,
            branchIndex = branchIdx,
        )
    }

    // ─────────────────────────── Solar Term Lookup ───────────────────────────

    /**
     * Find the current solar term for a given date.
     */
    fun currentSolarTerm(date: LocalDate): SolarTerm? {
        val month = date.monthValue
        val day = date.dayOfMonth

        // Find the solar term closest before or on this date
        var closest: SolarTerm? = null
        for (term in SOLAR_TERMS) {
            if (term.month < month || (term.month == month && term.day <= day)) {
                closest = term
            } else {
                break
            }
        }
        return closest
    }

    // ─────────────────────────── Zodiac ───────────────────────────

    fun zodiacForYear(year: Int): Pair<String, String> {
        val idx = (year - 4) % 12  // 1900 was 鼠年
        return ZODIAC_ANIMALS[idx] to ZODIAC_ENGLISH[idx]
    }

    // ─────────────────────────── Activity Generation ───────────────────────────

    /**
     * Generate auspicious/inauspicious activities for a given date.
     * Uses the day's Ganzhi and Five Elements interactions to determine
     * which activities are favorable or unfavorable.
     */
    fun generateActivities(date: LocalDate): Pair<List<DailyActivity>, List<DailyActivity>> {
        val dayGz = calculateGanzhi(date)
        val dayElement = dayGz.branchElement
        val dayBranchIdx = dayGz.branchIndex

        // Use a deterministic algorithm based on the day's Ganzhi
        val seed = date.toEpochDay()
        val auspicious = mutableListOf<DailyActivity>()
        val inauspicious = mutableListOf<DailyActivity>()

        // Selection algorithm: pick activities based on element compatibility
        val activitiesByCategory = ALL_ACTIVITIES.groupBy { it.category }

        for (activity in ALL_ACTIVITIES) {
            val actIdx = abs(activity.name.hashCode() + seed.toInt()) % 100
            val element = BRANCH_WUXING_LIST[actIdx % 5]

            val dayWuXing = HexagramData.toWuXing(dayElement)
            val actWuXing = HexagramData.toWuXing(element)
            val isCompatible = dayWuXing.isCompatibleWith(actWuXing)

            if (isCompatible && auspicious.size < 10) {
                auspicious.add(activity)
            } else if (!isCompatible && inauspicious.size < 8) {
                inauspicious.add(activity)
            }
        }

        // Ensure minimum entries
        if (auspicious.isEmpty()) {
            auspicious.addAll(ALL_ACTIVITIES.take(5))
        }
        if (inauspicious.isEmpty()) {
            inauspicious.addAll(ALL_ACTIVITIES.drop(5).take(3))
        }

        return auspicious to inauspicious
    }

    // ─────────────────────────── Daily Reading ───────────────────────────

    /**
     * Generate a complete daily reading for the given date.
     * Fully offline — no network calls.
     */
    fun dailyReading(date: LocalDate, birthYear: Int? = null): DayReading {
        val yearGz = calculateYearGanzhi(date)
        val monthGz = calculateMonthGanzhi(date)
        val dayGz = calculateGanzhi(date)
        val hourGz = calculateHourGanzhi(date, LocalTime.now())

        val (zodiac, zodiacEn) = birthYear?.let { zodiacForYear(it) }
            ?: zodiacForYear(date.year)

        val solarTerm = currentSolarTerm(date)
        val (auspicious, inauspicious) = generateActivities(date)

        // Calculate daily energy
        val dayElement = dayGz.branchElement
        val energy = calculateDailyEnergy(date, dayGz)

        // Element advice
        val elementAdvice = calculateElementAdvice(dayElement)

        // Lucky colors based on element
        val luckyColors = when (dayElement) {
            "Wood" -> listOf("绿色", "青色", "翠色")
            "Fire" -> listOf("红色", "橙色", "紫色")
            "Earth" -> listOf("黄色", "棕色", "咖啡色")
            "Metal" -> listOf("白色", "银色", "金色")
            "Water" -> listOf("黑色", "蓝色", "深灰色")
            else -> listOf("白色", "银色")
        }

        // Lucky numbers based on element
        val luckyNumbers = when (dayElement) {
            "Wood" -> listOf(3, 8, 13)
            "Fire" -> listOf(2, 7, 12)
            "Earth" -> listOf(5, 10, 15)
            "Metal" -> listOf(4, 9, 14)
            "Water" -> listOf(1, 6, 11)
            else -> listOf(1, 6)
        }

        // Warnings
        val warnings = mutableListOf<String>()
        if (solarTerm?.name == "冬至" || solarTerm?.name == "夏至") {
            warnings.add("今日为${solarTerm.name}，阴阳转换之际，宜静不宜动")
        }
        if (dayGz.stem == "庚" || dayGz.stem == "辛") {
            warnings.add("今日天干为${dayGz.stem}，属${dayGz.stemElementCn}气，需注意口舌是非")
        }

        // Overview
        val overview = generateOverview(date, dayGz, solarTerm)

        // Lunar calendar (offline)
        val lunarDate = LunarCalendar.solarToLunar(date)
        val lunarGanzhi = LunarCalendar.calculateGanzhi(date)

        // Daily quote from the 365-quote pool (offline)
        val dailyQuote = AlmanacQuotes.getQuoteForDate(date)

        return DayReading(
            date = date,
            yearGanzhi = yearGz,
            monthGanzhi = monthGz,
            dayGanzhi = dayGz,
            hourGanzhi = hourGz,
            zodiac = zodiac,
            zodiacEnglish = zodiacEn,
            currentSolarTerm = solarTerm,
            auspiciousActivities = auspicious,
            inauspiciousActivities = inauspicious,
            dailyEnergy = energy,
            elementAdvice = elementAdvice,
            luckyColors = luckyColors,
            luckyNumbers = luckyNumbers,
            overview = overview,
            warnings = warnings,
            lunarDate = lunarDate,
            lunarGanzhi = lunarGanzhi,
            dailyQuote = dailyQuote,
        )
    }

    // ─────────────────────────── Energy & Advice ───────────────────────────

    private fun calculateDailyEnergy(date: LocalDate, dayGz: Ganzhi): String {
        val dayOfYear = date.dayOfYear
        val branchIdx = dayGz.branchIndex

        // Simple energy calculation based on branch position in cycle
        return when {
            branchIdx in listOf(0, 4, 8) -> "旺 (Prosperous) — 今日能量充沛"
            branchIdx in listOf(1, 5, 9) -> "相 (Supportive) — 今日能量平稳"
            branchIdx in listOf(2, 6, 10) -> "休 (Resting) — 今日适合休养"
            branchIdx in listOf(3, 7, 11) -> "囚 (Restricted) — 今日需谨慎"
            else -> "死 (Dormant) — 今日宜守不宜攻"
        }
    }

    private fun calculateElementAdvice(dayElement: String): String {
        return when (dayElement) {
            "Wood" -> "木日: 适合创造性活动、社交、学习。避免冲动决策。"
            "Fire" -> "火日: 适合展示、表达、热情行动。避免过度消费。"
            "Earth" -> "土日: 适合稳定事务、投资、置业。避免变动太大。"
            "Metal" -> "金日: 适合决断、签约、整理。避免争执冲突。"
            "Water" -> "水日: 适合思考、策划、沟通。避免冒险投资。"
            else -> "今日五行平衡，无特别禁忌。"
        }
    }

    private fun generateOverview(date: LocalDate, dayGz: Ganzhi, solarTerm: SolarTerm?): String {
        val sb = StringBuilder()

        sb.append("今日${dayGz.combined}日，")
        sb.append("${dayGz.branchElementCn}气当令。")

        solarTerm?.let {
            sb.append("正值${it.name}（${it.englishName}），")
        }

        val dayOfWeek = date.dayOfWeek.value
        when (dayOfWeek) {
            1 -> sb.append("周一新始，宜规划本周事务。")
            2 -> sb.append("周二稳进，适合处理重要文件。")
            3 -> sb.append("周三中段，注意保持精力。")
            4 -> sb.append("周四冲刺，适合推进项目。")
            5 -> sb.append("周五收尾，总结本周成果。")
            6 -> sb.append("周六休闲，适合社交与休息。")
            7 -> sb.append("周日静养，为新一周蓄力。")
        }

        // Element-based advice
        when (dayGz.branchElement) {
            "Wood" -> sb.append("木气旺盛，利东方行事，着绿色衣物增运。")
            "Fire" -> sb.append("火气旺盛，利南方行事，着红色衣物增运。")
            "Earth" -> sb.append("土气旺盛，利中央行事，着黄色衣物增运。")
            "Metal" -> sb.append("金气旺盛，利西方行事，着白色衣物增运。")
            "Water" -> sb.append("水气旺盛，利北方行事，着黑色衣物增运。")
        }

        return sb.toString()
    }

    // ─────────────────────────── Five Elements Compatibility ───────────────────────────

    /**
     * Check compatibility between two elements (for dating, business, etc.)
     */
    fun elementCompatibility(element1: String, element2: String): String {
        val e1 = HexagramData.toWuXing(element1)
        val e2 = HexagramData.toWuXing(element2)

        return when {
            e1 == e2 -> "比和 — 和谐稳定，相互支持"
            HexagramData.produces(e1, e2) -> "${e1.chinese}生${e2.chinese} — 相生相助，吉"
            HexagramData.produces(e2, e1) -> "${e2.chinese}生${e1.chinese} — 被生者受益，吉"
            HexagramData.overcomes(e1, e2) -> "${e1.chinese}克${e2.chinese} — 有制约，需注意"
            HexagramData.overcomes(e2, e1) -> "${e2.chinese}克${e1.chinese} — 受制约，需化解"
            else -> "五行无直接关系"
        }
    }

    // ─────────────────────────── Utility Methods ───────────────────────────

    /**
     * Get the Heavenly Stem + Earthly Branch combination for a year.
     */
    fun yearGanzhi(year: Int): String {
        val stemIdx = ((year - 4) % 10 + 10) % 10
        val branchIdx = ((year - 4) % 12 + 12) % 12
        return "${HEAVENLY_STEMS[stemIdx]}${EARTHLY_BRANCHES[branchIdx]}"
    }

    /**
     * Get the Chinese Zodiac animal for a given year.
     */
    fun zodiacAnimal(year: Int): String {
        val idx = ((year - 4) % 12 + 12) % 12
        return ZODIAC_ANIMALS[idx]
    }

    /**
     * Get the Two-Hour Period (时辰) name for a given hour.
     */
    fun shichenName(hour: Int): String {
        val idx = ((hour + 1) / 2) % 12
        return "${EARTHLY_BRANCHES[idx]}时"
    }

    /**
     * Get element colors for display.
     */
    fun elementColors(element: String): List<String> = when (element) {
        "Wood" -> listOf("#228B22", "#AAAAAA") // ForestGreen, Lime→Gray
        "Fire" -> listOf("#CCCCCC", "#AAAAAA") // OrangeRed→Silver, Tomato→Gray
        "Earth" -> listOf("#DAA520", "#B8860B") // Goldenrod, DarkGoldenrod
        "Metal" -> listOf("#C0C0C0", "#FFD700") // Silver, Gold
        "Water" -> listOf("#000080", "#888888") // Navy, DodgerBlue→Gray
        else -> listOf("#808080", "#A9A9A9")
    }

    /**
     * Quick summary for today's almanac (for widget/notification).
     */
    fun quickSummary(date: LocalDate = LocalDate.now()): String {
        val gz = calculateGanzhi(date)
        val (zodiac, _) = zodiacForYear(date.year)
        val term = currentSolarTerm(date)
        val termStr = term?.let { " | ${it.name}" } ?: ""
        val lunar = LunarCalendar.solarToLunar(date)
        val lunarStr = " | 农历${lunar.monthName}${lunar.dayName}"
        return "${gz.combined}日 $zodiac$lunarStr$termStr"
    }

    /**
     * Get the lunar date string for a given date.
     */
    fun lunarDateStr(date: LocalDate = LocalDate.now()): String {
        val lunar = LunarCalendar.solarToLunar(date)
        return "${lunar.monthName}${lunar.dayName}"
    }

    /**
     * Get today's cyberpunk quote.
     */
    fun dailyQuote(date: LocalDate = LocalDate.now()): AlmanacQuotes.DailyQuote {
        return AlmanacQuotes.getQuoteForDate(date)
    }
}
