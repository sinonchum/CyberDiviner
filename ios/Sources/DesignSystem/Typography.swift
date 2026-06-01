import SwiftUI

public struct CyberTypography {
    /// Classical Chinese titles — HuiWen MingChao, fallback to serif
    public static let titleLarge = Font.custom("HuiwenMingChao", size: 32)
        .serif()
    public static let titleMedium = Font.custom("HuiwenMingChao", size: 24)
        .serif()
    public static let titleSmall = Font.custom("HuiwenMingChao", size: 20)
        .serif()

    /// Body / dialogue — LXGW WenKai, fallback to system
    public static let bodyLarge = Font.custom("LXGWWenKai", size: 18)
    public static let bodyMedium = Font.custom("LXGWWenKai", size: 16)
    public static let bodySmall = Font.custom("LXGWWenKai", size: 14)

    /// Metadata / hashes — JetBrains Mono, monospaced
    public static let monoMedium = Font.custom("JetBrainsMono", size: 14)
        .monospaced()
    public static let monoSmall = Font.custom("JetBrainsMono", size: 12)
        .monospaced()
    public static let monoCaption = Font.custom("JetBrainsMono", size: 10)
        .monospaced()
}
