import SwiftUI

public struct DividerLine: View {
    public init() {}

    public var body: some View {
        Rectangle()
            .fill(CyberColors.grayBorder)
            .frame(height: 1)
            .frame(maxWidth: .infinity)
    }
}
