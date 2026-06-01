import SwiftUI
import DesignSystem

struct LiuyaoScreen: View {
    @State private var vm = LiuyaoViewModel()

    var body: some View {
        VStack(spacing: 0) {
            SectionHeader(chineseTitle: "六爻起卦", englishSubtitle: "LIUYAO DIVINATION")
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.top, CyberSpacing.xs)

            DividerLine()

            ScrollView {
                VStack(spacing: CyberSpacing.md) {
                    switch vm.phase {
                    case .question:
                        questionPhase
                    case .casting:
                        castingPhase
                    case .result:
                        resultPhase
                    }
                }
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.vertical, CyberSpacing.sm)
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - Phase 1: Question

    private var questionPhase: some View {
        VStack(spacing: CyberSpacing.sm) {
            Text("心中默念所问之事")
                .font(CyberTypography.bodyMedium)
                .foregroundStyle(CyberColors.grayCaption)

            TextEditor(text: $vm.question)
                .font(CyberTypography.bodyMedium)
                .foregroundStyle(CyberColors.cyberWhite)
                .scrollContentBackground(.hidden)
                .background(CyberColors.graySurface)
                .frame(minHeight: 80, maxHeight: 120)
                .overlay(
                    RoundedRectangle(cornerRadius: 0)
                        .stroke(CyberColors.grayBorder, lineWidth: 1)
                )

            CyberButton("起卦") {
                vm.startCasting()
            }
            .disabled(vm.question.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
        }
    }

    // MARK: - Phase 2: Casting

    private var castingPhase: some View {
        VStack(spacing: CyberSpacing.md) {
            Text("请静心凝神，连续点击掷爻")
                .font(CyberTypography.bodyMedium)
                .foregroundStyle(CyberColors.grayCaption)

            Text("第 \(vm.currentLine + 1) 爻 / 共 6 爻")
                .font(CyberTypography.monoMedium)
                .foregroundStyle(CyberColors.grayBody)

            // Show already-cast lines (top to bottom)
            ForEach((0..<6).reversed(), id: \.self) { i in
                if i < vm.currentLine {
                    LineDisplay(lineIndex: i, lineState: vm.lines[i])
                } else {
                    Rectangle()
                        .fill(CyberColors.grayBorder)
                        .frame(height: 2)
                        .opacity(0.3)
                }
            }

            CyberButton("掷爻") {
                vm.castNextLine()
            }
        }
    }

    // MARK: - Phase 3: Result

    private var resultPhase: some View {
        VStack(alignment: .leading, spacing: CyberSpacing.sm) {
            if let result = vm.result {
                // Primary hexagram
                ResultRow(
                    label: "本卦",
                    value: "\(result.primaryHexagram.chineseName) \(result.primaryHexagram.englishName)"
                )

                // Changed hexagram
                if result.hasChangingLines {
                    ResultRow(
                        label: "变卦",
                        value: "\(result.changedHexagram.chineseName) \(result.changedHexagram.englishName)"
                    )
                }

                // Changing lines
                let changingPositions = result.tosses.enumerated()
                    .compactMap { $0.element.lineState.isChanging ? $0.offset + 1 : nil }
                if !changingPositions.isEmpty {
                    ResultRow(
                        label: "动爻",
                        value: changingPositions.map { "第\($0)爻" }.joined(separator: "、")
                    )
                }

                DividerLine()

                // Line diagram
                ForEach((0..<6).reversed(), id: \.self) { i in
                    LineDisplay(lineIndex: i, yaoLine: result.lines[i], spirit: result.spirits[i])
                }

                DividerLine()

                // Analysis
                Text(result.analysis.interpretation)
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.grayBody)

                Text(result.analysis.advice)
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.grayBody)
                    .padding(.top, CyberSpacing.xs)

                // AI interpretation
                if vm.isInterpreting {
                    Text("正在解读卦象...")
                        .font(CyberTypography.bodyMedium)
                        .foregroundStyle(CyberColors.grayCaption)
                        .padding(.top, CyberSpacing.xs)
                } else if let interp = vm.aiInterpretation {
                    DividerLine()
                    Text(interp)
                        .font(CyberTypography.titleSmall)
                        .foregroundStyle(CyberColors.grayBody)
                }
            }
        }
    }
}

// MARK: - Line Display

private struct LineDisplay: View {
    let lineIndex: Int
    var lineState: LiuyaoEngine.CoinToss? = nil
    var yaoLine: YaoLine? = nil
    var spirit: SixSpirit? = nil

    var body: some View {
        HStack(spacing: CyberSpacing.xs) {
            Text("\(lineIndex + 1)")
                .font(CyberTypography.monoSmall)
                .foregroundStyle(CyberColors.grayCaption)
                .frame(width: 20)

            if let state = lineState {
                Text(state.lineState.isYang ? "━━━━━" : "━   ━")
                    .font(CyberTypography.monoMedium)
                    .foregroundStyle(CyberColors.cyberWhite)
                if state.lineState.isChanging {
                    Text(state.lineState == .oldYang ? "×" : "○")
                        .font(CyberTypography.monoMedium)
                        .foregroundStyle(CyberColors.accentRed)
                }
            } else if let line = yaoLine {
                Text(line.isYang ? "━━━━━" : "━   ━")
                    .font(CyberTypography.monoMedium)
                    .foregroundStyle(CyberColors.cyberWhite)
                if line.state.isChanging {
                    Text(line.state == .oldYang ? "×" : "○")
                        .font(CyberTypography.monoMedium)
                        .foregroundStyle(CyberColors.accentRed)
                }
                Text("[\(line.branch)]")
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.grayCaption)
                if let rel = line.relation {
                    Text(rel.chinese)
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayBody)
                }
                if let sp = spirit {
                    Text(sp.chinese)
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayCaption)
                }
            }
        }
    }
}

// MARK: - Result Row

private struct ResultRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .top) {
            Text(label)
                .font(CyberTypography.monoMedium)
                .foregroundStyle(CyberColors.accentRed)
                .frame(width: 48, alignment: .leading)
            Text(value)
                .font(CyberTypography.bodyMedium)
                .foregroundStyle(CyberColors.grayBody)
        }
    }
}
