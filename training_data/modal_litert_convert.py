#!/usr/bin/env python3
"""
Convert CyberDiviner Gemma 3 1B merged Hugging Face model to a MediaPipe
Task Bundle (.task) on Modal Linux.

Usage:
  1. pip install modal
  2. modal setup
  3. modal run training_data/modal_litert_convert.py
  4. modal volume get cyberdiviner-litert-outputs gemma3_1b_int4.task ./dist
"""

import modal


APP_NAME = "cyberdiviner-litert-convert"
MODEL_REPO = "Sinonchum/cyberdiviner-gemma-3-1b"
OUTPUT_TASK = "/outputs/gemma3_1b_int4.task"

app = modal.App(APP_NAME)
vol = modal.Volume.from_name("cyberdiviner-litert-outputs", create_if_missing=True)

image = (
    modal.Image.debian_slim(python_version="3.11")
    .apt_install("libgl1", "libglib2.0-0", "libgles2", "libegl1")
    .pip_install(
        "huggingface_hub[hf_xet]",
        "tensorflow-cpu",
        "litert-torch==0.9.1",
        "mediapipe",
    )
)


@app.function(
    image=image,
    volumes={"/outputs": vol},
    secrets=[modal.Secret.from_name("huggingface-token")],
    timeout=7200,
    memory=32768,
    cpu=8,
)
def convert():
    import os
    import glob
    import shutil
    from huggingface_hub import snapshot_download

    from litert_torch.generative.examples.gemma3 import gemma3
    from litert_torch.generative.utilities import converter
    from litert_torch.generative.utilities.export_config import ExportConfig
    from litert_torch.generative.layers import kv_cache
    from mediapipe.tasks.python.genai import bundler

    model_dir = "/model/cyberdiviner-gemma-3-1b"
    tflite_dir = "/outputs/tflite"
    os.makedirs(tflite_dir, exist_ok=True)

    print(f"Downloading {MODEL_REPO} ...")
    snapshot_download(
        repo_id=MODEL_REPO,
        repo_type="model",
        local_dir=model_dir,
        local_dir_use_symlinks=False,
        token=os.environ.get("HF_TOKEN"),
    )
    print("Model files:")
    for path in sorted(glob.glob(f"{model_dir}/*")):
        print(f"  {path}: {os.path.getsize(path) / 1024 / 1024:.1f} MB")

    tflite_files = sorted(glob.glob(f"{tflite_dir}/*.tflite"))
    if not tflite_files:
        print("Building Gemma 3 1B model...")
        pytorch_model = gemma3.build_model_1b(model_dir)

        export_config = ExportConfig()
        export_config.kvcache_layout = kv_cache.KV_LAYOUT_TRANSPOSED
        export_config.mask_as_input = True

        print("Converting to LiteRT .tflite ...")
        converter.convert_to_tflite(
            pytorch_model,
            output_path=tflite_dir,
            output_name_prefix="cyberdiviner-gemma3-1b",
            prefill_seq_len=1024,
            kv_cache_max_len=1024,
            quantize="dynamic_int8",
            export_config=export_config,
        )
        tflite_files = sorted(glob.glob(f"{tflite_dir}/*.tflite"))
    else:
        print("Reusing existing LiteRT .tflite from Modal volume.")

    if not tflite_files:
        raise RuntimeError(f"No .tflite files produced in {tflite_dir}")
    tflite_model = tflite_files[0]
    print(f"LiteRT model: {tflite_model} ({os.path.getsize(tflite_model) / 1024 / 1024:.1f} MB)")
    vol.commit()

    print("Bundling .task ...")
    config = bundler.BundleConfig(
        tflite_model=tflite_model,
        tokenizer_model=f"{model_dir}/tokenizer.model",
        start_token="<bos>",
        stop_tokens=["<eos>", "<end_of_turn>"],
        output_filename=OUTPUT_TASK,
        enable_bytes_to_unicode_mapping=False,
    )
    bundler.create_bundle(config)

    if not os.path.exists(OUTPUT_TASK):
        raise RuntimeError(f"Task bundle not created: {OUTPUT_TASK}")

    task_size = os.path.getsize(OUTPUT_TASK)
    print(f"Task bundle created: {OUTPUT_TASK} ({task_size / 1024 / 1024:.1f} MB)")

    shutil.copy2(OUTPUT_TASK, "/outputs/cyberdiviner-gemma3-1b.task")
    vol.commit()
    print("Done. Download with:")
    print("modal volume get cyberdiviner-litert-outputs gemma3_1b_int4.task ./dist")


@app.local_entrypoint()
def main():
    convert.remote()
