import Foundation

// MARK: - Tarot Card

public struct TarotCard: Identifiable, Sendable {
    public let id: Int
    public let number: Int           // 0-21 for Major, 1-14 for Minor per suit
    public let nameCN: String
    public let nameEN: String
    public let suit: String          // "大阿卡纳", "权杖", "圣杯", "宝剑", "星币"
    public let arcanaType: ArcanaType
    public let uprightMeaning: String
    public let reversedMeaning: String

    public init(id: Int, number: Int, nameCN: String, nameEN: String, suit: String, arcanaType: ArcanaType, uprightMeaning: String, reversedMeaning: String) {
        self.id = id
        self.number = number
        self.nameCN = nameCN
        self.nameEN = nameEN
        self.suit = suit
        self.arcanaType = arcanaType
        self.uprightMeaning = uprightMeaning
        self.reversedMeaning = reversedMeaning
    }

    public enum ArcanaType: String, Sendable {
        case major = "大阿卡纳"
        case minor = "小阿卡纳"
    }

    public func displayName(isReversed: Bool) -> String {
        let position = isReversed ? "[逆位]" : "[正位]"
        return "\(nameCN) (\(nameEN)) \(position)"
    }

    public func currentMeaning(isReversed: Bool) -> String {
        isReversed ? reversedMeaning : uprightMeaning
    }
}

// MARK: - Spread Type

public enum SpreadType: Int, Sendable {
    case single    = 1
    case threeCard = 3

    public var positionNames: [String] {
        switch self {
        case .single:    return ["指引"]
        case .threeCard: return ["过去", "现在", "未来"]
        }
    }
}

// MARK: - Draw Result

public struct TarotDrawResult: Sendable {
    public let card: TarotCard
    public let position: String      // Position name in spread
    public let isReversed: Bool

    public init(card: TarotCard, position: String, isReversed: Bool) {
        self.card = card
        self.position = position
        self.isReversed = isReversed
    }
}

// MARK: - Tarot Engine

public enum TarotEngine {

    // MARK: - Major Arcana (大阿卡纳)

    public static let majorArcana: [TarotCard] = [
        TarotCard(id: 0, number: 0, nameCN: "愚者", nameEN: "The Fool", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "新的开始、冒险、自由、天真",
            reversedMeaning: "鲁莽、犹豫不决、冒不必要的风险"),
        TarotCard(id: 1, number: 1, nameCN: "魔术师", nameEN: "The Magician", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "创造力、技能、意志力、新的机会",
            reversedMeaning: "欺骗、操控、缺乏方向"),
        TarotCard(id: 2, number: 2, nameCN: "女祭司", nameEN: "The High Priestess", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "直觉、潜意识、神秘、内在智慧",
            reversedMeaning: "隐藏的动机、信息泄露、过度依赖理性"),
        TarotCard(id: 3, number: 3, nameCN: "皇后", nameEN: "The Empress", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "丰收、母性、自然、美丽、富足",
            reversedMeaning: "依赖、过度保护、创造力受阻"),
        TarotCard(id: 4, number: 4, nameCN: "皇帝", nameEN: "The Emperor", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "权威、稳定、领导力、父性",
            reversedMeaning: "专制、僵化、缺乏纪律"),
        TarotCard(id: 5, number: 5, nameCN: "教皇", nameEN: "The Hierophant", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "传统、信仰、教育、精神指导",
            reversedMeaning: "打破传统、非正统、个人信念"),
        TarotCard(id: 6, number: 6, nameCN: "恋人", nameEN: "The Lovers", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "爱情、和谐、选择、关系",
            reversedMeaning: "失衡、价值观冲突、错误的选择"),
        TarotCard(id: 7, number: 7, nameCN: "战车", nameEN: "The Chariot", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "胜利、意志力、决心、克服困难",
            reversedMeaning: "失控、缺乏方向、侵略性"),
        TarotCard(id: 8, number: 8, nameCN: "力量", nameEN: "Strength", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "勇气、耐心、内在力量、自律",
            reversedMeaning: "软弱、缺乏自信、自我怀疑"),
        TarotCard(id: 9, number: 9, nameCN: "隐士", nameEN: "The Hermit", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "内省、孤独、寻求真理、智慧",
            reversedMeaning: "孤立、逃避现实、过度封闭"),
        TarotCard(id: 10, number: 10, nameCN: "命运之轮", nameEN: "Wheel of Fortune", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "命运、转变、好运、循环",
            reversedMeaning: "厄运、抗拒改变、失控"),
        TarotCard(id: 11, number: 11, nameCN: "正义", nameEN: "Justice", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "公正、真相、因果报应、法律",
            reversedMeaning: "不公、逃避责任、偏见"),
        TarotCard(id: 12, number: 12, nameCN: "倒吊人", nameEN: "The Hanged Man", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "牺牲、等待、新的视角、放下",
            reversedMeaning: "拖延、抗拒、无谓的牺牲"),
        TarotCard(id: 13, number: 13, nameCN: "死神", nameEN: "Death", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "结束、转变、新生、放下过去",
            reversedMeaning: "抗拒改变、停滞不前、恐惧"),
        TarotCard(id: 14, number: 14, nameCN: "节制", nameEN: "Temperance", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "平衡、耐心、调和、中庸",
            reversedMeaning: "失衡、过度、极端"),
        TarotCard(id: 15, number: 15, nameCN: "恶魔", nameEN: "The Devil", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "束缚、诱惑、物质主义、阴影",
            reversedMeaning: "解脱、释放、打破束缚"),
        TarotCard(id: 16, number: 16, nameCN: "塔", nameEN: "The Tower", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "突变、破坏、觉醒、真相",
            reversedMeaning: "逃避灾难、恐惧改变、内乱"),
        TarotCard(id: 17, number: 17, nameCN: "星星", nameEN: "The Star", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "希望、灵感、宁静、治愈",
            reversedMeaning: "失望、缺乏信心、孤独"),
        TarotCard(id: 18, number: 18, nameCN: "月亮", nameEN: "The Moon", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "幻觉、恐惧、潜意识、直觉",
            reversedMeaning: "释放恐惧、清晰、真相大白"),
        TarotCard(id: 19, number: 19, nameCN: "太阳", nameEN: "The Sun", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "成功、快乐、活力、乐观",
            reversedMeaning: "短暂的快乐、过度乐观、自满"),
        TarotCard(id: 20, number: 20, nameCN: "审判", nameEN: "Judgement", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "觉醒、重生、召唤、判断",
            reversedMeaning: "自我怀疑、逃避、拒绝改变"),
        TarotCard(id: 21, number: 21, nameCN: "世界", nameEN: "The World", suit: "大阿卡纳", arcanaType: .major,
            uprightMeaning: "完成、成就、圆满、新的循环",
            reversedMeaning: "未完成、缺乏终结、停滞"),
    ]

    // MARK: - Minor Arcana (小阿卡纳)

    private static let suits = ["权杖", "圣杯", "宝剑", "星币"]
    private static let suitEN = ["Wands", "Cups", "Swords", "Pentacles"]
    private static let courtCards = ["侍从", "骑士", "王后", "国王"]
    private static let courtEN = ["Page", "Knight", "Queen", "King"]
    private static let chineseNumbers = ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十"]

    private static let suitBaseMeanings: [String: (upright: String, reversed: String)] = [
        "权杖": (upright: "创造力、行动、激情", reversed: "创造力受阻、行动力不足"),
        "圣杯": (upright: "情感、直觉、关系", reversed: "情感困惑、关系不顺"),
        "宝剑": (upright: "思想、沟通、冲突", reversed: "思维混乱、沟通障碍"),
        "星币": (upright: "物质、财富、实际", reversed: "财务不稳、物质匮乏"),
    ]

    private static let numberModifiers: [Int: (upright: String, reversed: String)] = [
        1:  (upright: "新的开始、机遇", reversed: "错失机会、方向不明"),
        2:  (upright: "平衡、选择", reversed: "犹豫不决、失衡"),
        3:  (upright: "成长、扩张", reversed: "成长受阻、停滞"),
        4:  (upright: "稳定、基础", reversed: "不稳定、根基动摇"),
        5:  (upright: "挑战、冲突", reversed: "冲突化解、走出困境"),
        6:  (upright: "和谐、合作", reversed: "不和谐、合作破裂"),
        7:  (upright: "内省、评估", reversed: "自我欺骗、评估偏差"),
        8:  (upright: "行动、进展", reversed: "行动迟缓、进展受阻"),
        9:  (upright: "完成、成熟", reversed: "未完成、不成熟"),
        10: (upright: "圆满、结束", reversed: "循环未尽、结局不完美"),
    ]

    private static let courtMeanings: [String: (upright: String, reversed: String)] = [
        "侍从": (upright: "学习、探索、新消息", reversed: "不成熟、缺乏方向"),
        "骑士": (upright: "行动、冒险、追寻", reversed: "鲁莽、冲动、缺乏耐心"),
        "王后": (upright: "直觉、养育、内在力量", reversed: "过度保护、情感依赖"),
        "国王": (upright: "权威、领导、掌控", reversed: "专制、控制欲强"),
    ]

    public static let minorArcana: [TarotCard] = {
        var cards: [TarotCard] = []
        var id = 22

        for (suitIdx, suit) in suits.enumerated() {
            let suitEN = suitEN[suitIdx]

            // Number cards Ace-10
            for num in 1...10 {
                let cnNum = chineseNumbers[num - 1]
                let base = suitBaseMeanings[suit]!
                let mod = numberModifiers[num]!
                cards.append(TarotCard(
                    id: id, number: num,
                    nameCN: "\(suit)\(cnNum)",
                    nameEN: "\(num) of \(suitEN)",
                    suit: suit, arcanaType: .minor,
                    uprightMeaning: "\(base.upright)，\(mod.upright)",
                    reversedMeaning: "\(base.reversed)，\(mod.reversed)"
                ))
                id += 1
            }

            // Court cards
            for (courtIdx, court) in courtCards.enumerated() {
                let courtENName = courtEN[courtIdx]
                let base = suitBaseMeanings[suit]!
                let courtMod = courtMeanings[court]!
                cards.append(TarotCard(
                    id: id, number: courtIdx + 11,
                    nameCN: "\(suit)\(court)",
                    nameEN: "\(courtENName) of \(suitEN)",
                    suit: suit, arcanaType: .minor,
                    uprightMeaning: "\(courtMod.upright)，与\(suit)元素相关",
                    reversedMeaning: "\(courtMod.reversed)，与\(suit)元素相关"
                ))
                id += 1
            }
        }
        return cards
    }()

    // MARK: - Full Deck

    public static let fullDeck: [TarotCard] = majorArcana + minorArcana

    // MARK: - Shuffle & Draw

    /// Shuffle the deck and draw cards for a given spread type.
    public static func shuffleAndDraw(count: Int, spreadType: SpreadType) -> [TarotDrawResult] {
        let shuffled = fullDeck.shuffled()
        let positions = spreadType.positionNames
        let drawCount = min(count, shuffled.count)

        return (0..<drawCount).map { i in
            TarotDrawResult(
                card: shuffled[i],
                position: i < positions.count ? positions[i] : "位置\(i + 1)",
                isReversed: Double.random(in: 0..<1) < 0.33
            )
        }
    }

    // MARK: - Lookup

    public static func findCard(byId id: Int) -> TarotCard? {
        fullDeck.first { $0.id == id }
    }

    public static func findCard(byName name: String) -> TarotCard? {
        fullDeck.first {
            $0.nameEN.caseInsensitiveCompare(name) == .orderedSame ||
            $0.nameCN.contains(name)
        }
    }
}
