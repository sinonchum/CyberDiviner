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

### 0. Colab 训练路径（推荐）

本仓库提供完整 Colab notebook：

```
training_data/CyberDiviner_Gemma3_1B_Finetune.ipynb
```

使用 Unsloth + QLoRA，免费 T4 GPU 即可运行。上传 `cyberdiviner_sft_dataset.jsonl` 后按顺序执行所有 cell，最终下载 GGUF 文件和合并后的 HF 模型。

注意：

1. Colab 负责训练和导出中间产物。
2. App 最终需要的是 LiteRT-LM `.task` 文件，不是 GGUF。
3. 如果 notebook 只导出了 GGUF 和 HF merged model，需要再用 Google AI Edge / LiteRT-LM 转换工具生成 `.task`。
4. Mimo 交付时必须同时给出训练日志、测试输出和模型文件 hash，不能只给一个模型文件。

### 0.1 傻瓜式 Colab 操作流程

适合不熟悉训练的人照着执行。

#### 第一步：准备账号和权限

需要准备：

1. 一个 Google 账号，用来打开 Google Colab。
2. 一个 Hugging Face 账号。
3. 在 Hugging Face 打开 Gemma 3 1B instruction model 页面，接受 Google/Gemma 模型许可。
4. 在 Hugging Face 创建 Access Token，权限选择 read 即可。

成功标志：

1. 能在浏览器里访问 Gemma 3 1B 模型页面。
2. Hugging Face token 能复制出来，格式通常以 `hf_` 开头。

#### 第二步：打开 notebook

操作：

1. 打开 Google Colab。
2. 选择 `File` -> `Upload notebook`。
3. 上传本仓库里的：

```text
training_data/CyberDiviner_Gemma3_1B_Finetune.ipynb
```

成功标志：

1. 页面标题显示 `CyberDiviner Gemma 3 1B QLoRA 微调`。
2. 页面里能看到多个可运行的代码格。

#### 第三步：切换 GPU

操作：

1. Colab 顶部菜单选择 `Runtime`。
2. 点击 `Change runtime type`。
3. Hardware accelerator 选择 `T4 GPU`。
4. 保存。

成功标志：

运行 notebook 里的 GPU 检查 cell 后，应该看到类似：

```text
CUDA: True
GPU: Tesla T4
VRAM: 14.x GB
```

如果显示 `CUDA: False`，说明没有开 GPU，必须回到这一步重设。

#### 第四步：配置 Hugging Face Token

推荐方式：

1. 点击 Colab 左侧的钥匙图标 `Secrets`。
2. 新增 secret：

```text
Name: HF_TOKEN
Value: 你的 Hugging Face token
```

3. 打开 notebook 对该 secret 的访问权限。

成功标志：

模型加载 cell 不再报 `401 Unauthorized`、`gated repo` 或 `token` 相关错误。

常见失败：

1. 没接受 Gemma 许可：去 Hugging Face 模型页接受协议。
2. token 权限不够：重新创建 read token。
3. secret 没授权给 notebook：在 Colab 左侧 Secrets 面板打开开关。

#### 第五步：上传数据集

运行 notebook 里的上传数据 cell。

弹窗出现后，上传：

```text
training_data/cyberdiviner_sft_dataset.jsonl
```

成功标志：

输出里应该看到文件名和文件大小，并且数据检查 cell 显示：

```text
Total samples: 78
Quality check: 0 issues
```

如果样本数不是 78：

1. 检查是否上传错文件。
2. 在本地重新运行：

```bash
python training_data/generate_cyberdiviner_sft_dataset.py
```

3. 再上传新生成的 JSONL。

#### 第六步：按顺序运行所有 cell

操作方式：

1. 从上到下逐个点击每个 cell 左侧的运行按钮。
2. 不要跳过环境安装、模型加载、LoRA 配置、数据准备、训练、测试、导出这些步骤。
3. 如果中间报错，停止，不要继续运行后面的 cell。

关键成功标志：

1. 安装依赖完成，没有红色异常。
2. 模型加载成功，显示 `Model loaded`。
3. LoRA 配置成功，显示 trainable parameters。
4. 数据集加载成功，显示各功能样本数量。
5. 训练 cell 开始滚动 loss。
6. loss 不需要降到很低，但不应为 `nan`。
7. 测试 cell 输出中文成品，而不是格式说明。

#### 第七步：看测试输出是否合格

训练完成后，必须运行 notebook 的测试 cell。

人工检查每条输出：

1. 叩问天机必须有 `[ 载入签文 ]`、`[ 逻辑解析 ]`、`[ 最终断语 ]`。
2. 签文必须是两行，每行两个分句。
3. 塔罗不能输出 `1到2句话`、`2句话`、牌位数字串、英文解释正文。
4. 六爻不能输出 `12222`、重复的断卦/解析。
5. 视界摸骨不能输出 `px`、比例、小数、MediaPipe 字段、英文特征名。
6. 不能出现 `请知会本地先知`、`作为AI`、`格式如下`。
7. 不能连续重复同一句或同一短语。

如果测试不合格：

1. 不要直接交付模型。
2. 把失败输出保存下来。
3. 增加对应反例到数据集。
4. 重新训练一次。

#### 第八步：下载产物

notebook 最后会下载：

1. GGUF Q4 文件。
2. 合并后的 HF 模型 zip。
3. LoRA adapter 可作为备份。

需要保留并交付：

```text
cyberdiviner_gguf/*.gguf
cyberdiviner-gemma3-1b-merged.zip
lora_model/
训练日志截图或文本
测试输出文本
```

如果浏览器下载大文件失败：

1. 先保存到 Google Drive。
2. 再从 Google Drive 下载。
3. 不要只交付 notebook 输出截图。

#### 第九步：转换为 App 可用 `.task`

Colab 导出的 GGUF/HF 模型还不能直接放进当前 Android App。

下一步必须转换为 LiteRT-LM `.task`：

```text
gemma3_1b_int4.task
```

转换时必须确认：

1. tokenizer 和模型版本匹配。
2. quantization 是 int4。
3. context length 不低于当前 App 使用值。
4. 输出文件能被当前 LiteRT-LM runtime 初始化。

成功标志：

adb logcat 中应看到：

```text
LiteRT-LM initialized
Offline generation complete
```

如果只看到 fallback 或 repair，不代表模型不可用，但必须继续检查原始离线输出质量。

#### 第十步：交给 Codex 最终验收

Mimo 完成后，把以下文件和信息交给我：

1. `.task` 文件。
2. `.task` 文件大小。
3. `.task` 文件 SHA256 hash。
4. 使用的数据集版本。
5. 训练参数。
6. 训练 loss 日志。
7. notebook 测试 cell 的完整输出。
8. 40 条固定验收 prompt 的原始输出。
9. 转换工具版本。

我会做：

1. 替换 App 模型。
2. 真机安装。
3. Config 设置为“仅离线”。
4. 跑叩问天机、赛博塔罗、周易六爻、视界摸骨。
5. 抓 logcat 确认确实走离线 LLM。
6. 对照格式、重复、禁词、速度和崩溃情况给最终通过/不通过结论。

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
