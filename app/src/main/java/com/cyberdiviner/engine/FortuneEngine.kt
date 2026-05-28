package com.cyberdiviner.engine

/**
 * FortuneEngine — shared 四字批命 (4-char fortune summary) logic.
 *
 * Used by all three divination types (Liuyao, Tarot, Vision) and the archive.
 */
object FortuneEngine {

    // ═══════════════════════════════════════════════════════════════════
    // LIUYAO (六爻) Fortune Mapping
    // ═══════════════════════════════════════════════════════════════════

    /** Generate 4-char fortune summary from hexagram name (64 hexagrams) */
    fun liuyaoFortune(hexName: String): String {
        val map = mapOf(
            // ── 上经 (1-30) ──
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
            // ── 下经 (31-64) ──
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

    /** One-line meaning for a liuyao 四字批命 */
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

    /** Generate 4-char thematic summary from tarot card */
    fun tarotFortune(cardName: String, isReversed: Boolean): String {
        val themeMap = mapOf(
            // Major Arcana
            "愚者" to if (isReversed) "迷途知返" else "无畏启程",
            "魔术师" to if (isReversed) "力不从心" else "心想事成",
            "女祭司" to if (isReversed) "表里不一" else "静待花开",
            "女皇" to if (isReversed) "丰盛受阻" else "万物生长",
            "皇帝" to if (isReversed) "刚愎自用" else "掌控全局",
            "教皇" to if (isReversed) "离经叛道" else "正道指引",
            "恋人" to if (isReversed) "情路坎坷" else "天作之合",
            "战车" to if (isReversed) "方向迷失" else "势如破竹",
            "力量" to if (isReversed) "信心动摇" else "以柔克刚",
            "隐者" to if (isReversed) "闭门造车" else "明心见性",
            "命运之轮" to if (isReversed) "时运不济" else "否极泰来",
            "正义" to if (isReversed) "偏颇失衡" else "公正无私",
            "倒吊人" to if (isReversed) "无谓牺牲" else "柳暗花明",
            "死神" to if (isReversed) "故步自封" else "涅槃重生",
            "节制" to if (isReversed) "失衡失调" else "中正平和",
            "恶魔" to if (isReversed) "挣脱枷锁" else "执念深重",
            "塔" to if (isReversed) "危机将至" else "大厦将倾",
            "星星" to if (isReversed) "希望渺茫" else "曙光初现",
            "月亮" to if (isReversed) "拨云见日" else "迷雾重重",
            "太阳" to if (isReversed) "短暂阴霾" else "光明普照",
            "审判" to if (isReversed) "逃避反思" else "浴火重生",
            "世界" to if (isReversed) "功亏一篑" else "功德圆满",
            // Minor Arcana — suits
            "权杖" to if (isReversed) "热情消退" else "行动果决",
            "圣杯" to if (isReversed) "情感受挫" else "心灵丰盈",
            "宝剑" to if (isReversed) "思绪混乱" else "洞察真相",
            "星币" to if (isReversed) "财运不稳" else "稳扎稳打"
        )

        // Try exact match first
        themeMap[cardName]?.let { return it }

        // Try partial match (for minor arcana: "权杖一", "圣杯王后", etc.)
        for ((key, value) in themeMap) {
            if (cardName.startsWith(key)) return value
        }

        // Fallback: generate based on reversal
        return if (isReversed) "逆境待变" else "顺势而为"
    }

    /** Brief one-line meaning for a tarot card */
    fun tarotMeaning(cardName: String, isReversed: Boolean): String {
        val meanings = mapOf(
            "愚者" to if (isReversed) "冲动行事将导致失控，应回归理性" else "新的旅程即将开始，保持纯真与勇气",
            "魔术师" to if (isReversed) "才华被误用，需重新聚焦目标" else "你拥有实现目标的一切资源",
            "女祭司" to if (isReversed) "忽视直觉的警示，需倾听内心" else "静心聆听内心深处的智慧",
            "女皇" to if (isReversed) "创造力枯竭，需滋养身心" else "丰饶与创造力正在涌流",
            "皇帝" to if (isReversed) "控制欲过强，需学会放手" else "建立秩序与稳固的基础",
            "死神" to if (isReversed) "抗拒必要的改变，需勇敢放手" else "旧阶段结束，新生命萌芽",
            "塔" to if (isReversed) "勉强维持将导致更大崩塌" else "旧有结构崩塌后方能重建",
            "星星" to if (isReversed) "信心受挫，但黎明终将到来" else "希望之光正在指引方向",
            "月亮" to if (isReversed) "迷雾渐散，真相即将显现" else "表象之下暗藏玄机，需谨慎",
            "太阳" to if (isReversed) "暂时的困难遮不住光明" else "成功与喜悦正在降临",
            "命运之轮" to if (isReversed) "运势低迷，需蛰伏待机" else "命运转折已至，把握机遇",
            "正义" to if (isReversed) "偏见蒙蔽判断，需客观审视" else "公正的裁决即将到来",
            "审判" to if (isReversed) "逃避过去，需直面内心" else "觉醒之时，过往皆有答案",
            "世界" to if (isReversed) "尚有未竟之事，需善始善终" else "圆满达成，进入新境界"
        )
        // Try exact match
        meanings[cardName]?.let { return it }
        // Try partial match for minor arcana
        for ((key, value) in meanings) {
            if (cardName.startsWith(key)) return value
        }
        // Fallback
        return if (isReversed) "当前形势不利，宜守不宜进" else "天时地利，可以有所作为"
    }

    // ═══════════════════════════════════════════════════════════════════
    // VISION Fortune Mapping
    // ═══════════════════════════════════════════════════════════════════

    /** Generate 4-char fortune summary for vision readings */
    fun visionFortune(text: String): String {
        if (text.isBlank()) return "面相玄机"

        val themeMap = listOf(
            listOf("事业", "工作", "职业", "升职", "发展") to "鹏程万里",
            listOf("财运", "金钱", "财富", "投资", "富贵") to "财源广进",
            listOf("感情", "爱情", "桃花", "婚姻", "异性") to "情缘天定",
            listOf("健康", "身体", "精力", "长寿") to "身心康泰",
            listOf("贵人", "人缘", "人际", "助力") to "贵人相助",
            listOf("智慧", "聪明", "悟性", "学习") to "慧根深厚",
            listOf("权力", "领导", "管理", "地位") to "权柄在握",
            listOf("福气", "福报", "好运", "吉祥") to "福泽绵长",
            listOf("性格", "坚毅", "果断", "意志") to "刚毅果决",
            listOf("潜力", "未来", "突破", "转机") to "破局之象",
            listOf("危机", "困难", "阻碍", "注意") to "明哲保身",
            listOf("变动", "迁移", "出行") to "逢凶化吉",
            listOf("修行", "内省", "修养") to "明心见性",
        )

        val best = themeMap
            .map { (keywords, phrase) -> phrase to keywords.count { text.contains(it) } }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }

        return best?.first ?: "面相玄机"
    }

    /** Derive a contextual one-sentence summary from the 4-char vision title */
    fun visionMeaning(title: String): String {
        return when (title) {
            "鹏程万里" -> "事业运势亨通，前途光明无量"
            "财源广进" -> "财运当头，正偏财皆有收获"
            "情缘天定" -> "桃花运旺，感情之事顺遂如意"
            "身心康泰" -> "面相显示健康运势良好，精力充沛"
            "贵人相助" -> "贵人运旺盛，凡事有人相助"
            "慧根深厚" -> "聪慧过人，学业悟性极高"
            "权柄在握" -> "领导才能出众，可掌权柄"
            "福泽绵长" -> "福气深厚，一生顺遂安康"
            "刚毅果决" -> "性格坚毅果决，意志力超群"
            "破局之象" -> "蕴含突破之机，未来大有可为"
            "明哲保身" -> "近期宜谨慎行事，明哲保身为上"
            "逢凶化吉" -> "虽有变动，但终能化险为夷"
            "明心见性" -> "内省修身，可得心境澄明"
            else -> "面相已解析，点击查看详细解读"
        }
    }
}
