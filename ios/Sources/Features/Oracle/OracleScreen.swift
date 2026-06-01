import SwiftUI
import DesignSystem
import DivinationCore

public struct OracleScreen: View {

    public init() {}

    @State private var vm = OracleViewModel()
    @State private var inputText = ""
    @State private var messages: [ChatMessage] = []

    public var body: some View {
        VStack(spacing: 0) {
            // Header
            SectionHeader(chineseTitle: "叩问天机", englishSubtitle: "CYBER ORACLE")
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.top, CyberSpacing.xs)

            DividerLine()

            // Messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: CyberSpacing.xs) {
                        ForEach(messages) { msg in
                            MessageBubble(message: msg)
                                .id(msg.id)
                        }
                        if vm.isLoading {
                            HStack {
                                Text("正在解读...")
                                    .font(CyberTypography.bodyMedium)
                                    .foregroundStyle(CyberColors.grayCaption)
                                    .padding(.horizontal, CyberSpacing.sm)
                                Spacer()
                            }
                        }
                    }
                    .padding(.vertical, CyberSpacing.xs)
                }
                .onChange(of: messages.count) {
                    withAnimation {
                        proxy.scrollTo(messages.last?.id, anchor: .bottom)
                    }
                }
            }

            DividerLine()

            // Input area
            HStack(spacing: CyberSpacing.xs) {
                TextEditor(text: $inputText)
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.cyberWhite)
                    .scrollContentBackground(.hidden)
                    .background(CyberColors.graySurface)
                    .frame(minHeight: 36, maxHeight: 80)
                    .overlay(
                        RoundedRectangle(cornerRadius: 0)
                            .stroke(CyberColors.grayBorder, lineWidth: 1)
                    )

                CyberButton("叩") {
                    send()
                }
                .frame(width: 48)
            }
            .padding(.horizontal, CyberSpacing.sm)
            .padding(.vertical, CyberSpacing.xs)
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func send() {
        let text = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }

        let userMsg = ChatMessage(role: .user, content: text)
        messages.append(userMsg)
        inputText = ""

        Task {
            if let result = await vm.sendQuestion(text) {
                let aiMsg = ChatMessage(
                    role: .ai,
                    content: formatOracleResult(result)
                )
                messages.append(aiMsg)
            }
        }
    }

    private func formatOracleResult(_ result: OracleResult) -> String {
        var s = ""
        if !result.verse.isEmpty {
            s += "[ 载入签文 ]\n\(result.verse)\n\n"
        }
        if !result.analysis.isEmpty {
            s += "[ 逻辑解析 ]\n\(result.analysis)\n\n"
        }
        if !result.verdict.isEmpty {
            s += "[ 最终断语 ]\n\(result.verdict)"
        }
        return s.isEmpty ? result.rawText : s
    }
}

// MARK: - Chat Message Model

struct ChatMessage: Identifiable {
    enum Role { case user, ai }
    let id = UUID()
    let role: Role
    let content: String
}

// MARK: - Message Bubble

struct MessageBubble: View {
    let message: ChatMessage

    public var body: some View {
        HStack {
            if message.role == .user { Spacer(minLength: 48) }

            VStack(alignment: message.role == .ai ? .leading : .trailing, spacing: 2) {
                Text(message.content)
                    .font(message.role == .ai
                          ? CyberTypography.titleSmall
                          : CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.cyberWhite)
                    .padding(CyberSpacing.xs)
                    .background(
                        message.role == .ai
                            ? CyberColors.graySurface
                            : CyberColors.grayBorder
                    )
            }

            if message.role == .ai { Spacer(minLength: 48) }
        }
        .padding(.horizontal, CyberSpacing.sm)
    }
}
