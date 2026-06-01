import SwiftUI

/// Simplified voice input field matching Android VoiceInput.kt
public struct VoiceInputField: View {
    @Binding var text: String
    let placeholder: String
    let onSend: () -> Void

    @FocusState private var isFocused: Bool

    public init(text: Binding<String>, placeholder: String, onSend: @escaping () -> Void) {
        self._text = text
        self.placeholder = placeholder
        self.onSend = onSend
    }

    public var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: CyberSpacing.xs) {
                TextField(placeholder, text: $text)
                    .font(CyberTypography.bodySmall)
                    .foregroundColor(CyberColors.grayTitle)
                    .tint(CyberColors.grayTitle)
                    .focused($isFocused)
                    .submitLabel(.send)
                    .onSubmit { sendAction() }

                // Send button — inverts when text present
                Button(action: sendAction) {
                    Text("↑")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(text.isEmpty ? CyberColors.grayCaption : CyberColors.cyberBlack)
                        .frame(width: 32, height: 32)
                        .background(text.isEmpty ? Color.clear : CyberColors.cyberWhite)
                        .overlay(
                            RoundedRectangle(cornerRadius: 0)
                                .stroke(text.isEmpty ? CyberColors.grayCaption : CyberColors.cyberWhite, lineWidth: 1)
                        )
                }
                .disabled(text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            }
            .padding(.horizontal, CyberSpacing.sm)
            .padding(.vertical, CyberSpacing.xs)

            // Bottom 1px border line
            Rectangle()
                .fill(CyberColors.grayBorder)
                .frame(height: 1)
        }
    }

    private func sendAction() {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        onSend()
    }
}
