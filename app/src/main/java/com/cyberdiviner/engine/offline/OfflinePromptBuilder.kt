package com.cyberdiviner.engine.offline

class OfflinePromptBuilder {
    companion object {
        private const val SYSTEM_INSTRUCTION =
            "你是一个算命AI。禁止使用emoji。只输出纯中文。"
        const val MAX_TOKENS_ORACLE = 300
        const val MAX_TOKENS_LIUYAO = 200
        const val MAX_TOKENS_TAROT = 200
        const val MAX_TOKENS_VISION = 100
    }

    /**
     * Build system instruction — simplified for 1.5B model.
     * The small model cannot follow complex multi-section format instructions.
     * We use a flat, direct prompt that maximizes chance of coherent output.
     */
    fun buildSystemInstruction(feature: String): String = when (feature) {
        "oracle" -> SYSTEM_INSTRUCTION +
            "用户会提出一个问题。请用3-5句话回答：先写两句诗意的签文，再用白话解释含义，最后给出一句直接的建议。直接开始回答，不要写标题或标签。"
        "liuyao" -> SYSTEM_INSTRUCTION +
            "用户会提供一个六爻卦象。请用2-3句话解读卦象含义，给出吉凶判断和建议。直接开始回答。"
        "tarot" -> SYSTEM_INSTRUCTION +
            "用户会提供塔罗牌面。请用2-3句话解读牌面含义，给出建议。直接开始回答。"
        "vision" -> SYSTEM_INSTRUCTION +
            "用户会提供面相特征。请用两个四字成语总结面相，再用一句话给出总评。直接开始回答。"
        else -> SYSTEM_INSTRUCTION
    }

    fun buildOraclePrompt(question: String): String =
        "问题是：" + question

    fun buildLiuyaoPrompt(hexagramName: String, upperTrigram: String, lowerTrigram: String, changingLines: String, question: String): String =
        "卦名：" + hexagramName + "\n上卦：" + upperTrigram + "，下卦：" + lowerTrigram + "\n动爻：" + changingLines + "\n问题：" + question + "\n\n请简要解读此卦。"

    fun buildTarotPrompt(cards: String, question: String): String =
        "牌面：" + cards + "\n问题：" + question + "\n\n请简要解读牌面含义。"

    fun buildVisionPrompt(faceDescription: String): String =
        "面相特征：" + faceDescription + "\n\n请给出四字批命和一句总评。"

    fun getMaxTokens(feature: String): Int = when (feature) {
        "oracle" -> MAX_TOKENS_ORACLE
        "liuyao" -> MAX_TOKENS_LIUYAO
        "tarot" -> MAX_TOKENS_TAROT
        "vision" -> MAX_TOKENS_VISION
        else -> MAX_TOKENS_ORACLE
    }
}
