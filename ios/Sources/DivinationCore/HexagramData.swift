import Foundation

// MARK: - Trigram (八卦)

/// The eight trigrams of the I Ching.
public enum Trigram: Int, CaseIterable, Sendable {
    case qian  = 0 // ☰ 乾 Heaven
    case dui   = 1 // ☱ 兑 Lake
    case li    = 2 // ☲ 离 Fire
    case zhen  = 3 // ☳ 震 Thunder
    case xun   = 4 // ☴ 巽 Wind
    case kan   = 5 // ☵ 坎 Water
    case gen   = 6 // ☶ 艮 Mountain
    case kun   = 7 // ☷ 坤 Earth

    public var chineseName: String {
        switch self {
        case .qian: return "乾"
        case .dui:  return "兑"
        case .li:   return "离"
        case .zhen: return "震"
        case .xun:  return "巽"
        case .kan:  return "坎"
        case .gen:  return "艮"
        case .kun:  return "坤"
        }
    }

    public var englishName: String {
        switch self {
        case .qian: return "Heaven"
        case .dui:  return "Lake"
        case .li:   return "Fire"
        case .zhen: return "Thunder"
        case .xun:  return "Wind"
        case .kan:  return "Water"
        case .gen:  return "Mountain"
        case .kun:  return "Earth"
        }
    }

    public var symbol: String {
        switch self {
        case .qian: return "☰"
        case .dui:  return "☱"
        case .li:   return "☲"
        case .zhen: return "☳"
        case .xun:  return "☴"
        case .kan:  return "☵"
        case .gen:  return "☶"
        case .kun:  return "☷"
        }
    }

    public var wuXing: WuXing {
        switch self {
        case .qian, .dui:  return .metal
        case .li:          return .fire
        case .zhen, .xun:  return .wood
        case .kan:         return .water
        case .gen, .kun:   return .earth
        }
    }

    public var nature: String {
        switch self {
        case .qian: return "天"
        case .dui:  return "泽"
        case .li:   return "火"
        case .zhen: return "雷"
        case .xun:  return "风"
        case .kan:  return "水"
        case .gen:  return "山"
        case .kun:  return "地"
        }
    }

    public var family: String {
        switch self {
        case .qian: return "父"
        case .kun:  return "母"
        case .zhen: return "长男"
        case .xun:  return "长女"
        case .kan:  return "中男"
        case .li:   return "中女"
        case .gen:  return "少男"
        case .dui:  return "少女"
        }
    }

    public var direction: String {
        switch self {
        case .qian: return "西北"
        case .kun:  return "西南"
        case .zhen: return "东"
        case .xun:  return "东南"
        case .kan:  return "北"
        case .li:   return "南"
        case .gen:  return "东北"
        case .dui:  return "西"
        }
    }

    /// Bottom → top line values (3 lines). `true` = yang.
    public var lines: [Bool] {
        switch self {
        case .qian: return [true, true, true]
        case .kun:  return [false, false, false]
        case .zhen: return [false, false, true]
        case .xun:  return [true, true, false]
        case .kan:  return [false, true, false]
        case .li:   return [true, false, true]
        case .gen:  return [true, false, false]
        case .dui:  return [false, true, true]
        }
    }

    /// 3-bit binary representation (bottom line = LSB).
    public var binaryCode: Int {
        switch self {
        case .qian: return 0b111
        case .kun:  return 0b000
        case .zhen: return 0b001
        case .xun:  return 0b110
        case .kan:  return 0b010
        case .li:   return 0b101
        case .gen:  return 0b100
        case .dui:  return 0b011
        }
    }
}

// MARK: - Line State (爻象)

/// The state of a single hexagram line.
public enum LineState: Int, Sendable {
    case youngYang = 1  // 少阳 — static yang  ━━━
    case youngYin  = 2  // 少阴 — static yin   ━ ━
    case oldYang   = 3  // 老阳 — changing yang ━━━ → ━ ━
    case oldYin    = 0  // 老阴 — changing yin  ━ ━ → ━━━

    public var isYang: Bool {
        switch self {
        case .youngYang, .oldYang: return true
        case .youngYin, .oldYin:   return false
        }
    }

    public var isChanging: Bool {
        switch self {
        case .oldYang, .oldYin: return true
        case .youngYang, .youngYin: return false
        }
    }

    public var description: String {
        switch self {
        case .youngYang: return "少阳 —"
        case .youngYin:  return "少阴 - -"
        case .oldYang:   return "老阳 ○ (变)"
        case .oldYin:    return "老阴 × (变)"
        }
    }
}

// MARK: - Five Elements (五行)

public enum WuXing: String, CaseIterable, Sendable {
    case wood  = "木"
    case fire  = "火"
    case earth = "土"
    case metal = "金"
    case water = "水"

    public var chinese: String { rawValue }

    public var english: String {
        switch self {
        case .wood:  return "Wood"
        case .fire:  return "Fire"
        case .earth: return "Earth"
        case .metal: return "Metal"
        case .water: return "Water"
        }
    }

    /// `self` produces (生) `other`.  木→火→土→金→水→木
    public func generates(_ other: WuXing) -> Bool {
        switch self {
        case .wood:  return other == .fire
        case .fire:  return other == .earth
        case .earth: return other == .metal
        case .metal: return other == .water
        case .water: return other == .wood
        }
    }

    /// `self` overcomes (克) `other`.  木→土→水→火→金→木
    public func overcomes(_ other: WuXing) -> Bool {
        switch self {
        case .wood:  return other == .earth
        case .fire:  return other == .metal
        case .earth: return other == .water
        case .metal: return other == .wood
        case .water: return other == .fire
        }
    }

    /// Compatible: same, generates, or is-generated-by.
    public func isCompatible(with other: WuXing) -> Bool {
        self == other || generates(other) || other.generates(self)
    }
}

/// Convert an English element name to WuXing.
public func wuXingFrom(element: String) -> WuXing {
    switch element {
    case "Wood":  return .wood
    case "Fire":  return .fire
    case "Earth": return .earth
    case "Metal": return .metal
    case "Water": return .water
    default:      return .earth
    }
}

// MARK: - Six Relations (六亲)

public enum SixRelation: String, CaseIterable, Sendable {
    case self_    = "兄弟"
    case parent   = "父母"
    case offspring = "子孙"
    case officer  = "官鬼"
    case wealth   = "妻财"

    public var chinese: String { rawValue }

    public var meaning: String {
        switch self {
        case .self_:     return "Siblings / Peers"
        case .parent:    return "Parents / Documents"
        case .offspring: return "Children / Creativity"
        case .officer:   return "Officer / Obstacles"
        case .wealth:    return "Wealth / Spouse"
        }
    }
}

/// Derive the Six Relation between subject element and target element.
public func relationFrom(subject: WuXing, target: WuXing) -> SixRelation {
    if subject == target               { return .self_ }
    if subject.generates(target)       { return .offspring }
    if target.generates(subject)       { return .parent }
    if subject.overcomes(target)       { return .officer }
    if target.overcomes(subject)       { return .wealth }
    return .self_
}

// MARK: - Six Spirits (六神)

public enum SixSpirit: String, CaseIterable, Sendable {
    case qinglong = "青龙"
    case zhuque   = "朱雀"
    case gouchen  = "勾陈"
    case tengshe  = "螣蛇"
    case baihu    = "白虎"
    case xuanwu   = "玄武"

    public var chinese: String { rawValue }

    public var animal: String {
        switch self {
        case .qinglong: return "Azure Dragon"
        case .zhuque:   return "Vermillion Bird"
        case .gouchen:  return "Yellow Dragon"
        case .tengshe:  return "Serpent"
        case .baihu:    return "White Tiger"
        case .xuanwu:   return "Black Tortoise"
        }
    }
}

// MARK: - Yao Line (爻)

public struct YaoLine: Sendable {
    public let position: Int           // 0–5 (bottom → top)
    public let isYang: Bool
    public let state: LineState
    public let branch: String          // 地支 (Earthly Branch)
    public let relation: SixRelation?
    public let isHidden: Bool          // 伏神

    public init(position: Int, isYang: Bool, state: LineState, branch: String, relation: SixRelation?, isHidden: Bool) {
        self.position = position
        self.isYang = isYang
        self.state = state
        self.branch = branch
        self.relation = relation
        self.isHidden = isHidden
    }
}

// MARK: - Hexagram (卦)

public struct Hexagram: Sendable {
    public let number: Int             // King Wen order 1–64
    public let chineseName: String
    public let pinyin: String
    public let englishName: String
    public let judgment: String        // 卦辞
    public let image: String           // 象辞
    public let binary: [Bool]          // 6 lines bottom→top (true = yang)
    public let upperTrigram: Trigram
    public let lowerTrigram: Trigram
    public let element: String         // "Wood", "Fire", etc.
    public let lineTexts: [String]     // 爻辞 × 6
    public let lineJudgments: [String] // 爻占断 × 6

    public init(number: Int, chineseName: String, pinyin: String, englishName: String, judgment: String, image: String, binary: [Bool], upperTrigram: Trigram, lowerTrigram: Trigram, element: String, lineTexts: [String], lineJudgments: [String]) {
        self.number = number
        self.chineseName = chineseName
        self.pinyin = pinyin
        self.englishName = englishName
        self.judgment = judgment
        self.image = image
        self.binary = binary
        self.upperTrigram = upperTrigram
        self.lowerTrigram = lowerTrigram
        self.element = element
        self.lineTexts = lineTexts
        self.lineJudgments = lineJudgments
    }

    /// Trigram code: upper 3 bits + lower 3 bits.
    public var trigramCode: Int {
        (upperTrigram.binaryCode << 3) | lowerTrigram.binaryCode
    }
}

// MARK: - Earthly Branches (地支)

public let earthlyBranches: [String] = [
    "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
]

public let branchToWuXing: [String: WuXing] = [
    "子": .water, "丑": .earth,
    "寅": .wood,  "卯": .wood,
    "辰": .earth, "巳": .fire,
    "午": .fire,  "未": .earth,
    "申": .metal, "酉": .metal,
    "戌": .earth, "亥": .water
]

// MARK: - NaJia (纳甲) Trigram → Earthly Branches

/// Traditional NaJia mapping: each trigram gets 6 branches.
/// Lower trigram uses indices 0-2, upper uses indices 3-5.
public let trigramBranches: [Trigram: [String]] = [
    .qian: ["子", "寅", "辰", "午", "申", "戌"],  // 乾纳甲壬
    .kun:  ["未", "巳", "卯", "丑", "亥", "酉"],  // 坤纳乙癸
    .zhen: ["子", "寅", "辰", "午", "申", "戌"],  // 震纳庚
    .xun:  ["丑", "亥", "酉", "未", "巳", "卯"],  // 巽纳辛
    .kan:  ["寅", "辰", "午", "申", "戌", "子"],  // 坎纳戊
    .li:   ["卯", "丑", "亥", "酉", "未", "巳"],  // 离纳己
    .gen:  ["辰", "午", "申", "戌", "子", "寅"],  // 艮纳丙
    .dui:  ["巳", "卯", "丑", "亥", "酉", "未"],  // 兑纳丁
]

// MARK: - Hexagram Lookup Tables

/// All 64 hexagrams in King Wen order.
public let allHexagrams: [Hexagram] = [
    Hexagram(number: 1, chineseName: "乾", pinyin: "Qián", englishName: "The Creative",
        judgment: "元亨利贞。", image: "天行健，君子以自强不息。",
        binary: [true,true,true,true,true,true],
        upperTrigram: .qian, lowerTrigram: .qian, element: "Metal",
        lineTexts: ["潜龙勿用。","见龙在田，利见大人。","君子终日乾乾，夕惕若厉，无咎。","或跃在渊，无咎。","飞龙在天，利见大人。","亢龙有悔。"],
        lineJudgments: ["初九 潜龙勿用，阳在下也。","九二 见龙在田，德施普也。","九三 终日乾乾，反复道也。","九四 或跃在渊，进无咎也。","九五 飞龙在天，大人造也。","上九 亢龙有悔，盈不可久也。"]),
    Hexagram(number: 2, chineseName: "坤", pinyin: "Kūn", englishName: "The Receptive",
        judgment: "元亨，利牝马之贞。", image: "地势坤，君子以厚德载物。",
        binary: [false,false,false,false,false,false],
        upperTrigram: .kun, lowerTrigram: .kun, element: "Earth",
        lineTexts: ["履霜，坚冰至。","直方大，不习无不利。","含章可贞。或从王事，无成有终。","括囊，无咎无誉。","黄裳，元吉。","龙战于野，其血玄黄。"],
        lineJudgments: ["初六 履霜，阴始凝也。","六二 直方大，不习无不利，地道光也。","六三 含章可贞，以时发也。","六四 括囊无咎，慎不害也。","六五 黄裳元吉，文在中也。","上六 龙战于野，其道穷也。"]),
    Hexagram(number: 3, chineseName: "屯", pinyin: "Zhūn", englishName: "Difficulty at the Beginning",
        judgment: "元亨利贞。勿用有攸往，利建侯。", image: "云雷屯，君子以经纶。",
        binary: [true,false,false,false,true,false],
        upperTrigram: .kan, lowerTrigram: .zhen, element: "Water",
        lineTexts: ["磐桓，利居贞。利建侯。","屯如邅如，乘马班如。匪寇婚媾，女子贞不字，十年乃字。","即鹿无虞，惟入于林中。君子几不如舍，往吝。","乘马班如，求婚媾。往吉，无不利。","屯其膏，小贞吉，大贞凶。","乘马班如，泣血涟如。"],
        lineJudgments: ["初九 磐桓，志行正也。","六二 六二之难，乘刚也。","六三 即鹿无虞，以从禽也。","六四 求而往，明也。","九五 屯其膏，施未光也。","上六 泣血涟如，何可长也。"]),
    Hexagram(number: 4, chineseName: "蒙", pinyin: "Méng", englishName: "Youthful Folly",
        judgment: "亨。匪我求童蒙，童蒙求我。", image: "山下出泉，蒙。君子以果行育德。",
        binary: [false,true,false,false,false,true],
        upperTrigram: .gen, lowerTrigram: .kan, element: "Earth",
        lineTexts: ["发蒙，利用刑人，用说桎梏。以往吝。","包蒙吉。纳妇吉。子克家。","勿用取女。见金夫，不有躬，无攸利。","困蒙，吝。","童蒙，吉。","击蒙，不利为寇，利御寇。"],
        lineJudgments: ["初六 利用刑人，以正法也。","九二 子克家，刚柔接也。","六三 勿用取女，行不顺也。","六四 困蒙之吝，独远实也。","六五 童蒙之吉，顺以巽也。","上九 利用御寇，上下顺也。"]),
    Hexagram(number: 5, chineseName: "需", pinyin: "Xū", englishName: "Waiting",
        judgment: "有孚，光亨。贞吉。利涉大川。", image: "云上于天，需。君子以饮食宴乐。",
        binary: [true,true,true,false,true,false],
        upperTrigram: .kan, lowerTrigram: .qian, element: "Metal",
        lineTexts: ["需于郊，利用恒，无咎。","需于沙，小有言，终吉。","需于泥，致寇至。","需于血，出自穴。","需于酒食，贞吉。","入于穴，有不速之客三人来。敬之，终吉。"],
        lineJudgments: ["初九 需于郊，不犯难行也。","九二 需于沙，衍在中也。","九三 需于泥，灾在外也。","六四 需于血，顺以听也。","九五 酒食贞吉，以中正也。","上六 不速之客来，敬之终吉，虽不当位，未大失也。"]),
    Hexagram(number: 6, chineseName: "讼", pinyin: "Sòng", englishName: "Conflict",
        judgment: "有孚窒惕，中吉，终凶。利见大人，不利涉大川。", image: "天与水违行，讼。君子以作事谋始。",
        binary: [false,true,false,true,true,true],
        upperTrigram: .qian, lowerTrigram: .kan, element: "Metal",
        lineTexts: ["不永所事，小有言，终吉。","不克讼，归而逋，其邑人三百户无眚。","食旧德，贞厉，终吉。或从王事，无成。","不克讼，复即命，渝，安贞吉。","讼，元吉。","或锡之鞶带，终朝三褫之。"],
        lineJudgments: ["初六 不永所事，讼不可长也。","九二 不克讼，归逋窜也。","六三 食旧德，从上吉也。","九四 复即命，渝，安贞不失也。","九五 讼，元吉，以中正也。","上九 以讼受服，亦不足敬也。"]),
    Hexagram(number: 7, chineseName: "师", pinyin: "Shī", englishName: "The Army",
        judgment: "贞，丈人吉，无咎。", image: "地中有水，师。君子以容民畜众。",
        binary: [false,true,false,false,false,false],
        upperTrigram: .kun, lowerTrigram: .kan, element: "Earth",
        lineTexts: ["师出以律，否臧凶。","在师中吉，无咎，王三锡命。","师或舆尸，凶。","师左次，无咎。","田有禽，利执言，无咎。长子帅师，弟子舆尸，贞凶。","大君有命，开国承家，小人勿用。"],
        lineJudgments: ["初六 师出以律，失律凶也。","九二 在师中吉，承天宠也。","六三 师或舆尸，大无功也。","六四 左次无咎，未失常也。","六五 长子帅师，以中行也。","上六 大君有命，以正功也。"]),
    Hexagram(number: 8, chineseName: "比", pinyin: "Bǐ", englishName: "Holding Together",
        judgment: "吉。原筮元永贞，无咎。不宁方来，后夫凶。", image: "地上有水，比。先王以建万国，亲诸侯。",
        binary: [false,false,false,false,true,false],
        upperTrigram: .kan, lowerTrigram: .kun, element: "Earth",
        lineTexts: ["有孚比之，无咎。有孚盈缶，终来有他，吉。","比之自内，贞吉。","比之匪人。","外比之，贞吉。","显比，王用三驱，失前禽。邑人不诫，吉。","比之无首，凶。"],
        lineJudgments: ["初六 比之初六，有他吉也。","六二 比之自内，不自失也。","六三 比之匪人，不亦伤乎。","六四 外比于贤，以从上也。","九五 显比之吉，位正中也。","上六 比之无首，无所终也。"]),
    Hexagram(number: 9, chineseName: "小畜", pinyin: "Xiǎo Xù", englishName: "Taming Power of the Small",
        judgment: "亨。密云不雨，自我西郊。", image: "风行天上，小畜。君子以懿文德。",
        binary: [true,true,true,false,true,true],
        upperTrigram: .xun, lowerTrigram: .qian, element: "Wood",
        lineTexts: ["复自道，何其咎，吉。","牵复，吉。","舆说辐，夫妻反目。","有孚，血去惕出，无咎。","有孚挛如，富以其邻。","既雨既处，尚德载，妇贞厉。月几望，君子征凶。"],
        lineJudgments: ["初九 复自道，其义吉也。","九二 牵复在中，亦不自失也。","九三 夫妻反目，不能正室也。","六四 有孚惕出，上合志也。","九五 有孚挛如，不独富也。","上九 既雨既处，德积载也。"]),
    Hexagram(number: 10, chineseName: "履", pinyin: "Lǚ", englishName: "Treading",
        judgment: "履虎尾，不咥人，亨。", image: "上天下泽，履。君子以辩上下，定民志。",
        binary: [true,true,false,true,true,true],
        upperTrigram: .qian, lowerTrigram: .dui, element: "Metal",
        lineTexts: ["素履，往无咎。","履道坦坦，幽人贞吉。","眇能视，跛能履，履虎尾，咥人凶。武人为于大君。","履虎尾，愬愬终吉。","夬履，贞厉。","视履考祥，其旋元吉。"],
        lineJudgments: ["初九 素履之往，独行愿也。","九二 幽人贞吉，中不自乱也。","六三 眇能视，不足以有明也。","九四 愬愬终吉，志行也。","九五 夬履贞厉，位正当也。","上九 元吉在上，大有庆也。"]),

    // Hexagrams 11-64 follow the same pattern.
    // Abbreviated for readability — all 64 are present below.

    Hexagram(number: 11, chineseName: "泰", pinyin: "Tài", englishName: "Peace",
        judgment: "小往大来，吉亨。", image: "天地交，泰。后以财成天地之道，辅相天地之宜，以左右民。",
        binary: [true,true,true,false,false,false],
        upperTrigram: .kun, lowerTrigram: .qian, element: "Earth",
        lineTexts: ["拔茅茹以其汇，征吉。","包荒，用冯河，不遐遗，朋亡，得尚于中行。","无平不陂，无往不复。艰贞无咎，勿恤其孚，于食有福。","翩翩不富以其邻，不戒以孚。","帝乙归妹，以祉元吉。","城复于隍，勿用师。自邑告命，贞吝。"],
        lineJudgments: ["初九 拔茅征吉，志在外也。","九二 包荒得尚于中行，以光大也。","九三 无往不复，天地际也。","六四 翩翩不富，皆失实也。","六五 以祉元吉，中以行愿也。","上六 城复于隍，其命乱也。"]),
    Hexagram(number: 12, chineseName: "否", pinyin: "Pǐ", englishName: "Standstill",
        judgment: "否之匪人，不利君子贞。大往小来。", image: "天地不交，否。君子以俭德辟难，不可荣以禄。",
        binary: [false,false,false,true,true,true],
        upperTrigram: .qian, lowerTrigram: .kun, element: "Metal",
        lineTexts: ["拔茅茹以其汇，贞吉亨。","包承，小人吉，大人否亨。","包羞。","有命无咎，畴离祉。","休否，大人吉。其亡其亡，系于苞桑。","倾否，先否后喜。"],
        lineJudgments: ["初六 拔茅贞吉，志在君也。","六二 大人否亨，不乱群也。","六三 包羞，位不当也。","九四 有命无咎，志行也。","九五 大人之吉，位正当也。","上九 否终则倾，何可长也。"]),
    Hexagram(number: 13, chineseName: "同人", pinyin: "Tóng Rén", englishName: "Fellowship",
        judgment: "同人于野，亨。利涉大川。利君子贞。", image: "天与火，同人。君子以类族辨物。",
        binary: [true,false,true,true,true,true],
        upperTrigram: .qian, lowerTrigram: .li, element: "Metal",
        lineTexts: ["同人于门，无咎。","同人于宗，吝。","伏戎于莽，升其高陵，三岁不兴。","乘其墉，弗克攻，吉。","同人先号咷而后笑，大师克相遇。","同人于郊，无悔。"],
        lineJudgments: ["初九 出门同人，又谁咎也。","九二 同人于宗，吝道也。","九三 伏戎于莽，敌刚也。","九四 乘其墉，义弗克也。","九五 同人之先，以中直也。","上九 同人于郊，志未得也。"]),
    Hexagram(number: 14, chineseName: "大有", pinyin: "Dà Yǒu", englishName: "Great Possession",
        judgment: "元亨。", image: "火在天上，大有。君子以遏恶扬善，顺天休命。",
        binary: [true,true,true,true,false,true],
        upperTrigram: .li, lowerTrigram: .qian, element: "Fire",
        lineTexts: ["无交害，匪咎，艰则无咎。","大车以载，有攸往，无咎。","公用亨于天子，小人弗克。","匪其彭，无咎。","厥孚交如威如，吉。","自天祐之，吉无不利。"],
        lineJudgments: ["初九 无交害，艰则无咎，不犯灾也。","九二 大车以载，积中不败也。","九三 公用亨于天子，小人害也。","九四 匪其彭，无咎，明辩晢也。","六五 厥孚交如，威如之吉，易而无备也。","上九 自天祐之，吉无不利，大有庆也。"]),
    Hexagram(number: 15, chineseName: "谦", pinyin: "Qiān", englishName: "Modesty",
        judgment: "亨，君子有终。", image: "地中有山，谦。君子以裒多益寡，称物平施。",
        binary: [false,false,true,false,false,false],
        upperTrigram: .kun, lowerTrigram: .gen, element: "Earth",
        lineTexts: ["谦谦君子，用涉大川，吉。","鸣谦，贞吉。","劳谦，君子有终，吉。","无不利，撝谦。","不富以其邻，利用侵伐，无不利。","鸣谦，利用行师征邑国。"],
        lineJudgments: ["初六 谦谦君子，卑以自牧也。","六二 鸣谦贞吉，中心得也。","九三 劳谦君子，万民服也。","六四 无不利撝谦，不违则也。","六五 利用侵伐，征不服也。","上六 鸣谦，志未得也。"]),
    Hexagram(number: 16, chineseName: "豫", pinyin: "Yù", englishName: "Enthusiasm",
        judgment: "利建侯行师。", image: "雷出地奋，豫。先王以作乐崇德，殷荐之上帝，以配祖考。",
        binary: [false,false,false,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .kun, element: "Wood",
        lineTexts: ["鸣豫，凶。","介于石，不终日，贞吉。","盱豫悔，迟有悔。","由豫，大有得，勿疑。朋盍簪。","贞疾，恒不死。","冥豫成，有渝无咎。"],
        lineJudgments: ["初六 初六鸣豫，志穷凶也。","六二 不终日贞吉，以中正也。","六三 盱豫有悔，位不当也。","九四 由豫大有得，志大行也。","六五 六五贞疾，乘刚也。","上六 冥豫在上，何可长也。"]),
    Hexagram(number: 17, chineseName: "随", pinyin: "Suí", englishName: "Following",
        judgment: "元亨利贞，无咎。", image: "泽中有雷，随。君子以向晦入宴息。",
        binary: [true,false,false,true,true,false],
        upperTrigram: .dui, lowerTrigram: .zhen, element: "Metal",
        lineTexts: ["官有渝，贞吉。出门交有功。","系小子，失丈夫。","系丈夫，失小子。随有求得，利居贞。","随有获，贞凶。有孚在道，以明，何咎。","孚于嘉，吉。","拘系之，乃从维之。王用亨于西山。"],
        lineJudgments: ["初九 官有渝，从正吉也。","六二 系小子，弗兼与也。","六三 系丈夫，志舍下也。","九四 随有获，其义凶也。","九五 孚于嘉，位正中也。","上六 拘系之上，穷也。"]),
    Hexagram(number: 18, chineseName: "蛊", pinyin: "Gǔ", englishName: "Work on What Has Been Spoiled",
        judgment: "元亨，利涉大川。先甲三日，后甲三日。", image: "山下有风，蛊。君子以振民育德。",
        binary: [false,true,true,false,false,true],
        upperTrigram: .gen, lowerTrigram: .xun, element: "Earth",
        lineTexts: ["干父之蛊，有子，考无咎，厉终吉。","干母之蛊，不可贞。","干父之蛊，小有悔，无大咎。","裕父之蛊，往见吝。","干父之蛊，用誉。","不事王侯，高尚其事。"],
        lineJudgments: ["初六 干父之蛊，意承考也。","九二 干母之蛊，得中道也。","九三 干父之蛊，终无咎也。","六四 裕父之蛊，往未得也。","六五 干父用誉，承以德也。","上九 不事王侯，志可则也。"]),
    Hexagram(number: 19, chineseName: "临", pinyin: "Lín", englishName: "Approach",
        judgment: "元亨利贞。至于八月有凶。", image: "泽上有地，临。君子以教思无穷，容保民无疆。",
        binary: [true,true,false,false,false,false],
        upperTrigram: .kun, lowerTrigram: .dui, element: "Earth",
        lineTexts: ["咸临，贞吉。","咸临，吉，无不利。","甘临，无攸利。既忧之，无咎。","至临，无咎。","知临，大君之宜，吉。","敦临，吉，无咎。"],
        lineJudgments: ["初九 咸临贞吉，志行正也。","九二 咸临吉无不利，未顺命也。","六三 甘临，位不当也。","六四 至临无咎，位当也。","六五 大君之宜，行中之谓也。","上六 敦临之吉，志在内也。"]),
    Hexagram(number: 20, chineseName: "观", pinyin: "Guān", englishName: "Contemplation",
        judgment: "盥而不荐，有孚颙若。", image: "风行地上，观。先王以省方观民设教。",
        binary: [false,false,false,false,true,true],
        upperTrigram: .xun, lowerTrigram: .kun, element: "Wood",
        lineTexts: ["童观，小人无咎，君子吝。","窥观，利女贞。","观我生，进退。","观国之光，利用宾于王。","观我生，君子无咎。","观其生，君子无咎。"],
        lineJudgments: ["初六 初六童观，小人道也。","六二 窥观女贞，亦可丑也。","六三 观我生进退，未失道也。","六四 观国之光，尚宾也。","九五 观我生，观民也。","上九 观其生，志未平也。"]),
    Hexagram(number: 21, chineseName: "噬嗑", pinyin: "Shì Kè", englishName: "Biting Through",
        judgment: "亨。利用狱。", image: "雷电噬嗑。先王以明罚敕法。",
        binary: [true,false,false,true,false,true],
        upperTrigram: .li, lowerTrigram: .zhen, element: "Wood",
        lineTexts: ["屦校灭趾，无咎。","噬肤灭鼻，无咎。","噬腊肉，遇毒。小吝，无咎。","噬干胏，得金矢。利艰贞，吉。","噬干肉得黄金，贞厉无咎。","何校灭耳，凶。"],
        lineJudgments: ["初九 屦校灭趾，不行也。","六二 噬肤灭鼻，乘刚也。","六三 遇毒，位不当也。","九四 利艰贞吉，未光也。","六五 贞厉无咎，得当也。","上九 何校灭耳，聪不明也。"]),
    Hexagram(number: 22, chineseName: "贲", pinyin: "Bì", englishName: "Grace",
        judgment: "亨。小利有攸往。", image: "山下有火，贲。君子以明庶政，无敢折狱。",
        binary: [true,false,true,false,false,true],
        upperTrigram: .gen, lowerTrigram: .li, element: "Earth",
        lineTexts: ["贲其趾，舍车而徒。","贲其须。","贲如濡如，永贞吉。","贲如皤如，白马翰如。匪寇婚媾。","贲于丘园，束帛戋戋，吝，终吉。","白贲，无咎。"],
        lineJudgments: ["初九 舍车而徒，义弗乘也。","六二 贲其须，与上兴也。","九三 永贞之吉，终莫之陵也。","六四 六四当位疑也，匪寇婚媾，终无尤也。","六五 六五之吉，有喜也。","上九 白贲无咎，上得志也。"]),
    Hexagram(number: 23, chineseName: "剥", pinyin: "Bō", englishName: "Splitting Apart",
        judgment: "不利有攸往。", image: "山附于地，剥。上以厚下安宅。",
        binary: [false,false,false,false,false,true],
        upperTrigram: .gen, lowerTrigram: .kun, element: "Earth",
        lineTexts: ["剥床以足，蔑贞凶。","剥床以辨，蔑贞凶。","剥之，无咎。","剥床以肤，凶。","贯鱼以宫人宠，无不利。","硕果不食，君子得舆，小人剥庐。"],
        lineJudgments: ["初六 剥床以足，以灭下也。","六二 剥床以辨，未有与也。","六三 剥之无咎，失上下也。","六四 剥床以肤，切近灾也。","六五 以宫人宠，终无尤也。","上九 君子得舆，民所载也。"]),
    Hexagram(number: 24, chineseName: "复", pinyin: "Fù", englishName: "Return",
        judgment: "亨。出入无疾。朋来无咎。反复其道，七日来复。利有攸往。", image: "雷在地中，复。先王以至日闭关，商旅不行，后不省方。",
        binary: [true,false,false,false,false,false],
        upperTrigram: .kun, lowerTrigram: .zhen, element: "Earth",
        lineTexts: ["不远复，无祗悔，元吉。","休复，吉。","频复，厉，无咎。","中行独复。","敦复，无悔。","迷复，凶，有灾眚。用行师，终有大败。以其国君凶，至于十年不克征。"],
        lineJudgments: ["初九 不远复，以修身也。","六二 休复之吉，以下仁也。","六三 频复之厉，义无咎也。","六四 中行独复，以从道也。","六五 敦复无悔，中以自考也。","上六 迷复之凶，反君道也。"]),
    Hexagram(number: 25, chineseName: "无妄", pinyin: "Wú Wàng", englishName: "Innocence",
        judgment: "元亨利贞。其匪正有眚，不利有攸往。", image: "天下雷行，物与无妄。先王以茂对时育万物。",
        binary: [true,false,false,true,true,true],
        upperTrigram: .qian, lowerTrigram: .zhen, element: "Metal",
        lineTexts: ["无妄，往吉。","不耕获，不菑畲，则利有攸往。","无妄之灾，或系之牛。行人之得，邑人之灾。","可贞，无咎。","无妄之疾，勿药有喜。","无妄，行有眚，无攸利。"],
        lineJudgments: ["初九 无妄之往，得志也。","六二 不耕获，未富也。","六三 行人得牛，邑人灾也。","九四 可贞无咎，固有之也。","九五 无妄之药，不可试也。","上九 无妄之行，穷之灾也。"]),
    Hexagram(number: 26, chineseName: "大畜", pinyin: "Dà Xù", englishName: "Taming Power of the Great",
        judgment: "利贞。不家食吉。利涉大川。", image: "天在山中，大畜。君子以多识前言往行，以畜其德。",
        binary: [false,false,true,true,true,true],
        upperTrigram: .qian, lowerTrigram: .gen, element: "Metal",
        lineTexts: ["有厉，利已。","舆说辐。","良马逐，利艰贞。曰闲舆卫，利有攸往。","童牛之牿，元吉。","豶豕之牙，吉。","何天之衢，亨。"],
        lineJudgments: ["初九 有厉利已，不犯灾也。","九二 舆说辐，中无尤也。","九三 利有攸往，合志也。","六四 元吉，有喜也。","六五 之吉，有庆也。","上九 何天之衢，道大行也。"]),
    Hexagram(number: 27, chineseName: "颐", pinyin: "Yí", englishName: "Nourishment",
        judgment: "贞吉。观颐，自求口实。", image: "山下有雷，颐。君子以慎言语，节饮食。",
        binary: [true,false,false,false,false,true],
        upperTrigram: .gen, lowerTrigram: .zhen, element: "Earth",
        lineTexts: ["舍尔灵龟，观我朵颐，凶。","颠颐，拂经于丘颐，征凶。","拂颐，贞凶。十年勿用，无攸利。","颠颐，吉。虎视眈眈，其欲逐逐，无咎。","拂经，居贞吉。不可涉大川。","由颐，厉吉。利涉大川。"],
        lineJudgments: ["初九 观我朵颐，亦不足贵也。","六二 六二征凶，行失类也。","六三 十年勿用，道大悖也。","六四 颠颐之吉，上施光也。","六五 居贞之吉，顺以从上也。","上九 由颐厉吉，大有庆也。"]),
    Hexagram(number: 28, chineseName: "大过", pinyin: "Dà Guò", englishName: "Preponderance of the Great",
        judgment: "栋桡。利有攸往，亨。", image: "泽灭木，大过。君子以独立不惧，遁世无闷。",
        binary: [false,true,true,true,true,false],
        upperTrigram: .dui, lowerTrigram: .xun, element: "Water",
        lineTexts: ["藉用白茅，无咎。","枯杨生稊，老夫得其女妻，无不利。","栋桡，凶。","栋隆，吉。有它吝。","枯杨生华，老妇得其士夫，无咎无誉。","过涉灭顶，凶，无咎。"],
        lineJudgments: ["初六 藉用白茅，柔在下也。","九二 老夫女妻，过以相与也。","九三 栋桡之凶，不可以有辅也。","九四 栋隆之吉，不桡乎下也。","九五 枯杨生华，何可久也。","上六 过涉之凶，不可咎也。"]),
    Hexagram(number: 29, chineseName: "坎", pinyin: "Kǎn", englishName: "The Abysmal Water",
        judgment: "习坎，有孚，维心亨，行有尚。", image: "水洊至，习坎。君子以常德行，习教事。",
        binary: [false,true,false,false,true,false],
        upperTrigram: .kan, lowerTrigram: .kan, element: "Water",
        lineTexts: ["习坎，入于坎窞，凶。","坎有险，求小得。","来之坎坎，险且枕。入于坎窞，勿用。","樽酒簋贰，用缶。纳约自牖，终无咎。","坎不盈，祗既平，无咎。","系用徽纆，寘于丛棘。三岁不得，凶。"],
        lineJudgments: ["初六 习坎入坎，失道凶也。","九二 求小得，未出中也。","六三 来之坎坎，终无功也。","六四 樽酒簋贰，刚柔际也。","九五 坎不盈，中未大也。","上六 上六失道，凶三岁也。"]),
    Hexagram(number: 30, chineseName: "离", pinyin: "Lí", englishName: "The Clinging Fire",
        judgment: "利贞，亨。畜牝牛，吉。", image: "明两作，离。大人以继明照于四方。",
        binary: [true,false,true,true,false,true],
        upperTrigram: .li, lowerTrigram: .li, element: "Fire",
        lineTexts: ["履错然，敬之无咎。","黄离，元吉。","日昃之离，不鼓缶而歌，则大耋之嗟，凶。","突如其来如，焚如，死如，弃如。","出涕沱若，戚嗟若，吉。","王用出征，有嘉折首。获匪其丑，无咎。"],
        lineJudgments: ["初九 履错之敬，以辟咎也。","六二 黄离元吉，得中道也。","九三 日昃之离，何可久也。","九四 突如其来如，无所容也。","六五 六五之吉，离王公也。","上九 王用出征，以正邦也。"]),
    Hexagram(number: 31, chineseName: "咸", pinyin: "Xián", englishName: "Influence",
        judgment: "亨，利贞。取女吉。", image: "山上有泽，咸。君子以虚受人。",
        binary: [false,false,true,true,true,false],
        upperTrigram: .dui, lowerTrigram: .gen, element: "Metal",
        lineTexts: ["咸其拇。","咸其腓，凶。居吉。","咸其股，执其随，往吝。","贞吉悔亡，憧憧往来，朋从尔思。","咸其脢，无悔。","咸其辅颊舌。"],
        lineJudgments: ["初六 咸其拇，志在外也。","六二 虽凶居吉，顺不害也。","九三 咸其股，亦不处也。","九四 贞吉悔亡，未感害也。","九五 咸其脢，志末也。","上六 咸其辅颊舌，滕口说也。"]),
    Hexagram(number: 32, chineseName: "恒", pinyin: "Héng", englishName: "Duration",
        judgment: "亨，无咎，利贞。利有攸往。", image: "雷风恒，君子以立不易方。",
        binary: [false,true,true,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .xun, element: "Wood",
        lineTexts: ["浚恒，贞凶，无攸利。","悔亡。","不恒其德，或承之羞，贞吝。","田无禽。","恒其德，贞，妇人吉，夫子凶。","振恒，凶。"],
        lineJudgments: ["初六 浚恒之凶，始求深也。","九二 九二悔亡，能久中也。","九三 不恒其德，无所容也。","九四 田无禽，久非其位也。","六五 妇人贞吉，从一而终也。","上六 振恒在上，大无功也。"]),
    Hexagram(number: 33, chineseName: "遁", pinyin: "Dùn", englishName: "Retreat",
        judgment: "亨。小利贞。", image: "天下有山，遁。君子以远小人，不恶而严。",
        binary: [true,true,true,false,false,true],
        upperTrigram: .gen, lowerTrigram: .qian, element: "Metal",
        lineTexts: ["遁尾，厉。勿用有攸往。","执之用黄牛之革，莫之胜说。","系遁，有疾厉。畜臣妾吉。","好遁，君子吉，小人否。","嘉遁，贞吉。","肥遁，无不利。"],
        lineJudgments: ["初六 遁尾之厉，不往何灾也。","九二 执用黄牛，固志也。","九三 系遁之厉，有疾惫也。","九四 君子好遁，小人否也。","九五 嘉遁贞吉，以正志也。","上九 肥遁无不利，无所疑也。"]),
    Hexagram(number: 34, chineseName: "大壮", pinyin: "Dà Zhuàng", englishName: "Power of the Great",
        judgment: "利贞。", image: "雷在天上，大壮。君子以非礼弗履。",
        binary: [true,true,true,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .qian, element: "Wood",
        lineTexts: ["壮于趾，征凶，有孚。","贞吉。","小人用壮，君子用罔，贞厉。羝羊触藩，羸其角。","贞吉悔亡，藩决不羸。壮于大舆之輹。","丧羊于易，无悔。","羝羊触藩，不能退，不能遂。无攸利，艰则吉。"],
        lineJudgments: ["初九 壮于趾，其孚穷也。","九二 九二贞吉，以中也。","九三 小人用壮，君子罔也。","九四 藩决不羸，尚往也。","六五 丧羊于易，位不当也。","上六 不能退不能遂，不祥也。"]),
    Hexagram(number: 35, chineseName: "晋", pinyin: "Jìn", englishName: "Progress",
        judgment: "康侯用锡马蕃庶，昼日三接。", image: "明出地上，晋。君子以自昭明德。",
        binary: [false,false,false,true,false,true],
        upperTrigram: .li, lowerTrigram: .kun, element: "Fire",
        lineTexts: ["晋如摧如，贞吉。罔孚裕无咎。","晋如愁如，贞吉。受兹介福于其王母。","众允，悔亡。","晋如鼫鼠，贞厉。","悔亡，失得勿恤。往吉，无不利。","晋其角，维用伐邑。厉吉无咎，贞吝。"],
        lineJudgments: ["初六 晋如摧如，独行正也。","六二 受兹介福，以中正也。","六三 众允之，志上行也。","九四 晋如鼫鼠，位不当也。","六五 失得勿恤，往有庆也。","上九 维用伐邑，道未光也。"]),
    Hexagram(number: 36, chineseName: "明夷", pinyin: "Míng Yí", englishName: "Darkening of the Light",
        judgment: "利艰贞。", image: "明入地中，明夷。君子以莅众，用晦而明。",
        binary: [true,false,true,false,false,false],
        upperTrigram: .kun, lowerTrigram: .li, element: "Earth",
        lineTexts: ["明夷于飞，垂其翼。君子于行，三日不食，有攸往，主人有言。","明夷，夷于左股。用拯马壮，吉。","明夷于南狩，得其大首。不可疾贞。","入于左腹，获明夷之心，出于门庭。","箕子之明夷，利贞。","不明，晦。初登于天，后入于地。"],
        lineJudgments: ["初九 君子于行，义不食也。","六二 六二之吉，顺以则也。","九三 南狩之志，乃大得也。","六四 入于左腹，获心意也。","六五 箕子之贞，明不可息也。","上六 初登于天，照四国也。后入于地，失则也。"]),
    Hexagram(number: 37, chineseName: "家人", pinyin: "Jiā Rén", englishName: "The Family",
        judgment: "利女贞。", image: "风自火出，家人。君子以言有物而行有恒。",
        binary: [true,false,true,false,true,true],
        upperTrigram: .xun, lowerTrigram: .li, element: "Wood",
        lineTexts: ["闲有家，悔亡。","无攸遂，在中馈，贞吉。","家人嗃嗃，悔厉吉。妇子嘻嘻，终吝。","富家，大吉。","王假有家，勿恤，吉。","有孚威如，终吉。"],
        lineJudgments: ["初九 闲有家，志未变也。","六二 六二之吉，顺以巽也。","九三 家人嗃嗃，未失也。","六四 富家大吉，顺在位也。","九五 王假有家，交相爱也。","上九 威如之吉，反身之谓也。"]),
    Hexagram(number: 38, chineseName: "睽", pinyin: "Kuí", englishName: "Opposition",
        judgment: "小事吉。", image: "上火下泽，睽。君子以同而异。",
        binary: [true,true,false,true,false,true],
        upperTrigram: .li, lowerTrigram: .dui, element: "Fire",
        lineTexts: ["悔亡，丧马勿逐，自复。见恶人，无咎。","遇主于巷，无咎。","见舆曳，其牛掣。其人天且劓，无初有终。","睽孤，遇元夫。交孚，厉无咎。","悔亡，厥宗噬肤，往何咎。","睽孤，见豕负涂，载鬼一车。先张之弧，后说之弧。匪寇婚媾，往遇雨则吉。"],
        lineJudgments: ["初九 见恶人，以辟咎也。","九二 遇主于巷，未失道也。","六三 见曳曳，位不当也。","九四 交孚无咎，志行也。","六五 厥宗噬肤，往有庆也。","上九 遇雨之吉，群疑亡也。"]),
    Hexagram(number: 39, chineseName: "蹇", pinyin: "Jiǎn", englishName: "Obstruction",
        judgment: "利西南，不利东北。利见大人，贞吉。", image: "山上有水，蹇。君子以反身修德。",
        binary: [false,false,true,false,true,false],
        upperTrigram: .kan, lowerTrigram: .gen, element: "Earth",
        lineTexts: ["往蹇来誉。","王臣蹇蹇，匪躬之故。","往蹇来反。","往蹇来连。","大蹇朋来。","往蹇来硕，吉。利见大人。"],
        lineJudgments: ["初六 往蹇来誉，宜待也。","六二 王臣蹇蹇，终无尤也。","九三 往蹇来反，内喜之也。","六四 往蹇来连，当位实也。","九五 大蹇朋来，以中节也。","上六 往蹇来硕，志在内也。"]),
    Hexagram(number: 40, chineseName: "解", pinyin: "Xiè", englishName: "Deliverance",
        judgment: "利西南。无所往，其来复吉。有攸往，夙吉。", image: "雷雨作，解。君子以赦过宥罪。",
        binary: [false,true,false,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .kan, element: "Wood",
        lineTexts: ["无咎。","田获三狐，得黄矢，贞吉。","负且乘，致寇至，贞吝。","解而拇，朋至斯孚。","君子维有解，吉。有孚于小人。","公用射隼于高墉之上，获之，无不利。"],
        lineJudgments: ["初六 刚柔之际，义无咎也。","九二 九二贞吉，得中道也。","六三 负且乘，亦可丑也。","九四 解而拇，未当位也。","六五 君子有解，小人退也。","上六 公用射隼，以解悖也。"]),
    Hexagram(number: 41, chineseName: "损", pinyin: "Sǔn", englishName: "Decrease",
        judgment: "有孚，元吉，无咎。可贞，利有攸往。曷之用，二簋可用享。", image: "山下有泽，损。君子以惩忿窒欲。",
        binary: [true,true,false,false,false,true],
        upperTrigram: .gen, lowerTrigram: .dui, element: "Earth",
        lineTexts: ["已事遄往，无咎，酌损之。","利贞，征凶。弗损益之。","三人行则损一人，一人行则得其友。","损其疾，使遄有喜，无咎。","或益之十朋之龟，弗克违，元吉。","弗损益之，无咎，贞吉。利有攸往，得臣无家。"],
        lineJudgments: ["初九 已事遄往，尚合志也。","九二 利贞，中以为志也。","六三 三人行则损一人，三则疑也。","六四 损其疾，亦可喜也。","六五 六五元吉，自上祐也。","上九 弗损益之，大得志也。"]),
    Hexagram(number: 42, chineseName: "益", pinyin: "Yì", englishName: "Increase",
        judgment: "利有攸往，利涉大川。", image: "风雷益，君子以见善则迁，有过则改。",
        binary: [true,false,false,false,true,true],
        upperTrigram: .xun, lowerTrigram: .zhen, element: "Wood",
        lineTexts: ["利用为大作，元吉，无咎。","或益之十朋之龟，弗克违。永贞吉。王用享于帝，吉。","益之用凶事，无咎。有孚中行，告公用圭。","中行告公从，利用为依迁国。","有孚惠心，勿问元吉。有孚惠我德。","莫益之，或击之。立心勿恒，凶。"],
        lineJudgments: ["初九 元吉无咎，下不厚事也。","九二 或益之，自外来也。","六三 益用凶事，固有之也。","六四 告公从，以益志也。","九五 有孚惠心，勿问之矣。","上九 莫益之，偏辞也。"]),
    Hexagram(number: 43, chineseName: "夬", pinyin: "Guài", englishName: "Breakthrough",
        judgment: "扬于王庭，孚号有厉。告自邑。不利即戎。利有攸往。", image: "泽上于天，夬。君子以施禄及下，居德则忌。",
        binary: [true,true,true,true,true,false],
        upperTrigram: .dui, lowerTrigram: .qian, element: "Water",
        lineTexts: ["壮于前趾，往不胜为咎。","惕号，莫夜有戎，勿恤。","壮于頄，有凶。君子夬夬独行，遇雨若濡。有愠无咎。","臀无肤，其行次且。牵羊悔亡，闻言不信。","苋陆夬夬，中行无咎。","无号，终有凶。"],
        lineJudgments: ["初九 不胜而往，咎也。","九二 有戎勿恤，得中道也。","九三 君子夬夬，终无咎也。","九四 其行次且，位不当也。","九五 中行无咎，中未光也。","上六 无号之凶，终不可长也。"]),
    Hexagram(number: 44, chineseName: "姤", pinyin: "Gòu", englishName: "Coming to Meet",
        judgment: "女壮，勿用取女。", image: "天下有风，姤。后以施命诰四方。",
        binary: [false,true,true,true,true,true],
        upperTrigram: .qian, lowerTrigram: .xun, element: "Metal",
        lineTexts: ["系于金柅，贞吉。有攸往，见凶。羸豕孚蹢躅。","包有鱼，无咎，不利宾。","臀无肤，其行次且，厉，无大咎。","包无鱼，起凶。","以杞包瓜，含章，有陨自天。","姤其角，吝，无咎。"],
        lineJudgments: ["初六 系于金柅，柔道牵也。","九二 包有鱼，义不及宾也。","九三 其行次且，行未牵也。","九四 无鱼之凶，远民也。","九五 九五含章，中正也。","上九 姤其角，上穷吝也。"]),
    Hexagram(number: 45, chineseName: "萃", pinyin: "Cuì", englishName: "Gathering Together",
        judgment: "亨。王假有庙，利见大人，亨，利贞。用大牲吉，利有攸往。", image: "泽上于地，萃。君子以除戎器，戒不虞。",
        binary: [false,false,false,true,true,false],
        upperTrigram: .dui, lowerTrigram: .kun, element: "Water",
        lineTexts: ["有孚不终，乃乱乃萃，若号，一握为笑。勿恤，往无咎。","引吉，无咎。孚乃利用禴。","萃如嗟如，无攸利。往无咎，小吝。","大吉，无咎。","萃有位，无咎。匪孚，元永贞，悔亡。","赍咨涕洟，无咎。"],
        lineJudgments: ["初六 乃乱乃萃，其志乱也。","六二 引吉无咎，中未变也。","六三 往无咎，上巽也。","九四 大吉无咎，位不当也。","九五 萃有位，志未光也。","上六 赍咨涕洟，未安上也。"]),
    Hexagram(number: 46, chineseName: "升", pinyin: "Shēng", englishName: "Pushing Upward",
        judgment: "元亨。用见大人，勿恤。南征吉。", image: "地中生木，升。君子以顺德，积小以高大。",
        binary: [false,true,true,false,false,false],
        upperTrigram: .kun, lowerTrigram: .xun, element: "Earth",
        lineTexts: ["允升，大吉。","孚乃利用禴，无咎。","升虚邑。","王用亨于岐山，吉，无咎。","贞吉，升阶。","冥升，利于不息之贞。"],
        lineJudgments: ["初六 允升大吉，上合志也。","九二 九二之孚，有喜也。","九三 升虚邑，无所疑也。","六四 王用亨于岐山，顺事也。","六五 贞吉升阶，大得志也。","上六 冥升在上，消不富也。"]),
    Hexagram(number: 47, chineseName: "困", pinyin: "Kùn", englishName: "Oppression",
        judgment: "亨，贞，大人吉，无咎。有言不信。", image: "泽无水，困。君子以致命遂志。",
        binary: [false,true,false,true,true,false],
        upperTrigram: .dui, lowerTrigram: .kan, element: "Water",
        lineTexts: ["臀困于株木，入于幽谷，三岁不觌。","困于酒食，朱绂方来。利用享祀。征凶，无咎。","困于石，据于蒺藜。入其宫不见其妻，凶。","来徐徐，困于金车。吝，有终。","劓刖，困于赤绂。乃徐有说，利用祭祀。","困于葛藟，于臲卼。曰动悔，有悔，征吉。"],
        lineJudgments: ["初六 入于幽谷，幽不明也。","九二 困于酒食，中有庆也。","六三 据于蒺藜，乘刚也。","九四 来徐徐，志在下也。","九五 乃徐有说，以中直也。","上六 动悔有悔，吉行也。"]),
    Hexagram(number: 48, chineseName: "井", pinyin: "Jǐng", englishName: "The Well",
        judgment: "改邑不改井，无丧无得。往来井井，汔至亦未繘井，羸其瓶，凶。", image: "木上有水，井。君子以劳民劝相。",
        binary: [false,true,true,false,true,false],
        upperTrigram: .kan, lowerTrigram: .xun, element: "Water",
        lineTexts: ["井泥不食，旧井无禽。","井谷射鲋，瓮敝漏。","井渫不食，为我心恻。可用汲。王明，并受其福。","井甃，无咎。","井冽寒泉，食。","井收勿幕，有孚元吉。"],
        lineJudgments: ["初六 井泥不食，下也。旧井无禽，时舍也。","九二 井谷射鲋，无与也。","九三 井渫不食，行恻也。求王明，受福也。","六四 井甃无咎，修井也。","九五 寒泉之食，中正也。","上六 元吉在上，大成也。"]),
    Hexagram(number: 49, chineseName: "革", pinyin: "Gé", englishName: "Revolution",
        judgment: "己日乃孚。元亨，利贞。悔亡。", image: "泽中有火，革。君子以治历明时。",
        binary: [true,false,true,true,true,false],
        upperTrigram: .dui, lowerTrigram: .li, element: "Water",
        lineTexts: ["巩用黄牛之革。","己日乃革之，征吉，无咎。","征凶，贞厉。革言三就，有孚。","悔亡，有孚改命，吉。","大人虎变，未占有孚。","君子豹变，小人革面，征凶，居贞吉。"],
        lineJudgments: ["初九 巩用黄牛，不可以有为也。","六二 己日革之，行有嘉也。","九三 革言三就，又何之矣。","九四 改命之吉，信志也。","九五 大人虎变，其文炳也。","上六 君子豹变，其文蔚也。"]),
    Hexagram(number: 50, chineseName: "鼎", pinyin: "Dǐng", englishName: "The Caldron",
        judgment: "元吉，亨。", image: "木上有火，鼎。君子以正位凝命。",
        binary: [false,true,true,true,false,true],
        upperTrigram: .li, lowerTrigram: .xun, element: "Fire",
        lineTexts: ["鼎颠趾，利出否。得妾以其子，无咎。","鼎有实，我仇有疾，不我能即，吉。","鼎耳革，其行塞。雉膏不食，方雨亏悔，终吉。","鼎折足，覆公餗，其形渥，凶。","鼎黄耳金铉，利贞。","鼎玉铉，大吉，无不利。"],
        lineJudgments: ["初六 鼎颠趾，未悖也。","九二 鼎有实，慎所之也。","九三 鼎耳革，失其义也。","九四 覆公餗，信如何也。","六五 鼎黄耳，中以为实也。","上九 玉铉在上，刚柔节也。"]),
    Hexagram(number: 51, chineseName: "震", pinyin: "Zhèn", englishName: "Thunder",
        judgment: "亨。震来虩虩，笑言哑哑。震惊百里，不丧匕鬯。", image: "洊雷震。君子以恐惧修省。",
        binary: [true,false,false,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .zhen, element: "Wood",
        lineTexts: ["震来虩虩，后笑言哑哑，吉。","震来厉，亿丧贝。跻于九陵，勿逐，七日得。","震苏苏，震行无眚。","震遂泥。","震往来厉，亿无丧，有事。","震索索，视矍矍，征凶。震不于其躬，于其邻，无咎。婚媾有言。"],
        lineJudgments: ["初九 震来虩虩，恐致福也。","六二 震来厉，乘刚也。","六三 震苏苏，位不当也。","九四 震遂泥，未光也。","六五 震往来厉，危行也。","上六 震索索，中未得也。"]),
    Hexagram(number: 52, chineseName: "艮", pinyin: "Gèn", englishName: "Keeping Still",
        judgment: "艮其背，不获其身。行其庭不见其人。无咎。", image: "兼山艮，君子以思不出其位。",
        binary: [false,false,true,false,false,true],
        upperTrigram: .gen, lowerTrigram: .gen, element: "Earth",
        lineTexts: ["艮其趾，无咎，利永贞。","艮其腓，不拯其随，其心不快。","艮其限，列其夤，厉薰心。","艮其身，无咎。","艮其辅，言有序，悔亡。","敦艮，吉。"],
        lineJudgments: ["初六 艮其趾，未失正也。","六二 不拯其随，未退听也。","九三 艮其限，危薰心也。","六四 艮其身，止诸躬也。","六五 艮其辅，以中正也。","上九 敦艮之吉，以厚终也。"]),
    Hexagram(number: 53, chineseName: "渐", pinyin: "Jiàn", englishName: "Development",
        judgment: "女归吉，利贞。", image: "山上有木，渐。君子以居贤德善俗。",
        binary: [false,false,true,false,true,true],
        upperTrigram: .xun, lowerTrigram: .gen, element: "Wood",
        lineTexts: ["鸿渐于干，小子厉，有言，无咎。","鸿渐于磐，饮食衎衎，吉。","鸿渐于陆。夫征不复，妇孕不育，凶。利御寇。","鸿渐于木，或得其桷，无咎。","鸿渐于陵，妇三岁不孕，终莫之胜，吉。","鸿渐于陆，其羽可用为仪，吉。"],
        lineJudgments: ["初六 小子之厉，义无咎也。","九二 饮食衎衎，素饱也。","九三 夫征不复，离群丑也。","六四 或得其桷，顺以巽也。","九五 终莫之胜，吉，得所愿也。","上九 其羽可用为仪，吉，不可乱也。"]),
    Hexagram(number: 54, chineseName: "归妹", pinyin: "Guī Mèi", englishName: "The Marrying Maiden",
        judgment: "征凶，无攸利。", image: "泽上有雷，归妹。君子以永终知敝。",
        binary: [true,true,false,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .dui, element: "Water",
        lineTexts: ["归妹以娣，跛能履，征吉。","眇能视，利幽人之贞。","归妹以须，反归以娣。","归妹愆期，迟归有时。","帝乙归妹，其君之袂不如其娣之袂良。月几望，吉。","女承筐无实，士刲羊无血，无攸利。"],
        lineJudgments: ["初九 归妹以娣，以恒也。","九二 利幽人之贞，未变常也。","六三 归妹以须，未当也。","九四 归妹愆期，志有待也。","六五 帝乙归妹，不如其娣之袂良也。","上六 上六无实，承虚筐也。"]),
    Hexagram(number: 55, chineseName: "丰", pinyin: "Fēng", englishName: "Abundance",
        judgment: "亨，王假之，勿忧，宜日中。", image: "雷电皆至，丰。君子以折狱致刑。",
        binary: [true,false,true,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .li, element: "Wood",
        lineTexts: ["遇其配主，虽旬无咎，往有尚。","丰其蔀，日中见斗。往得疑疾，有孚发若，吉。","丰其沛，日中见沬。折其右肱，无咎。","丰其蔀，日中见斗。遇其夷主，吉。","来章，有庆誉，吉。","丰其屋，蔀其家。窥其户，阒其无人。三岁不觌，凶。"],
        lineJudgments: ["初九 虽旬无咎，过旬灾也。","六二 有孚发若，信以发志也。","九三 折其右肱，终不可用也。","九四 丰其蔀，位不当也。","六五 六五之吉，有庆也。","上六 丰其屋，天际翔也。"]),
    Hexagram(number: 56, chineseName: "旅", pinyin: "Lǚ", englishName: "The Wanderer",
        judgment: "小亨。旅贞吉。", image: "山上有火，旅。君子以明慎用刑而不留狱。",
        binary: [false,false,true,true,false,true],
        upperTrigram: .li, lowerTrigram: .gen, element: "Fire",
        lineTexts: ["旅琐琐，斯其所取灾。","旅即次，怀其资，得童仆贞。","旅焚其次，丧其童仆，贞厉。","旅于处，得其资斧，我心不快。","射雉一矢亡，终以誉命。","鸟焚其巢，旅人先笑后号咷。丧牛于易，凶。"],
        lineJudgments: ["初六 旅琐琐，斯灾取也。","九二 得童仆贞，终无尤也。","九三 旅焚其次，亦以伤矣。","九四 旅于处，未得位也。","六五 终以誉命，上逮也。","上六 丧牛于易，终莫之闻也。"]),
    Hexagram(number: 57, chineseName: "巽", pinyin: "Xùn", englishName: "The Gentle Wind",
        judgment: "小亨。利有攸往，利见大人。", image: "随风巽，君子以申命行事。",
        binary: [false,true,true,false,true,true],
        upperTrigram: .xun, lowerTrigram: .xun, element: "Wood",
        lineTexts: ["进退，利武人之贞。","巽在床下，用史巫纷若，吉，无咎。","频巽，吝。","悔亡，田获三品。","贞吉悔亡，无不利。无初有终。先庚三日，后庚三日，吉。","巽在床下，丧其资斧，贞凶。"],
        lineJudgments: ["初六 进退，志疑也。","九二 纷若之吉，得中也。","九三 频巽之吝，志穷也。","六四 田获三品，有功也。","九五 九五之吉，位正中也。","上九 巽在床下，上穷也。"]),
    Hexagram(number: 58, chineseName: "兑", pinyin: "Duì", englishName: "The Joyous",
        judgment: "亨，利贞。", image: "丽泽兑，君子以朋友讲习。",
        binary: [true,true,false,true,true,false],
        upperTrigram: .dui, lowerTrigram: .dui, element: "Water",
        lineTexts: ["和兑，吉。","孚兑，吉，悔亡。","来兑，凶。","商兑未宁，介疾有喜。","孚于剥，有厉。","引兑。"],
        lineJudgments: ["初九 和兑之吉，行未疑也。","九二 孚兑之吉，信志也。","六三 来兑之凶，位不当也。","九四 九四之喜，有庆也。","九五 孚于剥，位正当也。","上六 引兑，未光也。"]),
    Hexagram(number: 59, chineseName: "涣", pinyin: "Huàn", englishName: "Dispersion",
        judgment: "亨。王假有庙。利涉大川。利贞。", image: "风行水上，涣。先王以享于帝立庙。",
        binary: [false,true,false,false,true,true],
        upperTrigram: .xun, lowerTrigram: .kan, element: "Wood",
        lineTexts: ["用拯马壮，吉。","涣奔其机，悔亡。","涣其躬，无悔。","涣其群，元吉。涣有丘，匪夷所思。","涣汗其大号，涣王居，无咎。","涣其血去逖出，无咎。"],
        lineJudgments: ["初六 初六之吉，顺也。","九二 涣奔其机，得愿也。","六三 涣其躬，志在外也。","六四 元吉，光大也。","九五 王居无咎，正位也。","上九 涣其血，远害也。"]),
    Hexagram(number: 60, chineseName: "节", pinyin: "Jié", englishName: "Limitation",
        judgment: "亨。苦节不可贞。", image: "泽上有水，节。君子以制数度，议德行。",
        binary: [true,true,false,false,true,false],
        upperTrigram: .kan, lowerTrigram: .dui, element: "Water",
        lineTexts: ["不出户庭，无咎。","不出门庭，凶。","不节若，则嗟若，无咎。","安节，亨。","甘节，吉。往有尚。","苦节，贞凶，悔亡。"],
        lineJudgments: ["初九 不出户庭，知通塞也。","九二 不出门庭，失时极也。","六三 不节之嗟，又谁咎也。","六四 安节之亨，承上道也。","九五 甘节之吉，居位中也。","上六 苦节贞凶，其道穷也。"]),
    Hexagram(number: 61, chineseName: "中孚", pinyin: "Zhōng Fú", englishName: "Inner Truth",
        judgment: "豚鱼吉。利涉大川，利贞。", image: "泽上有风，中孚。君子以议狱缓死。",
        binary: [true,true,false,false,true,true],
        upperTrigram: .xun, lowerTrigram: .dui, element: "Wood",
        lineTexts: ["虞吉，有它不燕。","鹤鸣在阴，其子和之。我有好爵，吾与尔靡之。","得敌，或鼓或罢，或泣或歌。","月几望，马匹亡，无咎。","有孚挛如，无咎。","翰音登于天，贞凶。"],
        lineJudgments: ["初九 初九虞吉，志未变也。","九二 其子和之，中心愿也。","六三 或鼓或罢，位不当也。","六四 马匹亡，绝类上也。","九五 有孚挛如，位正当也。","上九 翰音登于天，何可长也。"]),
    Hexagram(number: 62, chineseName: "小过", pinyin: "Xiǎo Guò", englishName: "Preponderance of the Small",
        judgment: "亨，利贞。可小事，不可大事。飞鸟遗之音，不宜上宜下，大吉。", image: "山上有雷，小过。君子以行过乎恭，丧过乎哀，用过乎俭。",
        binary: [false,false,true,true,false,false],
        upperTrigram: .zhen, lowerTrigram: .gen, element: "Earth",
        lineTexts: ["飞鸟以凶。","过其祖，遇其妣。不及其君，遇其臣。无咎。","弗过防之，从或戕之，凶。","无咎，弗过遇之，往厉必戒，勿用永贞。","密云不雨，自我西郊。公弋取彼在穴。","弗遇过之，飞鸟离之，凶，是谓灾眚。"],
        lineJudgments: ["初六 飞鸟以凶，不可如何也。","六二 不及其君，臣不可过也。","九三 从或戕之，凶如何也。","九四 弗过遇之，位不当也。","六五 密云不雨，已上也。","上六 弗遇过之，已亢也。"]),
    Hexagram(number: 63, chineseName: "既济", pinyin: "Jì Jì", englishName: "After Completion",
        judgment: "亨小，利贞。初吉终乱。", image: "水在火上，既济。君子以思患而预防之。",
        binary: [true,false,true,false,true,false],
        upperTrigram: .kan, lowerTrigram: .li, element: "Water",
        lineTexts: ["曳其轮，濡其尾，无咎。","妇丧其茀，勿逐，七日得。","高宗伐鬼方，三年克之。小人勿用。","繻有衣袽，终日戒。","东邻杀牛，不如西邻之禴祭。实受其福。","濡其首，厉。"],
        lineJudgments: ["初九 曳其轮，义无咎也。","六二 七日得，以中道也。","九三 三年克之，惫也。","六四 终日戒，有所疑也。","九五 东邻不如西邻，时也。","上六 濡其首厉，何可久也。"]),
    Hexagram(number: 64, chineseName: "未济", pinyin: "Wèi Jì", englishName: "Before Completion",
        judgment: "亨。小狐汔济，濡其尾，无攸利。", image: "火在水上，未济。君子以慎辨物居方。",
        binary: [false,true,false,true,false,true],
        upperTrigram: .li, lowerTrigram: .kan, element: "Fire",
        lineTexts: ["濡其尾，吝。","曳其轮，贞吉。","未济，征凶。利涉大川。","贞吉，悔亡。震用伐鬼方，三年有赏于大国。","贞吉，无悔。君子之光，有孚，吉。","有孚于饮酒，无咎。濡其首，有孚失是。"],
        lineJudgments: ["初六 濡其尾，亦不知极也。","九二 九二贞吉，中以行正也。","六三 未济征凶，位不当也。","九四 贞吉悔亡，志行也。","六五 君子之光，其晖吉也。","上九 饮酒濡首，亦不知节也。"]),
]

// MARK: - Lookup helpers

private let hexagramsByNumber: [Int: Hexagram] = Dictionary(
    uniqueKeysWithValues: allHexagrams.map { ($0.number, $0) }
)

private let hexagramsByBinary: [[Bool]: Hexagram] = Dictionary(
    uniqueKeysWithValues: allHexagrams.map { ($0.binary, $0) }
)

private let hexagramsByTrigrams: [TrigramPair: Hexagram] = Dictionary(
    uniqueKeysWithValues: allHexagrams.map { (TrigramPair(upper: $0.upperTrigram, lower: $0.lowerTrigram), $0) }
)

public struct TrigramPair: Hashable {
    public let upper: Trigram
    public let lower: Trigram

    public init(upper: Trigram, lower: Trigram) {
        self.upper = upper
        self.lower = lower
    }
}

public func hexagramByNumber(_ n: Int) -> Hexagram {
    hexagramsByNumber[n] ?? allHexagrams[0]
}

public func hexagramByBinary(_ pattern: [Bool]) -> Hexagram? {
    hexagramsByBinary[pattern]
}

public func hexagramByTrigrams(upper: Trigram, lower: Trigram) -> Hexagram? {
    hexagramsByTrigrams[TrigramPair(upper: upper, lower: lower)]
}

/// Given 6 line states, produce primary and changed hexagrams.
public func interpretLines(_ states: [LineState]) -> (primary: Hexagram, changed: Hexagram) {
    let primaryPattern = states.map { $0.isYang }
    let changedPattern = states.map { state -> Bool in
        switch state {
        case .oldYang:  return false
        case .oldYin:   return true
        case .youngYang: return true
        case .youngYin:  return false
        }
    }
    let primary = hexagramByBinary(primaryPattern) ?? allHexagrams[0]
    let changed = hexagramByBinary(changedPattern) ?? allHexagrams[0]
    return (primary, changed)
}
