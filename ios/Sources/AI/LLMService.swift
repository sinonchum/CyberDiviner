import Foundation

// MARK: - LLM Errors

/// Errors that can occur during LLM operations.
public enum LLMError: Error, LocalizedError {
    case invalidURL
    case networkError(Error)
    case httpError(statusCode: Int, body: String)
    case decodingError(Error)
    case missingAPIKey
    case unsupportedProvider
    case emptyResponse
    
    public var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .networkError(let error):
            return "Network error: \(error.localizedDescription)"
        case .httpError(let code, let body):
            return "HTTP \(code): \(body)"
        case .decodingError(let error):
            return "Decoding error: \(error.localizedDescription)"
        case .missingAPIKey:
            return "API key is required"
        case .unsupportedProvider:
            return "Unsupported provider"
        case .emptyResponse:
            return "Empty response from provider"
        }
    }
}

// MARK: - LLM Service

/// Model-agnostic LLM service. Routes requests to the correct provider wire format,
/// parses responses into a unified `LLMCompletion`.
public actor LLMService {
    private let session: URLSession
    private let decoder: JSONDecoder
    
    public init(session: URLSession = .shared) {
        self.session = session
        self.decoder = JSONDecoder()
    }
    
    // MARK: - Public API
    
    /// Send a completion request to the LLM provider.
    public func complete(config: LLMConfig, messages: [LLMMessage]) async throws -> LLMCompletion {
        guard !config.apiKey.isEmpty || config.model.provider == .ollama else {
            throw LLMError.missingAPIKey
        }
        
        let startTime = Date()
        let (url, body) = try buildRequest(config: config, messages: messages)
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = body
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 60
        
        // Add provider-specific auth headers
        switch config.model.provider {
        case .openAI, .openAICompatible:
            request.setValue("Bearer \(config.apiKey)", forHTTPHeaderField: "Authorization")
        case .anthropic:
            request.setValue(config.apiKey, forHTTPHeaderField: "x-api-key")
            request.setValue("2023-06-01", forHTTPHeaderField: "anthropic-version")
        case .ollama:
            break // No auth needed
        }
        
        let (data, response) = try await session.data(for: request)
        let latency = Date().timeIntervalSince(startTime) * 1000 // Convert to ms
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw LLMError.networkError(URLError(.badServerResponse))
        }
        
        guard (200...299).contains(httpResponse.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw LLMError.httpError(statusCode: httpResponse.statusCode, body: body)
        }
        
        return try parseResponse(
            data: data,
            provider: config.model.provider,
            modelId: config.model.modelId,
            latencyMs: latency
        )
    }
    
    // MARK: - Request Building
    
    private func buildRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
        switch config.model.provider {
        case .openAI, .openAICompatible:
            return try buildOpenAIRequest(config: config, messages: messages)
        case .anthropic:
            return try buildAnthropicRequest(config: config, messages: messages)
        case .ollama:
            return try buildOllamaRequest(config: config, messages: messages)
        }
    }
    
    private func buildOpenAIRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
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
            "temperature": config.temperature
        ]
        
        return (url, try JSONSerialization.data(withJSONObject: body))
    }
    
    private func buildAnthropicRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
        let baseUrl = config.model.baseUrl ?? "https://api.anthropic.com/v1"
        guard let url = URL(string: "\(baseUrl)/messages") else {
            throw LLMError.invalidURL
        }
        
        // Anthropic uses separate system parameter, not in messages
        let apiMessages = messages.filter { $0.role != "system" }.map {
            ["role": $0.role, "content": $0.content]
        }
        
        var body: [String: Any] = [
            "model": config.model.modelId,
            "max_tokens": config.maxTokens,
            "messages": apiMessages,
            "temperature": config.temperature
        ]
        
        if let systemPrompt = config.systemPrompt {
            body["system"] = systemPrompt
        }
        
        return (url, try JSONSerialization.data(withJSONObject: body))
    }
    
    private func buildOllamaRequest(config: LLMConfig, messages: [LLMMessage]) throws -> (URL, Data) {
        let baseUrl = config.model.baseUrl ?? "http://localhost:11434"
        guard let url = URL(string: "\(baseUrl)/api/chat") else {
            throw LLMError.invalidURL
        }
        
        var allMessages = messages
        if let systemPrompt = config.systemPrompt {
            allMessages.insert(.system(systemPrompt), at: 0)
        }
        
        let body: [String: Any] = [
            "model": config.model.modelId,
            "messages": allMessages.map { ["role": $0.role, "content": $0.content] },
            "stream": false,
            "options": [
                "temperature": config.temperature,
                "num_predict": config.maxTokens
            ]
        ]
        
        return (url, try JSONSerialization.data(withJSONObject: body))
    }
    
    // MARK: - Response Parsing
    
    private func parseResponse(
        data: Data,
        provider: LLMProvider,
        modelId: String,
        latencyMs: TimeInterval
    ) throws -> LLMCompletion {
        switch provider {
        case .openAI, .openAICompatible:
            return try parseOpenAIResponse(data: data, modelId: modelId, provider: provider, latencyMs: latencyMs)
        case .anthropic:
            return try parseAnthropicResponse(data: data, modelId: modelId, latencyMs: latencyMs)
        case .ollama:
            return try parseOllamaResponse(data: data, modelId: modelId, latencyMs: latencyMs)
        }
    }
    
    private func parseOpenAIResponse(
        data: Data,
        modelId: String,
        provider: LLMProvider,
        latencyMs: TimeInterval
    ) throws -> LLMCompletion {
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw LLMError.decodingError(URLError(.cannotParseResponse))
        }
        
        let choices = json["choices"] as? [[String: Any]] ?? []
        let choice = choices.first
        let message = choice?["message"] as? [String: Any]
        let text = message?["content"] as? String ?? ""
        
        let usage = json["usage"] as? [String: Any]
        let promptTokens = usage?["prompt_tokens"] as? Int ?? 0
        let completionTokens = usage?["completion_tokens"] as? Int ?? 0
        
        return LLMCompletion(
            text: text,
            model: modelId,
            provider: provider,
            promptTokens: promptTokens,
            completionTokens: completionTokens,
            latencyMs: latencyMs
        )
    }
    
    private func parseAnthropicResponse(
        data: Data,
        modelId: String,
        latencyMs: TimeInterval
    ) throws -> LLMCompletion {
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw LLMError.decodingError(URLError(.cannotParseResponse))
        }
        
        let content = json["content"] as? [[String: Any]] ?? []
        let textBlock = content.first { ($0["type"] as? String) == "text" }
        let text = textBlock?["text"] as? String ?? ""
        
        let usage = json["usage"] as? [String: Any]
        let promptTokens = usage?["input_tokens"] as? Int ?? 0
        let completionTokens = usage?["output_tokens"] as? Int ?? 0
        
        return LLMCompletion(
            text: text,
            model: modelId,
            provider: .anthropic,
            promptTokens: promptTokens,
            completionTokens: completionTokens,
            latencyMs: latencyMs
        )
    }
    
    private func parseOllamaResponse(
        data: Data,
        modelId: String,
        latencyMs: TimeInterval
    ) throws -> LLMCompletion {
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw LLMError.decodingError(URLError(.cannotParseResponse))
        }
        
        let message = json["message"] as? [String: Any]
        let text = message?["content"] as? String ?? ""
        
        let promptTokens = json["prompt_eval_count"] as? Int ?? 0
        let completionTokens = json["eval_count"] as? Int ?? 0
        
        return LLMCompletion(
            text: text,
            model: modelId,
            provider: .ollama,
            promptTokens: promptTokens,
            completionTokens: completionTokens,
            latencyMs: latencyMs
        )
    }
}
