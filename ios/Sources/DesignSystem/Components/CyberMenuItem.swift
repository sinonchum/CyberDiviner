import SwiftUI

public struct CyberMenuItem: View {
    let title: String
    let subtitle: String
    let description: String
    let action: () -> Void

    @State private var isPressed = false

    public init(title: String, subtitle: String, description: String = "", action: @escaping () -> Void) {
        self.title = title
        self.subtitle = subtitle
        self.description = description
        self.action = action
    }

    public var body: some View {
        HStack(spacing: 0) {
            // Red accent bar on press
            Rectangle()
                .fill(isPressed ? CyberColors.accentRed : .clear)
                .frame(width: 3)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.grayTitle)
                Text(subtitle)
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayCaption)
                if !description.isEmpty {
                    Text(description)
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayMuted)
                }
            }
            .padding(.horizontal, CyberSpacing.sm)
            .padding(.vertical, CyberSpacing.xs)

            Spacer()
        }
        .background(CyberColors.cyberBlack)
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
