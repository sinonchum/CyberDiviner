import SwiftUI
import DesignSystem
import DivinationCore

public struct LiuyaoScreen: View {

    public init() {}

    @State private var vm = LiuyaoViewModel()
    @State private var inputText = ""

    public var body: some View {
        VStack(spacing: 0) {
            switch vm.phase {
            case .question:
                inputPhase
            case .casting:
                tossingPhase
            case .computing:
                computingPhase
            case .result:
                resultPhase
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - INPUT Phase

    private var inputPhase: some View {
        VStack(alignment: .leading, spacing: 0) {
            SectionHeader(chineseTitle: "周易起卦", englishSubtitle: "三钱法 · 六次演算")
                .padding(.horizontal, CyberSpacing.lg)
                .padding(.top, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.xl)

            // Prompt
            Text("心诚则灵")
                .font(CyberTypography.bodyMedium) // WenKai 16sp
                .foregroundStyle(CyberColors.grayBody)
                .tracking(4) // letterSpacing 4sp
                .padding(.horizontal, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.xs)

            Text("静心冥想，然后输入你的问题")
                .font(CyberTypography.bodySmall) // WenKai 13sp
                .foregroundStyle(CyberColors.grayCaption)
                .padding(.horizontal, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.md)

            // Question input
            LiuyaoVoiceInputField(text: $inputText) {
                startDivination()
            }
            .padding(.horizontal, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.md)

            // Start button
            CyberButton("起卦") {
                startDivination()
            }
            .padding(.horizontal, CyberSpacing.lg)

            Spacer()
        }
    }

    // MARK: - TOSSING Phase

    private var tossingPhase: some View {
        VStack(spacing: 0) {
            Spacer()

            // Section header
            SectionHeader(chineseTitle: "周易起卦", englishSubtitle: "")
                .padding(.horizontal, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.xl)

            // Pulsing progress text
            PulsingText(text: progressText)
                .frame(maxWidth: .infinity)

            Spacer().frame(height: CyberSpacing.sm)

            // Shake instruction
            Text("用力摇动手机")
                .font(CyberTypography.bodySmall) // WenKai 13sp
                .foregroundStyle(CyberColors.grayCaption)

            Spacer().frame(height: CyberSpacing.md)

            // Line display
            VStack(spacing: 4) {
                if !vm.lines.isEmpty {
                    Text("已得")
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayCaption)
                        .tracking(2)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, CyberSpacing.lg)

                    Spacer().frame(height: 8)
                }

                // Show cast lines (top to bottom)
                ForEach((0..<6).reversed(), id: \.self) { i in
                    if i < vm.currentLine {
                        TossLineRow(index: i, toss: vm.lines[i])
                    } else {
                        PlaceholderLineRow(index: i)
                    }
                }
                .padding(.horizontal, CyberSpacing.lg)
            }

            Spacer().frame(height: CyberSpacing.md)

            // Cast button
            CyberButton("掷爻") {
                vm.castNextLine()
            }
            .padding(.horizontal, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.sm)

            // Counter
            Text("\(vm.currentLine) / 6")
                .font(CyberTypography.monoSmall) // Mono 12sp
                .foregroundStyle(CyberColors.grayMuted)

            Spacer()
        }
    }

    // MARK: - COMPUTING Phase

    private var computingPhase: some View {
        VStack(spacing: 0) {
            Spacer()

            Text("卦象已成")
                .font(CyberTypography.titleMedium) // Huiwen 24sp Bold
                .foregroundStyle(CyberColors.grayTitle)
                .fontWeight(.bold)

            Spacer().frame(height: CyberSpacing.xs)

            AnimatedDotsText()

            Spacer()
        }
    }

    // MARK: - RESULT Phase

    private var resultPhase: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: CyberSpacing.sm) {
                if let result = vm.result {
                    // 本卦
                    ResultRow(
                        label: "本卦",
                        value: "\(result.primaryHexagram.chineseName) \(result.primaryHexagram.englishName)"
                    )

                    // 变卦
                    if result.hasChangingLines {
                        ResultRow(
                            label: "变卦",
                            value: "\(result.changedHexagram.chineseName) \(result.changedHexagram.englishName)"
                        )
                    }

                    // 动爻
                    let changingPositions = result.tosses.enumerated()
                        .compactMap { $0.element.lineState.isChanging ? $0.offset + 1 : nil }
                    if !changingPositions.isEmpty {
                        ResultRow(
                            label: "动爻",
                            value: changingPositions.map { "第\($0)爻" }.joined(separator: "、")
                        )
                    }

                    DividerLine()

                    // Line diagram — top (上) to bottom (初)
                    let lineLabels = ["上", "五", "四", "三", "二", "初"]
                    ForEach(0..<6, id: \.self) { i in
                        let lineIdx = 5 - i
                        let line = result.lines[lineIdx]
                        let isYang = line.isYang
                        let isChanging = line.state.isChanging

                        HStack(spacing: 8) {
                            // Label
                            Text(lineLabels[i])
                                .font(CyberTypography.bodySmall) // Huiwen 11sp
                                .foregroundStyle(CyberColors.grayMuted)
                                .frame(width: 24, alignment: .trailing)

                            // Line drawing
                            if isYang {
                                Text("━━━━━")
                                    .font(CyberTypography.monoMedium)
                                    .foregroundStyle(isChanging ? CyberColors.accentRed : CyberColors.cyberWhite)
                            } else {
                                Text("━   ━")
                                    .font(CyberTypography.monoMedium)
                                    .foregroundStyle(isChanging ? CyberColors.accentRed : CyberColors.cyberWhite)
                            }

                            // Changing mark
                            if isChanging {
                                Text(line.state == .oldYang ? "○" : "×")
                                    .font(CyberTypography.monoMedium)
                                    .foregroundStyle(CyberColors.accentRed)
                            } else {
                                Spacer().frame(width: 14)
                            }

                            // Six Relation
                            if let rel = line.relation {
                                Text(rel.chinese)
                                    .font(CyberTypography.bodySmall)
                                    .foregroundStyle(CyberColors.grayBody)
                            }

                            // Six Spirit
                            Text(result.spirits[lineIdx].chinese)
                                .font(CyberTypography.bodySmall)
                                .foregroundStyle(CyberColors.grayCaption)

                            // World/Response markers
                            if lineIdx == result.worldLine {
                                CircleTag(text: "世", filled: true)
                            } else if lineIdx == result.responseLine {
                                CircleTag(text: "应", filled: false)
                            } else {
                                Spacer().frame(width: 20)
                            }
                        }
                    }
                    .padding(.horizontal, CyberSpacing.lg)

                    DividerLine()

                    // Analysis
                    Text(result.analysis.interpretation)
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayBody)
                        .lineSpacing(4)

                    Text(result.analysis.advice)
                        .font(CyberTypography.bodySmall)
                        .foregroundStyle(CyberColors.grayBody)
                        .lineSpacing(4)
                        .padding(.top, CyberSpacing.xs)

                    // AI interpretation
                    if vm.isInterpreting {
                        Text("正在解读卦象...")
                            .font(CyberTypography.bodySmall)
                            .foregroundStyle(CyberColors.grayCaption)
                            .padding(.top, CyberSpacing.xs)
                    } else if let interp = vm.aiInterpretation {
                        DividerLine()
                        TypewriterTextSimple(text: interp)
                            .padding(.top, CyberSpacing.xs)
                    }
                }
            }
            .padding(.horizontal, CyberSpacing.lg)
            .padding(.vertical, CyberSpacing.lg)
        }
    }

    // MARK: - Helpers

    private var progressText: String {
        if vm.currentLine == 0 {
            return "请静心凝神"
        }
        return "第 \(vm.currentLine) 爻"
    }

    private func startDivination() {
        let q = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return }
        vm.question = q
        vm.startCasting()
    }
}

// MARK: - Pulsing Text (alpha 0.4 ↔ 1.0)

private struct PulsingText: View {
    let text: String
    @State private var isPulsing = false

    var body: some View {
        Text(text)
            .font(CyberTypography.titleMedium) // Huiwen 24sp
            .fontWeight(.bold)
            .foregroundStyle(CyberColors.grayTitle.opacity(isPulsing ? 1.0 : 0.4))
            .tracking(6)
            .onAppear {
                withAnimation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true)) {
                    isPulsing = true
                }
            }
    }
}

// MARK: - Animated Dots

private struct AnimatedDotsText: View {
    @State private var dotCount = 0
    @State private var timer: Timer?

    var body: some View {
        let dots = String(repeating: ".", count: dotCount)
        Text("正在召唤赛博先知\(dots)")
            .font(CyberTypography.bodySmall) // WenKai 13sp
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

// MARK: - Toss Line Row

private struct TossLineRow: View {
    let index: Int
    let toss: LiuyaoEngine.CoinToss

    var body: some View {
        HStack(spacing: 8) {
            Text("\(index + 1)")
                .font(CyberTypography.monoCaption) // Mono 11sp
                .foregroundStyle(CyberColors.grayMuted)
                .frame(width: 20)

            let bar = toss.lineState.isYang ? "━━━━━" : "━   ━"
            let mark: String = {
                switch toss.lineState {
                case .oldYang: return " ○"
                case .oldYin: return " ×"
                default: return ""
                }
            }()
            Text("\(bar)\(mark)")
                .font(CyberTypography.monoMedium)
                .foregroundStyle(CyberColors.grayBody)

            Spacer().frame(width: 8)

            let label: String = {
                switch toss.lineState {
                case .youngYang: return "少阳"
                case .youngYin: return "少阴"
                case .oldYang: return "老阳"
                case .oldYin: return "老阴"
                }
            }()
            Text(label)
                .font(CyberTypography.bodySmall) // WenKai 11sp
                .foregroundStyle(CyberColors.grayCaption)
        }
    }
}

// MARK: - Placeholder Line Row

private struct PlaceholderLineRow: View {
    let index: Int

    var body: some View {
        HStack(spacing: 8) {
            Text("\(index + 1)")
                .font(CyberTypography.monoCaption)
                .foregroundStyle(CyberColors.grayMuted)
                .frame(width: 20)
            Text("- - - - -")
                .font(CyberTypography.monoMedium)
                .foregroundStyle(CyberColors.grayMuted.opacity(0.3))
        }
    }
}

// MARK: - Circle Tag (世/应)

private struct CircleTag: View {
    let text: String
    let filled: Bool

    var body: some View {
        Text(text)
            .font(CyberTypography.bodySmall) // Huiwen 10sp
            .fontWeight(.bold)
            .foregroundStyle(filled ? CyberColors.cyberWhite : CyberColors.accentRed)
            .frame(width: 20, height: 20)
            .background(filled ? CyberColors.accentRed : Color.clear)
            .clipShape(Circle())
            .overlay(
                Circle().stroke(CyberColors.accentRed, lineWidth: filled ? 0 : 1)
            )
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

// MARK: - TypewriterTextSimple

private struct TypewriterTextSimple: View {
    let text: String
    @State private var displayedCount: Int = 0

    var body: some View {
        let displayed = String(text.prefix(displayedCount))
        Text(displayed)
            .font(CyberTypography.bodySmall)
            .foregroundStyle(CyberColors.grayBody)
            .lineSpacing(4)
            .onAppear {
                displayedCount = 0
                Timer.scheduledTimer(withTimeInterval: 0.02, repeats: true) { t in
                    if displayedCount < text.count {
                        displayedCount += 1
                    } else {
                        t.invalidate()
                    }
                }
            }
    }
}

// MARK: - Liuyao VoiceInputField

private struct LiuyaoVoiceInputField: View {
    @Binding var text: String
    var onSend: () -> Void

    var body: some View {
        HStack(spacing: CyberSpacing.xs) {
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
}
