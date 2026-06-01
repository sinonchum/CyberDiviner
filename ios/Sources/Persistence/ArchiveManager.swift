import Foundation
import SwiftUI

// MARK: - Archive Manager

/// High-level convenience layer over `ReadingStoreProtocol` for saving
/// results from each divination module with type-specific metadata.
@Observable
final class ArchiveManager {

    // MARK: Properties

    let store: any ReadingStoreProtocol

    // MARK: Lifecycle

    init(store: any ReadingStoreProtocol = UserDefaultsReadingStore()) {
        self.store = store
    }

    // MARK: - Oracle (灵签)

    /// Save an oracle / 灵签 result.
    func saveOracleResult(
        question: String,
        verse: String,
        analysis: String,
        verdict: String
    ) -> SavedReading {
        let metadataDict: [String: Any] = [
            "verse": verse,
            "verdict": verdict
        ]
        let metadataJSON = jsonString(from: metadataDict)

        let title = generateShortTitle(from: verdict)

        let resultText = """
        【签文】
        \(verse)

        【解签】
        \(analysis)

        【判定】\(verdict)
        """

        let reading = SavedReading(
            type: .oracle,
            title: title,
            question: question,
            resultText: resultText,
            metadata: metadataJSON
        )
        store.save(reading)
        return reading
    }

    // MARK: - Liuyao (六爻)

    /// Save a 六爻 hexagram result.
    func saveLiuyaoResult(
        question: String,
        hexagramName: String,
        interpretation: String
    ) -> SavedReading {
        let metadataDict: [String: Any] = [
            "hexagramName": hexagramName
        ]
        let metadataJSON = jsonString(from: metadataDict)

        let title = generateShortTitle(from: hexagramName)

        let resultText = """
        【卦名】\(hexagramName)

        【解卦】
        \(interpretation)
        """

        let reading = SavedReading(
            type: .liuyao,
            title: title,
            question: question,
            resultText: resultText,
            metadata: metadataJSON
        )
        store.save(reading)
        return reading
    }

    // MARK: - Tarot (塔罗)

    /// Save a tarot card reading result.
    func saveTarotResult(
        question: String,
        cardNames: [String],
        interpretation: String
    ) -> SavedReading {
        let metadataDict: [String: Any] = [
            "cardNames": cardNames
        ]
        let metadataJSON = jsonString(from: metadataDict)

        let firstCard = cardNames.first ?? "塔罗"
        let title = generateShortTitle(from: firstCard)

        let cardsList = cardNames.joined(separator: "、")
        let resultText = """
        【牌阵】\(cardsList)

        【解读】
        \(interpretation)
        """

        let reading = SavedReading(
            type: .tarot,
            title: title,
            question: question,
            resultText: resultText,
            metadata: metadataJSON
        )
        store.save(reading)
        return reading
    }

    // MARK: - Fortune Title Generation

    /// Generate a 4-character summary title for a reading.
    /// Extracts meaningful characters from the reading's type-specific content.
    func generateFortuneTitle(for reading: SavedReading) -> String {
        // If title is already exactly 4 chars, return it
        if reading.title.count == 4 {
            return reading.title
        }
        return generateShortTitle(from: reading.title)
    }

    // MARK: - Private Helpers

    /// Produce a ≤4-character title string from a source phrase.
    /// Strategy: strip non-CJK / non-alphanumeric chars, take first 4 meaningful chars.
    /// Falls back to the first 4 chars of the raw string, or a default.
    private func generateShortTitle(from source: String) -> String {
        let cleaned = source.replacingOccurrences(
            of: "[^\\u4e00-\\u9fff\\w]",
            with: "",
            options: .regularExpression
        )
        if cleaned.isEmpty {
            // Fallback: use raw source truncated
            let raw = source.trimmingCharacters(in: .whitespacesAndNewlines)
            if raw.isEmpty { return "未知" }
            return String(raw.prefix(4))
        }
        return String(cleaned.prefix(4))
    }

    /// Serialize a dictionary to a JSON string, returning "{}" on failure.
    private func jsonString(from dict: [String: Any]) -> String {
        guard JSONSerialization.isValidJSONObject(dict),
              let data = try? JSONSerialization.data(withJSONObject: dict, options: []),
              let str = String(data: data, encoding: .utf8)
        else {
            return "{}"
        }
        return str
    }
}
