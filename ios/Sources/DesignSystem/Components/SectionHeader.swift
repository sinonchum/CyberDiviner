import SwiftUI

public struct SectionHeader: View {
    let chineseTitle: String
    let englishSubtitle: String

    public init(chineseTitle: String, englishSubtitle: String) {
        self.chineseTitle = chineseTitle
        self.englishSubtitle = englishSubtitle
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(chineseTitle)
                .font(CyberTypography.titleMedium)
                .foregroundStyle(CyberColors.grayTitle)

            // Red underline
            Rectangle()
                .fill(CyberColors.accentRed)
                .frame(width: 48, height: 2)

            Text(englishSubtitle)
                .font(CyberTypography.monoSmall)
                .foregroundStyle(CyberColors.grayCaption)
                .padding(.top, 2)
        }
    }
}
