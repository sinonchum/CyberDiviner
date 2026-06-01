import Foundation

// MARK: - Liuyao Engine (六爻算法)

/// Complete Six Lines (Liuyao) divination engine.
/// Ports the full workflow: coin toss → hexagram → NaJia → Six Relations → Six Spirits → analysis.
public struct LiuyaoEngine {

    // MARK: - World/Response Line Positions (八宫)

    /// World line position (0-indexed) based on palace position.
    private static let worldLineByPalace: [Int] = [
        0, // First hexagram of palace — 世 at line 1
        1, // Second — 世 at line 2
        2, // Third — 世 at line 3
        3, // Fourth — 世 at line 4
        4, // Fifth — 世 at line 5
        3, // 游魂 — 世 at line 4
        0, // 归魂 — 世 at line 1
    ]

    // MARK: - Coin Toss

    public struct CoinToss: Sendable {
        public let coins: [Int]    // 3 coins: 2=字(yang), 1=花(yin)
        public let sum: Int
        public let lineState: LineState

        public init(coins: [Int], sum: Int, lineState: LineState) {
            self.coins = coins
            self.sum = sum
            self.lineState = lineState
        }
    }

    // MARK: - Analysis

    public struct Analysis: Sendable {
        public let usefulGod: String
        public let strength: String
        public let interpretation: String
        public let advice: String

        public init(usefulGod: String, strength: String, interpretation: String, advice: String) {
            self.usefulGod = usefulGod
            self.strength = strength
            self.interpretation = interpretation
            self.advice = advice
        }
    }

    // MARK: - Divination Result

    public struct DivinationResult: Sendable {
        public let question: String
        public let timestamp: TimeInterval
        public let tosses: [CoinToss]
        public let primaryHexagram: Hexagram
        public let changedHexagram: Hexagram
        public let lines: [YaoLine]
        public let worldLine: Int          // 0-indexed
        public let responseLine: Int       // 0-indexed
        public let spirits: [SixSpirit]
        public let hiddenLines: [YaoLine]
        public let analysis: Analysis

        public init(question: String, timestamp: TimeInterval, tosses: [CoinToss], primaryHexagram: Hexagram, changedHexagram: Hexagram, lines: [YaoLine], worldLine: Int, responseLine: Int, spirits: [SixSpirit], hiddenLines: [YaoLine], analysis: Analysis) {
            self.question = question
            self.timestamp = timestamp
            self.tosses = tosses
            self.primaryHexagram = primaryHexagram
            self.changedHexagram = changedHexagram
            self.lines = lines
            self.worldLine = worldLine
            self.responseLine = responseLine
            self.spirits = spirits
            self.hiddenLines = hiddenLines
            self.analysis = analysis
        }

        public var hasChangingLines: Bool {
            tosses.contains { $0.lineState.isChanging }
        }

        public func summary() -> String {
            var s = ""
            s += "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
            s += "六爻占卜 — Liuyao Divination\n"
            s += "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
            s += "问事: \(question)\n\n"
            s += "本卦 (Primary): \(primaryHexagram.chineseName) \(primaryHexagram.englishName)\n"
            s += "  卦辞: \(primaryHexagram.judgment)\n"
            if hasChangingLines {
                s += "变卦 (Changed): \(changedHexagram.chineseName) \(changedHexagram.englishName)\n"
                s += "  卦辞: \(changedHexagram.judgment)\n"
            }
            s += "\n━━━ 六爻排列 (Lines) ━━━\n"
            for i in stride(from: 5, through: 0, by: -1) {
                let line = lines[i]
                let bar = line.isYang ? "━━━━━" : "━   ━"
                let changeMark: String
                switch line.state {
                case .oldYang: changeMark = "×"
                case .oldYin:  changeMark = "○"
                default:       changeMark = ""
                }
                let shiYing: String
                if i == worldLine { shiYing = "世" }
                else if i == responseLine { shiYing = "应" }
                else { shiYing = "" }
                let relLabel = line.relation?.chinese ?? ""
                s += "  \(i+1)爻 [\(line.branch)] \(bar) \(changeMark)  \(relLabel) \(spirits[i].chinese) \(shiYing)\n"
            }
            s += "\n━━━ 分析 (Analysis) ━━━\n"
            s += "  用神: \(analysis.usefulGod)\n"
            s += "  世爻: 第\(worldLine+1)爻 [\(lines[worldLine].branch)] \(lines[worldLine].relation?.chinese ?? "")\n"
            s += "  应爻: 第\(responseLine+1)爻 [\(lines[responseLine].branch)] \(lines[responseLine].relation?.chinese ?? "")\n"
            s += "  旺衰: \(analysis.strength)\n"
            s += "  卦象: \(analysis.interpretation)\n"
            if !hiddenLines.isEmpty {
                s += "  伏神:\n"
                for h in hiddenLines {
                    s += "    \(h.position+1)爻 [\(h.branch)] \(h.relation?.chinese ?? "")\n"
                }
            }
            s += "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
            return s
        }
    }

    // MARK: - Core Divination

    /// Perform a full Liuyao divination from 6 line values (3=老阳, 4=少阴, 5=少阳, 6=老阴).
    public static func castHexagram(from lineValues: [Int], question: String = "", dayStemIndex: Int? = nil) -> DivinationResult {
        let tosses = lineValues.map { value -> CoinToss in
            let state: LineState
            switch value {
            case 3:  state = .oldYang
            case 4:  state = .youngYin
            case 5:  state = .youngYang
            case 6:  state = .oldYin
            default: state = .youngYang
            }
            return CoinToss(coins: [], sum: value, lineState: state)
        }
        return buildResult(tosses: tosses, question: question, dayStemIndex: dayStemIndex)
    }

    /// Perform a full Liuyao divination by throwing coins (random).
    public static func divine(question: String = "", dayStemIndex: Int? = nil) -> DivinationResult {
        let tosses = (0..<6).map { _ in throwCoins() }
        return buildResult(tosses: tosses, question: question, dayStemIndex: dayStemIndex)
    }

    // MARK: - Coin Toss Simulation

    /// Simulate 3-coin toss (三钱法).
    /// 2 = yang side (字), 1 = yin side (花).
    /// Sum: 3=老阳, 4=少阴, 5=少阳, 6=老阴
    public static func throwCoins() -> CoinToss {
        let coins = (0..<3).map { _ in Bool.random() ? 2 : 1 }
        let sum = coins.reduce(0, +)
        let state: LineState
        switch sum {
        case 6: state = .oldYin
        case 5: state = .youngYang
        case 4: state = .youngYin
        case 3: state = .oldYang
        default: state = .youngYang
        }
        return CoinToss(coins: coins, sum: sum, lineState: state)
    }

    // MARK: - Private Build Pipeline

    private static func buildResult(tosses: [CoinToss], question: String, dayStemIndex: Int?) -> DivinationResult {
        let states = tosses.map { $0.lineState }

        // Primary and changed hexagrams
        let (primary, changed) = interpretLines(states)

        // World & response lines
        let worldIdx = calculateWorldLine(primary: primary, states: states)
        let responseIdx = (worldIdx + 3) % 6

        // Earthly branches via NaJia
        let branches = assignBranches(primary)

        // Six Relations
        let subjectWuXing = wuXingFrom(element: primary.element)
        let yaoLines: [YaoLine] = (0..<6).map { i in
            let branch = branches[i]
            let branchWX = branchToWuXing[branch] ?? .earth
            let relation = relationFrom(subject: subjectWuXing, target: branchWX)
            return YaoLine(
                position: i,
                isYang: primary.binary[i],
                state: states[i],
                branch: branch,
                relation: relation,
                isHidden: false
            )
        }

        // Six Spirits
        let resolvedDayStem = dayStemIndex ?? Calendar.current.ordinality(of: .day, in: .year, for: Date()).map { ($0 - 1) % 10 } ?? 0
        let spirits = assignSpirits(dayStemIndex: resolvedDayStem)

        // Hidden lines
        let hidden = findHiddenLines(lines: yaoLines, subjectWuXing: subjectWuXing)

        // Analysis
        let analysis = analyze(
            lines: yaoLines, primary: primary, changed: changed,
            worldIdx: worldIdx, responseIdx: responseIdx, states: states
        )

        return DivinationResult(
            question: question,
            timestamp: Date().timeIntervalSince1970,
            tosses: tosses,
            primaryHexagram: primary,
            changedHexagram: changed,
            lines: yaoLines,
            worldLine: worldIdx,
            responseLine: responseIdx,
            spirits: spirits,
            hiddenLines: hidden,
            analysis: analysis
        )
    }

    // MARK: - NaJia Branch Assignment

    private static func assignBranches(_ hexagram: Hexagram) -> [String] {
        let lowerBranches = trigramBranches[hexagram.lowerTrigram]!
        let upperBranches = trigramBranches[hexagram.upperTrigram]!
        return [
            lowerBranches[0], lowerBranches[1], lowerBranches[2],
            upperBranches[3], upperBranches[4], upperBranches[5],
        ]
    }

    // MARK: - World Line Calculation

    private static func calculateWorldLine(primary: Hexagram, states: [LineState]) -> Int {
        let changingCount = states.filter { $0.isChanging }.count

        // Pure hexagram (upper == lower) → 世 at position 0
        if primary.upperTrigram == primary.lowerTrigram { return 0 }

        // Single changing line → 世 near the change
        if changingCount == 1 {
            if let idx = states.firstIndex(where: { $0.isChanging }) {
                return idx
            }
        }

        // Default heuristic based on yang count
        let yangCount = primary.binary.filter { $0 }.count
        switch yangCount {
        case 5...: return 4
        case 3...: return 3
        case 1...: return 1
        default:   return 0
        }
    }

    // MARK: - Six Spirits Assignment

    /// 甲乙→青龙, 丙丁→朱雀, 戊→勾陈, 己→螣蛇, 庚辛→白虎, 壬癸→玄武
    private static func spiritForDayStem(_ stemIndex: Int) -> SixSpirit {
        switch stemIndex % 10 {
        case 0, 1: return .qinglong
        case 2, 3: return .zhuque
        case 4:    return .gouchen
        case 5:    return .tengshe
        case 6, 7: return .baihu
        case 8, 9: return .xuanwu
        default:   return .qinglong
        }
    }

    private static func assignSpirits(dayStemIndex: Int) -> [SixSpirit] {
        let start = spiritForDayStem(dayStemIndex)
        let all = SixSpirit.allCases
        let startIdx = all.firstIndex(of: start) ?? 0
        return (0..<6).map { all[(startIdx + $0) % all.count] }
    }

    // MARK: - Hidden Lines (伏神)

    private static func findHiddenLines(lines: [YaoLine], subjectWuXing: WuXing) -> [YaoLine] {
        let present = Set(lines.compactMap { $0.relation })
        let missing = Set(SixRelation.allCases).subtracting(present)
        if missing.isEmpty { return [] }

        return Array(missing).enumerated().map { idx, relation in
            let position = idx % 6
            let branchIdx = (position * 2) % earthlyBranches.count
            return YaoLine(
                position: position,
                isYang: position % 2 == 0,
                state: .youngYang,
                branch: earthlyBranches[branchIdx],
                relation: relation,
                isHidden: true
            )
        }
    }

    // MARK: - Analysis

    private static func analyze(
        lines: [YaoLine], primary: Hexagram, changed: Hexagram,
        worldIdx: Int, responseIdx: Int, states: [LineState]
    ) -> Analysis {
        let usefulGod = determineUsefulGod(lines: lines)
        let strength = calculateStrength(lines: lines, hexagram: primary, worldIdx: worldIdx)
        let interp = buildInterpretation(primary: primary, changed: changed, states: states, strength: strength)
        let advice = buildAdvice(strength: strength, states: states, primary: primary)

        return Analysis(
            usefulGod: usefulGod,
            strength: strength,
            interpretation: interp,
            advice: advice
        )
    }

    private static func determineUsefulGod(lines: [YaoLine]) -> String {
        var counts: [SixRelation: Int] = [:]
        for line in lines {
            if let rel = line.relation {
                counts[rel, default: 0] += 1
            }
        }
        return counts.max(by: { $0.value < $1.value })?.key.chinese ?? "无法确定"
    }

    private static func calculateStrength(lines: [YaoLine], hexagram: Hexagram, worldIdx: Int) -> String {
        let worldLine = lines[worldIdx]
        let branchWX = branchToWuXing[worldLine.branch] ?? .earth
        let hexWX = wuXingFrom(element: hexagram.element)

        let isStrong = (branchWX == hexWX) || branchWX.generates(hexWX)
        return isStrong ? "世爻旺相 — 有力" : "世爻休囚 — 力弱"
    }

    private static func buildInterpretation(
        primary: Hexagram, changed: Hexagram, states: [LineState], strength: String
    ) -> String {
        var s = ""
        s += "【卦象解读】\n"
        s += "本卦「\(primary.chineseName)」象征: \(hexagramImage(primary))\n"

        let changingPositions = states.enumerated().compactMap { $0.element.isChanging ? $0.offset : nil }
        if !changingPositions.isEmpty {
            let posStr = changingPositions.map { "\($0 + 1)" }.joined(separator: "、")
            s += "变卦「\(changed.chineseName)」象征: \(hexagramImage(changed))\n"
            s += "动爻在第\(posStr)爻，\n"
            s += "表示事态正在变化之中。\n"
        } else {
            s += "六爻安静，无动爻，事态较为稳定。\n"
        }
        s += strength
        return s
    }

    private static func hexagramImage(_ hex: Hexagram) -> String {
        switch hex.number {
        case 1:  return "刚健进取，创造力旺盛"
        case 2:  return "柔顺包容，厚积薄发"
        case 3:  return "初创艰难，需耐心坚持"
        case 4:  return "蒙昧待启，需虚心学习"
        case 5:  return "等待时机，耐心自有回报"
        case 6:  return "争讼不利，宜和解退让"
        case 7:  return "团队协作，集体力量"
        case 8:  return "团结合作，寻求同盟"
        case 9:  return "小事可成，大事尚需积蓄"
        case 10: return "谨慎行事，守礼方安"
        case 11: return "阴阳调和，万事亨通"
        case 12: return "闭塞不通，韬光养晦"
        case 13: return "志同道合，携手共进"
        case 14: return "大有收获，顺天应命"
        case 15: return "谦逊有礼，万事皆宜"
        case 16: return "热情高涨，把握机遇"
        case 17: return "顺势而为，随机应变"
        case 18: return "拨乱反正，革新除弊"
        case 19: return "渐进发展，前景可期"
        case 20: return "静观其变，洞察本质"
        case 21: return "果断决绝，除旧布新"
        case 22: return "文饰之美，注重形式"
        case 23: return "衰败之象，不宜妄动"
        case 24: return "否极泰来，恢复生机"
        case 25: return "纯真无妄，顺应自然"
        case 26: return "厚积薄发，蓄势待发"
        case 27: return "颐养正道，节制饮食"
        case 28: return "非常之时，需独立不惧"
        case 29: return "重重险阻，以恒心克之"
        case 30: return "光明依附，延续正道"
        case 31: return "感应相通，心心相印"
        case 32: return "恒久不变，持之以恒"
        case 33: return "适时退避，保存实力"
        case 34: return "阳刚壮盛，以礼节之"
        case 35: return "日进地上，前途光明"
        case 36: return "韬光养晦，内明外暗"
        case 37: return "家和万事兴，正家正己"
        case 38: return "异中求同，化解矛盾"
        case 39: return "前路受阻，反身修德"
        case 40: return "困难消解，雨过天晴"
        case 41: return "损上益下，适度减损"
        case 42: return "增益之象，见善则迁"
        case 43: return "果断决裂，清除障碍"
        case 44: return "不期而遇，谨慎应对"
        case 45: return "聚集人心，团结力量"
        case 46: return "循序渐进，步步高升"
        case 47: return "困境磨砺，坚守志向"
        case 48: return "深井养德，利人利己"
        case 49: return "变革除旧，革故鼎新"
        case 50: return "正位凝命，树立威望"
        case 51: return "震动惊醒，恐惧修省"
        case 52: return "止于至善，静心修养"
        case 53: return "循序渐进，稳步推进"
        case 54: return "婚嫁之时，谨慎行事"
        case 55: return "丰盛之极，居安思危"
        case 56: return "旅途在外，谨慎自守"
        case 57: return "柔顺渗透，循序渐进"
        case 58: return "喜悦和乐，朋友讲习"
        case 59: return "涣散离析，精神凝聚"
        case 60: return "节制有度，适可而止"
        case 61: return "诚信感化，内心真诚"
        case 62: return "小事可为，谦逊谨慎"
        case 63: return "功成之后，慎防松懈"
        case 64: return "尚未完成，继续努力"
        default: return "蕴含深意，需仔细体悟"
        }
    }

    private static func buildAdvice(strength: String, states: [LineState], primary: Hexagram) -> String {
        let hasChanges = states.contains { $0.isChanging }
        var s = "【建议】"

        if strength.contains("旺相") && hasChanges {
            s += "世爻有力且有动爻变化，建议积极行动，把握变化带来的机遇。"
        } else if strength.contains("旺相") && !hasChanges {
            s += "世爻有力但六爻安静，当前状况稳定，可维持现状，静待时机。"
        } else if strength.contains("休囚") && hasChanges {
            s += "世爻力弱且有动爻变化，需谨慎行事，宜守不宜攻，防小人暗害。"
        } else {
            s += "世爻力弱且六爻安静，当前不宜大举行动，应韬光养晦，等待时机好转。"
        }

        let hexAdvice: String
        switch primary.number {
        case 1, 11, 13, 14, 15, 26, 35, 42: hexAdvice = "大吉之象，宜积极进取"
        case 2, 12, 23, 33, 36, 47:          hexAdvice = "宜守不宜进，韬光养晦"
        case 3, 4, 29, 39, 52:               hexAdvice = "困难之时，需耐心坚持"
        case 6, 10, 38, 40:                  hexAdvice = "争讼不利，宜和解"
        case 5, 16, 17, 19, 20, 24, 25, 45, 46: hexAdvice = "时机渐至，顺势而为"
        default:                              hexAdvice = "综合判断，谨慎行事"
        }
        s += "\n\(hexAdvice)"
        return s
    }
}
