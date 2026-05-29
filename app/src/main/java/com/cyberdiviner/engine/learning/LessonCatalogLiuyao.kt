package com.cyberdiviner.engine.learning

import com.cyberdiviner.data.model.learning.*

object LessonCatalogLiuyao {

    val lessons: List<Lesson> = listOf(

        // ── B1: 三枚铜钱 ──────────────────────────────────────────────
        Lesson(
            id = "B1",
            pathId = "liuyao_intro",
            order = 1,
            title = "三枚铜钱",
            subtitle = "三钱摇六次——六爻起卦的基本方法",
            concept = "三钱法",
            explanation = "六爻占卜使用三枚铜钱，摇六次生成六爻。每次抛掷三枚铜钱，正面（字面）计3分，背面（花面）计2分。三枚合计：6分为老阴（×，变爻）、7分为少阳（⚊）、8分为少阴（⚋）、9分为老阳（○，变爻）。从下往上依次记录六次。",
            howToRead = listOf(
                "先摇钱：三枚铜钱同时抛掷，记录每枚正反面",
                "再计分：正面=3，背面=2，合计6/7/8/9对应老阴/少阳/少阴/老阳",
                "最后记录：从初爻到上爻，摇六次完成一卦"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "B1_Q1",
                    type = QuestionType.MATCHING,
                    prompt = "将以下铜钱抛掷结果与对应的爻象配对",
                    items = listOf(
                        MatchItem("三枚都是正面（3+3+3=9）", "老阳（○，动爻）"),
                        MatchItem("两正面一背面（3+3+2=8）", "少阴（⚋，静爻）"),
                        MatchItem("一正面两背面（3+2+2=7）", "少阳（⚊，静爻）"),
                        MatchItem("三枚都是背面（2+2+2=6）", "老阴（×，动爻）")
                    ),
                    explanation = "9为老阳（阳极将变阴）、8为少阴（静爻）、7为少阳（静爻）、6为老阴（阴极将变阳）。只有老阳和老阴是动爻。"
                ),
                QuizQuestion(
                    id = "B1_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "三枚铜钱抛掷，正面计几分？",
                    options = listOf(
                        "A. 正面计1分",
                        "B. 正面计2分",
                        "C. 正面计3分",
                        "D. 正面计4分"
                    ),
                    correctIndex = 2,
                    explanation = "传统三钱法中，铜钱正面（有字的一面）计3分，背面（有花纹的一面）计2分。三枚合计6-9分，对应四种爻象。"
                ),
                QuizQuestion(
                    id = "B1_Q3",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "三枚铜钱抛掷，合计7分代表什么？",
                    options = listOf(
                        "A. 老阳（动爻）",
                        "B. 少阳（静爻）",
                        "C. 少阴（静爻）",
                        "D. 老阴（动爻）"
                    ),
                    correctIndex = 1,
                    explanation = "7分=少阳，是稳定的阳爻，不会变化。只有6分（老阴）和9分（老阳）才是动爻，会发生阴阳转换。"
                )
            ),
            unlockReward = null
        ),

        // ── B2: 本卦与变卦 ────────────────────────────────────────────
        Lesson(
            id = "B2",
            pathId = "liuyao_intro",
            order = 2,
            title = "本卦与变卦",
            subtitle = "现状与趋势——一摇两卦的关系",
            concept = "本卦变卦",
            explanation = "本卦（主卦）是摇出的原始卦象，代表当前状况。变卦是本卦中动爻变化后产生的新卦象，代表事情的发展趋势。没有动爻则无变卦，只有本卦。本卦是「现在」，变卦是「未来方向」，两者结合才能完整判断。",
            howToRead = listOf(
                "先看本卦：确定当前卦象，分析世应、六亲、六神的格局",
                "再看变卦：动爻变化后的新卦，看变出的爻对用神的影响",
                "最后综合：本卦定基调，变卦看走向，动爻是关键转折点"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "B2_Q1",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "本卦与变卦分别代表什么？",
                    options = listOf(
                        "A. 本卦是好卦，变卦是坏卦",
                        "B. 本卦代表当前状况，变卦代表发展趋势",
                        "C. 本卦和变卦含义完全相同",
                        "D. 变卦是计算错误的产物"
                    ),
                    correctIndex = 1,
                    explanation = "本卦是摇出的原始卦象，反映当前局面；变卦是动爻变化后的新卦象，揭示事情的发展方向。两者缺一不可。"
                ),
                QuizQuestion(
                    id = "B2_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "如果一卦中没有任何动爻（全是少阴或少阳），会怎样？",
                    options = listOf(
                        "A. 会产生一个完全相同的变卦",
                        "B. 没有变卦，只有本卦，说明事情相对稳定",
                        "C. 卦象无效，必须重摇",
                        "D. 六个爻全部变成相反的阴阳"
                    ),
                    correctIndex = 1,
                    explanation = "没有动爻意味着没有变化，只有本卦没有变卦。这通常表示事情处于相对稳定的状态，短期内不会有大的转变。"
                )
            ),
            unlockReward = null
        ),

        // ── B3: 世应 ─────────────────────────────────────────────────
        Lesson(
            id = "B3",
            pathId = "liuyao_intro",
            order = 3,
            title = "世应",
            subtitle = "谁问事、谁回应——定位卦中主客关系",
            concept = "世应",
            explanation = "世爻代表问卦者自身，应爻代表对方或所问之事。世应的位置由卦的宫位决定，每卦固定。断卦时先看世爻和应爻的关系：世应相生则和顺，相克则有矛盾。世爻是核心参照点，用神（所问之事的代表爻）与世爻的关系决定吉凶。",
            howToRead = listOf(
                "先定世应：根据卦名查出世爻和应爻的位置（初到上爻）",
                "再看关系：世爻与应爻是相生还是相克，判断主客之间的和谐度",
                "最后看用神：用神与世爻的距离和生克关系，决定事情的吉凶倾向"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "B3_Q1",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小王问与合作伙伴的关系，得卦后世爻在二爻（阴爻），应爻在五爻（阳爻）。世应地支相生。这代表什么？",
                    options = listOf(
                        "A. 合作关系融洽，双方互助",
                        "B. 合作关系必然失败",
                        "C. 小王会被对方欺骗",
                        "D. 世应位置不重要"
                    ),
                    correctIndex = 0,
                    explanation = "世应相生表示问卦者与对方之间关系和谐、互相扶持。在合作问题中，这是积极的信号，说明双方配合顺畅。"
                ),
                QuizQuestion(
                    id = "B3_Q2",
                    type = QuestionType.CASE_JUDGE,
                    prompt = "小美问感情，世爻与应爻地支相克（世克应）。这通常暗示什么？",
                    options = listOf(
                        "A. 感情非常好",
                        "B. 双方可能存在矛盾或主导权之争，需要留意沟通方式",
                        "C. 注定分手",
                        "D. 对方完全听从小美的"
                    ),
                    correctIndex = 1,
                    explanation = "世克应表示问卦者在关系中较为主动或强势，可能给对方造成压力。这不一定是坏事，但提示需要注意沟通和平衡。"
                ),
                QuizQuestion(
                    id = "B3_Q3",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "世爻在六爻卦中代表什么？",
                    options = listOf(
                        "A. 代表所问的事情",
                        "B. 代表问卦者自身",
                        "C. 代表天气",
                        "D. 代表卦的编号"
                    ),
                    correctIndex = 1,
                    explanation = "世爻是问卦者的代表，是断卦的核心参照点。应爻则代表对方或所问之事的另一方。世应关系是六爻分析的基础框架。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "liuyao_shi_ying",
                "解锁六爻世应术语标注"
            )
        ),

        // ── B4: 六亲 ─────────────────────────────────────────────────
        Lesson(
            id = "B4",
            pathId = "liuyao_intro",
            order = 4,
            title = "六亲",
            subtitle = "父母·兄弟·子孙·官鬼·妻财——分类万物",
            concept = "六亲",
            explanation = "六亲是将卦中每一爻与问卦者的关系分为五类：父母（生我者）、兄弟（同类）、子孙（我生者）、官鬼（克我者）、妻财（我克者）。每爻的六亲由该爻地支五行与卦宫五行的生克关系决定。六亲是断卦时判断具体事项的核心工具。",
            howToRead = listOf(
                "先确定卦宫五行：每卦属于八个宫之一，宫的五行决定「我」的属性",
                "再看每爻地支五行：通过纳甲确定每爻的地支和五行",
                "最后判定六亲：生我为父母、我生为子孙、克我为官鬼、我克为妻财、同类为兄弟"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "B4_Q1",
                    type = QuestionType.MATCHING,
                    prompt = "将六亲与其生克关系配对",
                    items = listOf(
                        MatchItem("父母", "生我者"),
                        MatchItem("子孙", "我生者"),
                        MatchItem("官鬼", "克我者"),
                        MatchItem("妻财", "我克者"),
                        MatchItem("兄弟", "与我同类")
                    ),
                    explanation = "六亲以「我」（卦宫五行）为中心，按五行生克关系分类：生我=父母、我生=子孙、克我=官鬼、我克=妻财、比和=兄弟。"
                ),
                QuizQuestion(
                    id = "B4_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "问事业时，通常以哪个六亲为「用神」（主要参考爻）？",
                    options = listOf(
                        "A. 父母爻",
                        "B. 官鬼爻",
                        "C. 妻财爻",
                        "D. 兄弟爻"
                    ),
                    correctIndex = 1,
                    explanation = "问事业、升迁、考试等，以官鬼爻为用神，因为官鬼代表权力、压力和事业地位。问财运则以妻财爻为用神。用神的选择取决于所问何事。"
                ),
                QuizQuestion(
                    id = "B4_Q3",
                    type = QuestionType.MATCHING,
                    prompt = "将下列所问事项与对应的用神配对",
                    items = listOf(
                        MatchItem("问财运", "妻财"),
                        MatchItem("问工作升迁", "官鬼"),
                        MatchItem("问考试文书", "父母"),
                        MatchItem("问子女之事", "子孙")
                    ),
                    explanation = "不同事项取不同用神：财运看妻财、事业看官鬼、文书考试看父母、子女创意看子孙、竞争纠纷看兄弟。"
                )
            ),
            unlockReward = UnlockReward(
                UnlockType.TERM_ANNOTATION,
                "liuyao_liuqin",
                "解锁六爻六亲术语标注"
            )
        ),

        // ── B5: 六神 ─────────────────────────────────────────────────
        Lesson(
            id = "B5",
            pathId = "liuyao_intro",
            order = 5,
            title = "六神",
            subtitle = "青龙·朱雀·勾陈·螣蛇·白虎·玄武",
            concept = "六神",
            explanation = "六神（六兽）是附加在每一爻上的神煞，按日干分配：青龙（吉庆）、朱雀（口舌文书）、勾陈（田土纠缠）、螣蛇（惊恐虚幻）、白虎（凶险血光）、玄武（暗昧盗窃）。六神不决定吉凶，但修饰事情的性质和表现方式。",
            howToRead = listOf(
                "先排六神：根据占卜日的天干，确定初爻到上爻各配什么六神",
                "再看搭配：六神与六亲、爻的旺衰结合看，如青龙配妻财主进财",
                "最后看轻重：六神是辅助信息，不要喧宾夺主，重点仍在六亲和世应"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "B5_Q1",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "六神在断卦中的角色是什么？",
                    options = listOf(
                        "A. 六神是最重要的判断依据",
                        "B. 六神是辅助信息，修饰事情的性质，不能单独决定吉凶",
                        "C. 六神可以忽略不看",
                        "D. 六神每天固定不变"
                    ),
                    correctIndex = 1,
                    explanation = "六神是附加在每爻上的辅助信息，修饰事情的特征（如青龙主喜庆、白虎主凶险）。断卦的核心仍是六亲和世应，六神起补充作用。"
                ),
                QuizQuestion(
                    id = "B5_Q2",
                    type = QuestionType.MATCHING,
                    prompt = "将六神与其主要象征配对",
                    items = listOf(
                        MatchItem("青龙", "吉庆、喜事"),
                        MatchItem("朱雀", "口舌、文书"),
                        MatchItem("白虎", "凶险、血光"),
                        MatchItem("玄武", "暗昧、盗窃")
                    ),
                    explanation = "青龙主吉庆喜事；朱雀主口舌是非和文书信息；白虎主凶险灾伤；玄武主暗昧欺诈。勾陈主田土纠缠，螣蛇主惊恐虚幻。"
                )
            ),
            unlockReward = null
        ),

        // ── B6: 断卦流程 ──────────────────────────────────────────────
        Lesson(
            id = "B6",
            pathId = "liuyao_intro",
            order = 6,
            title = "断卦流程",
            subtitle = "从摇卦到出结论的完整步骤",
            concept = "断卦流程",
            explanation = "完整断卦流程：一、确定用神（所问何事对应哪个六亲）；二、看用神旺衰（月建日辰是否生扶）；三、看世应关系（主客互动）；四、看动爻变化（关键转折）；五、综合六神辅助信息；六、结合变卦趋势给出结论。按步骤分析，避免跳跃。",
            howToRead = listOf(
                "先定用神：根据所问之事确定哪个六亲是核心参考爻",
                "再分析格局：看用神旺衰、世应关系、动爻变化",
                "最后综合判断：结合变卦趋势和六神辅助信息，得出结论"
            ),
            questions = listOf(
                QuizQuestion(
                    id = "B6_Q1",
                    type = QuestionType.ORDERING,
                    prompt = "将断卦的主要步骤按正确顺序排列",
                    items = listOf(
                        MatchItem("确定用神", "1"),
                        MatchItem("分析用神旺衰", "2"),
                        MatchItem("看世应关系", "3"),
                        MatchItem("看动爻变化", "4"),
                        MatchItem("综合六神与变卦得出结论", "5")
                    ),
                    explanation = "断卦从确定用神开始，再看旺衰定基调，世应看主客互动，动爻看变化转折，最后综合六神和变卦得出完整结论。"
                ),
                QuizQuestion(
                    id = "B6_Q2",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "问「这笔投资能不能赚钱」，第一步应该做什么？",
                    options = listOf(
                        "A. 直接看世爻旺不旺",
                        "B. 先确定用神为妻财爻（财运以妻财为用神）",
                        "C. 看六神是什么",
                        "D. 看变卦是什么卦"
                    ),
                    correctIndex = 1,
                    explanation = "断卦第一步是确定用神。问财运，用神是妻财爻。确定了用神，后续的旺衰分析、动爻变化才有焦点。跳过这一步会导致解读混乱。"
                ),
                QuizQuestion(
                    id = "B6_Q3",
                    type = QuestionType.SINGLE_CHOICE,
                    prompt = "以下哪种断卦习惯最合理？",
                    options = listOf(
                        "A. 只看动爻，忽略其他信息",
                        "B. 按步骤系统分析，综合用神、世应、动爻、六神得出结论",
                        "C. 只看六神判断吉凶",
                        "D. 只看变卦，本卦不重要"
                    ),
                    correctIndex = 1,
                    explanation = "断卦是系统工程，需要按步骤综合分析。单一要素不能决定全局，用神、世应、动爻、六神各有角色，缺一不可。"
                )
            ),
            unlockReward = null
        )
    )
}
