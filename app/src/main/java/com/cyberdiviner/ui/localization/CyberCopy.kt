package com.cyberdiviner.ui.localization

import androidx.compose.runtime.staticCompositionLocalOf
import com.cyberdiviner.ui.settings.AppLanguage

/**
 * Canonical bilingual terminology layer for CyberDiviner.
 *
 * All user-facing text that needs bilingual support should be accessed
 * through this object. Chinese app terms are the source of truth;
 * English labels make the feature understandable while preserving
 * the paired Chinese anchor as cultural signal.
 *
 * Usage in Compose:
 *   val lang = LocalAppLanguage.current
 *   Text(CyberCopy.navOracle(lang))
 */
object CyberCopy {

    // ── CompositionLocal ─────────────────────────────────────────────────

    /** Provides current [AppLanguage] to the entire Compose tree. */

    // ── Brand ────────────────────────────────────────────────────────────

    fun brandName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "CYBERDIVINER"
        AppLanguage.ZH_CN -> "赛博算命"
    }

    fun brandSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "赛博算命 · Cyber Oracle"
        AppLanguage.ZH_CN -> "赛博算命"
    }

    // ── Bottom Navigation ────────────────────────────────────────────────

    fun navOracle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Oracle"
        AppLanguage.ZH_CN -> "叩问天机"
    }

    fun navRituals(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Rituals"
        AppLanguage.ZH_CN -> "术数推演"
    }

    fun navLearn(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Learn"
        AppLanguage.ZH_CN -> "修习之路"
    }

    fun navArchive(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Causal Ledger 因果命簿"
        AppLanguage.ZH_CN -> "因果命簿"
    }

    // ── Home Screen ──────────────────────────────────────────────────────

    fun homeOracle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Ask the Oracle 问卜"
        AppLanguage.ZH_CN -> "叩问天机"
    }

    fun homeRituals(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Divination 术数推演"
        AppLanguage.ZH_CN -> "术数推演"
    }

    fun homeArchive(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Causal Ledger 因果命簿"
        AppLanguage.ZH_CN -> "因果命簿"
    }

    // ── Rituals Menu ─────────────────────────────────────────────────────

    fun ritualsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Divination"
        AppLanguage.ZH_CN -> "术数推演"
    }

    fun ritualsSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "RITUAL EXECUTION"
        AppLanguage.ZH_CN -> ""
    }

    fun ritualIChing(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "I Ching 六爻"
        AppLanguage.ZH_CN -> "周易六爻"
    }

    fun ritualIChingDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cast hexagrams, interpret changing lines"
        AppLanguage.ZH_CN -> "摇钱起卦，六爻断事"
    }

    fun ritualTarot(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cyber Tarot 赛博塔罗"
        AppLanguage.ZH_CN -> "赛博塔罗"
    }

    fun ritualTarotDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "78 cards, spread layouts, AI interpretation"
        AppLanguage.ZH_CN -> "七十八牌，阵法推演"
    }

    fun ritualVision(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Face Reading 视界摸骨"
        AppLanguage.ZH_CN -> "视界摸骨"
    }

    fun ritualVisionDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Neural face scan, physiognomy analysis"
        AppLanguage.ZH_CN -> "镜阵观相，五官推演"
    }

    fun ritualBowl(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Singing Bowl 电子颂钵"
        AppLanguage.ZH_CN -> "电子颂钵"
    }

    fun ritualBowlDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Sound meditation, breath focus"
        AppLanguage.ZH_CN -> "一击清音，静心调息"
    }

    fun ritualAlmanac(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Daily Almanac 赛博黄历"
        AppLanguage.ZH_CN -> "赛博黄历"
    }

    fun ritualAlmanacDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Ganzhi calendar, daily fortune signals"
        AppLanguage.ZH_CN -> "干支黄历，每日宜忌"
    }

    // ── Settings ─────────────────────────────────────────────────────────

    fun settingsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Settings"
        AppLanguage.ZH_CN -> "算命设置"
    }

    fun settingsLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Language / 语言"
        AppLanguage.ZH_CN -> "语言设置"
    }

    fun settingsProvider(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Provider"
        AppLanguage.ZH_CN -> "服务商"
    }

    fun settingsPersona(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Persona"
        AppLanguage.ZH_CN -> "人格"
    }

    fun settingsInferenceMode(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Inference Mode"
        AppLanguage.ZH_CN -> "推理模式"
    }

    fun settingsOfflineModel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Offline Model"
        AppLanguage.ZH_CN -> "离线模型"
    }

    fun settingsOfflineDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Basic offline inference when no network. Generates verses, hexagrams, and card meanings after download."
        AppLanguage.ZH_CN -> "无网络时提供基础离线推理能力。下载后可离线生成签文、解卦、牌义。"
    }

    fun settingsDownloadModel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Download Offline Model"
        AppLanguage.ZH_CN -> "下载离线模型"
    }

    fun settingsDeleteModel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Delete Model"
        AppLanguage.ZH_CN -> "删除模型"
    }

    fun settingsModelReady(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Ready"
        AppLanguage.ZH_CN -> "已就绪"
    }

    fun settingsRetry(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Retry"
        AppLanguage.ZH_CN -> "重试"
    }

    fun settingsDownloadFailed(lang: AppLanguage, message: String): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Download failed: $message"
        AppLanguage.ZH_CN -> "下载失败: $message"
    }

    fun settingsDownloading(lang: AppLanguage, percent: Int, downloaded: String, total: String): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Downloading $percent% · $downloaded / $total"
        AppLanguage.ZH_CN -> "下载中 $percent% · $downloaded / $total"
    }

    fun settingsSaveButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Save Configuration"
        AppLanguage.ZH_CN -> "保存配置"
    }

    fun settingsSavedIndicator(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Configuration saved"
        AppLanguage.ZH_CN -> "配置已保存"
    }

    fun settingsInfoText(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "API key is stored locally on device using Android DataStore.\n" +
            "For OpenAI-Compatible providers, enter your relay base URL."
        AppLanguage.ZH_CN -> "API 密钥通过 Android DataStore 存储在本地设备上。\n" +
            "对于 OpenAI 兼容服务商，请输入你的中转 URL。"
    }

    // ── Canonical Terms (for use in prompts and generated content) ────────

    /** Causal Ledger — archive/history feature */
    const val TERM_CAUSAL_LEDGER_EN = "Causal Ledger"
    const val TERM_CAUSAL_LEDGER_CN = "因果命簿"

    /** Causal Card — archive share card */
    const val TERM_CAUSAL_CARD_EN = "Causal Card"
    const val TERM_CAUSAL_CARD_CN = "因果卡片"

    /** Electronic Stub — receipt-style share artifact */
    const val TERM_ELECTRONIC_STUB_EN = "Electronic Stub"
    const val TERM_ELECTRONIC_STUB_CN = "电子存根"

    /** Oracle Verse — poem/verse section */
    const val TERM_ORACLE_VERSE_EN = "Oracle Verse"
    const val TERM_ORACLE_VERSE_CN = "签文"

    /** Logic Analysis — interpretation section */
    const val TERM_LOGIC_ANALYSIS_EN = "Logic Analysis"
    const val TERM_LOGIC_ANALYSIS_CN = "逻辑解析"

    /** Final Omen — final guidance section */
    const val TERM_FINAL_OMEN_EN = "Final Omen"
    const val TERM_FINAL_OMEN_CN = "最终断语"

    /** Hexagram — I Ching result */
    const val TERM_HEXAGRAM_EN = "Hexagram"
    const val TERM_HEXAGRAM_CN = "卦"

    /** Primary Hexagram — Liuyao result */
    const val TERM_PRIMARY_HEXAGRAM_EN = "Primary Hexagram"
    const val TERM_PRIMARY_HEXAGRAM_CN = "本卦"

    /** Changed Hexagram — Liuyao result */
    const val TERM_CHANGED_HEXAGRAM_EN = "Changed Hexagram"
    const val TERM_CHANGED_HEXAGRAM_CN = "变卦"

    /** Changing Line — Liuyao result */
    const val TERM_CHANGING_LINE_EN = "Changing Line"
    const val TERM_CHANGING_LINE_CN = "变爻"

    /** Judgment Text — I Ching */
    const val TERM_JUDGMENT_EN = "Judgment Text"
    const val TERM_JUDGMENT_CN = "卦辞"

    /** Image Text — I Ching */
    const val TERM_IMAGE_TEXT_EN = "Image Text"
    const val TERM_IMAGE_TEXT_CN = "象辞"

    /** Cast Hexagram — action */
    const val TERM_CAST_HEXAGRAM_EN = "Cast Hexagram"
    const val TERM_CAST_HEXAGRAM_CN = "起卦"

    /** Interpret Hexagram — action */
    const val TERM_INTERPRET_EN = "Interpret Hexagram"
    const val TERM_INTERPRET_CN = "解卦"

    /** Today's Practice — learning home */
    const val TERM_TODAY_PRACTICE_EN = "Today's Practice"
    const val TERM_TODAY_PRACTICE_CN = "今日修习"

    /** Causal Chain — loading states */
    const val TERM_CAUSAL_CHAIN_EN = "Causal Chain"
    const val TERM_CAUSAL_CHAIN_CN = "因果链"

    /** Quantum Causal Chain — loading states */
    const val TERM_QUANTUM_CHAIN_EN = "Quantum Causal Chain"
    const val TERM_QUANTUM_CHAIN_CN = "量子因果链"

    /**
     * Returns a bilingual canonical term pair: "English 中文"
     * Use for UI labels that should always show both languages.
     */
    fun bilingualTerm(en: String, cn: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "$en $cn"
        AppLanguage.ZH_CN -> cn
    }
}

/** CompositionLocal providing the current [AppLanguage] throughout the Compose tree. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.BILINGUAL_EN }
