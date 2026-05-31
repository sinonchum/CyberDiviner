# CyberDiviner Gemma 3 1B int4 微调计划

## 目标

把端侧模型训练成 CyberDiviner 的“格式稳定器”和“古风占卜文案生成器”，优先解决以下问题：

1. 严格遵守各页面固定输出格式。
2. 叩问天机输出四句签诗，不出现“祝你一切顺利”等普通祝福。
3. 塔罗、六爻、视界摸骨不输出数字串、英文牌名、提示词、字段名、px、比例、小数。
4. 不重复循环，不出现同一句或短语刷屏。
5. 输出适合端侧短上下文：短、完整、可展示。

微调不是替代工程质量闸。App 仍需保留格式归一化、低质检测和 fallback 修复。

## 模型与路线

首选路线：

1. 基座：Gemma 3 1B instruction model。
2. 训练方式：LoRA / QLoRA SFT。
3. 训练精度：训练时用 4-bit QLoRA 或 bf16 LoRA，导出时再转 LiteRT int4。
4. 端侧部署：转换为 `.task`，替换 `offline_model/gemma3_1b_int4.task`。

不建议直接切 int8 作为首要方案：

1. int8 可能略改善语言质量，但内存和启动压力更大。
2. 当前日志证明模型能启动，主要问题是格式和重复退化。
3. 重复退化需要训练数据、采样参数、长度限制和质量闸共同解决。

## 依赖

训练侧：

1. Python 3.10+。
2. `transformers`、`datasets`、`peft`、`trl`、`accelerate`。
3. CUDA GPU，建议显存 12GB+；如果只有 8GB，降低 batch size 并启用 gradient checkpointing。
4. Gemma 3 1B 权重访问权限。

转换侧：

1. Google AI Edge / LiteRT-LM 转换工具链。
2. 能生成 Android 可加载的 `.task` 文件。
3. 和当前 App 中 LiteRT-LM 版本兼容。

App 侧：

1. 保留 `InferenceRouter`。
2. 保留 `OfflinePromptBuilder`。
3. 保留所有页面的低质检测。
4. 用 adb logcat 验证 `LiteRT-LM initialized` 与 `Offline generation complete`。

## 数据集

本仓库提供：

1. `cyberdiviner_sft_dataset.jsonl`：统一 SFT 数据集。
2. `generate_cyberdiviner_sft_dataset.py`：可复现生成脚本。
3. 旧版 `liuyao_finetune.jsonl`：六爻规则样本，可作为补充。

推荐先用本仓库数据做第一版小规模 SFT，再逐步加入真实在线 LLM 高质量输出。

数据格式：

```json
{
  "feature": "oracle",
  "messages": [
    {"role": "system", "content": "..."},
    {"role": "user", "content": "..."},
    {"role": "assistant", "content": "..."}
  ]
}
```

## 训练步骤

### 1. 数据准备

运行：

```bash
python training_data/generate_cyberdiviner_sft_dataset.py
```

检查：

1. JSONL 每行能被解析。
2. `assistant` 内容不含英文格式说明。
3. `assistant` 内容不含“请知会”“本地先知”“作为AI”“我不能”等字样。
4. 单条输出长度控制在端侧可展示范围。

### 2. 数据拆分

建议拆分：

1. 训练集 80%。
2. 验证集 10%。
3. 固定验收集 10%。

验收集必须覆盖：

1. 事业、感情、财运、健康。
2. 塔罗单牌、三牌、五牌。
3. 六爻有动爻、无动爻。
4. 视界摸骨不同脸型和五官特征。

### 3. SFT 训练

建议参数：

1. LoRA rank：8 或 16。
2. learning rate：`1e-4` 到 `2e-4`。
3. epoch：3 到 5。
4. max sequence length：1024。
5. batch size：按显存设置，优先稳定。
6. warmup ratio：0.03。
7. weight decay：0.01。

为什么这样做：

1. 本项目不是训练知识，而是训练输出格式和文风。
2. rank 太大容易过拟合小数据。
3. max length 1024 与端侧 LiteRT-LM 上下文更接近。

### 4. 合并与转换

1. 合并 LoRA 到基座或导出 adapter 后合并。
2. 用 LiteRT-LM 支持的转换链生成 int4 `.task`。
3. 文件命名保持 `gemma3_1b_int4.task`，或更新 `GemmaEngine.MODEL_FILENAME`。
4. 在 Android 设备上删除旧模型后导入新模型。

风险：

1. 转换工具链版本不一致会导致 App 初始化失败。
2. int4 量化可能损失格式遵循能力。
3. 需要保留 App 侧质量闸兜底。

### 5. App 集成

功能点：

1. Config 页面显示模型版本，如 `CyberDiviner Gemma 1B int4 v0.1`。
2. 本地模型文件校验大小和 hash。
3. 离线推理日志增加模型版本。
4. 结果页可在 debug 日志中标记 `source=offline_llm` 或 `source=repair`。

并行开发：

1. Mimo 训练模型。
2. Codex 维护 App 端质量闸和 UI。
3. 另一路整理在线 LLM 高质量样本，扩充数据集。

## 验收标准

### 自动验收

对固定 40 条 prompt 跑离线模型，要求：

1. 格式通过率 >= 95%。
2. 重复循环率 = 0。
3. 英文泄漏率 = 0，塔罗真实英文牌名除训练输入外不得进入中文解读正文。
4. 禁词命中率 = 0：`请知会`、`本地先知`、`作为AI`、`格式如下`、`1到2句话`、`px`。
5. 端侧单次生成不超过 45 秒。
6. App 不崩溃，不触发系统杀进程。

### 人工验收

我最后验收以下流程：

1. Config 设为“仅离线”。
2. 叩问天机问事业、感情、财运各一轮。
3. 赛博塔罗抽单牌和三牌。
4. 周易起卦完成一次六爻。
5. 视界摸骨完成一次扫描。
6. 因果命簿检查四字批命、摘要和分享图。

每个页面要求：

1. 视觉格式与在线模型一致。
2. 无重复刷屏。
3. 无提示词泄漏。
4. 无 fallback 口吻。
5. 输出可读、有古风质感，但不作绝对定命。

## 迭代计划

### v0.1 格式稳定版

数据量：80 到 120 条。

目标：

1. 固定输出格式。
2. 不重复。
3. 不泄漏提示词。

### v0.2 文风增强版

数据量：300 到 500 条。

目标：

1. 古风表达更自然。
2. 四字批命更丰富。
3. 针对事业、感情、财运、健康有差异化表达。

### v0.3 产品化版

数据量：1000 条以上，加入真实在线 LLM 高分样本。

目标：

1. 端侧质量接近在线模型短输出。
2. 低质修复触发率 < 5%。
3. 平均生成时间稳定。

## 交付物

Mimo 需要交付：

1. 训练脚本。
2. 训练日志。
3. 数据集版本号。
4. LoRA adapter。
5. 转换后的 `.task` 文件。
6. 40 条验收 prompt 的原始输出。
7. 模型大小、hash、端侧加载日志。

Codex 最后验收：

1. 安装到真机。
2. 跑完整 App 流程。
3. 抓 logcat。
4. 对照验收标准给通过/不通过结论。
