import Foundation

// MARK: - Divination Type

/// Matches the Android DivinationType enum
enum DivinationType: String, Codable, CaseIterable, Identifiable {
    case oracle   // 灵签 oracle divination
    case liuyao   // 六爻 Liu Yao / I Ching hexagram
    case tarot    // Tarot card reading

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .oracle: return "灵签"
        case .liuyao: return "六爻"
        case .tarot:  return "塔罗"
        }
    }

    var icon: String {
        switch self {
        case .oracle: return "scroll"
        case .liuyao: return "hexagon"
        case .tarot:  return "suit.spade"
        }
    }
}

// MARK: - Saved Reading

/// Core data model for a persisted divination reading.
/// Identifiable for SwiftUI lists, Codable for JSON serialization.
struct SavedReading: Identifiable, Codable, Equatable {
    /// Unique identifier
    let id: UUID

    /// Type of divination performed
    let type: DivinationType

    /// Short 4-character fortune title (e.g. "上上签", "大吉", "命运之")
    let title: String

    /// The user's original question
    let question: String

    /// Full result text / interpretation
    let resultText: String

    /// JSON-encoded string holding type-specific metadata
    /// (e.g. hexagram lines, tarot spread positions, verse number)
    let metadata: String

    /// When the reading was created
    let createdAt: Date

    // MARK: Initializer

    init(
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

    // MARK: Convenience

    /// Formatted creation date
    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: createdAt)
    }

    /// Short preview of the result text (first 80 chars)
    var preview: String {
        if resultText.count <= 80 { return resultText }
        let end = resultText.index(resultText.startIndex, offsetBy: 80)
        return String(resultText[..<end]) + "…"
    }
}
