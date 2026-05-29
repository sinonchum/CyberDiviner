package com.cyberdiviner.ui.learning

import com.cyberdiviner.data.model.learning.LearningProgressEntity

/**
 * Maps lesson IDs to their unlock annotations for display in divination result screens.
 * Each entry: lessonId -> (title, annotation text)
 */
object LearningAnnotations {

    private val annotations = mapOf(
        // Path A: Yijing
        "A1" to ("阴阳" to "阳为动、刚、明；阴为静、柔、暗。先判断事物偏扩张还是收束。"),
        "A2" to ("八卦" to "乾天坤地，震雷巽风，坎水离火，艮山兑泽。八卦两两相叠成六十四卦。"),
        "A3" to ("六十四卦" to "每卦由上卦和下卦组成，从下往上读六爻。下三爻为内卦，上三爻为外卦。"),
        "A4" to ("卦辞与象辞" to "卦辞给出整体基调，象辞描述自然意象。先抓大方向，不急着逐字玄解。"),
        "A5" to ("爻位" to "初爻潜藏、二爻居中、三爻多凶、四爻近君、五爻至尊、上爻极端。"),
        "A6" to ("动爻" to "动爻是变化焦点，变卦是趋势出口。动爻处阴阳转换，是事情转折的关键位置。"),

        // Path B: Liuyao
        "B1" to ("三钱法" to "正面3分背面2分，三枚合计6-9。6老阴、7少阳、8少阴、9老阳，老阴老阳为动爻。"),
        "B2" to ("本卦与变卦" to "本卦为当前格局，变卦为变化方向。动爻变后得变卦，看趋势出口。"),
        "B3" to ("世应" to "世爻代表自己，应爻代表对方或外部环境。世应之间的生克冲合是关系动态。"),
        "B4" to ("六亲" to "兄弟、父母、子孙、官鬼、妻财——分别对应竞争、文书、福德、阻碍、财运。"),
        "B5" to ("六神" to "青龙吉庆、朱雀口舌、勾陈田土、螣蛇惊恐、白虎凶伤、玄武暗昧。六神是事件气味，非吉凶结论。"),
        "B6" to ("断卦流程" to "问事→定用神→看旺衰→查世应→找动爻→综合六神变卦→得出结论。"),

        // Path C: Tarot
        "C1" to ("大阿卡纳" to "22张大牌描绘人生旅程：愚者（起点）→魔术师→女祭司→…→世界（完成）。"),
        "C2" to ("四元素" to "权杖＝火＝行动，圣杯＝水＝情感，宝剑＝风＝思维，星币＝土＝物质。"),
        "C3" to ("正逆位" to "逆位不等于坏牌。可能表示阻滞、内化、过度或延迟，需结合情境判断。"),
        "C4" to ("单牌解读" to "四步法：明确问题→识别牌面→提取关键词→给出建议方向。"),
        "C5" to ("三牌牌阵" to "过去—现在—未来。先看中间（现在）为锚点，再回溯过去，最后展望未来。"),
        "C6" to ("凯尔特十字" to "先看第1张（现状）和第2张（挑战）构成主轴，再看第5张和第10张了解来龙去脉。")
    )

    /**
     * Get annotations for completed lessons.
     * Returns list of (title, text) pairs for lessons the user has completed.
     */
    fun getForCompletedLessons(completedLessonIds: Set<String>): List<Pair<String, String>> {
        return completedLessonIds.mapNotNull { id -> annotations[id] }
    }

    /**
     * Get annotation for a specific lesson, if it exists.
     */
    fun getForLesson(lessonId: String): Pair<String, String>? = annotations[lessonId]

    /**
     * Check if a specific lesson is completed from progress list.
     */
    fun isLessonCompleted(progress: List<LearningProgressEntity>, lessonId: String): Boolean {
        return progress.any { it.lessonId == lessonId && it.completed }
    }
}
