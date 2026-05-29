package com.cyberdiviner.engine.learning

import com.cyberdiviner.data.model.learning.*

object LessonCatalogYijing {

    val lessons: List<Lesson> = listOf(

        // ── A1: 阴阳 ──────────────────────────────────────────────────
        Lesson(
            id = "A1",
            pathId = "yijing_intro",
            order = 1,
            title = "阴阳",
            subtitle = "万物皆有两面——周易的最基本语言",
            concept = "阴阳",
            explanation = "阴（⚋）与阳（⚊）是构成一切卦象的基本单位。阳代表刚健、积极、明亮；阴代表柔顺、收敛、晦暗。两者不是对立的善恶，而是相互依存、此消彼长的动态关系。理解阴阳，是读懂六十四卦的前提。",
            howToRead = listOf(
                "先看符号：一条完整横线为阳爻（⚊），中间断开的横线为阴爻（⚋）",
                "再看性质：阳主动、刚、明；阴主静、柔、暗——但两者同等重要",
                "最后看关系：阴阳交替出现才构成变化，纯阴纯阳只是静止状态"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "A1_Q1",
                    type = QuestionType.BINARY_CLASSIFY,
                    prompt = "将下列现象归类为「阳」或「阴」",
                    items = listOf(
                        MatchItem("正午的太阳", "阳"),
                        MatchItem("深夜的月光", "阴"),
                        MatchItem("奔跑冲刺", "阳"),
                        MatchItem("静坐冥想", "阴")
                    ),
                    explanation = "阳的特征是明亮、主动、外放；阴的特征是晦暗、静止、内收。太阳、冲刺属于阳；月光、冥想属于阴。"
                ),
                QuizQuestion(
                    id = "A1_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "关于阴阳关系，以下哪种理解最准确？",
                    options = listOf(
                        "A. 阳是好的，阴是不好的",
                        "B. 阴阳是相互依存、动态转化的两面，缺一不可",
                        "C. 阴阳只存在于卦象中，与生活无关",
                        "D. 阳永远大于阴"
                    ),
                    correctIndex = 1,
                    explanation = "阴阳不是善恶之分，而是描述事物两面性的基本范畴。白天与黑夜、运动与休息都是阴阳的体现，两者相互依存、缺一不可。"
                ),
                QuizQuestion(
                    id = "A1_Q3",
                    type = QuestionType.BINARY_CLASSIFY,
                    prompt = "判断以下爻画是「阳爻」还是「阴爻」",
                    items = listOf(
                        MatchItem("━━━━━━━", "阳爻"),
                        MatchItem("━━  ━━", "阴爻"),
                        MatchItem("━━━━━━━", "阳爻"),
                        MatchItem("━━  ━━", "阴爻")
                    ),
                    explanation = "阳爻是一条完整的横线，阴爻是中间断开的横线。六十四卦中的每一爻都是这两种符号之一。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "yijing_yinyang", "解锁阴阳概念标注")
        ),

        // ── A2: 八卦 ──────────────────────────────────────────────────
        Lesson(
            id = "A2",
            pathId = "yijing_intro",
            order = 2,
            title = "八卦",
            subtitle = "三爻成卦——八种基本自然力量",
            concept = "八卦",
            explanation = "八卦由三爻组成，代表八种基本自然意象：乾（天）、坤（地）、震（雷）、巽（风）、坎（水）、离（火）、艮（山）、兑（泽）。每卦有特定的五行属性、方位和家庭角色。八卦两两相叠，构成六十四卦。",
            howToRead = listOf(
                "先看三爻结构：从下往上读，初爻、中爻、上爻分别对应地、人、天",
                "再看自然意象：每个卦对应一种自然力量，如乾为天、坤为地",
                "最后看组合：两个八卦上下相叠，就构成一个六爻的完整卦象"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "A2_Q1",
                    type = QuestionType.MATCHING,
                    prompt = "将下列八卦与其代表的自然意象配对",
                    items = listOf(
                        MatchItem("乾", "天"),
                        MatchItem("坤", "地"),
                        MatchItem("坎", "水"),
                        MatchItem("离", "火")
                    ),
                    explanation = "乾为天、坤为地、坎为水、离为火。这四卦是八卦中最基本的两对：天地定位，水火既济。"
                ),
                QuizQuestion(
                    id = "A2_Q2",
                    type = QuestionType.MATCHING,
                    prompt = "将下列八卦与其五行属性配对",
                    items = listOf(
                        MatchItem("乾", "金"),
                        MatchItem("坤", "土"),
                        MatchItem("震", "木"),
                        MatchItem("坎", "水"),
                        MatchItem("离", "火")
                    ),
                    explanation = "乾兑属金、坤艮属土、震巽属木、坎属水、离属火。五行属性是六爻断卦的基础。"
                ),
                QuizQuestion(
                    id = "A2_Q3",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "八卦由几爻组成？",
                    options = listOf(
                        "A. 二爻",
                        "B. 三爻",
                        "C. 六爻",
                        "D. 八爻"
                    ),
                    correctIndex = 1,
                    explanation = "八卦（经卦）由三爻组成，两个八卦上下相叠才构成六爻卦（别卦）。三爻是八卦的基本结构。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "yijing_bagua", "解锁八卦意象标注")
        ),

        // ── A3: 六十四卦 ──────────────────────────────────────────────
        Lesson(
            id = "A3",
            pathId = "yijing_intro",
            order = 3,
            title = "六十四卦",
            subtitle = "八卦相叠——64种情境的完整体系",
            concept = "六十四卦",
            explanation = "六十四卦由八卦两两相叠而成：上卦（外卦）与下卦（内卦）各取一卦，8×8=64。每卦六爻，代表一种特定的情境或发展阶段。如乾为天（乾上乾下）、地天泰（坤上乾下）等。六十四卦是周易的核心符号体系。",
            howToRead = listOf(
                "先分上下卦：下面三爻为内卦（下卦），上面三爻为外卦（上卦）",
                "再识别卦名：根据上下卦的组合确定卦名，如上坎下乾为「需」卦",
                "最后看卦序：卦序反映事物发展的逻辑，也可按序号快速查找"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "A3_Q1",
                    type = QuestionType.MATCHING,
                    prompt = "将下列卦名与其上下卦组合配对",
                    items = listOf(
                        MatchItem("天地否", "乾上坤下"),
                        MatchItem("地天泰", "坤上乾下"),
                        MatchItem("水火既济", "坎上离下"),
                        MatchItem("火水未济", "离上坎下")
                    ),
                    explanation = "否卦乾上坤下，天气上升地气下沉，不交为否；泰卦坤上乾下，地气下降天气上升，交合为泰。既济与未济互为综卦。"
                ),
                QuizQuestion(
                    id = "A3_Q2",
                    type = QuestionType.ORDERING,
                    prompt = "将六爻从下到上的正确读取顺序排列",
                    items = listOf(
                        MatchItem("初爻（第一爻）", "1"),
                        MatchItem("二爻", "2"),
                        MatchItem("三爻", "3"),
                        MatchItem("四爻", "4"),
                        MatchItem("五爻", "5"),
                        MatchItem("上爻（第六爻）", "6")
                    ),
                    explanation = "六爻从下往上读：初、二、三、四、五、上。最下方为初爻，最上方为上爻。下三爻为内卦，上三爻为外卦。"
                ),
                QuizQuestion(
                    id = "A3_Q3",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "六十四卦是如何构成的？",
                    options = listOf(
                        "A. 阴阳两爻随机排列",
                        "B. 八卦两两相叠，8×8=64",
                        "C. 古代圣人凭空创造",
                        "D. 六爻按顺序排列所有可能"
                    ),
                    correctIndex = 1,
                    explanation = "六十四卦由八个经卦两两上下相叠而成：上卦取一个八卦，下卦取一个八卦，8×8正好64种组合。这是周易符号体系的完整结构。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "yijing_64gua", "解锁六十四卦结构标注")
        ),

        // ── A4: 卦辞与象辞 ────────────────────────────────────────────
        Lesson(
            id = "A4",
            pathId = "yijing_intro",
            order = 4,
            title = "卦辞与象辞",
            subtitle = "每卦的总纲——理解卦的整体气质",
            concept = "卦辞象辞",
            explanation = "卦辞是对一卦整体情境的简短判断，如「乾：元亨利贞」。象辞（大象）从上下卦的自然意象引申出人生启示，如天行健，君子以自强不息。卦辞告诉你这卦讲什么，象辞告诉你该怎么做。两者结合才能把握一卦的精神。",
            howToRead = listOf(
                "先读卦辞：了解这一卦的核心判断——是吉、是凶、还是有转折",
                "再读象辞：看上下卦的自然意象如何转化为行为指导",
                "最后反思：卦辞和象辞如何与你当下的处境产生关联"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "A4_Q1",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "「乾：元亨利贞」这句卦辞的正确理解是？",
                    options = listOf(
                        "A. 乾卦代表一切顺利，不需要努力",
                        "B. 乾卦象征天道运行，具有创始、亨通、适宜、正固四种品质",
                        "C. 乾卦只能问天气",
                        "D. 元亨利贞是四个不同的卦名"
                    ),
                    correctIndex = 1,
                    explanation = "「元亨利贞」是乾卦的卦辞，传统上理解为四种品质：元（创始）、亨（亨通）、利（适宜）、贞（正固），描述天道运行的完整特质。"
                ),
                QuizQuestion(
                    id = "A4_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "卦辞与象辞的主要区别是什么？",
                    options = listOf(
                        "A. 卦辞讲吉凶判断，象辞讲自然意象与行为启示",
                        "B. 卦辞比象辞更重要",
                        "C. 象辞是后人伪造的",
                        "D. 两者完全相同"
                    ),
                    correctIndex = 0,
                    explanation = "卦辞是对一卦整体的简短判断，象辞则从上下卦的自然意象出发，引申出人生启示和行动指导。两者视角不同，互相补充。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "yijing_guaci", "解锁卦辞象辞标注")
        ),

        // ── A5: 爻位 ─────────────────────────────────────────────────
        Lesson(
            id = "A5",
            pathId = "yijing_intro",
            order = 5,
            title = "爻位",
            subtitle = "六个位置各有含义——初至上的人生阶梯",
            concept = "爻位",
            explanation = "六爻从下到上分别处于不同位置，各有象征：初爻为潜藏、二爻为显现、三爻为危境、四爻为近君、五爻为至尊、上爻为极端。阳爻居阳位（初、三、五）、阴爻居阴位（二、四、上）为「当位」，反之为「不当位」。",
            howToRead = listOf(
                "先看位置：初爻潜藏、二爻居中、三爻危险、四爻近权、五爻至尊、上爻极端",
                "再看当位：阳爻在初、三、五位为当位；阴爻在二、四、上位为当位",
                "最后看关系：相邻爻之间有比应关系，初与四、二与五、三与上有应"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "A5_Q1",
                    type = QuestionType.ORDERING,
                    prompt = "将下列爻位从低到高按正确位置排列",
                    items = listOf(
                        MatchItem("初爻：事物萌发，潜藏待机", "1"),
                        MatchItem("二爻：崭露头角，居中得正", "2"),
                        MatchItem("三爻：多凶之位，进退两难", "3"),
                        MatchItem("四爻：近君之位，谨慎行事", "4"),
                        MatchItem("五爻：至尊之位，居中履正", "5"),
                        MatchItem("上爻：穷极之地，物极必反", "6")
                    ),
                    explanation = "六爻位置从下到上，对应事物发展的六个阶段。初爻潜藏、二爻居中得正、三爻多凶、四爻近君、五爻至尊、上爻极端。"
                ),
                QuizQuestion(
                    id = "A5_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "以下关于「当位」的说法，哪个正确？",
                    options = listOf(
                        "A. 阳爻在任何位置都算当位",
                        "B. 阳爻居奇数位（初、三、五）为当位，阴爻居偶数位（二、四、上）为当位",
                        "C. 只有五爻才算当位",
                        "D. 当位与爻的阴阳无关"
                    ),
                    correctIndex = 1,
                    explanation = "当位是爻位分析的基本规则：阳爻在奇数位（初、三、五）、阴爻在偶数位（二、四、上）为当位，代表处事得宜。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "yijing_yaowei", "解锁爻位阶段标注")
        ),

        // ── A6: 动爻 ─────────────────────────────────────────────────
        Lesson(
            id = "A6",
            pathId = "yijing_intro",
            order = 6,
            title = "动爻",
            subtitle = "变化的起点——爻的阴阳转换",
            concept = "动爻",
            explanation = "动爻是卦中发生变化的爻。当一爻为「老阳」（阳极将变阴）或「老阴」（阴极将变阳）时，该爻就会变动。动爻将本卦变为变卦，是占卜中判断吉凶和趋势的关键。老阳记为○，老阴记为×。一卦中可有一到多个动爻。",
            howToRead = listOf(
                "先辨动爻：看哪几爻标记为○（老阳）或×（老阴），这些就是动爻",
                "再看变化：动爻阴阳互变后，得到的新卦就是变卦",
                "最后综合：本卦代表现状，变卦代表趋势，动爻是变化的关键节点"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "A6_Q1",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "当一个爻位出现「老阳」时，会发生什么？",
                    options = listOf(
                        "A. 保持不变",
                        "B. 该爻由阳变阴，本卦变为变卦",
                        "C. 该爻消失",
                        "D. 整卦无效，需要重新占卜"
                    ),
                    correctIndex = 1,
                    explanation = "老阳（○）表示阳已极盛，将转为阴。这是物极必反的原理——阳极生阴，阴极生阳。动爻变化后产生变卦，揭示事物发展的趋势。"
                ),
                QuizQuestion(
                    id = "A6_Q2",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小林占得一卦，初爻为老阳（○），其余五爻不变。本卦初爻是阳爻，变卦初爻应为什么？",
                    options = listOf(
                        "A. 仍是阳爻",
                        "B. 变为阴爻",
                        "C. 变为老阴",
                        "D. 初爻被删除"
                    ),
                    correctIndex = 1,
                    explanation = "老阳（○）意味阳极将变阴，所以动爻变化后，初爻由阳爻变为阴爻。变卦就是本卦中所有动爻阴阳互变后得到的新卦。"
                ),
                QuizQuestion(
                    id = "A6_Q3",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "一卦中出现了三个动爻（二爻、四爻、上爻为老阳），断卦时应如何处理？",
                    options = listOf(
                        "A. 只看本卦，忽略变卦",
                        "B. 三个动爻都变化后得到变卦，综合本卦与变卦来判断趋势",
                        "C. 动爻太多，此卦作废",
                        "D. 只看第一个动爻"
                    ),
                    correctIndex = 1,
                    explanation = "多个动爻时，所有动爻同时变化产生变卦。断卦需综合本卦（现状）与变卦（趋势），动爻越多，事情变化越复杂，需要更仔细分析。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "liuyao_changing_lines",
                "解锁六爻动爻术语标注"
            )
        )
    )
}
