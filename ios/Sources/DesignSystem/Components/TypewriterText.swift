import SwiftUI

/// Character-by-character text reveal matching Android TypewriterText.kt
public struct TypewriterText: View {
    let text: String
    let charDelayMs: Double

    @State private var displayedCount: Int = 0
    @State private var cursorVisible: Bool = false
    @State private var typingDone: Bool = false

    public init(text: String, charDelayMs: Double = 30) {
        self.text = text
        self.charDelayMs = charDelayMs
    }

    public var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: 0) {
            Text(displayedText)
                .font(CyberTypography.bodyMedium)
                .foregroundColor(CyberColors.grayTitle)

            if typingDone {
                Text("_")
                    .font(CyberTypography.bodyMedium)
                    .foregroundColor(CyberColors.grayTitle)
                    .opacity(cursorVisible ? 1 : 0)
                    .onAppear { startCursorBlink() }
            }
        }
        .onAppear {
            HapticUtils.trigger(.light)
            startTyping()
        }
    }

    private var displayedText: String {
        let count = min(displayedCount, text.count)
        return String(text.prefix(count))
    }

    private func startTyping() {
        typingDone = false
        displayedCount = 0

        guard !text.isEmpty else {
            typingDone = true
            return
        }

        let totalChars = text.count
        for i in 0..<totalChars {
            DispatchQueue.main.asyncAfter(deadline: .now() + charDelayMs * Double(i) / 1000.0) {
                displayedCount = i + 1
                if i == totalChars - 1 {
                    typingDone = true
                }
            }
        }
    }

    private func startCursorBlink() {
        Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { _ in
            withAnimation(.easeInOut(duration: 0.1)) {
                cursorVisible.toggle()
            }
        }
    }
}
