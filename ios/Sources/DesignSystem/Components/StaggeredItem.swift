import SwiftUI

/// Fade-in + slide-up animation matching Android SharedPrefabs.kt staggered items
public struct StaggeredItem<Content: View>: View {
    let index: Int
    let content: () -> Content

    @State private var appeared = false

    public init(index: Int, @ViewBuilder content: @escaping () -> Content) {
        self.index = index
        self.content = content
    }

    public var body: some View {
        content()
            .opacity(appeared ? 1 : 0)
            .offset(y: appeared ? 0 : 20)
            .onAppear {
                withAnimation(
                    .easeOut(duration: 0.4)
                    .delay(Double(index) * 0.08)
                ) {
                    appeared = true
                }
            }
    }
}
