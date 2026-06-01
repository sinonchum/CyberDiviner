import Foundation
import DesignSystem
import DivinationCore
import AI
import Persistence

@Observable
final public class OracleViewModel {
    var isLoading = false
    var lastResult: OracleResult?

    private let archiveManager = ArchiveManager()
    private let templates = PromptTemplates()

    func sendQuestion(_ question: String) async -> OracleResult? {
        isLoading = true
        defer { isLoading = false }

        do {
            let config = try buildConfig(feature: "oracle")
            let userPrompt = templates.resolveUser(
                feature: "oracle",
                variables: ["query": question]
            )

            let messages: [LLMMessage] = [.user(userPrompt)]
            let completion = try await LLMService().complete(config: config, messages: messages)

            let normalized = OutputNormalizer.cleanOutput(completion.text)
            let result = OracleFormatter.parseOracleOutput(normalized)
            lastResult = result

            // Save to archive
            archiveManager.saveOracleResult(
                question: question,
                verse: result.verse,
                analysis: result.analysis,
                verdict: result.verdict
            )

            return result
        } catch {
            print("[OracleVM] Error: \(error)")
            return nil
        }
    }

    private func buildConfig(feature: String) throws -> LLMConfig {
        let providerRaw = UserDefaults.standard.string(forKey: "llm_provider") ?? "openai_compatible"
        let provider = LLMProvider(rawValue: providerRaw) ?? .openAICompatible
        let baseURL = UserDefaults.standard.string(forKey: "llm_base_url").flatMap { $0.isEmpty ? nil : $0 }
        let modelID = UserDefaults.standard.string(forKey: "llm_model_id") ?? "gpt-4o"
        let apiKey = (try? KeychainHelper.loadAPIKey(for: provider)) ?? ""

        let model = LLMModel(provider: provider, modelId: modelID, baseUrl: baseURL)
        let systemPrompt = templates.resolveSystem(feature: feature, personaName: "零")
        return LLMConfig(apiKey: apiKey, model: model, systemPrompt: systemPrompt)
    }
}
