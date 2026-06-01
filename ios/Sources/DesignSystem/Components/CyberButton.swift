import SwiftUI

public struct CyberButton: View {
    let title: String
    let action: () -> Void

    @State private var isPressed = false

    public init(_ title: String, action: @escaping () -> Void) {
        self.title = title
        self.action = action
    }

    public var body: some View {
        Text(title)
            .font(CyberTypography.bodyMedium)
            .foregroundStyle(isPressed ? CyberColors.cyberBlack : CyberColors.cyberWhite)
            .padding(.horizontal, CyberSpacing.sm)
            .padding(.vertical, CyberSpacing.xs)
            .frame(maxWidth: .infinity)
            .background(isPressed ? CyberColors.cyberWhite : CyberColors.cyberBlack)
            .overlay(
                Rectangle()
                    .stroke(CyberColors.grayBorder, lineWidth: 1)
            )
            .contentShape(Rectangle())
            .onLongPressGesture(
                minimumDuration: .infinity,
                pressing: { pressing in
                    withAnimation(.easeInOut(duration: 0.1)) {
                        isPressed = pressing
                    }
                },
                perform: {}
            )
            .simultaneousGesture(
                TapGesture().onEnded {
                    action()
                }
            )
    }
}
