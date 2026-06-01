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

    // ── Oracle Screen ───────────────────────────────────────────────────

    fun oracleTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Ask the Oracle"
        AppLanguage.ZH_CN -> "叩问天机"
    }

    fun oracleRound(lang: AppLanguage, current: Int, max: Int): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "ROUND $current/$max"
        AppLanguage.ZH_CN -> "ROUND $current/$max"
    }

    fun oracleComputing(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Divining"
        AppLanguage.ZH_CN -> "正在演算"
    }

    // ── Archive Screen ──────────────────────────────────────────────────

    fun archiveTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Causal Ledger"
        AppLanguage.ZH_CN -> "因果命簿"
    }

    fun archiveSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "CAUSAL LEDGER 因果命簿"
        AppLanguage.ZH_CN -> ""
    }

    fun archiveLearningReview(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "LEARNING REVIEW"
        AppLanguage.ZH_CN -> "学习复盘"
    }

    fun archiveDayStreak(lang: AppLanguage, days: Int): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "$days day streak"
        AppLanguage.ZH_CN -> "连续 $days 日"
    }

    fun archiveBestStreak(lang: AppLanguage, days: Int): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Best: $days days"
        AppLanguage.ZH_CN -> "最佳 $days 日"
    }

    fun archiveFullInterp(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "FULL INTERPRETATION"
        AppLanguage.ZH_CN -> "完整解读"
    }

    fun archiveNoInterp(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "No detailed interpretation available"
        AppLanguage.ZH_CN -> "暂无详细解读"
    }

    fun archiveShare(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "SHARE"
        AppLanguage.ZH_CN -> "分享"
    }

    fun archiveCausalCard(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Causal Card"
        AppLanguage.ZH_CN -> "因果卡片"
    }

    fun archiveSend(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Send"
        AppLanguage.ZH_CN -> "发送"
    }

    fun archiveSave(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Save"
        AppLanguage.ZH_CN -> "保存"
    }

    fun archiveCancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cancel"
        AppLanguage.ZH_CN -> "取消"
    }

    fun archiveSavedGallery(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Saved to gallery"
        AppLanguage.ZH_CN -> "已存入相册"
    }

    fun archiveSaveFailed(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Save failed"
        AppLanguage.ZH_CN -> "保存失败"
    }

    fun archiveDelete(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Delete"
        AppLanguage.ZH_CN -> "删除"
    }

    fun archiveEmpty(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "No Causal Records"
        AppLanguage.ZH_CN -> "因果链为空"
    }

    fun archiveIChingReading(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "I Ching Reading"
        AppLanguage.ZH_CN -> "六爻占卜"
    }

    fun archiveTarotReading(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Tarot Reading"
        AppLanguage.ZH_CN -> "塔罗占卜"
    }

    fun archiveFaceReading(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Face Reading"
        AppLanguage.ZH_CN -> "面相玄机"
    }

    fun archiveNoReading(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "No reading recorded"
        AppLanguage.ZH_CN -> "暂无解读"
    }

    // ── Liuyao Screen ───────────────────────────────────────────────────

    fun liuyaoTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cast Hexagram"
        AppLanguage.ZH_CN -> "周易起卦"
    }

    fun liuyaoSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Three-coin method · Six tosses"
        AppLanguage.ZH_CN -> "三钱法 · 六次演算"
    }

    fun liuyaoSincerity(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Sincerity brings clarity"
        AppLanguage.ZH_CN -> "心诚则灵"
    }

    fun liuyaoPrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Focus your mind, then enter your question"
        AppLanguage.ZH_CN -> "静心冥想，然后输入你的问题"
    }

    fun liuyaoPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "e.g., What does my career path look like?"
        AppLanguage.ZH_CN -> "例如：我的事业前景如何？"
    }

    fun liuyaoCast(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cast Hexagram"
        AppLanguage.ZH_CN -> "起卦"
    }

    fun liuyaoBack(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "[ Back ]"
        AppLanguage.ZH_CN -> "[ 返回 ]"
    }

    fun liuyaoShake(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Shake your phone"
        AppLanguage.ZH_CN -> "用力摇动手机"
    }

    fun liuyaoYoungYang(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Young Yang"
        AppLanguage.ZH_CN -> "少阳"
    }

    fun liuyaoYoungYin(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Young Yin"
        AppLanguage.ZH_CN -> "少阴"
    }

    fun liuyaoOldYang(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Old Yang (changing)"
        AppLanguage.ZH_CN -> "老阳"
    }

    fun liuyaoOldYin(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Old Yin (changing)"
        AppLanguage.ZH_CN -> "老阴"
    }

    fun liuyaoCastComplete(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Hexagram Cast"
        AppLanguage.ZH_CN -> "卦象已成"
    }

    fun liuyaoCastFailed(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cast Failed"
        AppLanguage.ZH_CN -> "起卦失败"
    }

    fun liuyaoRestart(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Start Over"
        AppLanguage.ZH_CN -> "重新开始"
    }

    fun liuyaoUnknownError(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Unknown error"
        AppLanguage.ZH_CN -> "未知错误"
    }

    fun liuyaoLoading(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Quantum Causal Chain processing..."
        AppLanguage.ZH_CN -> "量子因果链运算中..."
    }

    // ── Liuyao Result Screen ────────────────────────────────────────────

    fun liuyaoResultBack(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "< Back"
        AppLanguage.ZH_CN -> "< 返回"
    }

    fun liuyaoResultTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Hexagram Reading"
        AppLanguage.ZH_CN -> "卦象解读"
    }

    fun liuyaoSwipeNext(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "< Swipe left >"
        AppLanguage.ZH_CN -> "< 左滑翻页 >"
    }

    fun liuyaoSwipeBack(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "< Swipe right >"
        AppLanguage.ZH_CN -> "< 右滑返回 >"
    }

    fun liuyaoReturn(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Back"
        AppLanguage.ZH_CN -> "返回"
    }

    fun liuyaoRecast(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Recast Hexagram"
        AppLanguage.ZH_CN -> "重新起卦"
    }

    fun liuyaoCardFortune(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Fortune"
        AppLanguage.ZH_CN -> "批命"
    }

    fun liuyaoCardHexagram(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Hexagram"
        AppLanguage.ZH_CN -> "卦象"
    }

    fun liuyaoCardLines(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Lines"
        AppLanguage.ZH_CN -> "爻象"
    }

    fun liuyaoCardSpirits(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Spirits"
        AppLanguage.ZH_CN -> "六神"
    }

    fun liuyaoCardInterp(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Interpretation"
        AppLanguage.ZH_CN -> "解读"
    }

    fun liuyaoCardAnalysis(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Analysis"
        AppLanguage.ZH_CN -> "断卦"
    }

    fun liuyaoCardLearning(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Learning"
        AppLanguage.ZH_CN -> "学习"
    }

    fun liuyaoDefaultFortune(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Beyond mortal ken"
        AppLanguage.ZH_CN -> "天机莫测"
    }

    fun liuyaoDefaultMeaning(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "The hexagram is cast — quiet your mind to receive its wisdom"
        AppLanguage.ZH_CN -> "卦象已起，静心体悟天机"
    }

    fun liuyaoChangedHex(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Changed"
        AppLanguage.ZH_CN -> "变卦"
    }

    fun liuyaoHiddenSpirits(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Hidden Spirits"
        AppLanguage.ZH_CN -> "伏神"
    }

    fun liuyaoWorldLine(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "World"
        AppLanguage.ZH_CN -> "世爻"
    }

    fun liuyaoResponseLine(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Response"
        AppLanguage.ZH_CN -> "应爻"
    }

    fun liuyaoUsefulGod(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Useful God"
        AppLanguage.ZH_CN -> "用神"
    }

    fun liuyaoStrength(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Strength"
        AppLanguage.ZH_CN -> "旺衰"
    }

    fun liuyaoAdvice(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Strategic Counsel"
        AppLanguage.ZH_CN -> "进退之策"
    }

    fun liuyaoGuidance(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Guidance"
        AppLanguage.ZH_CN -> "指点迷津"
    }

    fun liuyaoObtained(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Obtained"
        AppLanguage.ZH_CN -> "已得"
    }

    fun liuyaoHexLabel(lang: AppLanguage, num: Int, engName: String): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "No.$num $engName"
        AppLanguage.ZH_CN -> "第${num}卦 $engName"
    }

    fun liuyaoLineLabel(lang: AppLanguage, num: Int): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Line $num"
        AppLanguage.ZH_CN -> "${num}爻"
    }

    fun liuyaoQuestionSummary(lang: AppLanguage, hexName: String, engName: String): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Your question yields the $hexName hexagram ($engName)"
        AppLanguage.ZH_CN -> "你所问之事，得${hexName}卦（${engName}）"
    }

    fun liuyaoChangedSummary(lang: AppLanguage, hexName: String, engName: String): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Changing lines transform into $hexName ($engName), indicating a shift."
        AppLanguage.ZH_CN -> "动爻变${hexName}卦（${engName}），主变化转化之势。"
    }

    fun liuyaoHeaderLine(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Line"
        AppLanguage.ZH_CN -> "爻"
    }

    fun liuyaoHeaderBranch(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Branch"
        AppLanguage.ZH_CN -> "地支"
    }

    fun liuyaoHeaderRelation(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Relation"
        AppLanguage.ZH_CN -> "六亲"
    }

    fun liuyaoHeaderElement(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Element"
        AppLanguage.ZH_CN -> "五行"
    }

    fun liuyaoHeaderSY(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "S/Y"
        AppLanguage.ZH_CN -> "世应"
    }

    // ── Tarot Screen ────────────────────────────────────────────────────

    fun tarotTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Cyber Tarot"
        AppLanguage.ZH_CN -> "赛博塔罗"
    }

    fun tarotDrawCards(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Draw Cards"
        AppLanguage.ZH_CN -> "抽牌"
    }

    fun tarotAskQuestion(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Ask your question..."
        AppLanguage.ZH_CN -> "输入你的问题..."
    }

    fun tarotInterpreting(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Interpreting..."
        AppLanguage.ZH_CN -> "正在解牌..."
    }

    fun tarotBack(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "[ Back ]"
        AppLanguage.ZH_CN -> "[ 返回 ]"
    }

    fun tarotSpreadPast(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Past"
        AppLanguage.ZH_CN -> "过去"
    }

    fun tarotSpreadPresent(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Present"
        AppLanguage.ZH_CN -> "现在"
    }

    fun tarotSpreadFuture(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Future"
        AppLanguage.ZH_CN -> "未来"
    }

    fun tarotCardUpright(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Upright"
        AppLanguage.ZH_CN -> "正位"
    }

    fun tarotCardReversed(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "Reversed"
        AppLanguage.ZH_CN -> "逆位"
    }

    fun tarotSignalInsight(lang: AppLanguage): String = when (lang) {
        AppLanguage.BILINGUAL_EN -> "SIGNAL INSIGHT"
        AppLanguage.ZH_CN -> "信号启示"
    }

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
