import SwiftUI
import DesignSystem
import DivinationCore

public struct TarotScreen: View {

    public init() {}

    @State private var vm = TarotViewModel()

    public var body: some View {
        VStack(spacing: 0) {
            switch vm.phase {
            case .spreadSelection:
                selectSpreadPhase
            case .shuffling:
                shufflePhase
            case .drawing:
                drawingPhase
            case .interpretation:
                interpretationPhase
            case .result:
                resultPhase
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - SELECT_SPREAD Phase

    private var selectSpreadPhase: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Header
                SectionHeader(chineseTitle: "塔罗协议", englishSubtitle: "静心凝神，然后输入你的问题")
                    .padding(.horizontal, CyberSpacing.lg)
                    .padding(.top, CyberSpacing.sm)

                Spacer().frame(height: CyberSpacing.md)

                // Question input
                TarotVoiceInputField(text: $vm.question)
                    .padding(.horizontal, CyberSpacing.lg)

                Spacer().frame(height: CyberSpacing.md)

                // Spread selection label
                Text("选择牌阵")
                    .font(CyberTypography.bodySmall) // WenKai 13sp Bold
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.grayCaption)
                    .tracking(2)
                    .padding(.horizontal, CyberSpacing.lg)

                Spacer().frame(height: 12)

                // Spread items
                ForEach(vm.availableSpreads, id: \.self) { spread in
                    SpreadItem(
                        spread: spread,
                        isSelected: spread == vm.selectedSpread
                    ) {
                        vm.selectSpread(spread)
                    }
                    .padding(.horizontal, CyberSpacing.lg)
                    .padding(.bottom, 8)
                }

                Spacer().frame(height: CyberSpacing.md)

                // Start button
                CyberButton("开始占卜") {
                    vm.startReading()
                }
                .padding(.horizontal, CyberSpacing.lg)
                .disabled(vm.question.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)

                Spacer().frame(height: 12)

                Text("赛博算命 · 玄学解读")
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayMuted)
                    .tracking(2)
                    .frame(maxWidth: .infinity)
                    .padding(.bottom, CyberSpacing.md)
            }
        }
    }

    // MARK: - SHUFFLE Phase

    private var shufflePhase: some View {
        VStack(spacing: 0) {
            Spacer()

            Text("准备好了吗？")
                .font(CyberTypography.bodySmall) // WenKai 14sp
                .foregroundStyle(CyberColors.grayCaption)
                .tracking(2)
                .padding(.bottom, CyberSpacing.md)

            // Bordered '洗牌' button with letter spacing
            Text("洗 牌")
                .font(CyberTypography.titleMedium) // Huiwen 20sp
                .fontWeight(.bold)
                .foregroundStyle(CyberColors.cyberWhite)
                .tracking(6)
                .padding(.horizontal, 40)
                .padding(.vertical, 16)
                .overlay(
                    Rectangle()
                        .stroke(CyberColors.cyberWhite, lineWidth: 1)
                )
                .contentShape(Rectangle())
                .onTapGesture {
                    vm.performShuffle()
                }

            Spacer().frame(height: 16)

            Text("轻触牌堆开始")
                .font(CyberTypography.bodySmall)
                .foregroundStyle(CyberColors.grayMuted)
                .tracking(1)

            Spacer()
        }
    }

    // MARK: - DRAWING Phase (card backs with ? + tap to reveal)

    private var drawingPhase: some View {
        VStack(spacing: CyberSpacing.md) {
            Spacer()

            Text("点击翻开卡牌")
                .font(CyberTypography.bodySmall)
                .foregroundStyle(CyberColors.grayCaption)
                .tracking(1)

            HStack(spacing: CyberSpacing.sm) {
                ForEach(vm.drawnCards.indices, id: \.self) { i in
                    TarotCardBack(
                        isRevealed: vm.revealedCards.contains(i),
                        drawResult: vm.drawnCards[i]
                    )
                    .onTapGesture {
                        vm.revealCard(at: i)
                    }
                }
            }
            .padding(.horizontal, CyberSpacing.lg)

            if vm.allRevealed {
                CyberButton("解读牌面") {
                    vm.requestInterpretation()
                }
                .padding(.horizontal, CyberSpacing.lg)
                .padding(.top, CyberSpacing.sm)
            }

            Spacer()
        }
    }

    // MARK: - INTERPRETATION Phase

    private var interpretationPhase: some View {
        VStack(spacing: CyberSpacing.md) {
            Spacer()

            // Show revealed cards
            HStack(spacing: CyberSpacing.sm) {
                ForEach(vm.drawnCards.indices, id: \.self) { i in
                    TarotCardBack(
                        isRevealed: true,
                        drawResult: vm.drawnCards[i]
                    )
                }
            }
            .padding(.horizontal, CyberSpacing.lg)

            DividerLine()
                .padding(.horizontal, CyberSpacing.lg)

            if vm.isInterpreting {
                AnimatedDotsText(message: "牌阵已开，正在等候本地先知落笔")
            } else if let reading = vm.aiInterpretation {
                Text(reading)
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayBody)
                    .lineSpacing(4)
                    .padding(.horizontal, CyberSpacing.lg)
            }

            Spacer()
        }
    }

    // MARK: - RESULT Phase (book-style paginated view)

    private var resultPhase: some View {
        TarotResultBookView(vm: vm)
    }
}

// MARK: - Spread Item

private struct SpreadItem: View {
    let spread: SpreadType
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        let borderColor = isSelected ? CyberColors.cyberWhite : CyberColors.grayBorder
        let bgColor = isSelected ? CyberColors.graySurface : CyberColors.cyberBlack

        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(spreadDisplayName)
                    .font(CyberTypography.bodySmall) // WenKai 14sp
                    .fontWeight(.bold)
                    .foregroundStyle(isSelected ? CyberColors.cyberWhite : CyberColors.grayBody)

                Text("\(spread.rawValue)张牌 · \(spread.positionNames.joined(separator: ", "))")
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayMuted)
            }

            Spacer()

            if isSelected {
                Text("|")
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.cyberWhite)
            }
        }
        .padding(16)
        .background(bgColor)
        .overlay(
            Rectangle()
                .stroke(borderColor, lineWidth: 1)
        )
        .contentShape(Rectangle())
        .onTapGesture { onTap() }
    }

    private var spreadDisplayName: String {
        switch spread {
        case .single: return "单牌指引"
        case .threeCard: return "三牌阵"
        }
    }
}

// MARK: - Tarot Card Back

private struct TarotCardBack: View {
    let isRevealed: Bool
    let drawResult: TarotDrawResult

    var body: some View {
        VStack(spacing: 4) {
            if isRevealed {
                Text("\(drawResult.card.number)")
                    .font(CyberTypography.monoMedium)
                    .foregroundStyle(CyberColors.accentRed)

                Text(drawResult.card.nameCN)
                    .font(CyberTypography.titleSmall) // Huiwen 20sp
                    .foregroundStyle(CyberColors.cyberWhite)

                Text(drawResult.isReversed ? "逆位" : "正位")
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(drawResult.isReversed ? CyberColors.accentRed : CyberColors.grayCaption)

                Text(drawResult.position)
                    .font(CyberTypography.monoCaption)
                    .foregroundStyle(CyberColors.grayMuted)
            } else {
                Text("?")
                    .font(CyberTypography.titleLarge) // Huiwen 32sp
                    .foregroundStyle(CyberColors.grayBorder)
            }
        }
        .frame(width: 96, height: 140)
        .background(CyberColors.cyberBlack)
        .overlay(
            Rectangle()
                .stroke(CyberColors.grayBorder, lineWidth: 1)
        )
    }
}

// MARK: - Animated Dots (for tarot)

private struct AnimatedDotsText: View {
    let message: String
    @State private var dotCount = 0
    @State private var timer: Timer?

    var body: some View {
        let dots = String(repeating: ".", count: dotCount)
        Text("\(message)\(dots)")
            .font(CyberTypography.bodySmall)
            .foregroundStyle(CyberColors.grayCaption)
            .onAppear {
                timer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { _ in
                    dotCount = (dotCount + 1) % 4
                }
            }
            .onDisappear {
                timer?.invalidate()
            }
    }
}

// MARK: - Tarot VoiceInputField

private struct TarotVoiceInputField: View {
    @Binding var text: String

    var body: some View {
        TextEditor(text: $text)
            .font(CyberTypography.bodySmall)
            .foregroundStyle(CyberColors.cyberWhite)
            .scrollContentBackground(.hidden)
            .background(CyberColors.graySurface)
            .frame(minHeight: 80, maxHeight: 120)
            .overlay(
                Rectangle()
                    .stroke(CyberColors.grayBorder, lineWidth: 1)
            )
    }
}

// MARK: - Tarot Result Book View (paginated swipe)

private struct TarotResultBookView: View {
    @Bindable var vm: TarotViewModel
    @State private var currentPage = 0
    private let cnNums = ["壹", "贰", "叁", "肆", "伍"]

    private var totalPages: Int { 3 } // 批命 + 牌阵 + 解读

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Text("< 返回")
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayCaption)
                    .onTapGesture { vm.resetForNewReading() }

                Spacer()

                Text("塔罗解读")
                    .font(CyberTypography.bodySmall)
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.grayCaption)
                    .tracking(3)

                Spacer()

                Text("\(cnNums[currentPage])/\(cnNums[totalPages - 1])")
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.grayMuted)
                    .tracking(2)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)

            DividerLine()

            // Page content (swipeable)
            TabView(selection: $currentPage) {
                // Page 0: 批命 (Fortune)
                fortunePage.tag(0)

                // Page 1: 牌阵 (Spread)
                spreadPage.tag(1)

                // Page 2: 解读 (Interpretation)
                interpretationPage.tag(2)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            DividerLine()

            // Swipe hint
            Text(currentPage < totalPages - 1 ? "< 左滑翻页 >" : "< 右滑返回 >")
                .font(CyberTypography.monoCaption)
                .foregroundStyle(CyberColors.grayMuted)
                .tracking(2)
                .frame(maxWidth: .infinity)
                .padding(.top, 8)

            // Page dots
            HStack(spacing: 4) {
                ForEach(0..<totalPages, id: \.self) { i in
                    Rectangle()
                        .fill(i == currentPage ? CyberColors.cyberWhite : CyberColors.grayBorder)
                        .frame(width: i == currentPage ? 16 : 6, height: 3)
                }
            }
            .padding(.vertical, 8)

            // Action button
            CyberButton("重新占卜") {
                vm.resetForNewReading()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 20)
        }
    }

    // MARK: - Fortune Page

    private var fortunePage: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("批命")
                    .font(CyberTypography.titleMedium) // Huiwen 22sp
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.cyberWhite)
                    .tracking(6)

                Text("FORTUNE")
                    .font(CyberTypography.monoCaption)
                    .foregroundStyle(CyberColors.grayMuted)
                    .tracking(3)

                Spacer().frame(height: 2)

                Rectangle()
                    .fill(CyberColors.accentRed)
                    .frame(width: 28, height: 1)

                Spacer().frame(height: 40)

                Text(fourCharFortune)
                    .font(CyberTypography.titleLarge) // Huiwen 32sp
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.grayTitle)
                    .tracking(8)
                    .frame(maxWidth: .infinity)

                Spacer().frame(height: 16)

                Text(fourCharMeaning)
                    .font(CyberTypography.bodySmall) // WenKai 14sp
                    .foregroundStyle(CyberColors.grayBody)
                    .lineSpacing(8)
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
        }
    }

    // MARK: - Spread Page

    private var spreadPage: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("牌阵")
                    .font(CyberTypography.titleMedium)
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.cyberWhite)
                    .tracking(6)

                Text("SPREAD")
                    .font(CyberTypography.monoCaption)
                    .foregroundStyle(CyberColors.grayMuted)
                    .tracking(3)

                Spacer().frame(height: 2)

                Rectangle()
                    .fill(CyberColors.accentRed)
                    .frame(width: 28, height: 1)

                Spacer().frame(height: 16)

                // Card list
                ForEach(vm.drawnCards.indices, id: \.self) { i in
                    let card = vm.drawnCards[i]
                    HStack {
                        Text(card.position)
                            .font(CyberTypography.bodySmall)
                            .foregroundStyle(CyberColors.grayMuted)
                            .frame(width: 72, alignment: .leading)

                        Text(card.card.nameCN)
                            .font(CyberTypography.bodyMedium) // Huiwen 16sp
                            .fontWeight(.bold)
                            .foregroundStyle(CyberColors.cyberWhite)

                        Spacer()

                        Text(card.isReversed ? "逆位" : "正位")
                            .font(CyberTypography.bodySmall)
                            .foregroundStyle(card.isReversed ? CyberColors.accentRed : CyberColors.grayBody)
                    }
                    .padding(.vertical, 6)
                }

                Spacer().frame(height: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
        }
    }

    // MARK: - Interpretation Page

    private var interpretationPage: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("解读")
                    .font(CyberTypography.titleMedium)
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.cyberWhite)
                    .tracking(6)

                Text("INTERPRETATION")
                    .font(CyberTypography.monoCaption)
                    .foregroundStyle(CyberColors.grayMuted)
                    .tracking(3)

                Spacer().frame(height: 2)

                Rectangle()
                    .fill(CyberColors.accentRed)
                    .frame(width: 28, height: 1)

                Spacer().frame(height: 16)

                if let reading = vm.aiInterpretation, !reading.isEmpty {
                    Text(reading)
                        .font(CyberTypography.bodySmall) // WenKai 14sp
                        .foregroundStyle(CyberColors.grayBody)
                        .lineSpacing(6) // lineHeight 26sp ≈ 14sp + 12
                } else {
                    Text("赛博先知解读中...")
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayMuted)
                }

                Spacer().frame(height: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
        }
    }

    // MARK: - Helpers

    private var fourCharFortune: String {
        // Generate a 4-char fortune from the reading
        guard let reading = vm.aiInterpretation else { return "顺势而为" }
        let themes = [
            ("事业", "鹏程万里"), ("感情", "情缘天定"), ("财运", "财源广进"),
            ("健康", "身心康泰"), ("学业", "金榜题名"), ("贵人", "贵人相助"),
            ("危机", "否极泰来"), ("变化", "革故鼎新"), ("等待", "静待花开"),
        ]
        for (keyword, fortune) in themes {
            if reading.contains(keyword) { return fortune }
        }
        return "天机莫测"
    }

    private var fourCharMeaning: String {
        guard let reading = vm.aiInterpretation else { return "卦象已起，静心体悟天机" }
        // Extract first meaningful sentence
        let sentences = reading.components(separatedBy: CharacterSet(charactersIn: "。！？\n"))
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty && $0.count > 4 }
        return sentences.first ?? "天时地利，可以有所作为"
    }
}
