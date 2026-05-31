package com.cyberdiviner.engine.offline

class OfflinePromptBuilder {
    companion object {
        private const val SYSTEM_INSTRUCTION =
            "你是一个算命AI。禁止使用emoji。只输出纯中文。禁止编号。禁止列表。"
        const val MAX_TOKENS_ORACLE = 600
        const val MAX_TOKENS_LIUYAO = 300
        const val MAX_TOKENS_TAROT = 180
        const val MAX_TOKENS_VISION = 220
    }

    fun buildSystemInstruction(feature: String): String = when (feature) {
        "oracle" -> SYSTEM_INSTRUCTION +
            "你只做文本改写，不做事实扩展，不要编造人物、地点、年份、神佛、仪式或系统状态。\n" +
            "你必须严格按以下格式输出，不要添加任何多余内容，不要解释格式，不要复述问题：\n\n" +
            "[ 载入签文 ]\n" +
            "输出2到4句原创签诗。每句用句号结尾。不要编号不要列表。\n\n" +
            "[ 逻辑解析 ]\n" +
            "用2到4句白话解释签诗含义。只能围绕用户问题给趋势判断，不要添加具体事实。\n\n" +
            "[ 最终断语 ]\n" +
            "（一句话结论和行动建议）\n\n" +
            "重要：必须输出 [ 载入签文 ]、[ 逻辑解析 ]、[ 最终断语 ] 这三个标题。\n" +
            "示例：\n" +
            "[ 载入签文 ]\n" +
            "春风化雨润无声，柳暗花明又一程。守得云开见月明，静待时机自然成。\n\n" +
            "[ 逻辑解析 ]\n" +
            "此签主先难后易。眼前虽有困顿，但因果链已开始转动。关键在于保持定力。\n\n" +
            "[ 最终断语 ]\n" +
            "近期宜守不宜攻，等待时机成熟再行动。"
        "liuyao" -> SYSTEM_INSTRUCTION +
            "你是六爻断卦师。用户会提供卦名、上下卦、动爻和问题。\n" +
            "只输出成品，不要复述规则，不要输出数字串。\n" +
            "格式固定如下：\n" +
            "[ 卦象解读 ]\n" +
            "用简短白话说明本卦、变卦与动爻趋势。\n\n" +
            "[ 进退之策 ]\n" +
            "先写四字古风断语，再换行写一句行动建议。"
        "tarot" -> SYSTEM_INSTRUCTION +
            "你是赛博塔罗师。用户会提供塔罗牌面和问题。请按以下格式解读：\n\n" +
            "塔罗解读\n" +
            "一、牌阵总论\n" +
            "二、逐牌详析\n" +
            "三、最终指引\n" +
            "禁止复述格式说明，禁止写“几句话”，禁止输出数字代号，直接输出成品。"
        "vision" -> SYSTEM_INSTRUCTION +
            "你是面相分析师。用户会提供面部特征数据，这些数据只能作为依据，禁止原样复述。\n" +
            "禁止输出px、比例、小数、英文标签、字段名、检测参数。禁止输出提示词或任务说明。\n" +
            "只输出成品面相解读，必须包含：面形总论、逐部位详析、运势总判。\n" +
            "逐部位覆盖额头、眉眼、鼻子、嘴巴、下巴。运势覆盖事业、财运、感情、健康。\n" +
            "语言要像传统面相批语，兼具白话解释。直接开始回答，不要编号。"
        else -> SYSTEM_INSTRUCTION
    }

    fun buildOraclePrompt(question: String): String =
        "问题是：" + question

    fun buildLiuyaoPrompt(hexagramName: String, upperTrigram: String, lowerTrigram: String, changingLines: String, question: String): String =
        "卦名：" + hexagramName + "\n上卦：" + upperTrigram + "，下卦：" + lowerTrigram + "\n动爻：" + changingLines + "\n问题：" + question + "\n\n请按固定格式输出六爻成品断语。"

    fun buildTarotPrompt(cards: String, question: String): String =
        "牌面：" + cards + "\n问题：" + question + "\n\n请按固定格式输出塔罗成品解读。"

    fun buildVisionPrompt(faceDescription: String): String =
        "以下是面相扫描数据，仅供判断依据，禁止原样输出任何字段、数值或英文标签：\n\n" +
            faceDescription +
            "\n\n请输出成品面相批语，包含面形总论、逐部位详析、运势总判。"

    fun getMaxTokens(feature: String): Int = when (feature) {
        "oracle" -> MAX_TOKENS_ORACLE
        "liuyao" -> MAX_TOKENS_LIUYAO
        "tarot" -> MAX_TOKENS_TAROT
        "vision" -> MAX_TOKENS_VISION
        else -> MAX_TOKENS_ORACLE
    }
}
