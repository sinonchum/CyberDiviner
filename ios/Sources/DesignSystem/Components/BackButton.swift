import SwiftUI

/// Back button matching Android '< 返回' style
public struct BackButton: View {
    let action: () -> Void

    public init(action: @escaping () -> Void) {
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            Text("< 返回")
                .font(.custom("Huiwen-mincho", size: 13))
                .foregroundColor(CyberColors.grayCaption)
        }
    }
}
