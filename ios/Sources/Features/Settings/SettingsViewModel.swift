import Foundation
import SwiftUI
import DesignSystem
import AI

@Observable
final public class SettingsViewModel {
    @ObservationIgnored
    @AppStorage("llm_provider") var provider: String = "openai_compatible"

    @ObservationIgnored
    @AppStorage("llm_base_url") var baseURL: String = ""

    @ObservationIgnored
    @AppStorage("llm_model_id") var modelID: String = ""

    @ObservationIgnored
    @AppStorage("inference_mode") var inferenceMode: String = "auto"

    var apiKey: String = ""
    var showAPIKey: Bool = false

    init() {
        // Load API key from Keychain for current provider
        let providerEnum = LLMProvider(rawValue: provider) ?? .openAICompatible
        apiKey = (try? KeychainHelper.loadAPIKey(for: providerEnum)) ?? ""
    }

    func save() {
        let providerEnum = LLMProvider(rawValue: provider) ?? .openAICompatible
        do {
            try KeychainHelper.saveAPIKey(apiKey, for: providerEnum)
        } catch {
            print("[SettingsVM] Keychain save error: \(error)")
        }
    }
}
