import Foundation
import DesignSystem

@Observable
final class LiuyaoViewModel {
    enum Phase { case question, casting, result }

    var phase: Phase = .question
    var question = ""
    var currentLine = 0
    var lines: [LiuyaoEngine.CoinToss] = []
    var result: LiuyaoEngine.DivinationResult?
    var isInterpreting = false
    var aiInterpretation: String?

    private let archiveManager = ArchiveManager()
    private let templates = PromptTemplates()

    func startCasting() {
        let q = question.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return }
        phase = .casting
        currentLine = 0
        lines = []
    }

    func castNextLine() {
        guard currentLine < 6 else { return }
        let toss = LiuyaoEngine.throwCoins()
        lines.append(toss)
        currentLine += 1

        if currentLine == 6 {
            buildResult()
        }
    }

    private func buildResult() {
        let lineValues = lines.map { $0.sum }
        result = LiuyaoEngine.castHexagram(from: lineValues, question: question)
        phase = .result

        Task {
            await interpretWithAI()
        }
    }

    private func interpretWithAI() async {
        guard let result else { return }
        isInterpreting = true
        defer { isInterpreting = false }

        do {
            let config = try buildConfig(feature: "liuyao")

            let changingPositions = result.tosses.enumerated()
                .compactMap { $0.element.lineState.isChanging ? "第\($0.offset + 1)爻" : nil }
                .joined(separator: "、")

            let userPrompt = templates.resolveUser(
                feature: "liuyao",
                variables: [
                    "hexagram_name": result.primaryHexagram.chineseName,
                    "hexagram_number": "\(result.primaryHexagram.number)",
                    "upper_trigram": result.primaryHexagram.upperTrigram.chineseName,
                    "lower_trigram": result.primaryHexagram.lowerTrigram.chineseName,
                    "changing_lines": changingPositions.isEmpty ? "无" : changingPositions,
                    "day_gan_zhi": "今日",
                    "question": question,
                ]
            )

            let messages: [LLMMessage] = [.user(userPrompt)]
            let completion = try await LLMService().complete(config: config, messages: messages)
            let normalized = OutputNormalizer.cleanOutput(completion.text)
            aiInterpretation = normalized

            archiveManager.saveLiuyaoResult(
                question: question,
                hexagramName: "\(result.primaryHexagram.chineseName) \(result.primaryHexagram.englishName)",
                interpretation: normalized
            )
        } catch {
            print("[LiuyaoVM] AI Error: \(error)")
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
