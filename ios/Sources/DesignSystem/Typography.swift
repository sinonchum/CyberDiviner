import SwiftUI

public struct CyberTypography {
    /// Classical Chinese titles — Huiwen-mincho
    public static let titleLarge = Font.custom("Huiwen-mincho", size: 32)
    public static let titleMedium = Font.custom("Huiwen-mincho", size: 24)
    public static let titleSmall = Font.custom("Huiwen-mincho", size: 20)

    /// Body / dialogue — LXGW WenKai
    public static let bodyLarge = Font.custom("LXGWWenKai-Regular", size: 18)
    public static let bodyMedium = Font.custom("LXGWWenKai-Regular", size: 16)
    public static let bodySmall = Font.custom("LXGWWenKai-Regular", size: 14)

    /// Metadata / hashes — JetBrains Mono
    public static let monoMedium = Font.custom("JetBrainsMono-Regular", size: 14)
    public static let monoSmall = Font.custom("JetBrainsMono-Regular", size: 12)
    public static let monoCaption = Font.custom("JetBrainsMono-Regular", size: 10)
}
