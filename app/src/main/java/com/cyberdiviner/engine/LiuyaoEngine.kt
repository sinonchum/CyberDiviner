package com.cyberdiviner.engine

import com.cyberdiviner.engine.HexagramData.Trigram
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.engine.HexagramData.SixRelation
import com.cyberdiviner.engine.HexagramData.SixSpirit
import com.cyberdiviner.engine.HexagramData.YaoLine
import com.cyberdiviner.engine.HexagramData.Hexagram
import com.cyberdiviner.engine.HexagramData.WuXing
import com.cyberdiviner.engine.HexagramData.EARTHLY_BRANCHES
import com.cyberdiviner.engine.HexagramData.BRANCH_TO_WUXING
import com.cyberdiviner.engine.HexagramData.relationFrom
import com.cyberdiviner.engine.HexagramData.produces
import com.cyberdiviner.engine.HexagramData.toWuXing
import com.cyberdiviner.engine.HexagramData.interpretLines
import kotlin.random.Random
import kotlin.math.abs

/**
 * LiuyaoEngine — 六爻算法 (Six Lines Divination Engine)
 *
 * Implements the complete Liuyao divination workflow:
 *   1. Coin Toss (三钱法) — 3 coins × 6 throws → 6 lines
 *   2. Primary Hexagram (本卦) + Changed Hexagram (变卦)
 *   3. World Line (世爻) & Response Line (应爻)
 *   4. Six Relations (六亲) per line
 *   5. Earthly Branches (地支) per line via NaJia (纳甲)
 *   6. Six Spirits (六神) per line
 *   7. Hidden Lines (伏神) — absent parents/children
 *   8. Comprehensive interpretation data
 *
 * Usage:
 *   val engine = LiuyaoEngine()
 *   val result = engine.divine("我想问事业")
 *   Log.d("Liuyao", result.summary())
 */
class LiuyaoEngine(
    private val random: Random = Random.Default
) {

    // ─────────────────────────── NaJia (纳甲) Trigram → Branches ───────────────────────────

    /**
     * Traditional NaJia mapping: each trigram gets a set of 6 branches
     * (3 for the 3 lines of the trigram, repeated top-to-bottom).
     * Lower trigram branches go to lines 1-3, upper to lines 4-6.
     */
    private val trigramBranches: Map<Trigram, List<String>> = mapOf(
        Trigram.QIAN to listOf("子", "寅", "辰", "午", "申", "戌"),  // 乾纳甲壬
        Trigram.KUN   to listOf("未", "巳", "卯", "丑", "亥", "酉"),  // 坤纳乙癸
        Trigram.ZHEN  to listOf("子", "寅", "辰", "午", "申", "戌"),  // 震纳庚
        Trigram.XUN   to listOf("丑", "亥", "酉", "未", "巳", "卯"),  // 巽纳辛
        Trigram.KAN   to listOf("寅", "辰", "午", "申", "戌", "子"),  // 坎纳戊
        Trigram.LI    to listOf("卯", "丑", "亥", "酉", "未", "巳"),  // 离纳己
        Trigram.GEN   to listOf("辰", "午", "申", "戌", "子", "寅"),  // 艮纳丙
        Trigram.DUI   to listOf("巳", "卯", "丑", "亥", "酉", "未"),  // 兑纳丁
    )

    // ─────────────────────────── World / Response Line Positions ───────────────────────────

    /**
     * 世爻 (Shì Yáo) and 应爻 (Yìng Yáo) positions (0-indexed, bottom = 0)
     * based on the hexagram's Palace (八宫所属).
     *
     * 世爻 = self/ego line;  应爻 = response/other line (always 3 apart).
     */
    private val worldLineByPalace = listOf(
        0, // First hexagram of palace — 世 at line 1
        1, // Second — 世 at line 2
        2, // Third — 世 at line 3
        3, // Fourth — 世 at line 4
        4, // Fifth — 世 at line 5
        3, // Changed (游魂) — 世 at line 4
        0, // Changed (归魂) — 世 at line 1
    )

    // ─────────────────────────── Six Spirits Rotation ───────────────────────────

    /**
     * Six Spirits cycle starts from the day's Heavenly Stem.
     * 根据日干排六神:
     * 甲乙日起青龙, 丙丁日起朱雀, 戊日起勾陈, 己日起螣蛇, 庚辛日起白虎, 壬癸日起玄武
     */
    private fun spiritForDayStem(stemIndex: Int): SixSpirit = when (stemIndex % 10) {
        0, 1 -> SixSpirit.QINGLONG       // 甲乙
        2, 3 -> SixSpirit.GUCHA           // 丙丁
        4    -> SixSpirit.GOU_CHEN        // 戊
        5    -> SixSpirit.TENG_SHE        // 己
        6, 7 -> SixSpirit.BAIHU           // 庚辛
        8, 9 -> SixSpirit.XUAN_WU         // 壬癸
        else -> SixSpirit.QINGLONG
    }

    // ─────────────────────────── Core Data Structures ───────────────────────────

    data class CoinToss(
        val coins: List<Int>,   // 3 coins: 2=正面(字), 1=反面(花)
        val sum: Int,           // 2+2+2=6(老阴), 2+2+1=5(少阳), 2+1+1=4(少阴), 1+1+1=3(老阳)
        val lineState: LineState,
    )

    data class DivinationResult(
        val question: String,
        val timestamp: Long,
        val tosses: List<CoinToss>,
        val primaryHexagram: Hexagram,
        val changedHexagram: Hexagram,
        val lines: List<YaoLine>,
        val worldLine: Int,            // 世爻 position (0-indexed)
        val responseLine: Int,          // 应爻 position (0-indexed)
        val spirits: List<SixSpirit>,   // 六神 per line (bottom→top)
        val hiddenLines: List<YaoLine>, // 伏神 lines (missing parents/children)
        val analysis: Analysis,
    ) {
        fun summary(): String = buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🔮 六爻占卜 — Liuyao Divination")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📋 问事: $question")
            appendLine()
            appendLine("☰ 本卦 (Primary): ${primaryHexagram.chineseName} ${primaryHexagram.englishName}")
            appendLine("  卦辞: ${primaryHexagram.judgment}")
            if (hasChangingLines()) {
                appendLine("☲ 变卦 (Changed): ${changedHexagram.chineseName} ${changedHexagram.englishName}")
                appendLine("  卦辞: ${changedHexagram.judgment}")
            }
            appendLine()
            appendLine("━━━ 六爻排列 (Lines) ━━━")
            for (i in 5 downTo 0) {
                val line = lines[i]
                val bar = if (line.isYang) "━━━━━" else "━   ━"
                val changeMark = when (line.state) {
                    LineState.OLD_YANG -> "×"  // 动爻
                    LineState.OLD_YIN -> "○"
                    else -> ""
                }
                val spiritLabel = spirits[i].chinese
                val relLabel = line.relation?.chinese ?: ""
                val branch = line.branch
                val shiYing = when (i) {
                    worldLine -> "世"
                    responseLine -> "应"
                    else -> ""
                }
                appendLine("  ${i + 1}爻 [$branch] $bar $changeMark  $relLabel $spiritLabel $shiYing")
            }
            appendLine()
            appendLine("━━━ 分析 (Analysis) ━━━")
            appendLine("  用神: ${analysis.usefulGod}")
            appendLine("  世爻: 第${worldLine + 1}爻 [${lines[worldLine].branch}] ${lines[worldLine].relation?.chinese ?: ""}")
            appendLine("  应爻: 第${responseLine + 1}爻 [${lines[responseLine].branch}] ${lines[responseLine].relation?.chinese ?: ""}")
            appendLine("  旺衰: ${analysis.strength}")
            appendLine("  卦象: ${analysis.interpretation}")
            if (hiddenLines.isNotEmpty()) {
                appendLine("  伏神:")
                hiddenLines.forEach { h ->
                    appendLine("    ${h.position + 1}爻 [${h.branch}] ${h.relation?.chinese ?: ""}")
                }
            }
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }

        fun hasChangingLines(): Boolean = tosses.any {
            it.lineState == LineState.OLD_YANG || it.lineState == LineState.OLD_YIN
        }
    }

    data class Analysis(
        val usefulGod: String,      // 用神描述
        val strength: String,       // 旺衰判断
        val interpretation: String, // 综合断语
        val advice: String,         // 建议
    )

    // ─────────────────────────── Main Divination Method ───────────────────────────

    /**
     * Perform a full Liuyao divination.
     *
     * @param question  The querent's question (问事)
     * @param dayStemIndex  Day's Heavenly Stem index (0=甲..9=癸), default = today
     * @return Complete DivinationResult
     */
    fun divine(
        question: String,
        dayStemIndex: Int = java.time.LocalDate.now().dayOfYear % 10,
    ): DivinationResult {
        // Step 1: Generate 6 coin tosses
        val tosses = (1..6).map { throwCoins() }

        // Step 2: Extract line states
        val lineStates = tosses.map { it.lineState }

        // Step 3: Get primary and changed hexagrams
        val (primary, changed) = interpretLines(lineStates)

        // Step 4: Determine World Line & Response Line
        val worldIdx = calculateWorldLine(primary, lineStates)
        val responseIdx = (worldIdx + 3) % 6

        // Step 5: Assign Earthly Branches (纳甲)
        val branches = assignBranches(primary)

        // Step 6: Determine Six Relations (六亲) — use primary hexagram's element as subject
        val subjectWuXing = toWuXing(primary.element)
        val lineObjects = (0..5).map { i ->
            val branch = branches[i]
            val branchWuXing = BRANCH_TO_WUXING[branch] ?: WuXing.EARTH
            val relation = relationFrom(subjectWuXing, branchWuXing)
            YaoLine(
                position = i,
                isYang = primary.binary[i],
                state = lineStates[i],
                branch = branch,
                relation = relation,
            )
        }

        // Step 7: Assign Six Spirits (六神)
        val spirits = assignSpirits(dayStemIndex)

        // Step 8: Find Hidden Lines (伏神)
        val hiddenLines = findHiddenLines(lineObjects, subjectWuXing)

        // Step 9: Analysis
        val analysis = analyze(lineObjects, primary, changed, worldIdx, responseIdx, lineStates)

        return DivinationResult(
            question = question,
            timestamp = System.currentTimeMillis(),
            tosses = tosses,
            primaryHexagram = primary,
            changedHexagram = changed,
            lines = lineObjects,
            worldLine = worldIdx,
            responseLine = responseIdx,
            spirits = spirits,
            hiddenLines = hiddenLines,
            analysis = analysis,
        )
    }

    // ─────────────────────────── Coin Toss Simulation ───────────────────────────

    /**
     * Simulate a 3-coin toss (三钱法).
     * Coin value: 2 = heads (字面), 1 = tails (花面)
     * Sum: 3=老阳(○), 4=少阴(×), 5=少阳(○→static), 6=老阴(×)
     *
     * Convention: 2 = yang side up (字面), 1 = yin side up (花面)
     * 3 coins all 2 = 6 → Old Yin (老阴) — changes
     * 3 coins all 1 = 3 → Old Yang (老阳) — changes
     */
    fun throwCoins(): CoinToss {
        val coins = List(3) { if (random.nextBoolean()) 2 else 1 }
        val sum = coins.sum()
        val state = when (sum) {
            6 -> LineState.OLD_YIN    // ⚊⚊ → ⚋⚋ (六冲, 变)
            5 -> LineState.YOUNG_YANG // ⚊⚊ (少阳, 不变)
            4 -> LineState.YOUNG_YIN  // ⚋⚋ (少阴, 不变)
            3 -> LineState.OLD_YANG   // ⚊⚊ → ⚋⚋ (九变, 变)
            else -> throw IllegalStateException("Invalid coin sum: $sum")
        }
        return CoinToss(coins = coins, sum = sum, lineState = state)
    }

    // ─────────────────────────── NaJia Branch Assignment ───────────────────────────

    private fun assignBranches(hexagram: Hexagram): List<String> {
        val lowerBranches = trigramBranches[hexagram.lowerTrigram] ?: listOf(
            "子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"
        )
        val upperBranches = trigramBranches[hexagram.upperTrigram] ?: listOf(
            "子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"
        )
        return listOf(
            lowerBranches[0], lowerBranches[1], lowerBranches[2],  // lines 1-3
            upperBranches[3], upperBranches[4], upperBranches[5],  // lines 4-6
        )
    }

    // ─────────────────────────── World Line Calculation ───────────────────────────

    /**
     * 世爻 position calculation based on the hexagram's palace assignment.
     * Uses a simplified heuristic based on which trigram changed from the
     * pure hexagram form of the palace trigram.
     *
     * For production use, a full palace table (八宫归属表) should be used.
     * This simplified version works for most common cases.
     */
    private fun calculateWorldLine(primary: Hexagram, states: List<LineState>): Int {
        // Count how many lines match the upper trigram's pattern
        val changingCount = states.count {
            it == LineState.OLD_YANG || it == LineState.OLD_YIN
        }

        return when {
            // Pure hexagram (all same) → 世 at position 0 (归魂 style)
            primary.upperTrigram == primary.lowerTrigram -> 0
            // Single changing line → 世 near the change
            changingCount == 1 -> {
                val changeIdx = states.indexOfFirst {
                    it == LineState.OLD_YANG || it == LineState.OLD_YIN
                }
                changeIdx
            }
            // Default heuristic: world line at position 3 (fifth line)
            else -> {
                // Count yang lines to determine palace position
                val yangCount = primary.binary.count { it }
                when {
                    yangCount >= 5 -> 4  // Mostly yang → late palace
                    yangCount >= 3 -> 3  // Middle → middle palace
                    yangCount >= 1 -> 1  // Mostly yin → early palace
                    else -> 0
                }
            }
        }
    }

    // ─────────────────────────── Six Spirits Assignment ───────────────────────────

    private fun assignSpirits(dayStemIndex: Int): List<SixSpirit> {
        val startSpirit = spiritForDayStem(dayStemIndex)
        val allSpirits = SixSpirit.entries
        val startIdx = allSpirits.indexOf(startSpirit)
        return (0..5).map { i ->
            allSpirits[(startIdx + i) % allSpirits.size]
        }
    }

    // ─────────────────────────── Hidden Lines (伏神) ───────────────────────────

    /**
     * Find hidden lines (伏神) — when a needed Six Relation is missing
     * from the primary hexagram, it becomes a hidden line attached to
     * the corresponding position.
     */
    private fun findHiddenLines(lines: List<YaoLine>, subjectWuXing: WuXing): List<YaoLine> {
        val presentRelations = lines.mapNotNull { it.relation }.toSet()
        val allRelations = SixRelation.entries.toSet()
        val missingRelations = allRelations - presentRelations

        if (missingRelations.isEmpty()) return emptyList()

        // Generate ideal hexagram lines for missing relations
        return missingRelations.mapIndexed { idx, relation ->
            val position = (idx) % 6
            // Generate a placeholder branch for the hidden line
            val branchIdx = (position * 2) % EARTHLY_BRANCHES.size
            val branch = EARTHLY_BRANCHES[branchIdx]
            YaoLine(
                position = position,
                isYang = position % 2 == 0,
                state = LineState.YOUNG_YANG,
                branch = branch,
                relation = relation,
                isHidden = true,
            )
        }
    }

    // ─────────────────────────── Analysis Engine ───────────────────────────

    private fun analyze(
        lines: List<YaoLine>,
        primary: Hexagram,
        changed: Hexagram,
        worldIdx: Int,
        responseIdx: Int,
        states: List<LineState>,
    ): Analysis {
        val subjectWuXing = toWuXing(primary.element)

        // Find the useful god (用神) — typically the line whose relation matches the question type
        val usefulRelation = determineUsefulGod(lines, primary)
        val usefulGodDesc = usefulRelation?.chinese ?: "无法确定"

        // Calculate overall strength (旺衰)
        val strength = calculateStrength(lines, primary, worldIdx)

        // Determine if the hexagram is favorable
        val hasChangingLines = states.any {
            it == LineState.OLD_YANG || it == LineState.OLD_YIN
        }
        val changingLinePositions = states.mapIndexedNotNull { idx, state ->
            if (state == LineState.OLD_YANG || state == LineState.OLD_YIN) idx else null
        }

        // Build interpretation
        val interp = buildInterpretation(primary, changed, hasChangingLines, changingLinePositions, strength)

        // Build advice
        val advice = buildAdvice(strength, hasChangingLines, primary)

        return Analysis(
            usefulGod = usefulGodDesc,
            strength = strength,
            interpretation = interp,
            advice = advice,
        )
    }

    private fun determineUsefulGod(lines: List<YaoLine>, hexagram: Hexagram): SixRelation? {
        // The useful god depends on the question type:
        // 求财 → 妻财; 求官 → 官鬼; 求学 → 父母; 求子 → 子孙; 同辈/竞争 → 兄弟
        // For generality, return the most prominent relation present
        val relationCounts = lines.groupBy { it.relation }
            .mapValues { it.value.size }
            .filterKeys { it != null }
            .toMap()
        return relationCounts.maxByOrNull { it.value }?.key
    }

    private fun calculateStrength(
        lines: List<YaoLine>,
        hexagram: Hexagram,
        worldIdx: Int,
    ): String {
        val worldLine = lines[worldIdx]
        val branchWuXing = BRANCH_TO_WUXING[worldLine.branch] ?: WuXing.EARTH
        val hexWuXing = toWuXing(hexagram.element)

        // Simple strength check: world line branch element vs hexagram element
        val isWorldStrong = when {
            branchWuXing == hexWuXing -> true                    // 比和
            produces(branchWuXing, hexWuXing) -> true            // 相生
            else -> false
        }

        return if (isWorldStrong) "世爻旺相 — 有力" else "世爻休囚 — 力弱"
    }

    private fun buildInterpretation(
        primary: Hexagram,
        changed: Hexagram,
        hasChanges: Boolean,
        changePositions: List<Int>,
        strength: String,
    ): String {
        val sb = StringBuilder()

        sb.appendLine("【卦象解读】")
        sb.appendLine("本卦「${primary.chineseName}」象征: ${interpretHexagramImage(primary)}")

        if (hasChanges) {
            sb.appendLine("变卦「${changed.chineseName}」象征: ${interpretHexagramImage(changed)}")
            sb.appendLine("动爻在第${changePositions.joinToString("、") { "${it + 1}" }}爻，")
            sb.appendLine("表示事态正在变化之中。")
        } else {
            sb.appendLine("六爻安静，无动爻，事态较为稳定。")
        }

        sb.appendLine(strength)

        return sb.toString()
    }

    private fun interpretHexagramImage(hex: Hexagram): String {
        // Provide meaningful interpretations based on hexagram themes
        return when (hex.number) {
            1 -> "刚健进取，创造力旺盛"
            2 -> "柔顺包容，厚积薄发"
            3 -> "初创艰难，需耐心坚持"
            4 -> "蒙昧待启，需虚心学习"
            5 -> "等待时机，耐心自有回报"
            6 -> "争讼不利，宜和解退让"
            7 -> "团队协作，集体力量"
            8 -> "团结合作，寻求同盟"
            9 -> "小事可成，大事尚需积蓄"
            10 -> "谨慎行事，守礼方安"
            11 -> "阴阳调和，万事亨通"
            12 -> "闭塞不通，韬光养晦"
            13 -> "志同道合，携手共进"
            14 -> "大有收获，顺天应命"
            15 -> "谦逊有礼，万事皆宜"
            16 -> "热情高涨，把握机遇"
            17 -> "顺势而为，随机应变"
            18 -> "拨乱反正，革新除弊"
            19 -> "渐进发展，前景可期"
            20 -> "静观其变，洞察本质"
            21 -> "果断决绝，除旧布新"
            22 -> "文饰之美，注重形式"
            23 -> "衰败之象，不宜妄动"
            24 -> "否极泰来，恢复生机"
            25 -> "纯真无妄，顺应自然"
            26 -> "厚积薄发，蓄势待发"
            27 -> "颐养正道，节制饮食"
            28 -> "非常之时，需独立不惧"
            29 -> "重重险阻，以恒心克之"
            30 -> "光明依附，延续正道"
            31 -> "感应相通，心心相印"
            32 -> "恒久不变，持之以恒"
            33 -> "适时退避，保存实力"
            34 -> "阳刚壮盛，以礼节之"
            35 -> "日进地上，前途光明"
            36 -> "韬光养晦，内明外暗"
            37 -> "家和万事兴，正家正己"
            38 -> "异中求同，化解矛盾"
            39 -> "前路受阻，反身修德"
            40 -> "困难消解，雨过天晴"
            41 -> "损上益下，适度减损"
            42 -> "增益之象，见善则迁"
            43 -> "果断决裂，清除障碍"
            44 -> "不期而遇，谨慎应对"
            45 -> "聚集人心，团结力量"
            46 -> "循序渐进，步步高升"
            47 -> "困境磨砺，坚守志向"
            48 -> "深井养德，利人利己"
            49 -> "变革除旧，革故鼎新"
            50 -> "正位凝命，树立威望"
            51 -> "震动惊醒，恐惧修省"
            52 -> "止于至善，静心修养"
            53 -> "循序渐进，稳步推进"
            54 -> "婚嫁之时，谨慎行事"
            55 -> "丰盛之极，居安思危"
            56 -> "旅途在外，谨慎自守"
            57 -> "柔顺渗透，循序渐进"
            58 -> "喜悦和乐，朋友讲习"
            59 -> "涣散离析，精神凝聚"
            60 -> "节制有度，适可而止"
            61 -> "诚信感化，内心真诚"
            62 -> "小事可为，谦逊谨慎"
            63 -> "功成之后，慎防松懈"
            64 -> "尚未完成，继续努力"
            else -> "蕴含深意，需仔细体悟"
        }
    }

    private fun buildAdvice(strength: String, hasChanges: Boolean, primary: Hexagram): String {
        val sb = StringBuilder()
        sb.append("【建议】")

        when {
            strength.contains("旺相") && hasChanges -> {
                sb.appendLine("世爻有力且有动爻变化，")
                sb.appendLine("建议积极行动，把握变化带来的机遇。")
            }
            strength.contains("旺相") && !hasChanges -> {
                sb.appendLine("世爻有力但六爻安静，")
                sb.appendLine("当前状况稳定，可维持现状，静待时机。")
            }
            strength.contains("休囚") && hasChanges -> {
                sb.appendLine("世爻力弱且有动爻变化，")
                sb.appendLine("需谨慎行事，宜守不宜攻，防小人暗害。")
            }
            strength.contains("休囚") && !hasChanges -> {
                sb.appendLine("世爻力弱且六爻安静，")
                sb.appendLine("当前不宜大举行动，应韬光养晦，等待时机好转。")
            }
        }

        // Hexagram-specific advice
        val advice = when (primary.number) {
            1, 11, 13, 14, 15, 26, 35, 42 -> "大吉之象，宜积极进取"
            2, 12, 23, 33, 36, 47, 52 -> "宜守不宜进，韬光养晦"
            3, 4, 29, 39, 52 -> "困难之时，需耐心坚持"
            6, 10, 38, 40 -> "争讼不利，宜和解"
            5, 16, 17, 19, 20, 24, 25, 45, 46 -> "时机渐至，顺势而为"
            else -> "综合判断，谨慎行事"
        }
        sb.appendLine(advice)

        return sb.toString()
    }

    // ─────────────────────────── Utility Methods ───────────────────────────

    /**
     * Quick coin toss summary (for UI display).
     * Returns list of 6 toss descriptions.
     */
    fun quickToss(): List<Pair<String, LineState>> {
        return (1..6).map { i ->
            val toss = throwCoins()
            val label = when (toss.lineState) {
                LineState.OLD_YANG -> "老阳 ○ (变)"
                LineState.OLD_YIN -> "老阴 × (变)"
                LineState.YOUNG_YANG -> "少阳 —"
                LineState.YOUNG_YIN -> "少阴 - -"
            }
            label to toss.lineState
        }
    }

    /**
     * Generate hexagram from specific line states (for testing).
     */
    fun divineFromStates(question: String, states: List<LineState>): DivinationResult {
        val (primary, changed) = interpretLines(states)
        val worldIdx = calculateWorldLine(primary, states)
        val responseIdx = (worldIdx + 3) % 6
        val branches = assignBranches(primary)
        val subjectWuXing = toWuXing(primary.element)

        val lines = (0..5).map { i ->
            YaoLine(
                position = i,
                isYang = primary.binary[i],
                state = states[i],
                branch = branches[i],
                relation = relationFrom(subjectWuXing, BRANCH_TO_WUXING[branches[i]] ?: WuXing.EARTH),
            )
        }

        val dayStem = java.time.LocalDate.now().dayOfYear % 10
        val spirits = assignSpirits(dayStem)
        val hiddenLines = findHiddenLines(lines, subjectWuXing)
        val analysis = analyze(lines, primary, changed, worldIdx, responseIdx, states)

        val tosses = states.map { state ->
            val (coins, sum) = when (state) {
                LineState.OLD_YANG -> listOf(1,1,1) to 3
                LineState.OLD_YIN -> listOf(2,2,2) to 6
                LineState.YOUNG_YANG -> listOf(1,1,2) to 5
                LineState.YOUNG_YIN -> listOf(1,2,2) to 4
            }
            CoinToss(coins, sum, state)
        }

        return DivinationResult(
            question = question,
            timestamp = System.currentTimeMillis(),
            tosses = tosses,
            primaryHexagram = primary,
            changedHexagram = changed,
            lines = lines,
            worldLine = worldIdx,
            responseLine = responseIdx,
            spirits = spirits,
            hiddenLines = hiddenLines,
            analysis = analysis,
        )
    }
}
