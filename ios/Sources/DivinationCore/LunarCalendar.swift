import Foundation

/// LunarCalendar — 农历转换器
///
/// Converts solar (Gregorian) dates to lunar dates using a compact bitmask
/// lookup table encoding 201 years of lunar data (1900–2100).
///
/// Each Int64 encodes 1 year of lunar data:
///   bits 0-3:    leap month number (0 = no leap month)
///   bits 4-15:   each bit = 0 for 29-day month, 1 for 30-day month (12 months)
///   bit 16:      leap month days (0 = 29, 1 = 30)
///
/// Reference: Jan 31, 1900 = 农历庚子年正月初一
public enum LunarCalendar {

    // MARK: - Data Structures

    public struct LunarDate {
        public let year: Int
        public let month: Int
        public let day: Int
        public let isLeapMonth: Bool
        public let monthName: String
        public let dayName: String
    }

    public struct GanzhiDate {
        public let yearStem: String
        public let yearBranch: String
        public let monthStem: String
        public let monthBranch: String
        public let dayStem: String
        public let dayBranch: String
        public let zodiac: String

        public var yearGanzhi: String { "\(yearStem)\(yearBranch)" }
        public var monthGanzhi: String { "\(monthStem)\(monthBranch)" }
        public var dayGanzhi: String { "\(dayStem)\(dayBranch)" }
    }

    // MARK: - Lunar Data Table (1900–2100)

    private static let lunarInfo: [Int64] = [
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06aa0, 0x1a6c4, 0x0aae0,
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252,
        0x0d520,
    ]

    // MARK: - Constants

    private static let lunarMonths = [
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月",
    ]

    private static let lunarDays = [
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十",
    ]

    private static let heavenlyStems = ["甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"]

    private static let earthlyBranches = ["子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"]

    private static let zodiacAnimals = ["鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"]

    // MARK: - Solar to Lunar Conversion

    /// Convert a solar date to a lunar date.
    public static func solarToLunar(year: Int, month: Int, day: Int) -> LunarDate {
        let calendar = Calendar(identifier: .gregorian)
        let baseDate = calendar.date(from: DateComponents(year: 1900, month: 1, day: 31))!
        let targetDate = calendar.date(from: DateComponents(year: year, month: month, day: day))!

        let dayCount = calendar.dateComponents([.day], from: baseDate, to: targetDate).day ?? 0

        guard dayCount >= 0 else {
            return LunarDate(
                year: year, month: month, day: day,
                isLeapMonth: false, monthName: "未知月", dayName: "未知日"
            )
        }

        var remainingDays = dayCount
        var lunarYear = 1900

        while lunarYear <= 2100 {
            let yearDays = daysInLunarYear(lunarYear)
            if remainingDays < yearDays { break }
            remainingDays -= yearDays
            lunarYear += 1
        }

        guard lunarYear <= 2100 else {
            return LunarDate(year: 2100, month: 1, day: 1, isLeapMonth: false, monthName: "正月", dayName: "初一")
        }

        let leap = leapMonthOf(lunarYear)
        let totalMonths = leap > 0 ? 13 : 12
        var lunarMonth = 1
        var isLeapMonth = false

        for i in 1...totalMonths {
            let monthDays: Int
            let currentIsLeap: Bool
            let currentMonth: Int

            if leap > 0 && i == leap + 1 {
                // This iteration is the leap month
                monthDays = leapMonthDays(lunarYear)
                currentIsLeap = true
                currentMonth = leap
            } else if leap > 0 && i > leap + 1 {
                // After leap month
                monthDays = daysInLunarMonth(lunarYear, i - 1)
                currentIsLeap = false
                currentMonth = i - 1
            } else {
                monthDays = daysInLunarMonth(lunarYear, i)
                currentIsLeap = false
                currentMonth = i
            }

            if remainingDays < monthDays {
                lunarMonth = currentMonth
                isLeapMonth = currentIsLeap
                break
            }
            remainingDays -= monthDays
        }

        let lunarDay = remainingDays + 1
        let monthIdx = (lunarMonth - 1).clamped(to: 0...11)
        let dayIdx = (lunarDay - 1).clamped(to: 0...29)

        return LunarDate(
            year: lunarYear,
            month: lunarMonth,
            day: lunarDay,
            isLeapMonth: isLeapMonth,
            monthName: isLeapMonth ? "闰\(lunarMonths[monthIdx])" : lunarMonths[monthIdx],
            dayName: lunarDays[dayIdx]
        )
    }

    // MARK: - Ganzhi Calculations

    /// Calculate full Ganzhi date for a solar date.
    public static func calculateGanzhi(year: Int, month: Int, day: Int) -> GanzhiDate {
        let lunar = solarToLunar(year: year, month: month, day: day)

        // Year Ganzhi (1984 = 甲子)
        let yearStemIdx = ((lunar.year - 4) % 10 + 10) % 10
        let yearBranchIdx = ((lunar.year - 4) % 12 + 12) % 12

        // Month Ganzhi
        let baseStem: Int = switch yearStemIdx {
        case 0, 5: 2   // 甲/己 → 丙寅
        case 1, 6: 4   // 乙/庚 → 戊寅
        case 2, 7: 6   // 丙/辛 → 庚寅
        case 3, 8: 8   // 丁/壬 → 壬寅
        case 4, 9: 0   // 戊/癸 → 甲寅
        default: 2
        }
        let monthBranchIdx = (lunar.month + 1) % 12
        let monthStemIdx = (baseStem + (monthBranchIdx - 2 + 12) % 12) % 10

        // Day Ganzhi (Julian Day Number based)
        let calendar = Calendar(identifier: .gregorian)
        let date = calendar.date(from: DateComponents(year: year, month: month, day: day))!
        let epochDay = Int(date.timeIntervalSince1970 / 86400)
        let jdn = epochDay + 2440587
        let dayStemIdx = ((jdn + 9) % 10 + 10) % 10
        let dayBranchIdx = ((jdn + 1) % 12 + 12) % 12

        // Zodiac
        let zodiacIdx = ((lunar.year - 4) % 12 + 12) % 12

        return GanzhiDate(
            yearStem: heavenlyStems[yearStemIdx],
            yearBranch: earthlyBranches[yearBranchIdx],
            monthStem: heavenlyStems[monthStemIdx],
            monthBranch: earthlyBranches[monthBranchIdx],
            dayStem: heavenlyStems[dayStemIdx],
            dayBranch: earthlyBranches[dayBranchIdx],
            zodiac: zodiacAnimals[zodiacIdx]
        )
    }

    /// Get zodiac animal for a year.
    public static func zodiacAnimal(year: Int) -> String {
        zodiacAnimals[((year - 4) % 12 + 12) % 12]
    }

    /// Get year Ganzhi string.
    public static func yearGanzhi(year: Int) -> String {
        let s = ((year - 4) % 10 + 10) % 10
        let b = ((year - 4) % 12 + 12) % 12
        return "\(heavenlyStems[s])\(earthlyBranches[b])"
    }

    // MARK: - Formatting

    public static func formatLunarDate(_ lunar: LunarDate) -> String {
        let ygz = yearGanzhi(year: lunar.year)
        let dayStr = lunarDays[(lunar.day - 1).clamped(to: 0...29)]
        return "农历\(ygz)年\(lunar.monthName)\(dayStr)"
    }

    public static func formatFullDate(year: Int, month: Int, day: Int) -> String {
        let lunar = solarToLunar(year: year, month: month, day: day)
        let ganzhi = calculateGanzhi(year: year, month: month, day: day)
        let dayStr = lunarDays[(lunar.day - 1).clamped(to: 0...29)]
        return "\(ganzhi.yearGanzhi)年 \(lunar.monthName)\(dayStr)日 | \(ganzhi.zodiac)年"
    }

    // MARK: - Private Helpers

    private static func daysInLunarYear(_ year: Int) -> Int {
        let info = lunarInfo[year - 1900]
        var sum = 348
        var bit: Int64 = 0x8000
        while bit > 0x8 {
            if info & bit != 0 { sum += 1 }
            bit >>= 1
        }
        let leap = leapMonthOf(year)
        if leap > 0 { sum += leapMonthDays(year) }
        return sum
    }

    private static func daysInLunarMonth(_ year: Int, _ month: Int) -> Int {
        let info = lunarInfo[year - 1900]
        let bit: Int64 = 1 << (16 - month)
        return (info & bit) != 0 ? 30 : 29
    }

    private static func leapMonthOf(_ year: Int) -> Int {
        Int(lunarInfo[year - 1900] & 0xf)
    }

    private static func leapMonthDays(_ year: Int) -> Int {
        guard leapMonthOf(year) > 0 else { return 0 }
        return (lunarInfo[year - 1900] & 0x10000) != 0 ? 30 : 29
    }
}

// MARK: - Int Clamping Helper

private extension Int {
    func clamped(to range: ClosedRange<Int>) -> Int {
        Swift.min(Swift.max(self, range.lowerBound), range.upperBound)
    }
}
