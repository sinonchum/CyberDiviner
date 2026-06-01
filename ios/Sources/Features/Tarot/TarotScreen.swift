import SwiftUI
import DesignSystem
import DivinationCore

public struct TarotScreen: View {

    public init() {}

    @State private var vm = TarotViewModel()

    public var body: some View {
        VStack(spacing: 0) {
            SectionHeader(chineseTitle: "赛博塔罗", englishSubtitle: "CYBER TAROT")
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.top, CyberSpacing.xs)

            DividerLine()

            ScrollView {
                VStack(spacing: CyberSpacing.md) {
                    switch vm.phase {
                    case .spreadSelection:
                        spreadSelectionPhase
                    case .drawing:
                        drawingPhase
                    case .interpretation:
                        interpretationPhase
                    }
                }
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.vertical, CyberSpacing.sm)
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - Phase 1: Choose Spread

    private var spreadSelectionPhase: some View {
        VStack(spacing: CyberSpacing.sm) {
            Text("选择牌阵")
                .font(CyberTypography.bodyMedium)
                .foregroundStyle(CyberColors.grayCaption)

            CyberButton("单牌指引") {
                vm.drawCards(count: 1)
            }

            CyberButton("三牌阵 — 过去·现在·未来") {
                vm.drawCards(count: 3)
            }
        }
    }

    // MARK: - Phase 2: Draw Cards

    private var drawingPhase: some View {
        VStack(spacing: CyberSpacing.md) {
            Text("点击翻开卡牌")
                .font(CyberTypography.bodyMedium)
                .foregroundStyle(CyberColors.grayCaption)

            HStack(spacing: CyberSpacing.sm) {
                ForEach(vm.drawnCards.indices, id: \.self) { i in
                    TarotCardView(
                        drawResult: vm.drawnCards[i],
                        isRevealed: vm.revealedCards.contains(i)
                    )
                    .onTapGesture {
                        vm.revealCard(at: i)
                    }
                }
            }

            if vm.allRevealed {
                CyberButton("解读牌面") {
                    vm.requestInterpretation()
                }
                .padding(.top, CyberSpacing.sm)
            }
        }
    }

    // MARK: - Phase 3: Interpretation

    private var interpretationPhase: some View {
        VStack(alignment: .leading, spacing: CyberSpacing.sm) {
            // Show revealed cards
            HStack(spacing: CyberSpacing.sm) {
                ForEach(vm.drawnCards.indices, id: \.self) { i in
                    TarotCardView(
                        drawResult: vm.drawnCards[i],
                        isRevealed: true
                    )
                }
            }
            .frame(maxWidth: .infinity)

            DividerLine()

            if vm.isInterpreting {
                Text("正在解读牌面...")
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.grayCaption)
            } else if let reading = vm.aiInterpretation {
                Text(reading)
                    .font(CyberTypography.titleSmall)
                    .foregroundStyle(CyberColors.grayBody)
            }
        }
    }
}

// MARK: - Tarot Card View

private struct TarotCardView: View {
    let drawResult: TarotDrawResult
    let isRevealed: Bool

    public var body: some View {
        VStack(spacing: 4) {
            if isRevealed {
                Text("\(drawResult.card.number)")
                    .font(CyberTypography.monoMedium)
                    .foregroundStyle(CyberColors.accentRed)

                Text(drawResult.card.nameCN)
                    .font(CyberTypography.titleSmall)
                    .foregroundStyle(CyberColors.cyberWhite)

                Text(drawResult.isReversed ? "逆位" : "正位")
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(
                        drawResult.isReversed
                            ? CyberColors.accentRed
                            : CyberColors.grayCaption
                    )

                Text(drawResult.position)
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.grayMuted)
            } else {
                Text("?")
                    .font(CyberTypography.titleLarge)
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
