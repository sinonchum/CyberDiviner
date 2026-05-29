# CyberDiviner 学习闯关 — L4/L5 开发计划

> **目标**：在 CyberDiviner 中实现「学习闯关」功能，用多邻国式短课教用户易经术数与塔罗术语
> **框架**：L4 自主执行 + L5 多代理并行
> **代码量**：75 个现有 Kotlin 文件，~19,000 行
> **预计总工时**：4-5 天（含内容编写）

---

## 一、总体架构

### 1.1 新增文件清单

```
app/src/main/java/com/cyberdiviner/
├── data/model/learning/
│   ├── Lesson.kt                  # 课程数据模型
│   ├── LessonPath.kt              # 学习路径模型
│   ├── QuizQuestion.kt            # 题目模型
│   ├── LearningProgress.kt        # 学习进度实体
│   └── UnlockReward.kt            # 解锁奖励模型
├── data/dao/
│   └── LearningDao.kt             # 学习进度 DAO
├── engine/learning/
│   ├── LessonCatalog.kt           # 课程目录（本地静态数据）
│   ├── LearningScorer.kt          # 评分逻辑（XP/Streak）
│   └── UnlockManager.kt           # 解锁逻辑
└── ui/learning/
    ├── LearnHomeScreen.kt         # 学习首页（路径选择 + 每日一关）
    ├── LessonMapScreen.kt         # 关卡地图（进度可视化）
    ├── LessonScreen.kt            # 单关学习页（术语卡 + 题目）
    ├── QuizComponents.kt          # 题目组件（单选/匹配/排序/判断）
    ├── LessonResultScreen.kt      # 结算页（XP/称号/知识卡）
    └── LearningViewModel.kt       # ViewModel
```

### 1.2 修改文件清单

| 文件 | 改动 |
|------|------|
| `NavGraph.kt` | 新增 LEARN / LESSON / LESSON_RESULT 路由 |
| `BottomNavBar.kt` | 新增「研习」Tab |
| `CyberDivinerDatabase.kt` | version 1→2，新增 Entity + DAO |
| `DatabaseModule.kt` | 提供 LearningDao |
| `ui/theme/Color.kt` | 无需改（现有 B&W + AccentRed 已够用） |

### 1.3 数据库迁移

```kotlin
// version 1 → 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS learning_progress (
                lessonId TEXT NOT NULL PRIMARY KEY,
                pathId TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0,
                score INTEGER NOT NULL DEFAULT 0,
                attempts INTEGER NOT NULL DEFAULT 0,
                lastCompletedAt INTEGER,
                mastery INTEGER NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS learning_stats (
                id TEXT NOT NULL PRIMARY KEY,
                totalXp INTEGER NOT NULL DEFAULT 0,
                currentStreak INTEGER NOT NULL DEFAULT 0,
                bestStreak INTEGER NOT NULL DEFAULT 0,
                lastStudyDate TEXT,
                title TEXT NOT NULL DEFAULT '初入卦门'
            )
        """)
        // Insert default stats row
        db.execSQL("INSERT OR IGNORE INTO learning_stats (id, totalXp, currentStreak, bestStreak, title) VALUES ('default', 0, 0, 0, '初入卦门')")
    }
}
```

---

## 二、L4 执行阶段

### Phase 0 — 内容定稿与数据结构（Day 1）

**策略**：内容先行，不接 UI，纯数据层验证。

#### Task 0.1: 数据模型定义

```kotlin
// Lesson.kt
data class Lesson(
    val id: String,           // "C1", "A3", "B6"
    val pathId: String,       // "tarot_intro", "liuyao_intro", "yijing_intro", "practice"
    val order: Int,           // 1-6 within path
    val title: String,        // "大阿卡纳：人生主线"
    val subtitle: String,     // "22张大牌看人生阶段"
    val concept: String,      // 术语名
    val explanation: String,  // 术语解释（<=120字）
    val howToRead: List<String>,  // 3步看法
    val questions: List<QuizQuestion>,
    val unlockReward: UnlockReward?
)

// QuizQuestion.kt
enum class QuizType {
    SINGLE_CHOICE,    // 单选
    BINARY_CLASSIFY,  // 二选一归类
    MATCHING,         // 匹配
    ORDERING,         // 排序
    CASE_JUDGE        // 小案例判断
}

data class QuizQuestion(
    val id: String,
    val type: QuizType,
    val prompt: String,
    val options: List<String>,
    val correctAnswerIds: List<String>,
    val explanationCorrect: String,
    val explanationWrong: String
)

// UnlockReward.kt
enum class UnlockType {
    TERM_ANNOTATION,   // 术语标注（在占卜结果中显示）
    HINT_DISPLAY,      // 提示显示
    POSTER_STYLE,      // 海报样式
    TITLE              // 称号
}
data class UnlockReward(
    val type: UnlockType,
    val target: String,  // "liuyao_shiying", "tarot_reversed", etc.
    val description: String
)
```

**验收**：单元测试读取 24 关数据，每关有 title/concept/questions。

#### Task 0.2: LessonCatalog 静态数据

编写 `LessonCatalog.kt`，包含 4 条路径 × 6 关的完整内容。

**路径 C「塔罗入门」优先**（TarotEngine 数据最完整）：
- C1: 大阿卡纳（22牌主题匹配题）
- C2: 四元素小阿卡纳（分类题）
- C3: 正位与逆位（情境判断题）
- C4: 单牌怎么读（填空/选择题）
- C5: 三牌牌阵（排列解读题）
- C6: 凯尔特十字（简化案例题）

每关 2-4 道题，内容从 Obsidian 大纲的 §11 样例扩展。

#### Task 0.3: Room Schema + DAO

```kotlin
@Entity(tableName = "learning_progress")
data class LearningProgressEntity(
    @PrimaryKey val lessonId: String,
    val pathId: String,
    val completed: Boolean = false,
    val score: Int = 0,           // 0-100
    val attempts: Int = 0,
    val lastCompletedAt: Long? = null,
    val mastery: Int = 0          // 0-100
)

@Entity(tableName = "learning_stats")
data class LearningStatsEntity(
    @PrimaryKey val id: String = "default",
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastStudyDate: String? = null,
    val title: String = "初入卦门"
)

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_progress WHERE pathId = :pathId ORDER BY lessonId")
    fun getProgressForPath(pathId: String): Flow<List<LearningProgressEntity>>

    @Query("SELECT * FROM learning_progress WHERE lessonId = :lessonId")
    suspend fun getProgress(lessonId: String): LearningProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LearningProgressEntity)

    @Query("SELECT * FROM learning_stats WHERE id = 'default'")
    fun getStats(): Flow<LearningStatsEntity?>

    @Update
    suspend fun updateStats(stats: LearningStatsEntity)
}
```

**验收**：数据库 migration 测试通过，DAO 查询返回正确数据。

---

### Phase 1 — MVP 可玩闭环（Day 2）

**策略**：3 个子代理并行，互不修改同一文件。

#### Task 1.1: ViewModel + 导航接入

**子代理 A** — 修改文件：`LearningViewModel.kt`, `NavGraph.kt`, `BottomNavBar.kt`

```kotlin
// LearningViewModel.kt
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningDao: LearningDao
) : ViewModel() {
    val stats = learningDao.getStats()
    val currentPath = mutableStateOf("tarot_intro")

    fun getPathProgress(pathId: String) = learningDao.getProgressForPath(pathId)

    suspend fun submitAnswer(lessonId: String, questionId: String, answerId: String): Boolean {
        // 返回是否正确
    }

    suspend fun completeLesson(lessonId: String, score: Int) {
        // 保存进度 + 更新 XP/Streak + 检查解锁
    }
}
```

底部导航新增第 4 个 Tab：
```kotlin
sealed class BottomNavItem(val route: String, val label: String, val icon: @Composable () -> Unit) {
    // ... existing items ...
    object Learn : BottomNavItem("learn", "研习", { /* BookCanvasIcon */ })
}
```

#### Task 1.2: LearnHomeScreen + LessonMapScreen

**子代理 B** — 新建文件：`LearnHomeScreen.kt`, `LessonMapScreen.kt`

**LearnHomeScreen 布局**：
```
┌──────────────────────────┐
│ SectionHeader("术数研习")  │
│ "PATH TO ENLIGHTENMENT"  │
├──────────────────────────┤
│ ┌──────────────────────┐ │
│ │ 今日一关              │ │
│ │ C3 正位与逆位         │ │
│ │ [开始学习]            │ │
│ └──────────────────────┘ │
│                          │
│ 学习路径                  │
│ ┌────┐ ┌────┐ ┌────┐    │
│ │周易│ │六爻│ │塔罗│    │
│ │入门│ │入门│ │入门│    │
│ │ 2/6│ │ 0/6│ │ 4/6│    │
│ └────┘ └────┘ └────┘    │
│ ┌────┐                   │
│ │实战│                   │
│ │看盘│                   │
│ │ 🔒 │                   │
│ └────┘                   │
│                          │
│ XP: 120  Streak: 3       │
│ 称号: 识象者              │
└──────────────────────────┘
```

**LessonMapScreen**：竖向关卡列表，已完成的显示 ✅，当前高亮，未完成灰色。

#### Task 1.3: LessonScreen + QuizComponents + LessonResultScreen

**子代理 C** — 新建文件：`LessonScreen.kt`, `QuizComponents.kt`, `LessonResultScreen.kt`

**LessonScreen 五段式**：
1. **开场签语**（SectionHeader 风格标题）
2. **术语卡**（白底灰边卡片，120 字以内）
3. **怎么看**（3 步列表，带序号）
4. **互动题**（2-4 道，根据 QuizType 渲染）
5. **结算页**（XP 增加、正确率、称号变化、知识卡）

**QuizComponents 题型**：
- `SingleChoiceQuiz`：RadioGroup 风格
- `BinaryClassifyQuiz`：两个大按钮（A/B）
- `MatchingQuiz`：左侧列表 + 右侧列表，点击配对
- `OrderingQuiz`：可上下移动的列表
- `CaseJudgeQuiz`：案例描述 + 选择判断入口

---

### Phase 2 — 占卜联动（Day 3）

**策略**：占卜结果页根据学习进度显示术语注释。

#### Task 2.1: 六爻结果页联动

修改 `LiuyaoResultScreen.kt`：
- 如果用户完成了 B3「世应」→ 结果页显示世爻/应爻标注
- 如果用户完成了 A6「动爻」→ 动爻高亮 + 解释
- 如果用户完成了 B4「六亲」→ 六亲标签可点击查看解释

实现方式：`LearningViewModel` 提供 `isLessonCompleted(lessonId)` 方法。

#### Task 2.2: 塔罗结果页联动

修改 `TarotScreen.kt`：
- 如果用户完成了 C3「正逆位」→ 显示正逆位解释提示
- 如果用户完成了 C5「三牌」→ 显示位置含义（过去/现在/未来）

#### Task 2.3: Archive 学习复盘入口

修改 `ArchiveScreen.kt`：
- 展开卡片底部新增「学习此术语」入口（如果相关术语有对应课程）

---

### Phase 3 — 分享与商业化（Day 4）

#### Task 3.1: 知识卡生成

复用 `PosterGenerator.kt` 或新建 `KnowledgeCardGenerator.kt`：
- 完课后生成极简知识卡（B&W + AccentRed 风格）
- 内容：今日学会 + 一句话 + 关卡编号
- Canvas 绘制 1080×1920 或 1080×1080

#### Task 3.2: 称号系统

在 `LearningScorer.kt` 中实现：
```kotlin
fun calculateTitle(stats: LearningStatsEntity): String {
    val completedPaths = // count completed paths
    return when {
        stats.totalXp >= 1000 && stats.currentStreak >= 7 -> "因果校准员"
        completedPaths >= 3 -> "断语者"
        completedPaths >= 2 -> "读牌者"
        completedPaths >= 1 -> "起爻者"
        // ... etc
    }
}
```

#### Task 3.3: Pro 功能占位

在 `LearnHomeScreen` 中添加 Pro 入口（灰色锁图标）：
- AI 错题讲解（Pro）
- 进阶课程包（Pro）
- 个性化学习路径（Pro）

不实现功能，只做 UI 占位，点击显示「即将推出」。

---

### Phase 4 — AI 教练增强（后置）

#### Task 4.1: PromptManager learn 模板

在 `PromptManager.kt` 新增 `learn` feature：
```yaml
system: |
  You are a patient Chinese metaphysics tutor.
  The user answered a quiz question incorrectly.
  Explain why their answer was wrong and what the correct reasoning is.
  Keep it under 100 characters. Use plain language.
```

#### Task 4.2: 错题 AI 讲解

在 `LessonResultScreen` 中，错题旁边显示「AI 讲解」按钮（需要 API Key）。

---

## 三、并行执行矩阵

### Day 1（Phase 0）— 串行

| 时间 | 任务 | 子代理 |
|------|------|--------|
| 0-2h | 数据模型 + Room Schema | A |
| 2-4h | 路径 C 内容编写（6 关） | A |
| 4-6h | 路径 A/B/D 内容编写（18 关） | B, C, D 并行 |

### Day 2（Phase 1）— 并行

| 子代理 | 任务 | 修改文件 |
|--------|------|----------|
| A | ViewModel + Navigation | LearningViewModel, NavGraph, BottomNavBar |
| B | LearnHomeScreen + Map | LearnHomeScreen, LessonMapScreen |
| C | Lesson + Quiz + Result | LessonScreen, QuizComponents, LessonResultScreen |

三个子代理修改不同文件，可完全并行。

### Day 3（Phase 2）— 并行

| 子代理 | 任务 | 修改文件 |
|--------|------|----------|
| A | 六爻联动 | LiuyaoResultScreen |
| B | 塔罗联动 | TarotScreen |
| C | Archive 复盘 | ArchiveScreen |

### Day 4（Phase 3）— 并行

| 子代理 | 任务 | 修改文件 |
|--------|------|----------|
| A | 知识卡生成 | KnowledgeCardGenerator (new) |
| B | 称号系统 | LearningScorer, LearnHomeScreen |
| C | Pro 占位 | LearnHomeScreen |

---

## 四、L5 多代理协调

### 4.1 编排器模式

对于 Phase 1（最大并行度），使用编排器：

```
Orchestrator (parent)
├── Worker A: ViewModel + Navigation
├── Worker B: Home + Map screens
└── Worker C: Lesson + Quiz screens
    └── 各自独立编译验证
    └── 父代理最终集成编译
```

### 4.2 任务契约模板

每个子代理收到的契约必须包含：

```text
TASK CONTRACT
├── Scope: [what to build]
├── Non-goals: [what NOT to touch]
├── Acceptance criteria: [specific, testable]
├── Verification: cd /Users/fsimon/CyberDiviner && ./gradlew assembleDebug
├── Side effects: [files created/modified]
└── Handoff: [notes for integration]
```

### 4.3 质量门

每个 Phase 结束时：

1. `./gradlew assembleDebug` — 编译通过
2. `adb install -r` — 安装到设备
3. 手动验证核心路径（进入学习 → 完成一关 → 查看进度）
4. Git commit + push

---

## 五、验收清单

### Phase 0 验收
- [ ] 24 关课程数据可从 LessonCatalog 读取
- [ ] 每关有 title/concept/explanation/questions
- [ ] Room migration 1→2 测试通过
- [ ] DAO CRUD 操作正确

### Phase 1 验收
- [ ] 底部导航出现「研习」Tab
- [ ] 进入学习首页，显示 4 条路径
- [ ] 可选择路径，进入关卡地图
- [ ] 可进入课程，阅读术语卡和看法
- [ ] 可答题，显示正确/错误反馈
- [ ] 完成一关后 XP 增加，Streak 更新
- [ ] 进度持久化，杀进程后仍存在
- [ ] 黑白 + AccentRed 风格一致
- [ ] 无 API Key 时学习功能完全可用

### Phase 2 验收
- [ ] 完成 B3 后，六爻结果页显示世应标注
- [ ] 完成 C3 后，塔罗结果页显示正逆位解释
- [ ] Archive 展开卡片有「学习此术语」入口（如有对应课程）

### Phase 3 验收
- [ ] 完课后可生成知识卡文案
- [ ] 称号系统正确更新
- [ ] Pro 功能入口可见但锁定

---

## 六、风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 内容过多导致膨胀 | 开发超时 | 首版锁死 24 关，不扩展 |
| Database migration 破坏老用户 | 数据丢失 | 使用 fallbackToDestructiveMigration（已配置） |
| 题型过多导致 UI 复杂 | 开发时间翻倍 | 首版只做 5 种题型，不用拖拽动画 |
| 学习与占卜联动太紧 | 耦合度高 | 通过 UnlockManager 解耦，学习状态只读 |
| 内容风格不一致 | 用户体验差 | 统一内容风格准则（见大纲 §8.3） |

---

## 七、内容编写指南

### 每关内容结构

```json
{
  "id": "C1",
  "pathId": "tarot_intro",
  "order": 1,
  "title": "大阿卡纳：人生主线",
  "subtitle": "22张大牌看人生阶段和核心主题",
  "concept": "大阿卡纳",
  "explanation": "大阿卡纳是塔罗牌中的22张主牌，从愚者到世界，描绘了人生旅程的完整弧线。每张大牌代表一个重要的生命主题或转折点。",
  "howToRead": [
    "先看牌面的主色调和构图情绪",
    "再读牌名和核心关键词",
    "最后结合问题看这张牌代表什么阶段"
  ],
  "questions": [
    {
      "id": "C1_Q1",
      "type": "MATCHING",
      "prompt": "将以下大阿卡纳牌与它们的核心主题匹配",
      "options": ["愚者", "魔术师", "女祭司", "死神"],
      "correctAnswerIds": ["新旅程", "创造力", "直觉", "转变"],
      "explanationCorrect": "判断成立。你抓住了大牌的核心象征。",
      "explanationWrong": "此处不宜先断。再看每张牌的核心象征。"
    }
  ],
  "unlockReward": {
    "type": "TERM_ANNOTATION",
    "target": "tarot_major_arcana",
    "description": "解锁塔罗结果页大阿卡纳主题标注"
  }
}
```

### 内容风格准则

1. 每关只教一个核心概念
2. 先白话，再术语
3. 不承诺预测准确性，强调「解释框架」
4. 避免迷信化恐吓，不输出绝对断言
5. 用「观察、倾向、结构、变化」替代「必然、注定」
6. 反馈文案：正确=确认判断，错误=引导思考，不嘲笑
7. 视觉：B&W + AccentRed，汇文明朝体标题，无卡通

---

## 八、里程碑

| 里程碑 | 交付 | 预计日期 |
|--------|------|----------|
| M0 | 数据模型 + 24 关内容 | Day 1 |
| M1 | MVP 可玩闭环（路径 C 可通关） | Day 2 |
| M2 | 占卜联动 + 全路径可玩 | Day 3 |
| M3 | 分享 + 称号 + Pro 占位 | Day 4 |
| M4 | 集成测试 + 设备验证 + Release | Day 5 |
| M5 | AI 教练增强（后置） | Week 2 |

---

> **计划版本**：v1.0
> **创建日期**：2026-05-29
> **关联文档**：CyberDiviner 学习闯关 — 开发大纲 / CyberDiviner 赛博算命 — 项目经验
