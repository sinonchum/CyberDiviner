import Foundation
import SwiftUI
import DesignSystem
import DivinationCore
import AI
import Persistence

// MARK: - Oracle Message

public struct OracleMessage: Identifiable {
    public let id = UUID()
    public let role: MessageRole
    public var content: String

    public enum MessageRole {
        case user
        case ai
    }

    public init(role: MessageRole, content: String) {
        self.role = role
        self.content = content
    }
}

// MARK: - Oracle ViewModel

@Observable
final public class OracleViewModel {

    public let maxRounds = 5

    var messages: [OracleMessage] = []
    var round: Int = 0
    var isLoading = false
    var streamingText: String = ""

    private let archiveManager = ArchiveManager()
    private let templates = PromptTemplates()

    public init() {
        // Initial system greeting
        messages.append(OracleMessage(
            role: .ai,
            content: "[ 系统载入 ] 赛博算命系统已上线。因果链就绪。\n\n输入你的困惑。事业、感情、财运、健康——系统将为你演算签文。"
        ))
    }

    // MARK: - Send Message

    func sendMessage(_ text: String) {
        guard !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        guard !isLoading else { return }
        guard round < maxRounds else { return }

        // Add user message
        let userMsg = OracleMessage(role: .user, content: text)
        messages.append(userMsg)
        isLoading = true

        Task {
            await performInference(question: text)
        }
    }

    // MARK: - Inference

    @MainActor
    private func performInference(question: String) async {
        defer {
            isLoading = false
            round += 1
        }

        do {
            let config = try buildConfig(feature: "oracle")

            // Build conversation history (all previous messages)
            var apiMessages: [LLMMessage] = []
            for msg in messages {
                switch msg.role {
                case .user:
                    apiMessages.append(.user(msg.content))
                case .ai:
                    apiMessages.append(.assistant(msg.content))
                }
            }

            // Try streaming first, fall back to complete
            let result: String
            if LLMService.supportsStreaming(config.model.provider) {
                result = await streamCompletion(config: config, messages: apiMessages)
            } else {
                let completion = try await LLMService().complete(config: config, messages: apiMessages)
                result = OutputNormalizer.cleanOutput(completion.text)
            }

            let formatted = formatOracleResult(result)
            let aiMsg = OracleMessage(role: .ai, content: formatted)
            messages.append(aiMsg)

            // Save to archive
            archiveManager.saveOracleResult(
                question: question,
                verse: "",
                analysis: "",
                verdict: formatted
            )
        } catch {
            print("[OracleVM] Error: \(error)")
            let errorMsg = OracleMessage(
                role: .ai,
                content: "[ 系统异常 ] 量子因果链中断。错误码: \(error.localizedDescription)。请稍后重试。"
            )
            messages.append(errorMsg)
        }
    }

    // MARK: - Streaming

    @MainActor
    private func streamCompletion(config: LLMConfig, messages: [LLMMessage]) async -> String {
        streamingText = ""

        do {
            let stream = try await LLMService().completeStream(config: config, messages: messages)
            for try await chunk in stream {
                streamingText += chunk
                // Update the last AI message in-place if it exists and is streaming
                if let lastIdx = self.messages.indices.last,
                   self.messages[lastIdx].role == .ai,
                   self.messages[lastIdx].content.hasSuffix("...") {
                    self.messages[lastIdx].content = streamingText
                }
            }
            return OutputNormalizer.cleanOutput(streamingText)
        } catch {
            print("[OracleVM] Stream error, falling back: \(error)")
            do {
                let completion = try await LLMService().complete(config: config, messages: messages)
                return OutputNormalizer.cleanOutput(completion.text)
            } catch {
                return "解读失败：\(error.localizedDescription)"
            }
        }
    }

    // MARK: - Format Result

    private func formatOracleResult(_ raw: String) -> String {
        let normalized = OutputNormalizer.cleanOutput(raw)
        // Try to parse as OracleResult first
        let result = OracleFormatter.parseOracleOutput(normalized)
        if !result.verse.isEmpty || !result.analysis.isEmpty || !result.verdict.isEmpty {
            var s = ""
            if !result.verse.isEmpty {
                s += "[ 载入签文 ]\n\(result.verse)\n\n"
            }
            if !result.analysis.isEmpty {
                s += "[ 逻辑解析 ]\n\(result.analysis)\n\n"
            }
            if !result.verdict.isEmpty {
                s += "[ 最终断语 ]\n\(result.verdict)"
            }
            return s
        }
        return normalized
    }

    // MARK: - Config Builder

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

// MARK: - LLMService Streaming Extension

extension LLMService {
    /// Check if the provider supports streaming.
    public static func supportsStreaming(_ provider: LLMProvider) -> Bool {
        switch provider {
        case .openAI, .openAICompatible, .anthropic:
            return true
        case .ollama:
            return false
        }
    }

    /// Streaming completion — returns an AsyncThrowingStream of text chunks.
    public func completeStream(config: LLMConfig, messages: [LLMMessage]) async throws -> AsyncThrowingStream<String, Error> {
        guard !config.apiKey.isEmpty || config.model.provider == .ollama else {
            throw LLMError.missingAPIKey
        }

        let (url, body) = try buildStreamRequest(config: config, messages: messages)

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = body
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("text/event-stream", forHTTPHeaderField: "Accept")
        request.timeoutInterval = 120

        switch config.model.provider {
        case .openAI, .openAICompatible:
            request.setValue("Bearer \(config.apiKey)", forHTTPHeaderField: "Authorization")
        case .anthropic:
            request.setValue(config.apiKey, forHTTPHeaderField: "x-api-key")
            request.setValue("2023-06-01", forHTTPHeaderField: "anthropic-version")
        case .ollama:
            break
        }

        let (bytes, response) = try await URLSession.shared.bytes(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw LLMError.networkError(URLError(.badServerResponse))
        }
        guard (200...299).contains(httpResponse.statusCode) else {
            throw LLMError.httpError(statusCode: httpResponse.statusCode, body: "")
        }

        let provider = config.model.provider

        return AsyncThrowingStream<String, Error> { continuation in
            Task {
                do {
                    for try await line in bytes.lines {
                        guard line.hasPrefix("data: ") else { continue }
                        let payload = String(line.dropFirst(6)).trimmingCharacters(in: .whitespaces)
                        if payload == "[DONE]" { break }

                        if let data = payload.data(using: .utf8),
                           let chunk = extractStreamChunk(data: data, provider: provider) {
                            continuation.yield(chunk)
                        }
                    }
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
        }
    }

    private func buildStreamRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
        switch config.model.provider {
        case .anthropic:
            return try buildAnthropicStreamRequest(config: config, messages: messages)
        default:
            return try buildOpenAIStreamRequest(config: config, messages: messages)
        }
    }

    private func buildOpenAIStreamRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
        let baseUrl = config.model.baseUrl ?? "https://api.openai.com/v1"
        guard let url = URL(string: "\(baseUrl)/chat/completions") else {
            throw LLMError.invalidURL
        }

        var allMessages = messages
        if let systemPrompt = config.systemPrompt {
            allMessages.insert(.system(systemPrompt), at: 0)
        }

        let body: [String: Any] = [
            "model": config.model.modelId,
            "messages": allMessages.map { ["role": $0.role, "content": $0.content] },
            "max_tokens": config.maxTokens,
            "temperature": config.temperature,
            "stream": true
        ]

        return (url, try JSONSerialization.data(withJSONObject: body))
    }

    private func buildAnthropicStreamRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
        let baseUrl = config.model.baseUrl ?? "https://api.anthropic.com/v1"
        guard let url = URL(string: "\(baseUrl)/messages") else {
            throw LLMError.invalidURL
        }

        let apiMessages = messages.filter { $0.role != "system" }.map {
            ["role": $0.role, "content": $0.content]
        }

        var body: [String: Any] = [
            "model": config.model.modelId,
            "max_tokens": config.maxTokens,
            "messages": apiMessages,
            "temperature": config.temperature,
            "stream": true
        ]

        if let systemPrompt = config.systemPrompt {
            body["system"] = systemPrompt
        }

        return (url, try JSONSerialization.data(withJSONObject: body))
    }

    private func extractStreamChunk(data: Data, provider: LLMProvider) -> String? {
        guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return nil
        }

        switch provider {
        case .anthropic:
            if let type = json["type"] as? String, type == "content_block_delta" {
                let delta = json["delta"] as? [String: Any]
                return delta?["text"] as? String
            }
        default: // OpenAI / compatible
            let choices = json["choices"] as? [[String: Any]] ?? []
            let delta = choices.first?["delta"] as? [String: Any]
            return delta?["content"] as? String
        }
        return nil
    }
}
