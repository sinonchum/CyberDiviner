import SwiftUI
import DesignSystem
import DivinationCore

/// EpiphanyScreen — 每日干支顿悟页面
///
/// Matches Android EpiphanyScreen.kt exactly.
/// Background: black placeholder (mountain image).
/// Font: HuiwenMingChao.
/// Text: Traditional Chinese.
/// Interaction: tap anywhere to dissolve into main interface.
public struct EpiphanyScreen: View {
    let onEnter: () -> Void

    public init(onEnter: @escaping () -> Void) {
        self.onEnter = onEnter
    }

    @State private var dissolving = false
    @State private var showYear = false
    @State private var showMonth = false
    @State private var showDay = false
    @State private var showBottom = false
    @State private var cursorVisible = true

    private var today: Date { Date() }
    private var calendar: Calendar { Calendar.current }
    private var ganzhiDate: LunarCalendar.GanzhiDate {
        let y = calendar.component(.year, from: today)
        let m = calendar.component(.month, from: today)
        let d = calendar.component(.day, from: today)
        return LunarCalendar.calculateGanzhi(year: y, month: m, day: d)
    }
    private var solarTermString: String? {
        let y = calendar.component(.year, from: today)
        let m = calendar.component(.month, from: today)
        let d = calendar.component(.day, from: today)
        return SolarTermHelper.currentSolarTerm(year: y, month: m, day: d)
    }
    private var logicPhrase: String {
        generateLogicPhrase(dayStem: ganzhiDate.dayStem)
    }

    public var body: some View {
        let ganzhi = ganzhiDate

        ZStack {
            // Background (black placeholder for mountain image)
            CyberColors.cyberBlack
                .ignoresSafeArea()

            // Bottom gradient overlay
            LinearGradient(
                stops: [
                    .init(color: Color.black.opacity(0.0), location: 0.0),
                    .init(color: Color.black.opacity(0.1), location: 0.3),
                    .init(color: Color.black.opacity(0.35), location: 0.6),
                    .init(color: Color.black.opacity(0.7), location: 1.0)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            // Main content
            VStack(spacing: 0) {
                // Ganzhi area (~55%)
                VStack(spacing: 0) {
                    Spacer()

                    // Year
                    Text("\(ganzhi.yearStem)\(ganzhi.yearBranch)年")
                        .font(.custom("HuiwenMingChao", size: 42))
                        .foregroundStyle(CyberColors.cyberWhite)
                        .kerning(10)
                        .opacity(showYear ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showYear)

                    Spacer().frame(height: 8)
                    AccentDivider()
                        .opacity(showYear ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showYear)
                    Spacer().frame(height: 18)

                    // Month
                    Text("\(ganzhi.monthStem)\(ganzhi.monthBranch)月")
                        .font(.custom("HuiwenMingChao", size: 42))
                        .foregroundStyle(CyberColors.cyberWhite)
                        .kerning(10)
                        .opacity(showMonth ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showMonth)

                    Spacer().frame(height: 8)
                    AccentDivider()
                        .opacity(showMonth ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showMonth)
                    Spacer().frame(height: 18)

                    // Day (hero size)
                    Text("\(ganzhi.dayStem)\(ganzhi.dayBranch)日")
                        .font(.custom("HuiwenMingChao", size: 58))
                        .foregroundStyle(CyberColors.cyberWhite)
                        .kerning(14)
                        .opacity(showDay ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showDay)

                    Spacer()
                }
                .frame(maxHeight: .infinity)

                // Solar term
                if let term = solarTermString {
                    Text("[ \(term) ]")
                        .font(CyberTypography.monoSmall)
                        .foregroundStyle(CyberColors.grayMuted)
                        .kerning(4)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)
                    Spacer().frame(height: 24)
                }

                // Logic phrase — terminal style
                VStack(spacing: 0) {
                    Text("> \(logicPhrase)")
                        .font(CyberTypography.bodyMedium)
                        .foregroundStyle(CyberColors.grayCaption)
                        .kerning(1)
                        .lineSpacing(6)
                        .multilineTextAlignment(.center)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)
                    Text(cursorVisible ? "_" : " ")
                        .font(CyberTypography.monoMedium)
                        .foregroundStyle(CyberColors.cyberWhite)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)
                }
                .padding(.horizontal, 32)

                Spacer().frame(maxHeight: .infinity)

                // Bottom section
                VStack(spacing: 16) {
                    // Red accent divider
                    Rectangle()
                        .fill(CyberColors.accentRed)
                        .frame(width: 48, height: 1.5)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)

                    Text("[ TOUCH TO ENTER ]")
                        .font(CyberTypography.monoSmall)
                        .foregroundStyle(CyberColors.grayMuted)
                        .kerning(3)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)
                }
                .padding(.bottom, 56)
            }
        }
        .opacity(dissolving ? 0 : 1)
        .animation(.easeOut(duration: 0.6), value: dissolving)
        .contentShape(Rectangle())
        .onTapGesture {
            guard !dissolving else { return }
            dissolving = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                onEnter()
            }
        }
        .onAppear {
            // Sequential fade-in
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { showYear = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { showMonth = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) { showDay = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.3) { showBottom = true }

            // Blinking cursor every 530ms
            Timer.scheduledTimer(withTimeInterval: 0.53, repeats: true) { _ in
                cursorVisible.toggle()
            }
        }
    }

    private func generateLogicPhrase(dayStem: String) -> String {
        let element = stemElement(dayStem)
        switch element {
        case "Wood":
            return "木氣延展，系統燃值升高。宜：拓展分支；忌：強行封閉。"
        case "Fire":
            return "火氣邁進，信號強度過載。宜：釋放冗餘；忌：追加邏輯。"
        case "Earth":
            return "土氣沉積，系統進入穩態。宜：修補冗餘邏輯；忌：強行建立鏈接。"
        case "Metal":
            return "金氣收斂，精密度提升。宜：檢查邊界條件；忌：擴張輸入集。"
        case "Water":
            return "水氣流動，網絡節點活躍。宜：充分緩存；忌：滲透未經驗證鏈路。"
        default:
            return "系統運行中，等待下一個指令。"
        }
    }

    private func stemElement(_ stem: String) -> String {
        switch stem {
        case "甲", "乙": return "Wood"
        case "丙", "丁": return "Fire"
        case "戊", "己": return "Earth"
        case "庚", "辛": return "Metal"
        case "壬", "癸": return "Water"
        default: return "Unknown"
        }
    }
}

// MARK: - Accent Divider

private struct AccentDivider: View {
    var body: some View {
        Rectangle()
            .fill(CyberColors.accentRed)
            .frame(width: 64, height: 1.5)
    }
}

#Preview {
    EpiphanyScreen(onEnter: {})
        .preferredColorScheme(.dark)
}
