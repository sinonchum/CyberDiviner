package com.cyberdiviner.engine.learning

import com.cyberdiviner.data.model.learning.*

object LessonCatalogTarot {

    val lessons: List<Lesson> = listOf(

        // ── C1: 大阿卡纳：人生主线 ─────────────────────────────────────
        Lesson(
            id = "C1",
            pathId = "tarot_intro",
            order = 1,
            title = "大阿卡纳：人生主线",
            subtitle = "22张大牌看人生阶段和核心主题",
            concept = "大阿卡纳",
            explanation = "大阿卡纳共22张，从0号愚者到21号世界，构成一条象征性的生命旅程。每张牌代表一个人生主题——如初生、选择、冲突、转变与完成。它们不是预言，而是一套描述成长阶段的符号语言。",
            howToRead = listOf(
                "先看牌面编号：编号越小越靠近旅程起点，越大越趋向成熟与整合",
                "再看核心意象：牌面的主要人物或场景提示该阶段的核心议题",
                "最后连结问题：这张牌的主题与你的问题情境有何对应"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "C1_Q1",
                    type = QuizType.MATCHING,
                    prompt = "将下列大阿卡纳牌与其代表的核心主题配对：愚者、死神、女祭司、战车",
                    options = listOf(
                        "愚者 → 新开始与未知",
                        "死神 → 结束与转化",
                        "女祭司 → 直觉与内在智慧",
                        "战车 → 意志力与前进"
                    ),
                    correctAnswerIds = listOf("愚者 → 新开始与未知", "死神 → 结束与转化", "女祭司 → 直觉与内在智慧", "战车 → 意志力与前进"),
                    explanationCorrect = "配对全部正确。愚者象征旅程的起点与可能性；死神代表旧阶段的终结与转化；女祭司掌管内在知识与直觉；战车则对应意志驱动的行动。",
                    explanationWrong = "请回顾每张大阿卡纳的核心主题。注意编号位置：愚者在起点（0号），死神在旅程后段（13号），这与它们的象征意义对应。"
                ),
                QuizQuestion(
                    id = "C1_Q2",
                    type = QuizType.SINGLE_CHOICE,
                    prompt = "大阿卡纳从愚者到世界的排列，最准确的理解是什么？",
                    options = listOf(
                        "A. 22种好坏运势的轮替",
                        "B. 一段象征性的人生旅程与成长阶段",
                        "C. 22个神灵的等级排序",
                        "D. 占卜准确率的依据"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。大阿卡纳描绘的是人生原型阶段的象征性旅程，从无知到整合，而非运势预测或神灵信仰。",
                    explanationWrong = "大阿卡纳不是运势轮替或神灵排序，而是一套描述人生成长阶段的象征语言。从愚者（起点）到世界（完成），体现的是一个完整的成长弧线。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "tarot_major_arcana",
                "解锁塔罗大阿卡纳主题标注"
            )
        ),

        // ── C2: 四元素小阿卡纳 ─────────────────────────────────────────
        Lesson(
            id = "C2",
            pathId = "tarot_intro",
            order = 2,
            title = "四元素小阿卡纳",
            subtitle = "权杖·圣杯·宝剑·星币对应四大生活领域",
            concept = "四元素",
            explanation = "小阿卡纳共56张，分为四组花色，每组对应一个元素和生活领域：权杖＝火＝行动与动力；圣杯＝水＝情感与关系；宝剑＝风＝思维与决策；星币＝土＝物质与实际事务。花色告诉你问题属于哪个领域，数字则描述该领域中的发展阶段。",
            howToRead = listOf(
                "识别花色：确定这张牌对应的是行动、情感、思维还是物质层面",
                "理解数字含义：1到10代表从萌芽到完成的阶段，宫廷牌代表人格面向",
                "结合问题领域：问事业时抽到圣杯，重点可能在人际关系而非业务本身"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "C2_Q1",
                    type = QuizType.BINARY_CLASSIFY,
                    prompt = "判断下列情境最接近哪个元素（火＝行动、水＝情感、风＝思维、土＝物质）",
                    options = listOf(
                        "决定是否跳槽到新公司 → 风/思维",
                        "和伴侣讨论未来的方向 → 水/情感",
                        "制定下季度的储蓄计划 → 土/物质",
                        "开始一个新的健身习惯 → 火/行动"
                    ),
                    correctAnswerIds = listOf(
                        "决定是否跳槽到新公司 → 风/思维",
                        "和伴侣讨论未来的方向 → 水/情感",
                        "制定下季度的储蓄计划 → 土/物质",
                        "开始一个新的健身习惯 → 火/行动"
                    ),
                    explanationCorrect = "全部正确。跳槽是思维决策（宝剑/风）；伴侣对话关乎情感连结（圣杯/水）；储蓄计划是物质规划（星币/土）；新习惯需要行动驱力（权杖/火）。",
                    explanationWrong = "注意区分「思考做决定」和「采取行动」——前者是风，后者是火。情感交流是水，物质规划是土。花色对应的是问题的核心性质，而非表面行为。"
                ),
                QuizQuestion(
                    id = "C2_Q2",
                    type = QuizType.MATCHING,
                    prompt = "将花色与对应元素配对：权杖、圣杯、宝剑、星币",
                    options = listOf(
                        "权杖 → 火",
                        "圣杯 → 水",
                        "宝剑 → 风",
                        "星币 → 土"
                    ),
                    correctAnswerIds = listOf("权杖 → 火", "圣杯 → 水", "宝剑 → 风", "星币 → 土"),
                    explanationCorrect = "全部正确。权杖对应火（行动）、圣杯对应水（情感）、宝剑对应风（思维）、星币对应土（物质），这是塔罗四元素的基础对应体系。",
                    explanationWrong = "请重新记忆四元素对应：权杖＝火（燃烧的行动力）、圣杯＝水（流动的情感）、宝剑＝风（锐利的思维）、星币＝土（稳固的物质）。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "tarot_minor_suits",
                "解锁塔罗小阿卡纳花色标注"
            )
        ),

        // ── C3: 正位与逆位 ─────────────────────────────────────────────
        Lesson(
            id = "C3",
            pathId = "tarot_intro",
            order = 3,
            title = "正位与逆位",
            subtitle = "逆位不是「坏牌」——理解翻转的多重含义",
            concept = "正逆位",
            explanation = "牌面朝上为正位，倒置为逆位。逆位不等于负面，通常表示该牌的能量受到干扰或以不同方式运作：可能是阻滞（能量未充分表达）、内化（转向内在体验）、过度（超出平衡）或延迟（尚在酝酿中）。解读时需要结合具体情境判断。",
            howToRead = listOf(
                "先按正位含义理解牌的基本主题，不急于套用逆位",
                "判断逆位属于哪种模式：阻滞、内化、过度还是延迟",
                "结合周围牌面和问题背景，选择最贴切的逆位解释"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "C3_Q1",
                    type = QuizType.SINGLE_CHOICE,
                    prompt = "关于逆位，以下哪种理解最准确？",
                    options = listOf(
                        "A. 逆位代表厄运和不幸",
                        "B. 逆位意味着这张牌的能量出现干扰，需要多角度判断",
                        "C. 逆位应该忽略，只看正位含义",
                        "D. 逆位永远比正位的含义弱"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。逆位表示该牌能量的运作方式发生了变化——可能是阻滞、内化、过度或延迟——而非简单的「好」或「坏」。需要结合情境具体分析。",
                    explanationWrong = "逆位不是厄运，也不应忽略。它提示该牌的能量以另一种方式运作——可能是被阻碍、向内转化、过度表现或延迟出现。具体是哪种，要看情境。"
                ),
                QuizQuestion(
                    id = "C3_Q2",
                    type = QuizType.CASE_JUDGE,
                    prompt = "小明问工作发展，抽到逆位「战车」。战车牌正位通常代表意志力和前进。逆位最可能的解读方向是？",
                    options = listOf(
                        "A. 他会失去工作",
                        "B. 他的前进动力可能受阻，或方向不够明确，需要先厘清目标",
                        "C. 他不适合这份工作",
                        "D. 这张牌没有意义"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。逆位战车在工作议题中，通常暗示前进的能量受到干扰——可能是目标不够清晰、动力不足或外在阻力较大。这是一个调整方向的信号，而非灾难性的结论。",
                    explanationWrong = "逆位战车不会直接推导出「失去工作」或「不适合」这类结论。它提示的是能量层面的状况：前进受阻、方向模糊或动力不足。解读要落在可观察的趋势上，而非极端预测。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "tarot_reversed",
                "解锁塔罗逆位解读标注"
            )
        ),

        // ── C4: 单牌怎么读 ─────────────────────────────────────────────
        Lesson(
            id = "C4",
            pathId = "tarot_intro",
            order = 4,
            title = "单牌怎么读",
            subtitle = "四步法：问题→牌名→关键词→建议",
            concept = "单牌解读",
            explanation = "单张牌是最基础的问卜形式。解读有四个步骤：先明确你问的是什么；再看抽出的牌名和编号；接着提取该牌的2-3个核心关键词；最后将关键词与你的问题情境结合，形成行动方向。记住：牌提供的是思考框架，不是确定答案。",
            howToRead = listOf(
                "明确问题：把模糊的焦虑转化为具体可回答的问题",
                "识别牌面：看牌名、编号、花色，建立基本认知",
                "提取关键词与建议：用2-3个词概括牌义，转化为可操作的方向"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "C4_Q1",
                    type = QuizType.ORDERING,
                    prompt = "将单牌解读的四个步骤按正确顺序排列",
                    options = listOf(
                        "1. 明确问题",
                        "2. 识别牌面",
                        "3. 提取关键词",
                        "4. 给出建议方向"
                    ),
                    correctAnswerIds = listOf("1. 明确问题", "2. 识别牌面", "3. 提取关键词", "4. 给出建议方向"),
                    explanationCorrect = "顺序正确。先有清晰的问题，才能有效解读牌面；识别牌面后提取关键词；最终将关键词与问题结合，形成建议方向。这是从「输入」到「输出」的逻辑链。",
                    explanationWrong = "正确顺序是：明确问题 → 识别牌面 → 提取关键词 → 给出建议。先定义问题，再解读牌面，最后转化为行动方向。顺序颠倒会导致解读脱离实际需求。"
                ),
                QuizQuestion(
                    id = "C4_Q2",
                    type = QuizType.SINGLE_CHOICE,
                    prompt = "用单牌问卜时，哪种提问方式最有效？",
                    options = listOf(
                        "A. 我的命运是什么？",
                        "B. 这段关系中，我需要注意什么？",
                        "C. 我会不会死？",
                        "D. 我下一期彩票号码是多少？"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。有效的提问应该是具体的、开放的、与自身行动相关的。「我需要注意什么」将焦点放在可观察和可行动的层面。",
                    explanationWrong = "有效的提问应该具体且可操作。宏大的命运问题或预测性问题超出了象征性解读的合理范围——牌提供的是思考框架，不是确定性预言。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "tarot_single_card",
                "解锁塔罗单牌解读法标注"
            )
        ),

        // ── C5: 三牌牌阵 ───────────────────────────────────────────────
        Lesson(
            id = "C5",
            pathId = "tarot_intro",
            order = 5,
            title = "三牌牌阵",
            subtitle = "过去→现在→未来：把三张牌连成叙事",
            concept = "三牌牌阵",
            explanation = "三牌牌阵是最常用的简易牌阵，基本排列为「过去—现在—未来」。三张牌不是三个孤立的答案，而是一条因果与发展的叙事线：过去的状况如何影响了现在，现在的能量又指向怎样的发展趋势。阅读的关键是「连成故事」而非逐张翻译。",
            howToRead = listOf(
                "先看中间（现在）：这是当前处境的核心，是整个叙事的锚点",
                "再看左边（过去）：是什么因素或经历导致了现在的状况",
                "最后看右边（未来）：基于当前趋势，事情可能往哪个方向发展"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "C5_Q1",
                    type = QuizType.CASE_JUDGE,
                    prompt = "小华问职业发展，三牌依次为：过去「星币四正位」、现在「宝剑五逆位」、未来「权杖女王正位」。哪项是最核心的洞察？",
                    options = listOf(
                        "A. 她过去一直抓着稳定不放，现在内心的冲突正在消退，未来将展现出行动力和自信",
                        "B. 她过去很穷，现在很痛苦，未来会变好",
                        "C. 三张牌各说各的，没有关联",
                        "D. 这个牌阵显示她不适合工作"
                    ),
                    correctAnswerIds = listOf("A"),
                    explanationCorrect = "很好。星币四正位暗示过去倾向保守固守；宝剑五逆位表示内心的纷争或纠结正在缓解；权杖女王正位指向未来充满热情与领导力的展现。三张牌串联出一个「从固守到释放到行动」的成长叙事。",
                    explanationWrong = "三张牌必须连成叙事来读，而非各自翻译。星币四（固守）→宝剑五逆位（纷争缓解）→权杖女王（自信行动），这条线讲的是「放下固执后重获行动力」的故事。"
                ),
                QuizQuestion(
                    id = "C5_Q2",
                    type = QuizType.ORDERING,
                    prompt = "解读三牌牌阵时，最有效的阅读顺序是什么？",
                    options = listOf(
                        "1. 先看中间（现在）",
                        "2. 回溯左边（过去）",
                        "3. 展望右边（未来）"
                    ),
                    correctAnswerIds = listOf("1. 先看中间（现在）", "2. 回溯左边（过去）", "3. 展望右边（未来）"),
                    explanationCorrect = "正确。以「现在」为锚点，先理解当前处境，再追溯原因，最后看发展趋势。这样叙事更连贯，避免三张牌变成割裂的碎片。",
                    explanationWrong = "正确顺序是：先看现在（中间）→ 回溯过去（左边）→ 展望未来（右边）。以当前处境为核心锚点，才能将三张牌串成有因果关系的叙事。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "tarot_three_card",
                "解锁塔罗三牌牌阵标注"
            )
        ),

        // ── C6: 凯尔特十字不必全懂 ─────────────────────────────────────
        Lesson(
            id = "C6",
            pathId = "tarot_intro",
            order = 6,
            title = "凯尔特十字不必全懂",
            subtitle = "高阶牌阵先抓主轴和矛盾",
            concept = "凯尔特十字",
            explanation = "凯尔特十字是10张牌的经典牌阵，结构复杂但有阅读优先级。不必一次读懂全部——先看第1张（现状）和第2张（挑战/交叉牌），这两张构成主轴。再看第5张（近期过去）和第10张（最终结果）了解来龙去脉。其余位置是补充信息，随着经验加深再逐步整合。",
            howToRead = listOf(
                "先看主轴：第1张（现状）与第2张（交叉/挑战）构成核心矛盾",
                "再看首尾：第5张（根源/近期过去）和第10张（结果倾向）给出背景与方向",
                "补充整合：第3、4张是潜意识与近期未来，第6-9张是外在影响与自我认知"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "C6_Q1",
                    type = QuizType.SINGLE_CHOICE,
                    prompt = "面对凯尔特十字这样的高阶牌阵，最合理的入门策略是什么？",
                    options = listOf(
                        "A. 逐张翻译全部10张牌的含义，再加总",
                        "B. 先聚焦第1张和第2张（主轴），理解核心矛盾，再逐步扩展",
                        "C. 只看最后一张，因为它代表结果",
                        "D. 跳过这个牌阵，永远不学"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。高阶牌阵的关键是「抓主轴」——先理解现状与挑战的核心矛盾，其他位置是补充信息。逐张翻译会失去整体叙事。",
                    explanationWrong = "面对10张牌的复杂牌阵，关键是分清主次。先看第1张（现状）和第2张（挑战）构成的核心矛盾，再逐步补充其他位置的信息。逐张平铺翻译会导致信息过载。"
                ),
                QuizQuestion(
                    id = "C6_Q2",
                    type = QuizType.CASE_JUDGE,
                    prompt = "凯尔特十字中，第1张抽到「月亮正位」，第2张抽到「宝剑十逆位」。这对主轴最可能提示什么？",
                    options = listOf(
                        "A. 一切顺利，没有问题",
                        "B. 当前处于不确定或模糊的状态（月亮），而挑战在于某个困境正走向结束但仍有余波（宝剑十逆位）",
                        "C. 会有可怕的事情发生",
                        "D. 这两张牌不能一起出现"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。月亮正位常提示不确定、模糊或需要面对潜意识的时期；宝剑十逆位表示一段艰难的阶段正在过去，但残留影响仍在。主轴暗示：在迷雾中，旧的痛苦正逐渐消退，需要耐心等待清明。",
                    explanationWrong = "月亮正位不会直接带来「可怕的事」，它描述的是一种不确定和模糊的心理状态。宝剑十逆位也不是灾难，而是困境走向结束的信号。两者结合的叙事是「在迷雾中等待旧痛消退」。"
                ),
                QuizQuestion(
                    id = "C6_Q3",
                    type = QuizType.SINGLE_CHOICE,
                    prompt = "在凯尔特十字牌阵中，第1张和第2张分别代表什么？",
                    options = listOf(
                        "A. 过去和未来",
                        "B. 现状与面对的挑战（交叉牌）",
                        "C. 潜意识和外在影响",
                        "D. 好运和坏运"
                    ),
                    correctAnswerIds = listOf("B"),
                    explanationCorrect = "正确。凯尔特十字中，第1张代表当前处境，第2张横跨其上，代表当前面临的主要挑战或张力，两者共同构成牌阵的核心解读轴。",
                    explanationWrong = "凯尔特十字的第1张是现状，第2张横跨其上代表当前面临的挑战。这两张「主轴牌」是整个牌阵的核心，其他8张围绕它们展开补充信息。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "tarot_celtic_cross",
                "解锁塔罗凯尔特十字牌阵标注"
            )
        )
    )
}
