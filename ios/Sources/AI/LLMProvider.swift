import Foundation

// MARK: - LLM Provider

/// Supported LLM providers — each maps to its own API format.
public enum LLMProvider: String, Codable, CaseIterable {
    case openAI = "openai"
    case anthropic = "anthropic"
    case ollama = "ollama"
    case openAICompatible = "openai_compatible"
    
    public var displayName: String {
        switch self {
        case .openAI: return "OpenAI"
        case .anthropic: return "Anthropic"
        case .ollama: return "Ollama"
        case .openAICompatible: return "OpenAI-Compatible"
        }
    }
}

// MARK: - LLM Model

/// Model identifier + provider pairing.
public struct LLMModel: Codable, Identifiable {
    public let provider: LLMProvider
    public let modelId: String
    public let displayName: String
    public let baseUrl: String?
    public let maxTokens: Int
    
    public var id: String { "\(provider.rawValue)/\(modelId)" }
    
    public init(
        provider: LLMProvider,
        modelId: String,
        displayName: String? = nil,
        baseUrl: String? = nil,
        maxTokens: Int = 4096
    ) {
        self.provider = provider
        self.modelId = modelId
        self.displayName = displayName ?? modelId
        self.baseUrl = baseUrl
        self.maxTokens = maxTokens
    }
}

// MARK: - LLM Config

/// Resolved configuration for a single LLM call.
public struct LLMConfig {
    public let apiKey: String
    public let model: LLMModel
    public let temperature: Double
    public let systemPrompt: String?
    public let maxTokens: Int
    
    public init(
        apiKey: String,
        model: LLMModel,
        temperature: Double = 0.7,
        systemPrompt: String? = nil,
        maxTokens: Int? = nil
    ) {
        self.apiKey = apiKey
        self.model = model
        self.temperature = temperature
        self.systemPrompt = systemPrompt
        self.maxTokens = maxTokens ?? model.maxTokens
    }
}

// MARK: - LLM Message

/// A message in the conversation (role: "system" | "user" | "assistant").
public struct LLMMessage: Codable {
    public let role: String
    public let content: String
    
    public init(role: String, content: String) {
        self.role = role
        self.content = content
    }
    
    public static func system(_ content: String) -> LLMMessage {
        LLMMessage(role: "system", content: content)
    }
    
    public static func user(_ content: String) -> LLMMessage {
        LLMMessage(role: "user", content: content)
    }
    
    public static func assistant(_ content: String) -> LLMMessage {
        LLMMessage(role: "assistant", content: content)
    }
}

// MARK: - LLM Completion

/// Unified response regardless of provider.
public struct LLMCompletion {
    public let text: String
    public let model: String
    public let provider: LLMProvider
    public let promptTokens: Int
    public let completionTokens: Int
    public let latencyMs: TimeInterval
    
    public init(
        text: String,
        model: String,
        provider: LLMProvider,
        promptTokens: Int = 0,
        completionTokens: Int = 0,
        latencyMs: TimeInterval = 0
    ) {
        self.text = text
        self.model = model
        self.provider = provider
        self.promptTokens = promptTokens
        self.completionTokens = completionTokens
        self.latencyMs = latencyMs
    }
}

// MARK: - Predefined Models

public enum LLMModels {
    // OpenAI
    public static let gpt4o = LLMModel(provider: .openAI, modelId: "gpt-4o", displayName: "GPT-4o")
    public static let gpt4oMini = LLMModel(provider: .openAI, modelId: "gpt-4o-mini", displayName: "GPT-4o Mini")
    public static let gpt4Turbo = LLMModel(provider: .openAI, modelId: "gpt-4-turbo", displayName: "GPT-4 Turbo")
    
    // Anthropic
    public static let claudeSonnet = LLMModel(provider: .anthropic, modelId: "claude-sonnet-4-20250514", displayName: "Claude Sonnet 4")
    public static let claudeHaiku = LLMModel(provider: .anthropic, modelId: "claude-3-5-haiku-20241022", displayName: "Claude 3.5 Haiku")
    public static let claudeOpus = LLMModel(provider: .anthropic, modelId: "claude-opus-4-20250514", displayName: "Claude Opus 4")
    
    // Ollama
    public static let llama3 = LLMModel(provider: .ollama, modelId: "llama3", displayName: "Llama 3", baseUrl: "http://localhost:11434")
    public static let mistral = LLMModel(provider: .ollama, modelId: "mistral", displayName: "Mistral", baseUrl: "http://localhost:11434")
    public static let qwen2 = LLMModel(provider: .ollama, modelId: "qwen2", displayName: "Qwen 2", baseUrl: "http://localhost:11434")
    
    public static func defaults(for provider: LLMProvider) -> [LLMModel] {
        switch provider {
        case .openAI:
            return [gpt4o, gpt4oMini, gpt4Turbo]
        case .anthropic:
            return [claudeSonnet, claudeHaiku, claudeOpus]
        case .ollama:
            return [llama3, mistral, qwen2]
        case .openAICompatible:
            return [gpt4o]
        }
    }
}
