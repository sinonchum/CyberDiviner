import Foundation
import DesignSystem
import Persistence

@Observable
final public class ArchiveViewModel {
    var filterType: DivinationType?
    var allReadings: [SavedReading] = []

    private let store: any ReadingStoreProtocol = UserDefaultsReadingStore()

    var filteredReadings: [SavedReading] {
        guard let filterType else { return allReadings }
        return allReadings.filter { $0.type == filterType }
    }

    init() {
        loadReadings()
    }

    func loadReadings() {
        allReadings = store.getAll()
    }

    func delete(_ reading: SavedReading) {
        store.delete(id: reading.id)
        loadReadings()
    }
}
