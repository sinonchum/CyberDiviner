#!/usr/bin/env python3
"""
CyberDiviner Gemma 3 1B QLoRA fine-tuning on Modal serverless GPU.

Usage:
  1. pip install modal
  2. modal setup  (paste token from modal.com/settings)
  3. modal run training_data/modal_finetune.py

Cost: ~$0.5-1 on T4 (15 min training)
"""

import modal
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
DATASET_FILE = REPO_ROOT / "training_data" / "cyberdiviner_sft_dataset.jsonl"

app = modal.App("cyberdiviner-finetune")

# Persistent volume for outputs
vol = modal.Volume.from_name("cyberdiviner-outputs", create_if_missing=True)

# Container image with all deps
image = (
    modal.Image.debian_slim(python_version="3.11")
    .pip_install(
        "unsloth",
        "protobuf==5.29.4",
        "datasets",
        "transformers>=4.51.0",
    )
    .add_local_file(
        str(DATASET_FILE),
        "/data/cyberdiviner_sft_dataset.jsonl",
    )
)

@app.function(
    gpu="T4",
    image=image,
    volumes={"/outputs": vol},
    timeout=1800,  # 30 min max
)
def train():
    import torch
    import json
    import random
    import os
    import time
    from collections import Counter

    from unsloth import FastLanguageModel
    from trl import SFTTrainer, SFTConfig
    from datasets import Dataset

    print(f"GPU: {torch.cuda.get_device_name(0)}")
    print(f"VRAM: {round(torch.cuda.get_device_properties(0).total_mem/1024**3,1)} GB")

    # ---- Load model ----
    max_seq_length = 1024
    model, tokenizer = FastLanguageModel.from_pretrained(
        model_name="unsloth/gemma-3-1b-it",
        max_seq_length=max_seq_length,
        load_in_4bit=True,
        dtype=None,
    )
    print(f"Model loaded, VRAM: {round(torch.cuda.memory_allocated()/1024**3, 2)} GB")

    # ---- LoRA ----
    model = FastLanguageModel.get_peft_model(
        model,
        r=16,
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj",
                        "gate_proj", "up_proj", "down_proj"],
        lora_alpha=32,
        lora_dropout=0.05,
        bias="none",
        use_gradient_checkpointing="unsloth",
        random_state=42,
    )
    model.print_trainable_parameters()

    # ---- Data ----
    raw_data = []
    with open("/data/cyberdiviner_sft_dataset.jsonl", "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                raw_data.append(json.loads(line))

    print(f"Total samples: {len(raw_data)}")
    features = Counter(item["feature"] for item in raw_data)
    for feat, count in sorted(features.items()):
        print(f"  {feat}: {count}")

    # Quality check
    bad_words = ["请知会", "本地先知", "作为AI", "格式如下", "1到2句话", "px"]
    bad_count = 0
    for item in raw_data:
        for msg in item["messages"]:
            if msg["role"] == "assistant":
                for bw in bad_words:
                    if bw in msg["content"]:
                        print(f"  ⚠️ '{bw}' found!")
                        bad_count += 1
    print(f"Quality check: {bad_count} issues")

    # Format
    def format_sample(item):
        text = tokenizer.apply_chat_template(
            item["messages"], tokenize=False, add_generation_prompt=False
        )
        return text

    all_texts = [format_sample(item) for item in raw_data]
    all_features = [item["feature"] for item in raw_data]

    # Split
    paired = list(zip(all_texts, all_features))
    random.seed(42)
    random.shuffle(paired)
    split_idx = int(len(paired) * 0.9)

    train_dataset = Dataset.from_dict({"text": [t for t, _ in paired[:split_idx]]})
    val_dataset = Dataset.from_dict({"text": [t for t, _ in paired[split_idx:]]})
    print(f"Train: {len(train_dataset)}, Val: {len(val_dataset)}")

    # ---- Train ----
    training_args = SFTConfig(
        output_dir="/outputs/cyberdiviner-gemma3-1b-qlora",
        num_train_epochs=5,
        per_device_train_batch_size=2,
        gradient_accumulation_steps=4,
        learning_rate=2e-4,
        lr_scheduler_type="cosine",
        warmup_ratio=0.03,
        weight_decay=0.01,
        max_seq_length=max_seq_length,
        logging_steps=5,
        eval_strategy="epoch",
        save_strategy="epoch",
        save_total_limit=2,
        load_best_model_at_end=True,
        metric_for_best_model="eval_loss",
        bf16=True,
        seed=42,
        dataset_text_field="text",
        dataset_num_proc=2,
        packing=False,
    )

    trainer = SFTTrainer(
        model=model,
        args=training_args,
        train_dataset=train_dataset,
        eval_dataset=val_dataset,
        processing_class=tokenizer,
    )

    print("Starting training...")
    start = time.time()
    result = trainer.train()
    elapsed = time.time() - start
    print(f"Training done in {elapsed/60:.1f} min, loss: {result.training_loss:.4f}")

    # Save adapter
    trainer.save_model("/outputs/lora_adapter")
    tokenizer.save_pretrained("/outputs/lora_adapter")

    # ---- Inference test ----
    FastLanguageModel.for_inference(model)
    test_prompts = [
        {"system": "你是CyberDiviner叩问天机。只输出[ 载入签文 ][ 逻辑解析 ][ 最终断语 ]。签文必须四句古风短句，不要祝福语，不要直接复述问题。",
         "user": "问题：下个月面试能成功吗？"},
        {"system": "你是CyberDiviner赛博塔罗。只输出塔罗解读、牌阵总论、逐牌详析、最终指引。中文成品，不要数字代号，不要提示词。",
         "user": "牌面：月亮（正位）。问题：感情走向。"},
    ]
    for p in test_prompts:
        msgs = [{"role": "system", "content": p["system"]},
                {"role": "user", "content": p["user"]}]
        inp = tokenizer.apply_chat_template(msgs, tokenize=False, add_generation_prompt=True)
        inputs = tokenizer(inp, return_tensors="pt").to("cuda")
        with torch.no_grad():
            out = model.generate(**inputs, max_new_tokens=300, temperature=0.7,
                                 top_p=0.9, repetition_penalty=1.1, use_cache=True)
        resp = tokenizer.decode(out[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True)
        print(f"\n{'='*50}\n{p['user']}\n{'='*50}\n{resp}")

    # ---- Merge + GGUF ----
    print("\nSaving LoRA adapter for GGUF conversion...")
    model.save_pretrained("/outputs/lora_model")
    tokenizer.save_pretrained("/outputs/lora_model")

    # Merge to 16-bit
    print("Merging LoRA into base model (16-bit)...")
    base_model, base_tok = FastLanguageModel.from_pretrained(
        model_name="unsloth/gemma-3-1b-it",
        max_seq_length=max_seq_length,
        load_in_4bit=False,
        dtype=torch.float16,
    )
    from peft import PeftModel
    merged = PeftModel.from_pretrained(base_model, "/outputs/lora_model")
    merged = merged.merge_and_unload()
    merged.save_pretrained("/outputs/cyberdiviner-gemma3-1b-merged", safe_serialization=True)
    base_tok.save_pretrained("/outputs/cyberdiviner-gemma3-1b-merged")

    # GGUF export
    print("Converting to GGUF Q4_K_M...")
    # Re-load for Unsloth GGUF export
    model2, tok2 = FastLanguageModel.from_pretrained(
        model_name="/outputs/cyberdiviner-gemma3-1b-merged",
        max_seq_length=max_seq_length,
        load_in_4bit=False,
        dtype=torch.float16,
    )
    model2 = FastLanguageModel.get_peft_model(model2, r=16,
        target_modules=["q_proj","k_proj","v_proj","o_proj","gate_proj","up_proj","down_proj"],
        lora_alpha=32, lora_dropout=0.05, bias="none")
    # Load adapter weights
    from peft import PeftModel as PM
    model2 = PM.from_pretrained(model2, "/outputs/lora_adapter")
    model2 = model2.merge_and_unload()
    model2.save_pretrained_gguf("/outputs/gguf", tok2, quantization_method="q4_k_m")

    # List outputs
    for root, dirs, files_list in os.walk("/outputs"):
        for fn in files_list:
            fp = os.path.join(root, fn)
            sz = os.path.getsize(fp)
            print(f"  {fp}: {sz/1024**2:.1f} MB")

    vol.commit()
    print("\nAll done. Download with:")
    print("  modal volume get cyberdiviner-outputs /outputs/gguf/ --dest ./cyberdiviner_gguf")
    print("  modal volume get cyberdiviner-outputs /outputs/cyberdiviner-gemma3-1b-merged/ --dest ./merged")


@app.local_entrypoint()
def main():
    train.remote()
