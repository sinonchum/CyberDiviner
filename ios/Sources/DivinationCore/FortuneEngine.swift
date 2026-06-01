import Foundation

// MARK: - Fortune Engine (四字批命)

/// Generates concise 4-character fortune summaries for all divination types.
enum FortuneEngine {

    // MARK: - Liuyao Fortune (六十四卦 → 四字)

    private static let hexagramFortunes: [String: String] = [
        "乾": "自强不息", "坤": "厚德载物", "屯": "蓄势待发",
        "蒙": "启蒙开智", "需": "静待时机", "讼": "以和为贵",
        "师": "德行服众", "比": "同心协力", "小畜": "厚积薄发",
        "履": "如履薄冰", "泰": "国泰民安", "否": "否极泰来",
        "同人": "志同道合", "大有": "鸿运当头", "谦": "谦逊有礼",
        "豫": "顺势而为", "随": "随机应变", "蛊": "拨乱反正",
        "临": "居高临下", "观": "静观其变", "噬嗑": "明断是非",
        "贲": "文质彬彬", "剥": "韬光养晦", "复": "一阳来复",
        "无妄": "无妄之灾", "大畜": "大有可为", "颐": "修身养性",
        "大过": "非常之时", "坎": "险中求胜", "离": "光明普照",
        "咸": "感应相通", "恒": "持之以恒", "遁": "急流勇退",
        "大壮": "刚健有为", "晋": "步步高升", "明夷": "韬光养晦",
        "家人": "家和万事", "睽": "求同存异", "蹇": "砥砺前行",
        "解": "化险为夷", "损": "有舍有得", "益": "锦上添花",
        "夬": "当机立断", "姤": "不期而遇", "萃": "聚沙成塔",
        "升": "步步高升", "困": "困中求通", "井": "润泽万物",
        "革": "破旧立新", "鼎": "革故鼎新", "震": "雷厉风行",
        "艮": "止于至善", "渐": "循序渐进", "归妹": "因缘际会",
        "丰": "盛极一时", "旅": "行旅通达", "巽": "顺势而为",
        "兑": "和悦相处", "涣": "涣然冰释", "节": "节制有度",
        "中孚": "诚信为本", "小过": "谨小慎微", "既济": "功成身退",
        "未济": "柳暗花明",
    ]

    /// Generate 4-char fortune from a Hexagram.
    static func liuyaoFortune(hexagram: Hexagram) -> String {
        if let fortune = hexagramFortunes[hexagram.chineseName] {
            return fortune
        }
        // Partial match
        for (key, value) in hexagramFortunes {
            if hexagram.chineseName.contains(key) { return value }
        }
        return "天机莫测"
    }

    /// One-line meaning for a liuyao fortune title.
    static func liuyaoMeaning(_ title: String) -> String {
        let meanings: [String: String] = [
            "自强不息": "天行健，君子以自强不息，运势昂扬向上",
            "厚德载物": "地势坤，以厚德承载万物，宜稳扎稳打",
            "蓄势待发": "万物初生，困难重重，蓄力之后方能突破",
            "启蒙开智": "蒙以养正，学习进取之时，宜虚心求教",
            "静待时机": "云上于天，需待时机成熟，不可操之过急",
            "以和为贵": "讼则终凶，宜化解纷争，以和为贵",
            "德行服众": "师出以律，以德服人方能聚人心",
            "同心协力": "比之自内，团结协作，众人拾柴火焰高",
            "厚积薄发": "密云不雨，积小成大，时机未到尚需等待",
            "如履薄冰": "履虎尾，小心谨慎行事，方可无咎",
            "国泰民安": "天地交泰，万事亨通，大吉大利之象",
            "否极泰来": "天地不交，困顿之中暗藏转机",
            "志同道合": "同人于野，同心同德，事业可成",
            "鸿运当头": "火在天上，大有收获，运势极旺",
            "谦逊有礼": "谦谦君子，卑以自牧，越谦虚越有福",
            "顺势而为": "雷出地奋，顺时而动，把握良机",
            "随机应变": "泽中有雷，随时而动，灵活应对方为上策",
            "拨乱反正": "山下有风，振弊起衰，正是扭转局面之时",
            "居高临下": "泽上有地，居上临下，宜以仁德感化",
            "静观其变": "风行地上，俯察万物，静观其变再行动",
            "明断是非": "雷电皆至，明察秋毫，公正决断",
            "文质彬彬": "山下有火，文饰光明，内外兼修为佳",
            "韬光养晦": "山附于地，暂时蛰伏，静待东山再起",
            "一阳来复": "雷在地中，一阳来复，万物复苏之时",
            "无妄之灾": "天下雷行，意外之变，守正方可避祸",
            "大有可为": "天在山中，积蓄深厚，大有可为",
            "修身养性": "山下有雷，颐养身心，饮食言语皆需谨慎",
            "非常之时": "泽灭木，非常之时需非常之策",
            "险中求胜": "水洊至，重重险阻，以诚信方可渡过",
            "光明普照": "明两作离，光明相继，事业前景光明",
            "感应相通": "山泽通气，阴阳感应，感情事业皆顺",
            "持之以恒": "雷风相与，恒久不变，坚持必有回报",
            "急流勇退": "天下有山，见好就收，退守为上",
            "刚健有为": "雷在天上，气势如虹，大展宏图之时",
            "步步高升": "明出地上，步步高升，前景光明",
            "家和万事": "风自火出，家和万事兴，齐家治业",
            "求同存异": "火动而上，求同存异，化解矛盾",
            "砥砺前行": "山上有水，前路虽艰，坚持可过",
            "化险为夷": "雷雨作，百果草木皆甲坼，险难已解",
            "有舍有得": "山下有泽，损上益下，有舍方有得",
            "锦上添花": "风雷相与，益上益下，好运连连",
            "当机立断": "泽上于天，刚决柔也，果断行事",
            "不期而遇": "天下有风，不期而遇，把握意外机缘",
            "聚沙成塔": "泽上于地，聚沙成塔，集众力成大事",
            "困中求通": "泽无水，困中求通，坚守信念",
            "润泽万物": "木上有水，井养而不穷，泽被苍生",
            "破旧立新": "泽中有火，破旧立新，变革之时",
            "革故鼎新": "木上有火，革故鼎新，重铸辉煌",
            "雷厉风行": "洊雷震，雷厉风行，奋发有为",
            "止于至善": "兼山艮，止于至善，知止而后有定",
            "循序渐进": "山上有木，循序渐进，稳健发展",
            "因缘际会": "泽上有雷，因缘际会，顺势而为",
            "盛极一时": "雷电皆至，盛极一时，宜居安思危",
            "行旅通达": "山上有火，行旅通达，利于出行",
            "和悦相处": "丽泽兑，和悦相处，人缘极佳",
            "涣然冰释": "风行水上，涣然冰释，困局已解",
            "节制有度": "泽上有水，节制有度，适可而止",
            "诚信为本": "泽上有风，诚信为本，以信立身",
            "谨小慎微": "山上有雷，小过宜谦，谨言慎行",
            "功成身退": "水在火上，功成身退，守成不易",
            "柳暗花明": "火在水上，未济之象，柳暗花明又一村",
            "天机莫测": "天道幽远，卦象玄妙，宜静心体悟",
        ]
        return meanings[title] ?? "卦象已起，静心体悟天机"
    }

    // MARK: - Tarot Fortune

    private static let tarotThemeMap: [String: (upright: String, reversed: String)] = [
        "愚者":    (upright: "无畏启程", reversed: "迷途知返"),
        "魔术师":  (upright: "心想事成", reversed: "重整心火"),
        "女祭司":  (upright: "静待花开", reversed: "返观内心"),
        "女皇":    (upright: "万物生长", reversed: "丰盛受阻"),
        "皇帝":    (upright: "掌控全局", reversed: "松弛有度"),
        "教皇":    (upright: "正道指引", reversed: "另辟新径"),
        "恋人":    (upright: "天作之合", reversed: "重新择心"),
        "战车":    (upright: "势如破竹", reversed: "校准方向"),
        "力量":    (upright: "以柔克刚", reversed: "蓄养心力"),
        "隐者":    (upright: "明心见性", reversed: "出关见世"),
        "命运之轮": (upright: "否极泰来", reversed: "静候轮转"),
        "正义":    (upright: "公正无私", reversed: "重归中衡"),
        "倒吊人":  (upright: "柳暗花明", reversed: "换位重启"),
        "死神":    (upright: "涅槃重生", reversed: "旧念待解"),
        "节制":    (upright: "中正平和", reversed: "调息归中"),
        "恶魔":    (upright: "照见执念", reversed: "挣脱枷锁"),
        "塔":      (upright: "破旧立新", reversed: "转危为醒"),
        "星星":    (upright: "曙光初现", reversed: "微光仍在"),
        "月亮":    (upright: "雾中寻真", reversed: "拨云见日"),
        "太阳":    (upright: "光明普照", reversed: "短暂阴霾"),
        "审判":    (upright: "浴火重生", reversed: "回声待答"),
        "世界":    (upright: "功德圆满", reversed: "圆满未竟"),
    ]

    private static let tarotSuitFortunes: [String: (upright: String, reversed: String)] = [
        "权杖": (upright: "行动果决", reversed: "火候待稳"),
        "圣杯": (upright: "心灵丰盈", reversed: "心潮待平"),
        "宝剑": (upright: "洞察真相", reversed: "思路待清"),
        "星币": (upright: "稳扎稳打", reversed: "根基待固"),
    ]

    /// Generate 4-char fortune from tarot draw results.
    static func tarotFortune(cards: [TarotDrawResult]) -> String {
        guard let first = cards.first else { return "天机莫测" }
        let isReversed = first.isReversed
        let name = first.card.nameCN

        // Exact match
        if let theme = tarotThemeMap[name] {
            return isReversed ? theme.reversed : theme.upright
        }

        // Partial match for minor arcana
        for (key, value) in tarotThemeMap {
            if name.hasPrefix(key) {
                return isReversed ? value.reversed : value.upright
            }
        }

        // Suit-based match
        for (key, value) in tarotSuitFortunes {
            if name.hasPrefix(key) {
                return isReversed ? value.reversed : value.upright
            }
        }

        return isReversed ? "转念待明" : "顺势而为"
    }

    // MARK: - Oracle Fortune

    private static let topicKeywords: [(keywords: [String], fortune: String)] = [
        (["健康", "身体", "养生", "疾病", "医疗", "康复"], "身心康泰"),
        (["感情", "爱情", "恋爱", "婚姻", "桃花", "姻缘"], "情缘天定"),
        (["事业", "工作", "职业", "升职", "创业", "前程"], "鹏程万里"),
        (["财运", "金钱", "投资", "财富", "理财"], "财源广进"),
        (["学业", "考试", "学习", "智慧"], "金榜题名"),
        (["家庭", "亲人", "父母", "子女"], "家宅安宁"),
        (["贵人", "人缘", "人际"], "贵人相助"),
        (["出行", "旅行", "迁移"], "逢凶化吉"),
    ]

    /// Generate 4-char fortune from oracle text.
    static func oracleFortune(from text: String) -> String {
        guard !text.isEmpty else { return "天机莫测" }

        let scored = topicKeywords.map { topic -> (String, Int) in
            let count = topic.keywords.filter { text.contains($0) }.count
            return (topic.fortune, count)
        }.filter { $0.1 > 0 }.sorted { $0.1 > $1.1 }

        guard let topScore = scored.first?.1, topScore > 0 else {
            return "天机莫测"
        }

        let topGroup = scored.filter { $0.1 == topScore }
        let hash = abs(text.hashValue) % max(topGroup.count, 1)
        return topGroup[hash].0
    }
}
