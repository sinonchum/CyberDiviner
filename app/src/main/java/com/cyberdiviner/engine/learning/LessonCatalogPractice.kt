package com.cyberdiviner.engine.learning

import com.cyberdiviner.data.model.learning.*

object LessonCatalogPractice {

    val lessons: List<Lesson> = listOf(

        // ── D1: 好问题 ────────────────────────────────────────────────
        Lesson(
            id = "D1",
            pathId = "practice",
            order = 1,
            title = "好问题",
            subtitle = "问对问题，卦才有意义",
            concept = "提问技巧",
            explanation = "好的占卜问题应该是具体的、开放的、与自身行动相关的。避免问「会不会」的封闭式问题，改为「我需要注意什么」。避免问超大问题（如命运），聚焦具体情境。好问题让卦象有明确的解读方向，坏问题让卦象模糊无从下手。",
            howToRead = listOf(
                "先检查问题：是否具体？是否开放？是否与自身行动相关？",
                "再调整措辞：把「会不会成功」改为「成功需要什么条件」",
                "最后确认：这个问题的答案能帮你做出实际决定吗"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "D1_Q1",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "以下哪个问题最适合用六爻来问？",
                    options = listOf(
                        "A. 我的命运好不好？",
                        "B. 我下一期彩票号码是多少？",
                        "C. 目前这份工作中，我应该优先提升哪方面的能力？",
                        "D. 我什么时候会死？"
                    ),
                    correctIndex = 2,
                    explanation = "好的占卜问题具体、开放、与行动相关。C选项聚焦具体情境，答案可以转化为实际行动。命运、彩票、死亡这类问题超出合理范围。"
                ),
                QuizQuestion(
                    id = "D1_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "「我能不能和他在一起」这个问题存在什么问题？",
                    options = listOf(
                        "A. 没有任何问题",
                        "B. 这是封闭式问题，建议改为「这段关系中我需要注意什么」以获得更有价值的指引",
                        "C. 太简单了，不需要占卜",
                        "D. 涉及他人隐私，不能问"
                    ),
                    correctIndex = 1,
                    explanation = "封闭式问题（能不能、会不会）只能得到是或否，信息量有限。改为开放式的「需要注意什么」或「如何改善」，能让卦象给出更有操作性的指引。"
                ),
                QuizQuestion(
                    id = "D1_Q3",
                    type = QuestionType.BINARY_CLASSIFY,
                    prompt = "判断以下问题是「好问题」还是「待改善」",
                    items = listOf(
                        MatchItem("这个项目的风险点在哪里？", "好问题"),
                        MatchItem("我今年会不会发财？", "待改善"),
                        MatchItem("跳槽前我需要准备什么？", "好问题"),
                        MatchItem("他是不是我的真命天子？", "待改善")
                    ),
                    explanation = "好问题聚焦具体情境和行动方向；待改善的问题过于封闭或绝对化，难以从卦象中获得有用的行动指引。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "practice_questions", "解锁提问技巧标注")
        ),

        // ── D2: 先看结构 ──────────────────────────────────────────────
        Lesson(
            id = "D2",
            pathId = "practice",
            order = 2,
            title = "先看结构",
            subtitle = "不急着解爻——先把握全局格局",
            concept = "结构分析",
            explanation = "拿到一个卦，不要急于逐爻分析。先看大局：本卦是什么卦？卦辞传递什么整体基调？世应关系如何？有几个动爻？变卦是什么？这些结构性信息决定了后续分析的框架。先有全局观，再深入细节，避免见树不见林。",
            howToRead = listOf(
                "先看卦名和卦辞：这卦整体讲什么？基调是吉、凶还是有转折？",
                "再看世应和动爻：世应关系如何？有几爻在动？变化大不大？",
                "最后再逐爻分析：在全局框架下，再展开六亲、六神的细节解读"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "D2_Q1",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小张问工作调动，摇得「地天泰」卦，三爻动，变卦「地泽临」。拿到这个卦，第一步应该做什么？",
                    options = listOf(
                        "A. 直接看三爻的爻辞",
                        "B. 先看整体结构：泰卦象征通泰，基调积极；三爻有变化，变卦临卦提示临近、亲临",
                        "C. 直接看六神判断吉凶",
                        "D. 只看变卦就好"
                    ),
                    correctIndex = 1,
                    explanation = "拿到卦后应先看整体结构。泰卦本身是吉卦，象征上下交泰；变卦临卦提示亲近、临近的趋势。有了全局基调，再分析三爻细节才有方向。"
                ),
                QuizQuestion(
                    id = "D2_Q2",
                    type = QuestionType.ORDERING,
                    prompt = "拿到一个完整的六爻卦象，以下分析步骤的正确顺序是什么？",
                    items = listOf(
                        MatchItem("看卦名和卦辞，把握整体基调", "1"),
                        MatchItem("确定世爻和应爻的位置", "2"),
                        MatchItem("找动爻，看变卦", "3"),
                        MatchItem("分析用神旺衰和六亲关系", "4"),
                        MatchItem("综合六神和变卦给出结论", "5")
                    ),
                    explanation = "从全局到细节：先看卦名定基调，世应定主客，动爻看变化，再深入六亲六神，最后综合结论。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "practice_structure", "解锁结构化阅读标注")
        ),

        // ── D3: 情感问题 ──────────────────────────────────────────────
        Lesson(
            id = "D3",
            pathId = "practice",
            order = 3,
            title = "情感问题",
            subtitle = "感情卦怎么看——世应关系是关键",
            concept = "情感断卦",
            explanation = "感情类问题以世爻为问卦者，应爻为对方。看世应的生克关系判断互动质量：相生则和谐，相克则有矛盾。男问感情看妻财爻（女友/妻子），女问感情看官鬼爻（男友/丈夫）。用神旺相且与世爻相生，感情顺遂；用神受克或与世爻相冲，需警惕。",
            howToRead = listOf(
                "先定世应：世爻是你，应爻是对方，看两者五行生克关系",
                "再看用神：男看妻财、女看官鬼，用神旺相则感情基础好",
                "最后看动爻：动爻变化是否对用神有利，判断感情发展趋势"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "D3_Q1",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小雅（女生）问与男友的感情发展，世爻属木，应爻属火，官鬼爻旺相且与世爻相生。这意味着什么？",
                    options = listOf(
                        "A. 感情基础好，双方互动积极，发展趋势向好",
                        "B. 注定分手",
                        "C. 对方有外遇",
                        "D. 小雅不该问这个问题"
                    ),
                    correctIndex = 0,
                    explanation = "世应相生说明双方关系和谐；官鬼（男友）旺相且与世爻相生，表示对方对这段感情投入且关系稳固。综合来看是积极的信号。"
                ),
                QuizQuestion(
                    id = "D3_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "女生问感情时，应该以哪个六亲为主要参考？",
                    options = listOf(
                        "A. 妻财爻",
                        "B. 官鬼爻",
                        "C. 兄弟爻",
                        "D. 父母爻"
                    ),
                    correctIndex = 1,
                    explanation = "传统六爻中，女生问感情以官鬼爻为用神，代表男友或丈夫。男生问感情则以妻财爻为用神。用神的选择取决于性别和所问对象。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "practice_emotion", "解锁情感问题标注")
        ),

        // ── D4: 事业问题 ──────────────────────────────────────────────
        Lesson(
            id = "D4",
            pathId = "practice",
            order = 4,
            title = "事业问题",
            subtitle = "工作和财运——分清官鬼与妻财",
            concept = "事业断卦",
            explanation = "事业类问题分两种：问升迁、工作变动看官鬼爻；问收入、利润看妻财爻。两者不能混淆。问「这份工作好不好」是综合问题，既看官鬼（职位）也看妻财（收入），但要以世爻为核心判断自身状态。官鬼旺则事业有压力也有机会，妻财旺则进财有望。",
            howToRead = listOf(
                "先分清问题类型：是问职位（官鬼）还是问收入（妻财）",
                "再看用神旺衰：用神得月建日辰生扶为旺，受克为衰",
                "最后看用神与世爻关系：用神生世爻则利己，用神克世爻则有压力"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "D4_Q1",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小李问「今年能不能升职」，摇卦后官鬼爻旺相，但与世爻相克。怎么解读？",
                    options = listOf(
                        "A. 官鬼旺相说明有机会升职，但与世爻相克意味着过程有压力或竞争，需要更多努力",
                        "B. 一定会升职",
                        "C. 注定失败",
                        "D. 世爻不重要"
                    ),
                    correctIndex = 0,
                    explanation = "官鬼旺相说明升迁的机会存在；但与世爻相克暗示过程不轻松——可能有竞争、压力或挑战。有机会但需要付出更多努力去争取。"
                ),
                QuizQuestion(
                    id = "D4_Q2",
                    type = QuestionType.MATCHING,
                    prompt = "将下列事业问题与对应的用神配对",
                    items = listOf(
                        MatchItem("能不能升职", "官鬼"),
                        MatchItem("今年收入如何", "妻财"),
                        MatchItem("想跳槽换工作", "官鬼"),
                        MatchItem("投资能不能赚钱", "妻财")
                    ),
                    explanation = "职位变动、升迁、工作机会看官鬼爻；收入、利润、投资回报看妻财爻。分清问题类型才能准确取用神。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "practice_career", "解锁事业问题标注")
        ),

        // ── D5: 决策问题 ──────────────────────────────────────────────
        Lesson(
            id = "D5",
            pathId = "practice",
            order = 5,
            title = "决策问题",
            subtitle = "A还是B——怎么用卦辅助决策",
            concept = "决策断卦",
            explanation = "面临选择时，六爻提供的是思考框架而非简单答案。如果问「该不该做」，看用神与世爻关系；如果问「A还是B」，可以分别起两个卦对应两个选项。关键不是卦告诉你选什么，而是卦帮你看到每个选项的利弊和风险，最终决定权在你自己。",
            howToRead = listOf(
                "先明确选项：你的选择是什么？每个选项的核心关切是什么？",
                "再分别分析：一个卦对应一个选项，看用神旺衰和世应关系",
                "最后权衡：卦象帮你看清每个选项的利弊，决定仍由你来做"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "D5_Q1",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小陈面临两个工作机会：A公司稳定但薪资一般，B公司高薪但不确定。他分别摇卦，A的卦象妻财旺但官鬼衰，B的卦象官鬼旺但妻财受克。怎么理解？",
                    options = listOf(
                        "A. A收入稳定但发展空间有限；B有职位提升但收入有风险。根据自己的优先级选择",
                        "B. 两个都不好，继续找",
                        "C. B一定比A好",
                        "D. 卦象矛盾，无法判断"
                    ),
                    correctIndex = 0,
                    explanation = "A卦妻财旺=收入有保障，官鬼衰=职位发展空间有限。B卦官鬼旺=有晋升机会，妻财受克=收入可能不如预期。卦帮你看到各自利弊，选择取决于你的优先级。"
                ),
                QuizQuestion(
                    id = "D5_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "面对A还是B的选择，六爻的作用是什么？",
                    options = listOf(
                        "A. 直接告诉你选A还是选B",
                        "B. 帮你看清每个选项的利弊和风险，最终决定权在你自己",
                        "C. 替你做决定",
                        "D. 只能问「该不该做」，不能问「选哪个」"
                    ),
                    correctIndex = 1,
                    explanation = "六爻提供的是分析框架和决策参考，帮你看到各选项的潜在优势和风险。它不替代你的判断力，而是增强你的判断力。"
                )
            ),
            unlockReward = UnlockReward(UnlockType.TERM_ANNOTATION, "practice_decision", "解锁决策问题标注")
        ),

        // ── D6: 复盘 ─────────────────────────────────────────────────
        Lesson(
            id = "D6",
            pathId = "practice",
            order = 6,
            title = "复盘",
            subtitle = "回头看——从过往卦例中学习",
            concept = "复盘",
            explanation = "复盘是提升断卦能力最有效的方法。定期回顾之前的卦例：当时的判断对不对？忽略了什么信息？哪些因素后来被证实是关键？通过记录和反思，你会发现自己的断卦盲点和擅长领域。建议每次断卦后记录卦象和判断，三个月后回头检验。",
            howToRead = listOf(
                "先找回旧记录：打开存档，找到之前的卦例和当时的判断",
                "再对照实际：后来事情的实际发展与卦象是否吻合？哪里判断准了，哪里偏差了",
                "最后总结规律：积累自己的断卦经验，找出常见盲点和改进方向"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "D6_Q1",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "复盘对断卦能力的提升有什么帮助？",
                    options = listOf(
                        "A. 没有帮助，卦是玄学，无法总结规律",
                        "B. 通过回顾实际结果与卦象的对照，发现自己的判断盲点和改进方向",
                        "C. 复盘只是为了证明卦有多准",
                        "D. 只需要复盘准的卦，不准的忽略"
                    ),
                    correctIndex = 1,
                    explanation = "复盘的核心价值在于对照实际结果，发现判断中的盲点和偏差。准和不准的卦都要回顾——不准的卦往往更有学习价值。"
                ),
                QuizQuestion(
                    id = "D6_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "以下哪种复盘习惯最合理？",
                    options = listOf(
                        "A. 只复盘断错的卦",
                        "B. 定期回顾所有卦例，对照实际结果，总结判断中的亮点和不足",
                        "C. 从不复盘，摇完就算了",
                        "D. 只复盘断对的卦来增强信心"
                    ),
                    correctIndex = 1,
                    explanation = "有效复盘需要系统性：对的和错的卦都要回顾。对的巩固信心，错的发现盲点。建议养成记录习惯，定期回顾总结。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "archive_review",
                "解锁存档复盘功能标注"
            )
        )
    )
}
