import SwiftUI
import DesignSystem
import DivinationCore

/// SplashScreen — 每日道字开屏
///
/// Matches Android SplashScreen.kt exactly.
/// Layout: black background → solar date → Dao word (96sp hero) → Ganzhi line →
///         solar term → subtitle → CYBERDIVINER → TOUCH TO ENTER
/// Font: HuiwenMingChao (HuiwenFontFamily).
/// Interaction: tap anywhere to enter, or auto-timeout after 6 seconds.
public struct SplashScreen: View {
    let onEnter: () -> Void

    public init(onEnter: @escaping () -> Void) {
        self.onEnter = onEnter
    }

    // 30 Dao words rotated by dayOfYear % 30
    private static let daoWords = [
        "道", "德", "無", "玄", "虛",
        "靜", "和", "常", "明", "朴",
        "柔", "反", "損", "益", "沖",
        "盈", "歸", "化", "妙", "真",
        "一", "清", "靈", "隱", "默",
        "守", "復", "根", "命", "氣"
    ]

    @State private var dissolving = false
    @State private var showDate = false
    @State private var showWord = false
    @State private var showGanzhi = false
    @State private var showSolarTerm = false
    @State private var showSubtitle = false
    @State private var showBottom = false
    @State private var cursorVisible = true

    private var today: Date { Date() }
    private var calendar: Calendar { Calendar.current }
    private var dayOfYear: Int { calendar.ordinality(of: .day, in: .year, for: today) ?? 1 }
    private var dailyWord: String {
        Self.daoWords[dayOfYear % Self.daoWords.count]
    }
    private var solarDateString: String {
        let y = calendar.component(.year, from: today)
        let m = calendar.component(.month, from: today)
        let d = calendar.component(.day, from: today)
        return "\(y).\(m).\(d)"
    }
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

    public var body: some View {
        ZStack {
            // Mountain background placeholder (black)
            CyberColors.cyberBlack
                .ignoresSafeArea()

            // Bottom gradient overlay
            LinearGradient(
                stops: [
                    .init(color: Color.black.opacity(0.0), location: 0.0),
                    .init(color: Color.black.opacity(0.05), location: 0.3),
                    .init(color: Color.black.opacity(0.25), location: 0.6),
                    .init(color: Color.black.opacity(0.65), location: 1.0)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            // Main content
            VStack(spacing: 0) {
                Spacer().frame(maxHeight: .infinity)

                // Solar date
                Text(solarDateString)
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.grayBody)
                    .kerning(6)
                    .opacity(showDate ? 1 : 0)
                    .animation(.easeOut(duration: 0.7), value: showDate)

                Spacer().frame(height: 32)

                // Dao word (hero)
                Text(dailyWord)
                    .font(.custom("HuiwenMingChao", size: 96))
                    .foregroundStyle(CyberColors.cyberWhite)
                    .opacity(showWord ? 1 : 0)
                    .animation(.easeOut(duration: 0.9), value: showWord)

                Spacer().frame(height: 12)

                // Ganzhi line
                Text("\(ganzhiDate.yearGanzhi)年  \(ganzhiDate.monthGanzhi)月  \(ganzhiDate.dayGanzhi)日")
                    .font(.custom("HuiwenMingChao", size: 18))
                    .foregroundStyle(CyberColors.cyberWhite)
                    .kerning(4)
                    .opacity(showGanzhi ? 1 : 0)
                    .animation(.easeOut(duration: 0.7), value: showGanzhi)

                Spacer().frame(height: 60)

                // Solar term
                if let term = solarTermString {
                    Text("[ \(term) ]")
                        .font(CyberTypography.monoSmall)
                        .foregroundStyle(CyberColors.cyberWhite)
                        .kerning(4)
                        .opacity(showSolarTerm ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showSolarTerm)
                    Spacer().frame(height: 12)
                }

                // Subtitle
                Text("萬物共歸道，演算法虛靈。")
                    .font(.custom("HuiwenMingChao", size: 13))
                    .foregroundStyle(CyberColors.cyberWhite)
                    .kerning(3)
                    .opacity(showSubtitle ? 1 : 0)
                    .animation(.easeOut(duration: 0.7), value: showSubtitle)

                Spacer().frame(maxHeight: .infinity)

                // Bottom section
                VStack(spacing: 10) {
                    Text("CYBERDIVINER")
                        .font(CyberTypography.monoMedium)
                        .foregroundStyle(CyberColors.grayBody)
                        .kerning(8)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)

                    Text("[ TOUCH TO ENTER ]")
                        .font(CyberTypography.monoSmall)
                        .foregroundStyle(CyberColors.grayCaption)
                        .kerning(3)
                        .opacity(showBottom ? 1 : 0)
                        .animation(.easeOut(duration: 0.7), value: showBottom)
                }
                .padding(.bottom, 48)
            }
            .padding(.horizontal, 32)
        }
        .opacity(dissolving ? 0 : 1)
        .animation(.easeOut(duration: 0.5), value: dissolving)
        .contentShape(Rectangle())
        .onTapGesture {
            guard !dissolving else { return }
            dissolving = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                onEnter()
            }
        }
        .onAppear {
            // Sequential fade-in
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { showDate = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) { showWord = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.1) { showGanzhi = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { showSolarTerm = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { showSubtitle = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { showBottom = true }

            // Auto-timeout 6s
            DispatchQueue.main.asyncAfter(deadline: .now() + 6.0) {
                guard !dissolving else { return }
                dissolving = true
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    onEnter()
                }
            }

            // Blinking cursor every 530ms
            Timer.scheduledTimer(withTimeInterval: 0.53, repeats: true) { _ in
                cursorVisible.toggle()
            }
        }
    }
}

// MARK: - Solar Term Helper

/// Simple solar term lookup — returns the current solar term name if within range.
enum SolarTermHelper {
    private static let solarTerms: [(month: Int, day: Int, name: String)] = [
        (1, 6, "小寒"), (1, 20, "大寒"),
        (2, 4, "立春"), (2, 19, "雨水"),
        (3, 6, "惊蛰"), (3, 21, "春分"),
        (4, 5, "清明"), (4, 20, "谷雨"),
        (5, 6, "立夏"), (5, 21, "小满"),
        (6, 6, "芒种"), (6, 21, "夏至"),
        (7, 7, "小暑"), (7, 23, "大暑"),
        (8, 7, "立秋"), (8, 23, "处暑"),
        (9, 8, "白露"), (9, 23, "秋分"),
        (10, 8, "寒露"), (10, 23, "霜降"),
        (11, 7, "立冬"), (11, 22, "小雪"),
        (12, 7, "大雪"), (12, 22, "冬至")
    ]

    /// Returns the current solar term name if the date is within ±1 day of a solar term.
    static func currentSolarTerm(year: Int, month: Int, day: Int) -> String? {
        for term in solarTerms {
            if term.month == month && abs(term.day - day) <= 1 {
                return term.name
            }
        }
        return nil
    }
}

#Preview {
    SplashScreen(onEnter: {})
        .preferredColorScheme(.dark)
}
