package com.cyberdiviner.engine

import kotlin.random.Random

/**
 * 塔罗牌数据类
 * Represents a single Tarot card with its properties and meanings
 */
data class TarotCard(
    val id: Int,
    val name: String,           // English name
    val nameZh: String,         // Chinese name
    val suit: String,           // 大阿卡纳/权杖/圣杯/宝剑/星币
    var isReversed: Boolean = false,
    val meaning: String,        // Upright meaning
    val meaningReversed: String, // Reversed meaning
    val description: String     // Card description
) {
    fun getDisplayName(): String {
        return if (isReversed) {
            "$nameZh ($name) [逆位]"
        } else {
            "$nameZh ($name) [正位]"
        }
    }

    fun getCurrentMeaning(): String {
        return if (isReversed) meaningReversed else meaning
    }
}

/**
 * 塔罗牌牌阵类型
 */
enum class TarotSpreadType(val cardCount: Int) {
    SINGLE(1),           // 单牌占卜
    THREE_CARD(3),       // 三牌牌阵
    CELTIC_CROSS(10),    // 凯尔特十字
    PENTACLE(5),         // 五芒星牌阵
    YES_NO(1)            // 是非占卜
}

/**
 * 塔罗牌牌阵
 * Represents a spread layout with position meanings
 */
data class TarotSpread(
    val type: TarotSpreadType,
    val name: String,
    val nameZh: String,
    val description: String,
    val cardPositions: List<String> // 位置名称
)

/**
 * 塔罗牌抽取结果
 */
data class TarotDrawResult(
    val spread: TarotSpread,
    val cards: List<TarotCard>,
    val question: String
)

/**
 * 塔罗牌引擎
 * Core engine for Tarot card operations
 */
object TarotEngine {

    // ==================== 牌组定义 ====================

    private val majorArcana = listOf(
        TarotCard(
            id = 0,
            name = "The Fool",
            nameZh = "愚者",
            suit = "大阿卡纳",
            meaning = "新的开始、冒险、自由、天真",
            meaningReversed = "鲁莽、犹豫不决、冒不必要的风险",
            description = "愚者代表着新的旅程和无限的可能性。他背着行囊，向着未知的未来出发，象征着纯真的冒险精神和对生活的热情。"
        ),
        TarotCard(
            id = 1,
            name = "The Magician",
            nameZh = "魔术师",
            suit = "大阿卡纳",
            meaning = "创造力、技能、意志力、新的机会",
            meaningReversed = "欺骗、操控、缺乏方向",
            description = "魔术师掌握着四种元素的力量，象征着意志力和创造力。他提醒你拥有实现目标所需的所有工具和能力。"
        ),
        TarotCard(
            id = 2,
            name = "The High Priestess",
            nameZh = "女祭司",
            suit = "大阿卡纳",
            meaning = "直觉、潜意识、神秘、内在智慧",
            meaningReversed = "隐藏的动机、信息泄露、过度依赖理性",
            description = "女祭司坐在黑白两柱之间，守护着隐藏的知识。她代表着直觉和潜意识的力量，提醒你倾听内心的声音。"
        ),
        TarotCard(
            id = 3,
            name = "The Empress",
            nameZh = "皇后",
            suit = "大阿卡纳",
            meaning = "丰收、母性、自然、美丽、富足",
            meaningReversed = "依赖、过度保护、创造力受阻",
            description = "皇后象征着自然的丰饶和母性的力量。她代表着繁荣、美丽和感官享受，提醒你享受生活的美好。"
        ),
        TarotCard(
            id = 4,
            name = "The Emperor",
            nameZh = "皇帝",
            suit = "大阿卡纳",
            meaning = "权威、稳定、领导力、父性",
            meaningReversed = "专制、僵化、缺乏纪律",
            description = "皇帝代表着权威和结构。他象征着稳定、领导力和父性力量，提醒你需要建立秩序和规则。"
        ),
        TarotCard(
            id = 5,
            name = "The Hierophant",
            nameZh = "教皇",
            suit = "大阿卡纳",
            meaning = "传统、信仰、教育、精神指导",
            meaningReversed = "打破传统、非正统、个人信念",
            description = "教皇象征着传统和精神信仰。他代表着教育和指导，提醒你遵循传统智慧或寻求精神上的指引。"
        ),
        TarotCard(
            id = 6,
            name = "The Lovers",
            nameZh = "恋人",
            suit = "大阿卡纳",
            meaning = "爱情、和谐、选择、关系",
            meaningReversed = "失衡、价值观冲突、错误的选择",
            description = "恋人象征着爱情和重要的选择。他提醒你在感情和生活中做出正确的决定，追求和谐的关系。"
        ),
        TarotCard(
            id = 7,
            name = "The Chariot",
            nameZh = "战车",
            suit = "大阿卡纳",
            meaning = "胜利、意志力、决心、克服困难",
            meaningReversed = "失控、缺乏方向、侵略性",
            description = "战车象征着胜利和前进的动力。他提醒你运用意志力克服障碍，坚定地朝着目标前进。"
        ),
        TarotCard(
            id = 8,
            name = "Strength",
            nameZh = "力量",
            suit = "大阿卡纳",
            meaning = "勇气、耐心、内在力量、自律",
            meaningReversed = "软弱、缺乏自信、自我怀疑",
            description = "力量象征着内在的勇气和韧性。她提醒你运用温柔的力量来克服困难，相信自己的能力。"
        ),
        TarotCard(
            id = 9,
            name = "The Hermit",
            nameZh = "隐士",
            suit = "大阿卡纳",
            meaning = "内省、孤独、寻求真理、智慧",
            meaningReversed = "孤立、逃避现实、过度封闭",
            description = "隐士在山中寻找真理。他象征着内省和智慧的追求，提醒你需要独处来寻找答案。"
        ),
        TarotCard(
            id = 10,
            name = "Wheel of Fortune",
            nameZh = "命运之轮",
            suit = "大阿卡纳",
            meaning = "命运、转变、好运、循环",
            meaningReversed = "厄运、抗拒改变、失控",
            description = "命运之轮象征着生命的循环和变化。它提醒你接受命运的安排，把握好运的机会。"
        ),
        TarotCard(
            id = 11,
            name = "Justice",
            nameZh = "正义",
            suit = "大阿卡纳",
            meaning = "公正、真相、因果报应、法律",
            meaningReversed = "不公、逃避责任、偏见",
            description = "正义象征着公平和真理。她提醒你做出公正的决定，承担自己的责任。"
        ),
        TarotCard(
            id = 12,
            name = "The Hanged Man",
            nameZh = "倒吊人",
            suit = "大阿卡纳",
            meaning = "牺牲、等待、新的视角、放下",
            meaningReversed = "拖延、抗拒、无谓的牺牲",
            description = "倒吊人以全新的角度看世界。他象征着牺牲和等待，提醒你换个角度思考问题。"
        ),
        TarotCard(
            id = 13,
            name = "Death",
            nameZh = "死神",
            suit = "大阿卡纳",
            meaning = "结束、转变、新生、放下过去",
            meaningReversed = "抗拒改变、停滞不前、恐惧",
            description = "死神象征着结束和新生。它提醒你放下过去，迎接新的开始和转变。"
        ),
        TarotCard(
            id = 14,
            name = "Temperance",
            nameZh = "节制",
            suit = "大阿卡纳",
            meaning = "平衡、耐心、调和、中庸",
            meaningReversed = "失衡、过度、极端",
            description = "节制象征着平衡和调和。她提醒你在生活中寻求中庸之道，保持身心的平衡。"
        ),
        TarotCard(
            id = 15,
            name = "The Devil",
            nameZh = "恶魔",
            suit = "大阿卡纳",
            meaning = "束缚、诱惑、物质主义、阴影",
            meaningReversed = "解脱、释放、打破束缚",
            description = "恶魔象征着束缚和诱惑。它提醒你警惕物质主义和负面的束缚，寻求自由和解脱。"
        ),
        TarotCard(
            id = 16,
            name = "The Tower",
            nameZh = "塔",
            suit = "大阿卡纳",
            meaning = "突变、破坏、觉醒、真相",
            meaningReversed = "逃避灾难、恐惧改变、内乱",
            description = "塔象征着突如其来的变化和破坏。它提醒你接受必要的改变，即使过程痛苦，也能带来觉醒。"
        ),
        TarotCard(
            id = 17,
            name = "The Star",
            nameZh = "星星",
            suit = "大阿卡纳",
            meaning = "希望、灵感、宁静、治愈",
            meaningReversed = "失望、缺乏信心、孤独",
            description = "星星象征着希望和灵感。它提醒你保持信心，相信美好的未来即将到来。"
        ),
        TarotCard(
            id = 18,
            name = "The Moon",
            nameZh = "月亮",
            suit = "大阿卡纳",
            meaning = "幻觉、恐惧、潜意识、直觉",
            meaningReversed = "释放恐惧、清晰、真相大白",
            description = "月亮象征着潜意识和幻觉。它提醒你面对内心的恐惧，相信直觉的指引。"
        ),
        TarotCard(
            id = 19,
            name = "The Sun",
            nameZh = "太阳",
            suit = "大阿卡纳",
            meaning = "成功、快乐、活力、乐观",
            meaningReversed = "短暂的快乐、过度乐观、自满",
            description = "太阳象征着成功和快乐。它提醒你享受生活的美好，保持乐观的心态。"
        ),
        TarotCard(
            id = 20,
            name = "Judgement",
            nameZh = "审判",
            suit = "大阿卡纳",
            meaning = "觉醒、重生、召唤、判断",
            meaningReversed = "自我怀疑、逃避、拒绝改变",
            description = "审判象征着觉醒和重生。它提醒你接受内心的召唤，做出重要的人生选择。"
        ),
        TarotCard(
            id = 21,
            name = "The World",
            nameZh = "世界",
            suit = "大阿卡纳",
            meaning = "完成、成就、圆满、新的循环",
            meaningReversed = "未完成、缺乏终结、停滞",
            description = "世界象征着完成和圆满。它提醒你庆祝成就，同时准备迎接新的挑战和循环。"
        )
    )

    // Minor Arcana 牌组定义
    private val minorArcanaSuits = listOf("权杖", "圣杯", "宝剑", "星币")
    private val courtCards = listOf("侍从", "骑士", "王后", "国王")
    private val numbers = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    /**
     * 生成完整的小阿卡纳牌组
     */
    private fun generateMinorArcana(): List<TarotCard> {
        val cards = mutableListOf<TarotCard>()
        var id = 22  // Major Arcana IDs 0-21

        minorArcanaSuits.forEach { suit ->
            // Number cards (Ace - 10)
            numbers.forEachIndexed { index, number ->
                val numberValue = index + 1
                val chineseNumber = convertToChineseNumber(numberValue)
                cards.add(
                    TarotCard(
                        id = id++,
                        name = "$number of $suit",
                        nameZh = "$suit$chineseNumber",
                        suit = suit,
                        meaning = getNumberMeaning(suit, numberValue),
                        meaningReversed = getNumberMeaningReversed(suit, numberValue),
                        description = "${suit}牌组中的${chineseNumber}，代表着${suit}元素的力量。"
                    )
                )
            }

            // Court cards (侍从、骑士、王后、国王)
            courtCards.forEach { court ->
                cards.add(
                    TarotCard(
                        id = id++,
                        name = "$court of $suit",
                        nameZh = "$suit$court",
                        suit = suit,
                        meaning = getCourtMeaning(suit, court),
                        meaningReversed = getCourtMeaningReversed(suit, court),
                        description = "${suit}牌组中的${court}，象征着${court}的特质。"
                    )
                )
            }
        }

        return cards
    }

    /**
     * 数字转中文
     */
    private fun convertToChineseNumber(num: Int): String {
        val chineseNums = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
        return if (num in 1..10) chineseNums[num - 1] else num.toString()
    }

    /**
     * 数字牌含义
     */
    private fun getNumberMeaning(suit: String, number: Int): String {
        val baseMeanings = mapOf(
            "权杖" to "创造力、行动、激情",
            "圣杯" to "情感、直觉、关系",
            "宝剑" to "思想、沟通、冲突",
            "星币" to "物质、财富、实际"
        )
        val numberModifiers = mapOf(
            1 to "新的开始、机遇",
            2 to "平衡、选择",
            3 to "成长、扩张",
            4 to "稳定、基础",
            5 to "挑战、冲突",
            6 to "和谐、合作",
            7 to "内省、评估",
            8 to "行动、进展",
            9 to "完成、成熟",
            10 to "圆满、结束"
        )
        return "${baseMeanings[suit]}，${numberModifiers[number]}"
    }

    /**
     * 数字牌逆位含义
     */
    private fun getNumberMeaningReversed(suit: String, number: Int): String {
        return "${getNumberMeaning(suit, number)}的阻碍或相反面"
    }

    /**
     * 宫廷牌含义
     */
    private fun getCourtMeaning(suit: String, court: String): String {
        val courtMeanings = mapOf(
            "侍从" to "学习、探索、新消息",
            "骑士" to "行动、冒险、追寻",
            "王后" to "直觉、养育、内在力量",
            "国王" to "权威、领导、掌控"
        )
        return "${courtMeanings[court]}，与${suit}元素相关"
    }

    /**
     * 宫廷牌逆位含义
     */
    private fun getCourtMeaningReversed(suit: String, court: String): String {
        return "${getCourtMeaning(suit, court)}的阻碍或相反面"
    }

    // ==================== 完整牌组 ====================

    val fullDeck: List<TarotCard> by lazy { majorArcana + generateMinorArcana() }

    // ==================== 牌阵定义 ====================

    val spreads = mapOf(
        TarotSpreadType.SINGLE to TarotSpread(
            type = TarotSpreadType.SINGLE,
            name = "Single Card",
            nameZh = "单牌占卜",
            description = "抽取一张牌，获得简短的指引或答案",
            cardPositions = listOf("指引")
        ),
        TarotSpreadType.THREE_CARD to TarotSpread(
            type = TarotSpreadType.THREE_CARD,
            name = "Three Card Spread",
            nameZh = "三牌牌阵",
            description = "过去、现在、未来的三张牌解读",
            cardPositions = listOf("过去", "现在", "未来")
        ),
        TarotSpreadType.CELTIC_CROSS to TarotSpread(
            type = TarotSpreadType.CELTIC_CROSS,
            name = "Celtic Cross",
            nameZh = "凯尔特十字牌阵",
            description = "最经典的十张牌牌阵，全面解读",
            cardPositions = listOf(
                "当前状况",
                "挑战/阻碍",
                "目标/理想",
                "近期过去",
                "近期未来",
                "潜意识",
                "自我认知",
                "环境影响",
                "希望与恐惧",
                "最终结果"
            )
        ),
        TarotSpreadType.PENTACLE to TarotSpread(
            type = TarotSpreadType.PENTACLE,
            name = "Pentacle Spread",
            nameZh = "五芒星牌阵",
            description = "五张牌的五芒星形状牌阵",
            cardPositions = listOf("当前状况", "挑战", "根源", "近期行动", "结果")
        ),
        TarotSpreadType.YES_NO to TarotSpread(
            type = TarotSpreadType.YES_NO,
            name = "Yes/No",
            nameZh = "是非占卜",
            description = "抽取一张牌回答是非问题",
            cardPositions = listOf("答案")
        )
    )

    // ==================== 核心功能 ====================

    /**
     * 创建洗好的牌组副本
     */
    fun createShuffledDeck(): List<TarotCard> {
        return fullDeck.map { it.copy() }.shuffled()
    }

    /**
     * 从牌组中抽取指定数量的牌
     * @param deck 洗好的牌组
     * @param count 要抽取的牌数
     * @param allowReversed 是否允许逆位
     * @return 抽取的牌列表
     */
    fun drawCards(
        deck: List<TarotCard>,
        count: Int,
        allowReversed: Boolean = true
    ): List<TarotCard> {
        if (count > deck.size) {
            throw IllegalArgumentException("抽取的牌数 ($count) 超过牌组剩余数量 (${deck.size})")
        }

        return deck.take(count).map { card ->
            card.copy(isReversed = if (allowReversed) Random.nextFloat() < 0.33f else false)
        }
    }

    /**
     * 根据问题复杂度推荐牌阵类型
     * @param question 问题内容
     * @return 推荐的牌阵类型
     */
    fun recommendSpreadType(question: String): TarotSpreadType {
        val length = question.length

        return when {
            length < 20 -> TarotSpreadType.SINGLE
            length < 50 -> TarotSpreadType.THREE_CARD
            length < 100 -> TarotSpreadType.PENTACLE
            else -> TarotSpreadType.CELTIC_CROSS
        }
    }

    /**
     * 根据牌阵类型和问题进行洗牌、抽牌
     * @param spreadType 牌阵类型
     * @param question 问题
     * @return 抽牌结果
     */
    fun shuffleAndDraw(
        spreadType: TarotSpreadType,
        question: String
    ): TarotDrawResult {
        val deck = createShuffledDeck()
        val cards = drawCards(deck, spreadType.cardCount)
        val spread = spreads[spreadType]
            ?: throw IllegalArgumentException("未知的牌阵类型: $spreadType")

        return TarotDrawResult(
            spread = spread,
            cards = cards,
            question = question
        )
    }

    /**
     * 自动推荐牌阵并抽牌
     * @param question 问题
     * @return 抽牌结果
     */
    fun autoRead(question: String): TarotDrawResult {
        val spreadType = recommendSpreadType(question)
        return shuffleAndDraw(spreadType, question)
    }

    /**
     * 获取牌组中所有大阿卡纳
     */
    fun getMajorArcana(): List<TarotCard> {
        return majorArcana.map { it.copy() }
    }

    /**
     * 根据ID查找牌
     */
    fun findCardById(id: Int): TarotCard? {
        return fullDeck.find { it.id == id }?.copy()
    }

    /**
     * 根据名称查找牌（支持中英文）
     */
    fun findCardByName(name: String): TarotCard? {
        return fullDeck.find {
            it.name.equals(name, ignoreCase = true) ||
                    it.nameZh.contains(name)
        }?.copy()
    }
}
