import SwiftUI

/// Haptic feedback utility matching Android HapticUtils.kt
public enum HapticType {
    case light
    case medium
    case heavy
    case success
    case warning
}

public enum HapticUtils {
    public static func trigger(_ type: HapticType) {
        switch type {
        case .light:
            let generator = UIImpactFeedbackGenerator(style: .light)
            generator.impactOccurred()
        case .medium:
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
        case .heavy:
            let generator = UIImpactFeedbackGenerator(style: .heavy)
            generator.impactOccurred()
        case .success:
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(.success)
        case .warning:
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(.warning)
        }
    }
}

// MARK: - View Modifier

private struct CyberHapticModifier: ViewModifier {
    func body(content: Content) -> some View {
        content.onTapGesture {
            HapticUtils.trigger(.light)
        }
    }
}

extension View {
    /// Triggers a light haptic on tap
    public func cyberHaptic() -> some View {
        modifier(CyberHapticModifier())
    }
}
