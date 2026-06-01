import SwiftUI

public struct StatusLine: View {
    let text: String

    public init(_ text: String) {
        self.text = text
    }

    public var body: some View {
        Text(text)
            .font(CyberTypography.monoSmall)
            .foregroundStyle(CyberColors.grayCaption)
            .lineLimit(1)
            .truncationMode(.tail)
    }
}
