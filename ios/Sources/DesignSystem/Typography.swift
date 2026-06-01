import SwiftUI

public struct CyberTypography {
    /// Classical Chinese titles — HuiWen MingChao
    public static let titleLarge = Font.custom("HuiwenMingChao", size: 32)
    public static let titleMedium = Font.custom("HuiwenMingChao", size: 24)
    public static let titleSmall = Font.custom("HuiwenMingChao", size: 20)

    /// Body / dialogue — LXGW WenKai
    public static let bodyLarge = Font.custom("LXGWWenKai", size: 18)
    public static let bodyMedium = Font.custom("LXGWWenKai", size: 16)
    public static let bodySmall = Font.custom("LXGWWenKai", size: 14)

    /// Metadata / hashes — JetBrains Mono
    public static let monoMedium = Font.custom("JetBrainsMono", size: 14)
    public static let monoSmall = Font.custom("JetBrainsMono", size: 12)
    public static let monoCaption = Font.custom("JetBrainsMono", size: 10)
}
