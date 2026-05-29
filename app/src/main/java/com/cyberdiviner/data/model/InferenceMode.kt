package com.cyberdiviner.data.model

/**
 * Inference mode for LLM — controls whether the app uses online (cloud),
 * offline (on-device Gemma), or auto (online-first with offline fallback).
 */
enum class InferenceMode(val displayName: String) {
    AUTO("自动"),
    ONLINE("仅在线"),
    OFFLINE("仅离线");

    companion object {
        fun fromName(name: String): InferenceMode =
            entries.find { it.name == name } ?: AUTO
    }
}
