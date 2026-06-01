import Foundation
import DesignSystem

@Observable
final class TarotViewModel {
    enum Phase { case spreadSelection, drawing, interpretation }

    var phase: Phase = .spreadSelection
    var drawnCards: [TarotDrawResult] = []
    var revealedCards: Set<Int> = []
    var isInterpreting = false
    var aiInterpretation: String?

    private let archiveManager = ArchiveManager()
    private let templates = PromptTemplates()

    var allRevealed: Bool {
        !drawnCards.isEmpty && revealedCards.count == drawnCards.count
    }

    func drawCards(count: Int) {
        let spreadType: SpreadType = count == 1 ? .single : .threeCard
        drawnCards = TarotEngine.shuffleAndDraw(count: count, spreadType: spreadType)
        revealedCards = []
        phase = .drawing
    }

    func revealCard(at index: Int) {
        guard !revealedCards.contains(index) else { return }
        revealedCards.insert(index)
    }

    func requestInterpretation() {
        phase = .interpretation
        Task {
            await interpretWithAI()
        }
    }

    private func interpretWithAI() async {
        isInterpreting = true
        defer { isInterpreting = false }

        do {
            let config = try buildConfig(feature: "tarot")

            let cardDescriptions = drawnCards.map { draw in
                "\(draw.position): \(draw.card.displayName(isReversed: draw.isReversed)) — \(draw.card.currentMeaning(isReversed: draw.isReversed))"
            }.joined(separator: "\n")

            let userPrompt = templates.resolveUser(
                feature: "tarot",
                variables: [
                    "spread": cardDescriptions,
                    "question": "",
                ]
            )

            let messages: [LLMMessage] = [.user(userPrompt)]
            let completion = try await LLMService().complete(config: config, messages: messages)
            let normalized = OutputNormalizer.cleanOutput(completion.text)
            aiInterpretation = normalized

            let cardNames = drawnCards.map { $0.card.nameCN }
            archiveManager.saveTarotResult(
                question: "",
                cardNames: cardNames,
                interpretation: normalized
            )
        } catch {
            print("[TarotVM] AI Error: \(error)")
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
