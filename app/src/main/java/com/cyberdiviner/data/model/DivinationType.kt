package com.cyberdiviner.data.model

/**
 * Supported divination methods in CyberDiviner.
 */
enum class DivinationType(val displayName: String, val icon: String) {
    ORACLE("天机", ""),
    LIUYAO("六爻", ""),
    TAROT("塔罗", ""),
    VISION("面相", ""),
    MUYU("颂钵", "")
}
