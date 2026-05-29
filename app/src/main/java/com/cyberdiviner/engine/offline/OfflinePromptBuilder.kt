package com.cyberdiviner.engine.offline

class OfflinePromptBuilder {
    companion object {
        private const val SYSTEM_INSTRUCTION =
            "你是一个算命AI。禁止使用emoji。只输出纯中文。"
        const val MAX_TOKENS_ORACLE = 500
        const val MAX_TOKENS_LIUYAO = 200
        const val MAX_TOKENS_TAROT = 200
        const val MAX_TOKENS_VISION = 400
    }

    /**
     * Build system instruction — simplified for 1.5B model.
     * The small model cannot follow complex multi-section format instructions.
     * We use a flat, direct prompt that maximizes chance of coherent output.
     */
    fun buildSystemInstruction(feature: String): String = when (feature) {
        "oracle" -> SYSTEM_INSTRUCTION +
            "根据用户的问题，生成签诗和解析。必须严格按以下格式输出，不要有多余内容：\n\n" +
            "[ 载入签文 ]\n" +
            "（四句签诗）\n\n" +
            "[ 逻辑解析 ]\n" +
            "（白话解析）\n\n" +
            "[ 最终断语 ]\n" +
            "（结论和建议）"
        "liuyao" -> SYSTEM_INSTRUCTION +
            "用户会提供一个六爻卦象。请用2-3句话解读卦象含义，给出吉凶判断和建议。直接开始回答。"
        "tarot" -> SYSTEM_INSTRUCTION +
            "用户会提供塔罗牌面。请用2-3句话解读牌面含义，给出建议。直接开始回答。"
        "vision" -> SYSTEM_INSTRUCTION +
            "你是面相分析师。根据面相数据，给出详细分析。必须按以下格式输出：\n\n" +
            "先写两个四字成语总结面相（如：天庭饱满、地阁方圆）。\n" +
            "然后分析面形、额头、眉眼、鼻子、嘴巴、下巴各部位，每个部位1-2句话。\n" +
            "再给出事业、财运、感情、健康四方面运势判断。\n" +
            "最后用一句话总结。直接开始回答，不要写标题或编号。"
        else -> SYSTEM_INSTRUCTION
    }

    fun buildOraclePrompt(question: String): String =
        "问题是：" + question

    fun buildLiuyaoPrompt(hexagramName: String, upperTrigram: String, lowerTrigram: String, changingLines: String, question: String): String =
        "卦名：" + hexagramName + "\n上卦：" + upperTrigram + "，下卦：" + lowerTrigram + "\n动爻：" + changingLines + "\n问题：" + question + "\n\n请简要解读此卦。"

    fun buildTarotPrompt(cards: String, question: String): String =
        "牌面：" + cards + "\n问题：" + question + "\n\n请简要解读牌面含义。"

    fun buildVisionPrompt(faceDescription: String): String =
        "以下是面相扫描数据：\n\n" + faceDescription + "\n\n请根据以上数据进行面相分析。"

    fun getMaxTokens(feature: String): Int = when (feature) {
        "oracle" -> MAX_TOKENS_ORACLE
        "liuyao" -> MAX_TOKENS_LIUYAO
        "tarot" -> MAX_TOKENS_TAROT
        "vision" -> MAX_TOKENS_VISION
        else -> MAX_TOKENS_ORACLE
    }
}
