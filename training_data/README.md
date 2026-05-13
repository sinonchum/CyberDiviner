# CyberDiviner 六爻微调数据集

## 概述

本数据集用于微调 CyberDiviner Android 应用中的六爻（Liuyao）占卜 LLM 模型。数据集包含 55 个高质量训练样本，每个样本模拟一个真实的六爻卦象分析场景。

## 数据格式

JSONL 文件（每行一个 JSON 对象），格式遵循 `reference.md` 第 5 节的模板：

```json
{
  "instruction": "分析指令（指定卦象、动爻等）",
  "input": "用户的具体问题（含时间/月令信息）",
  "response": {
    "conclusion": "总体判定结论",
    "reasoning": [
      {"logic": "体用关系", "detail": "体用分析详情"},
      {"logic": "能量状态", "detail": "五行旺相休囚死分析"},
      {"logic": "爻位判定", "detail": "爻位含义与当位分析"},
      {"logic": "动变逻辑", "detail": "本卦→变卦的变化分析"}
    ],
    "judgment": "吉/凶/中吉/小吉/小凶 等判定"
  }
}
```

## 数据集统计

| 维度 | 覆盖情况 |
|------|---------|
| **问题类型** | 事业、感情、财运、健康、出行（5大类） |
| **八卦** | 乾、坤、震、巽、坎、离、艮、兑（全部覆盖） |
| **动爻位置** | 初爻（1）至上爻（6）全覆盖 |
| **月令** | 寅/卯/辰/巳/午/未/申/酉/戌/亥/子/丑（12个月） |
| **判定结果** | 吉、凶、中吉、小吉、小凶（多种判定） |
| **体用关系** | 用生体、体生用、体克用、用克体、比和（5种） |

## 核心规则体系

### 体用逻辑（第一法则）
- **不动之卦为体**（我方/核心），**动爻所在之卦为用**（外因/事）
- 用生体 → 大吉 | 体克用 → 小吉 | 体用比和 → 中吉 | 体生用 → 小凶 | 用克体 → 大凶

### 五行生克
- **相生**：木 → 火 → 土 → 金 → 水 → 木
- **相克**：木 → 土 → 水 → 火 → 金 → 木
- **旺相休囚死**：由月令（季节）决定五行能量状态

### 爻位逻辑
- 初爻 = 始位 | 二爻 = 臣位/宅位 | 三爻 = 多忧之位
- 四爻 = 近君之位 | 五爻 = 至尊之位 | 上爻 = 终极之位
- **当位**：奇位（1,3,5）应阳爻，偶位（2,4,6）应阴爻

### 动变逻辑
- **本卦**：初始状态 | **变卦**：最终结局 | **互卦**：过程细节

## 使用方法

### 方法一：直接微调（SFT）

使用标准的 instruction-tuning 格式进行有监督微调：

```python
from datasets import load_dataset
from transformers import AutoModelForCausalLM, AutoTokenizer, TrainingArguments, Trainer

# 加载数据集
dataset = load_dataset("json", data_files="liuyao_finetune.jsonl")

# 格式化为对话格式
def format_prompt(sample):
    prompt = f"你是CyberDiviner六爻推演系统。请根据以下信息进行卦象分析。\n\n## 指令\n{sample['instruction']}\n\n## 场景\n{sample['input']}\n\n## 请输出详细的卦象分析：\n"
    return {"prompt": prompt, "response": json.dumps(sample['response'], ensure_ascii=False)}

# 进行微调...
```

### 方法二：Few-shot Prompting

将数据集作为 few-shot 示例嵌入到 prompt 中：

```python
system_prompt = """你是CyberDiviner六爻推演系统，精通周易八卦、五行生克、爻位判定。

输出格式要求：
1. 卦象识别
2. 体用分析
3. 五行能量
4. 爻位解读
5. 动变逻辑
6. 综合判定
7. 务实建议"""

# 从数据集中随机选取 3-5 个样本作为 few-shot 示例
```

### 方法三：评估集

将数据集拆分为训练集和验证集，用于评估微调效果：

```python
# 建议拆分比例：训练 45 样本 / 验证 10 样本
train_dataset = dataset.select(range(45))
eval_dataset = dataset.select(range(45, 55))
```

## 生成脚本

`generate_dataset.py` 是数据集的生成脚本，包含完整的六爻逻辑框架：

- 八卦五行映射
- 生克关系计算
- 旺相休囚死判定
- 爻位含义与当位判定
- 体用关系推导

可基于此脚本扩展更多训练样本。

## 扩展建议

1. **增加样本数量**：目标 500+ 样本以覆盖更多卦象组合
2. **增加问题细分**：如投资理财细分到股票、基金、房产等
3. **增加多爻动场景**：当前均为单爻动，可增加二爻动、三爻动
4. **增加旬空判定**：在月令分析中加入空亡因素
5. **RLHF 对齐**：使用本数据集的判定逻辑作为奖励模型的参考标准

## 注意事项

- 所有判定基于传统六爻推演逻辑，遵循 `reference.md` 中的规则体系
- 模型输出应描述"趋势"而非"宿命"，体现辩证逻辑
- 判定需根据用户具体问题进行语义转换（去语境化原则）
- 强调"吉中藏凶"或"凶中化吉"的动态转化

## 相关文件

- `reference.md` — 完整的术数推演核心逻辑大纲
- `generate_dataset.py` — 数据集生成脚本
- `liuyao_finetune.jsonl` — 训练数据集（55 样本）
