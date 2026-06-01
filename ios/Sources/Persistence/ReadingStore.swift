import Foundation
import SwiftUI

// MARK: - Protocol

/// Protocol defining CRUD operations for reading persistence.
/// Designed so a SwiftData implementation can be swapped in later when built with Xcode.
public protocol ReadingStoreProtocol {
    func getAll() -> [SavedReading]
    func save(_ reading: SavedReading)
    func delete(id: UUID)
    func getByType(_ type: DivinationType) -> [SavedReading]
    func search(query: String) -> [SavedReading]
}

// MARK: - UserDefaults-Backed Implementation

/// A concrete `ReadingStoreProtocol` that persists readings as JSON in UserDefaults.
/// This is the default implementation used when SwiftData is unavailable (e.g. non-Xcode builds).
@Observable
public final class UserDefaultsReadingStore: ReadingStoreProtocol {

    // MARK: Storage key

    private let storageKey = "cyberdiviner_saved_readings"

    // MARK: Published state for SwiftUI binding

    /// All readings, kept in-memory and synced to UserDefaults.
    /// SwiftUI views observing this property will re-render on changes.
    public private(set) var readings: [SavedReading] = []

    // MARK: Lifecycle

    public init() {
        loadFromDisk()
    }

    // MARK: ReadingStoreProtocol

    public func getAll() -> [SavedReading] {
        readings.sorted { $0.createdAt > $1.createdAt }
    }

    public func save(_ reading: SavedReading) {
        // Upsert: replace existing or append
        if let idx = readings.firstIndex(where: { $0.id == reading.id }) {
            readings[idx] = reading
        } else {
            readings.append(reading)
        }
        persistToDisk()
    }

    public func delete(id: UUID) {
        readings.removeAll { $0.id == id }
        persistToDisk()
    }

    public func getByType(_ type: DivinationType) -> [SavedReading] {
        readings
            .filter { $0.type == type }
            .sorted { $0.createdAt > $1.createdAt }
    }

    public func search(query: String) -> [SavedReading] {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return getAll() }
        let lowered = trimmed.lowercased()
        return readings.filter { reading in
            reading.question.lowercased().contains(lowered)
            || reading.resultText.lowercased().contains(lowered)
            || reading.title.lowercased().contains(lowered)
        }.sorted { $0.createdAt > $1.createdAt }
    }

    // MARK: - Private persistence helpers

    private func loadFromDisk() {
        guard let data = UserDefaults.standard.data(forKey: storageKey) else {
            readings = []
            return
        }
        do {
            readings = try JSONDecoder().decode([SavedReading].self, from: data)
        } catch {
            print("[ReadingStore] Failed to decode readings: \(error)")
            readings = []
        }
    }

    private func persistToDisk() {
        do {
            let data = try JSONEncoder().encode(readings)
            UserDefaults.standard.set(data, forKey: storageKey)
        } catch {
            print("[ReadingStore] Failed to encode readings: \(error)")
        }
    }
}
