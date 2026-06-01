import Foundation

// MARK: - DivinationType

public enum DivinationType: String, Codable, CaseIterable {
    case oracle
    case liuyao
    case tarot

    public var displayName: String {
        switch self {
        case .oracle: "叩问天机"
        case .liuyao: "周易六爻"
        case .tarot: "赛博塔罗"
        }
    }

    public var icon: String {
        switch self {
        case .oracle: "签"
        case .liuyao: "爻"
        case .tarot: "牌"
        }
    }
}

// MARK: - SavedReading

public struct SavedReading: Identifiable, Codable, Equatable {
    public let id: UUID
    public let type: DivinationType
    public let title: String
    public let question: String
    public let resultText: String
    public let metadata: String
    public let createdAt: Date

    public init(
        id: UUID = UUID(),
        type: DivinationType,
        title: String,
        question: String,
        resultText: String,
        metadata: String = "{}",
        createdAt: Date = Date()
    ) {
        self.id = id
        self.type = type
        self.title = title
        self.question = question
        self.resultText = resultText
        self.metadata = metadata
        self.createdAt = createdAt
    }

    public var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        return formatter.string(from: createdAt)
    }

    public static let preview = SavedReading(
        type: .oracle,
        title: "春风化雨",
        question: "近期事业如何？",
        resultText: "[ 载入签文 ]\n春风化雨润无声，柳暗花明又一程。\n\n[ 逻辑解析 ]\n当前局势正在悄然变化...\n\n[ 最终断语 ]\n保持耐心，时机将至。"
    )
}
