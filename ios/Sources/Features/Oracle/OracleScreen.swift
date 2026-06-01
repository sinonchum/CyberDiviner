import SwiftUI
import DesignSystem
import DivinationCore

public struct OracleScreen: View {

    public init() {}

    @State private var vm = OracleViewModel()
    @State private var inputText = ""
    @State private var scrollToBottom = false

    public var body: some View {
        VStack(spacing: 0) {
            // Header
            SectionHeader(chineseTitle: "叩问天机", englishSubtitle: "ROUND \(vm.round)/\(vm.maxRounds)")
                .padding(.horizontal, CyberSpacing.lg)
                .padding(.top, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.md)

            // Chat messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: CyberSpacing.md) {
                        ForEach(vm.messages) { msg in
                            if msg.role == .ai {
                                AiBubble(text: msg.content)
                                    .id(msg.id)
                            } else {
                                UserBubble(text: msg.content)
                                    .id(msg.id)
                            }
                        }

                        // Loading indicator
                        if vm.isLoading {
                            LoadingIndicator()
                                .id("loading")
                        }
                    }
                    .padding(.vertical, CyberSpacing.xs)
                }
                .onChange(of: vm.messages.count) {
                    withAnimation {
                        proxy.scrollTo(vm.messages.last?.id ?? UUID(), anchor: .bottom)
                    }
                }
                .onChange(of: vm.isLoading) {
                    if vm.isLoading {
                        withAnimation {
                            proxy.scrollTo("loading", anchor: .bottom)
                        }
                    }
                }
            }

            Spacer().frame(height: CyberSpacing.md)

            // Input bar
            VoiceInputField(text: $inputText) {
                send()
            }
            .padding(.horizontal, CyberSpacing.lg)
            .padding(.bottom, CyberSpacing.lg)
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func send() {
        let text = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, !vm.isLoading, vm.round < vm.maxRounds else { return }
        inputText = ""
        vm.sendMessage(text)
    }
}

// MARK: - AI Bubble (left-aligned, serif, plain)

private struct AiBubble: View {
    let text: String

    var body: some View {
        TypewriterText(text: text)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 0)
    }
}

// MARK: - TypewriterText (animated character-by-character reveal)

private struct TypewriterText: View {
    let text: String
    @State private var displayedCount: Int = 0
    @State private var timer: Timer?

    var body: some View {
        let displayed = String(text.prefix(displayedCount))
        Text(displayed)
            .font(CyberTypography.bodySmall) // WenKai 14sp ≈ bodySmall
            .foregroundStyle(CyberColors.grayTitle)
            .lineSpacing(4) // lineHeight 24sp ≈ 14sp font + 10pt spacing
            .frame(maxWidth: .infinity, alignment: .leading)
            .onAppear {
                displayedCount = text.count
                // If text is short, animate; otherwise show immediately
                if text.count <= 60 {
                    displayedCount = 0
                    timer = Timer.scheduledTimer(withTimeInterval: 0.03, repeats: true) { t in
                        if displayedCount < text.count {
                            displayedCount += 1
                        } else {
                            t.invalidate()
                        }
                    }
                }
            }
            .onDisappear {
                timer?.invalidate()
            }
            .onChange(of: text) {
                timer?.invalidate()
                displayedCount = text.count
            }
    }
}

// MARK: - Loading Indicator (spinning chars + animated dots + text)

private struct LoadingIndicator: View {
    @State private var spinIndex = 0
    @State private var dotCount = 0
    @State private var spinTimer: Timer?
    @State private var dotTimer: Timer?

    private let symbols = ["|", "/", "—", "\\"]

    var body: some View {
        let dots = String(repeating: ".", count: dotCount)
        let symbol = symbols[spinIndex]

        Text("正在演算 \(symbol)\(dots)")
            .font(CyberTypography.monoMedium) // Mono 14sp
            .foregroundStyle(CyberColors.grayMuted)
            .padding(.vertical, 4)
            .onAppear {
                spinTimer = Timer.scheduledTimer(withTimeInterval: 0.15, repeats: true) { _ in
                    spinIndex = (spinIndex + 1) % symbols.count
                }
                dotTimer = Timer.scheduledTimer(withTimeInterval: 0.4, repeats: true) { _ in
                    dotCount = (dotCount + 1) % 4
                }
            }
            .onDisappear {
                spinTimer?.invalidate()
                dotTimer?.invalidate()
            }
    }
}

// MARK: - User Bubble (right-aligned, bordered box)

private struct UserBubble: View {
    let text: String

    var body: some View {
        HStack {
            Spacer(minLength: 48)
            Text(text)
                .font(CyberTypography.bodySmall) // WenKai 14sp
                .foregroundStyle(CyberColors.grayTitle)
                .lineSpacing(2)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .frame(maxWidth: 280, alignment: .trailing)
                .overlay(
                    Rectangle()
                        .stroke(CyberColors.grayBorder, lineWidth: 1)
                )
        }
    }
}

// MARK: - VoiceInputField (TextEditor + send button)

private struct VoiceInputField: View {
    @Binding var text: String
    var onSend: () -> Void

    var body: some View {
        HStack(spacing: CyberSpacing.xs) {
            TextEditor(text: $text)
                .font(CyberTypography.bodySmall)
                .foregroundStyle(CyberColors.cyberWhite)
                .scrollContentBackground(.hidden)
                .background(CyberColors.graySurface)
                .frame(minHeight: 36, maxHeight: 80)
                .overlay(
                    Rectangle()
                        .stroke(CyberColors.grayBorder, lineWidth: 1)
                )

            CyberButton("叩") {
                onSend()
            }
            .frame(width: 48)
        }
    }
}
