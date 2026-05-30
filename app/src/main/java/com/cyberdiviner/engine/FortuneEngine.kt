package com.cyberdiviner.engine

/**
 * FortuneEngine — shared 4-char fortune summary (四字批命) logic.
 *
 * Used by all three divination types (Liuyao, Tarot, Vision) and the archive.
 */
object FortuneEngine {

    // ═══════════════════════════════════════════════════════════════════
    // LIUYAO Fortune Mapping
    // ═══════════════════════════════════════════════════════════════════

    /** Generate 4-char fortune summary from hexagram name (64 hexagrams) */
    fun liuyaoFortune(hexName: String): String {
        val map = mapOf(
            // ── Upper Canon (1-30) ──
            "乾" to "自强不息", "坤" to "厚德载物", "屯" to "蓄势待发",
            "蒙" to "启蒙开智", "需" to "静待时机", "讼" to "以和为贵",
            "师" to "德行服众", "比" to "同心协力", "小畜" to "厚积薄发",
            "履" to "如履薄冰", "泰" to "国泰民安", "否" to "否极泰来",
            "同人" to "志同道合", "大有" to "鸿运当头", "谦" to "谦逊有礼",
            "豫" to "顺势而为", "随" to "随机应变", "蛊" to "拨乱反正",
            "临" to "居高临下", "观" to "静观其变", "噬嗑" to "明断是非",
            "贲" to "文质彬彬", "剥" to "韬光养晦", "复" to "一阳来复",
            "无妄" to "无妄之灾", "大畜" to "大有可为", "颐" to "修身养性",
            "大过" to "非常之时", "坎" to "险中求胜", "离" to "光明普照",
            // ── Lower Canon (31-64) ──
            "咸" to "感应相通", "恒" to "持之以恒", "遁" to "急流勇退",
            "大壮" to "刚健有为", "晋" to "步步高升", "明夷" to "韬光养晦",
            "家人" to "家和万事", "睽" to "求同存异", "蹇" to "砥砺前行",
            "解" to "化险为夷", "损" to "有舍有得", "益" to "锦上添花",
            "夬" to "当机立断", "姤" to "不期而遇", "萃" to "聚沙成塔",
            "升" to "步步高升", "困" to "困中求通", "井" to "润泽万物",
            "革" to "破旧立新", "鼎" to "革故鼎新", "震" to "雷厉风行",
            "艮" to "止于至善", "渐" to "循序渐进", "归妹" to "因缘际会",
            "丰" to "盛极一时", "旅" to "行旅通达", "巽" to "顺势而为",
            "兑" to "和悦相处", "涣" to "涣然冰释", "节" to "节制有度",
            "中孚" to "诚信为本", "小过" to "谨小慎微", "既济" to "功成身退",
            "未济" to "柳暗花明"
        )
        // Try exact match first
        map[hexName]?.let { return it }
        // Try partial match (some engines return "X卦" or full name)
        for ((key, value) in map) {
            if (hexName.contains(key)) return value
        }
        return "天机莫测"
    }

    /** One-line meaning for a liuyao 4-char fortune summary */
    fun liuyaoMeaning(title: String): String {
        val meanings = mapOf(
            "自强不息" to "天行健，君子以自强不息，运势昂扬向上",
            "厚德载物" to "地势坤，以厚德承载万物，宜稳扎稳打",
            "蓄势待发" to "万物初生，困难重重，蓄力之后方能突破",
            "启蒙开智" to "蒙以养正，学习进取之时，宜虚心求教",
            "静待时机" to "云上于天，需待时机成熟，不可操之过急",
            "以和为贵" to "讼则终凶，宜化解纷争，以和为贵",
            "德行服众" to "师出以律，以德服人方能聚人心",
            "同心协力" to "比之自内，团结协作，众人拾柴火焰高",
            "厚积薄发" to "密云不雨，积小成大，时机未到尚需等待",
            "如履薄冰" to "履虎尾，小心谨慎行事，方可无咎",
            "国泰民安" to "天地交泰，万事亨通，大吉大利之象",
            "否极泰来" to "天地不交，困顿之中暗藏转机",
            "志同道合" to "同人于野，同心同德，事业可成",
            "鸿运当头" to "火在天上，大有收获，运势极旺",
            "谦逊有礼" to "谦谦君子，卑以自牧，越谦虚越有福",
            "顺势而为" to "雷出地奋，顺时而动，把握良机",
            "随机应变" to "泽中有雷，随时而动，灵活应对方为上策",
            "拨乱反正" to "山下有风，振弊起衰，正是扭转局面之时",
            "居高临下" to "泽上有地，居上临下，宜以仁德感化",
            "静观其变" to "风行地上，俯察万物，静观其变再行动",
            "明断是非" to "雷电皆至，明察秋毫，公正决断",
            "文质彬彬" to "山下有火，文饰光明，内外兼修为佳",
            "韬光养晦" to "山附于地，暂时蛰伏，静待东山再起",
            "一阳来复" to "雷在地中，一阳来复，万物复苏之时",
            "无妄之灾" to "天下雷行，意外之变，守正方可避祸",
            "大有可为" to "天在山中，积蓄深厚，大有可为",
            "修身养性" to "山下有雷，颐养身心，饮食言语皆需谨慎",
            "非常之时" to "泽灭木，非常之时需非常之策",
            "险中求胜" to "水洊至，重重险阻，以诚信方可渡过",
            "光明普照" to "明两作离，光明相继，事业前景光明",
            "感应相通" to "山泽通气，阴阳感应，感情事业皆顺",
            "持之以恒" to "雷风相与，恒久不变，坚持必有回报",
            "急流勇退" to "天下有山，见好就收，退守为上",
            "刚健有为" to "雷在天上，气势如虹，大展宏图之时",
            "步步高升" to "明出地上，步步高升，前景光明",
            "家和万事" to "风自火出，家和万事兴，齐家治业",
            "求同存异" to "火动而上，求同存异，化解矛盾",
            "砥砺前行" to "山上有水，前路虽艰，坚持可过",
            "化险为夷" to "雷雨作，百果草木皆甲坼，险难已解",
            "有舍有得" to "山下有泽，损上益下，有舍方有得",
            "锦上添花" to "风雷相与，益上益下，好运连连",
            "当机立断" to "泽上于天，刚决柔也，果断行事",
            "不期而遇" to "天下有风，不期而遇，把握意外机缘",
            "聚沙成塔" to "泽上于地，聚沙成塔，集众力成大事",
            "困中求通" to "泽无水，困中求通，坚守信念",
            "润泽万物" to "木上有水，井养而不穷，泽被苍生",
            "破旧立新" to "泽中有火，破旧立新，变革之时",
            "革故鼎新" to "木上有火，革故鼎新，重铸辉煌",
            "雷厉风行" to "洊雷震，雷厉风行，奋发有为",
            "止于至善" to "兼山艮，止于至善，知止而后有定",
            "循序渐进" to "山上有木，循序渐进，稳健发展",
            "因缘际会" to "泽上有雷，因缘际会，顺势而为",
            "盛极一时" to "雷电皆至，盛极一时，宜居安思危",
            "行旅通达" to "山上有火，行旅通达，利于出行",
            "和悦相处" to "丽泽兑，和悦相处，人缘极佳",
            "涣然冰释" to "风行水上，涣然冰释，困局已解",
            "节制有度" to "泽上有水，节制有度，适可而止",
            "诚信为本" to "泽上有风，诚信为本，以信立身",
            "谨小慎微" to "山上有雷，小过宜谦，谨言慎行",
            "功成身退" to "水在火上，功成身退，守成不易",
            "柳暗花明" to "火在水上，未济之象，柳暗花明又一村",
            "天机莫测" to "天道幽远，卦象玄妙，宜静心体悟"
        )
        return meanings[title] ?: "卦象已起，静心体悟天机"
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAROT Fortune Mapping
    // ═══════════════════════════════════════════════════════════════════

    /** Generate 4-char thematic summary from tarot card, context-aware */
    fun tarotFortune(cardName: String, isReversed: Boolean, question: String = "", interpretation: String = ""): String {
        // If user asked about a specific topic, prioritize topic-based fortune
        val topicFortune = matchTopicFortune(question, interpretation)
        if (topicFortune != null) return topicFortune

        // Otherwise fall back to card-based fortune
        val themeMap = mapOf(
            // Major Arcana
            "愚者" to if (isReversed) "迷途知返" else "无畏启程",
            "魔术师" to if (isReversed) "重整心火" else "心想事成",
            "女祭司" to if (isReversed) "返观内心" else "静待花开",
            "女皇" to if (isReversed) "丰盛受阻" else "万物生长",
            "皇帝" to if (isReversed) "松弛有度" else "掌控全局",
            "教皇" to if (isReversed) "另辟新径" else "正道指引",
            "恋人" to if (isReversed) "重新择心" else "天作之合",
            "战车" to if (isReversed) "校准方向" else "势如破竹",
            "力量" to if (isReversed) "蓄养心力" else "以柔克刚",
            "隐者" to if (isReversed) "出关见世" else "明心见性",
            "命运之轮" to if (isReversed) "静候轮转" else "否极泰来",
            "正义" to if (isReversed) "重归中衡" else "公正无私",
            "倒吊人" to if (isReversed) "换位重启" else "柳暗花明",
            "死神" to if (isReversed) "旧念待解" else "涅槃重生",
            "节制" to if (isReversed) "调息归中" else "中正平和",
            "恶魔" to if (isReversed) "挣脱枷锁" else "照见执念",
            "塔" to if (isReversed) "转危为醒" else "破旧立新",
            "星星" to if (isReversed) "微光仍在" else "曙光初现",
            "月亮" to if (isReversed) "拨云见日" else "雾中寻真",
            "太阳" to if (isReversed) "短暂阴霾" else "光明普照",
            "审判" to if (isReversed) "回声待答" else "浴火重生",
            "世界" to if (isReversed) "圆满未竟" else "功德圆满",
            // Minor Arcana — suits
            "权杖" to if (isReversed) "火候待稳" else "行动果决",
            "圣杯" to if (isReversed) "心潮待平" else "心灵丰盈",
            "宝剑" to if (isReversed) "思路待清" else "洞察真相",
            "星币" to if (isReversed) "根基待固" else "稳扎稳打"
        )

        // Try exact match first
        themeMap[cardName]?.let { return it }

        // Try partial match (for minor arcana: "权杖一", "圣杯王后", etc.)
        for ((key, value) in themeMap) {
            if (cardName.startsWith(key)) return value
        }

        // Fallback: generate based on reversal
        return if (isReversed) "转念待明" else "顺势而为"
    }

    /** Match fortune based on question topic keywords */
    private fun matchTopicFortune(question: String, interpretation: String): String? {
        val combined = question + interpretation
        val topicMap = listOf(
            listOf("健康", "身体", "养生", "疾病", "医疗", "康复") to "身心康泰",
            listOf("感情", "爱情", "恋爱", "婚姻", "桃花", "姻缘") to "情缘天定",
            listOf("事业", "工作", "职业", "升职", "创业", "前程") to "鹏程万里",
            listOf("财运", "金钱", "投资", "财富", "理财") to "财源广进",
            listOf("学业", "考试", "学习", "智慧") to "金榜题名",
            listOf("家庭", "亲人", "父母", "子女") to "家宅安宁",
            listOf("贵人", "人缘", "人际") to "贵人相助",
            listOf("出行", "旅行", "迁移") to "逢凶化吉",
        )
        // Score each topic by keyword frequency, pick from top group via hash
        val scored = topicMap
            .map { (keywords, phrase) -> phrase to keywords.count { combined.contains(it) } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        if (scored.isEmpty()) return null

        val topScore = scored.first().second
        val topGroup = scored.filter { it.second == topScore }
        val index = (combined.hashCode().toLong().let { if (it < 0) -it else it }).toInt() % topGroup.size
        return topGroup[index].first
    }

    /** Brief one-line meaning for a tarot card */
    fun tarotMeaning(cardName: String, isReversed: Boolean): String {
        val meanings = mapOf(
            "愚者" to if (isReversed) "脚步宜慢，先看清路再启程" else "新的旅程即将开始，保持纯真与勇气",
            "魔术师" to if (isReversed) "资源尚未聚拢，先重新校准目标" else "你拥有实现目标的一切资源",
            "女祭司" to if (isReversed) "答案仍在心中，只是暂被杂音遮蔽" else "静心聆听内心深处的智慧",
            "女皇" to if (isReversed) "创造力枯竭，需滋养身心" else "丰饶与创造力正在涌流",
            "皇帝" to if (isReversed) "秩序需要弹性，松一步反能稳局" else "建立秩序与稳固的基础",
            "死神" to if (isReversed) "旧念仍有牵缠，放下之后路会变宽" else "旧阶段结束，新生命萌芽",
            "塔" to if (isReversed) "变化已在门前，先整理根基再迎新局" else "旧有结构松动，正适合破旧立新",
            "星星" to if (isReversed) "信心受挫，但黎明终将到来" else "希望之光正在指引方向",
            "月亮" to if (isReversed) "迷雾渐散，真相即将显现" else "表象之下暗藏玄机，需谨慎",
            "太阳" to if (isReversed) "暂时的困难遮不住光明" else "成功与喜悦正在降临",
            "命运之轮" to if (isReversed) "轮转未到高处，守住节奏便有回升" else "命运转折已至，把握机遇",
            "正义" to if (isReversed) "偏见蒙蔽判断，需客观审视" else "公正的裁决即将到来",
            "审判" to if (isReversed) "逃避过去，需直面内心" else "觉醒之时，过往皆有答案",
            "世界" to if (isReversed) "尚有未竟之事，需善始善终" else "圆满达成，进入新境界"
        )
        // Try exact match
        meanings[cardName]?.let { return it }
        val suitMeanings = mapOf(
            "权杖" to if (isReversed) "行动之火尚需收束，先稳节奏再推进" else "行动之火已燃，适合主动开局",
            "圣杯" to if (isReversed) "心潮需要安放，关系中宜少猜多问" else "情感之水流动，人心与缘分都有回应",
            "宝剑" to if (isReversed) "思绪尚有纠结，先厘清事实再决断" else "理性之刃已明，适合看清真相",
            "星币" to if (isReversed) "现实根基尚待加固，宜从小处积累" else "现实根基渐稳，付出会慢慢见形"
        )
        suitMeanings[cardName]?.let { return it }
        for ((key, value) in suitMeanings) {
            if (cardName.startsWith(key)) return value
        }
        // Try partial match for minor arcana
        for ((key, value) in meanings) {
            if (cardName.startsWith(key)) return value
        }
        // Fallback
        return if (isReversed) "局势尚未定型，先稳住心念与步伐" else "天时渐开，可以顺势有所作为"
    }

    // ═══════════════════════════════════════════════════════════════════
    // VISION Fortune Mapping
    // ═══════════════════════════════════════════════════════════════════

    /** Generate 4-char fortune summary for vision readings */
    fun visionFortune(text: String): String {
        if (text.isBlank()) return "面相玄机"

        val themeMap = listOf(
            // 事业
            listOf("事业", "工作", "职业", "升职", "发展", "创业", "管理", "官禄") to "鹏程万里",
            listOf("领导", "权力", "地位", "权柄", "掌权") to "权柄在握",
            listOf("根基", "稳扎", "循序", "厚积") to "基业稳固",
            // 财运
            listOf("财运", "金钱", "财富", "投资", "富贵", "理财", "财帛") to "财源广进",
            listOf("食禄", "口福", "衣食") to "衣食无忧",
            // 感情
            listOf("感情", "爱情", "桃花", "婚姻", "异性", "情缘") to "情缘天定",
            listOf("专一", "深情", "重感情") to "情深意重",
            listOf("异性缘", "魅力", "吸引") to "桃花盈门",
            // 健康
            listOf("健康", "身体", "精力", "长寿", "肾气", "养生") to "身心康泰",
            listOf("体质", "先天", "禀赋") to "先天充沛",
            // 贵人/人际
            listOf("贵人", "人缘", "人际", "助力", "交际") to "贵人相助",
            listOf("兄弟", "朋友", "社交") to "广结善缘",
            // 智慧
            listOf("智慧", "聪明", "悟性", "学习", "聪慧", "敏") to "慧根深厚",
            listOf("直觉", "洞察", "观察", "心思") to "洞若观火",
            // 性格
            listOf("性格", "坚毅", "果断", "意志", "刚直") to "刚毅果决",
            listOf("温和", "随和", "圆融", "变通") to "温润如玉",
            listOf("沉稳", "稳重", "冷静", "内敛") to "沉稳内敛",
            listOf("豁达", "开阔", "大度") to "胸襟豁达",
            // 潜力/未来
            listOf("潜力", "未来", "突破", "转机", "大有可为") to "破局之象",
            listOf("积累", "沉淀", "等待", "蓄势") to "蓄势待发",
            // 需注意
            listOf("危机", "困难", "阻碍", "注意", "谨慎") to "明哲保身",
            listOf("多思", "忧虑", "心血", "暗耗") to "静心养性",
            // 运势
            listOf("变动", "迁移", "出行", "化险") to "逢凶化吉",
            listOf("修行", "内省", "修养", "心境") to "明心见性",
            listOf("晚年", "子女", "田产", "安乐") to "晚景从容",
            listOf("少年", "早年", "根基") to "少年得志",
            listOf("中年", "稳步") to "中年亨通",
            // 面形五行
            listOf("水形", "圆面", "圆融") to "上善若水",
            listOf("木形", "长面", "坚韧") to "木秀于林",
            listOf("金形", "方面", "刚正") to "金石之坚",
            listOf("土形", "厚实", "敦厚") to "厚德载物",
            // 对称/气色
            listOf("对称", "端正", "平衡", "公允") to "端正祥和",
            listOf("气色", "红润", "光泽") to "气色明朗",
        )

        // Score each theme by keyword frequency
        val scored = themeMap
            .map { (keywords, phrase) -> phrase to keywords.count { text.contains(it) } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        if (scored.isEmpty()) return "面相玄机"

        // Take top-scoring group (same score) and pick one based on text hash
        // This avoids always returning "鹏程万里" when career keywords dominate
        val topScore = scored.first().second
        val topGroup = scored.filter { it.second == topScore }
        val index = (text.hashCode().toLong().let { if (it < 0) -it else it }).toInt() % topGroup.size
        return topGroup[index].first
    }

    /** Derive a philosophical one-sentence summary from the 4-char vision title */
    fun visionMeaning(title: String): String {
        return when (title) {
            "鹏程万里" -> "眉目藏锋，前路宜以定力换远行。"
            "权柄在握" -> "有执掌之相，越能自持，越能服众。"
            "基业稳固" -> "根深者不惧风急，慢行亦是长进。"
            "财源广进" -> "财不逐躁心而来，守正则流泉自至。"
            "衣食无忧" -> "口福与食禄相随，惜福则福长。"
            "情缘天定" -> "缘分将至未必喧哗，真心自有回响。"
            "情深意重" -> "重情是福，知分寸则情不成累。"
            "桃花盈门" -> "花开有时，择其清者方成佳缘。"
            "身心康泰" -> "形安则神定，养气即是养运。"
            "先天充沛" -> "先天有余，更宜以后天修持护之。"
            "贵人相助" -> "贵人多在善念处，先结善缘后得助力。"
            "广结善缘" -> "人和即是风水，言行温厚自聚良缘。"
            "慧根深厚" -> "慧由静生，少言多察则机心自明。"
            "洞若观火" -> "眼明不贵看破，贵在看破之后仍从容。"
            "刚毅果决" -> "刚中须藏柔，断事方能不伤和气。"
            "温润如玉" -> "温和不是退让，是把锋芒藏入分寸。"
            "沉稳内敛" -> "不露者未必无光，厚积之人自有后发。"
            "胸襟豁达" -> "心宽则路宽，能容人处即是好运处。"
            "破局之象" -> "局未必困人，困人者多是旧念。"
            "蓄势待发" -> "未动不是停滞，是风起之前的蓄力。"
            "明哲保身" -> "避其锋不是怯，留其身方能待时。"
            "静心养性" -> "心静则气顺，气顺则运自平。"
            "逢凶化吉" -> "险处藏转机，稳住一念便有生路。"
            "明心见性" -> "照见本心之后，外境便少一分牵缠。"
            "晚景从容" -> "早修善因，晚来方得从容。"
            "少年得志" -> "早开的花更需护根，得意时尤宜自省。"
            "中年亨通" -> "中道见真章，稳处最能生势。"
            "上善若水" -> "柔能载物，顺势而行反成大力。"
            "木秀于林" -> "木向光而生，人向远而立。"
            "金石之坚" -> "坚者贵在有节，过刚则易折。"
            "厚德载物" -> "能承其重者，终得其厚。"
            "端正祥和" -> "相由心定，心正则诸事少偏。"
            "气色明朗" -> "气明则运开，近日宜顺势添柴。"
            else -> "相不定命，观其势而修其心。"
        }
    }
}
