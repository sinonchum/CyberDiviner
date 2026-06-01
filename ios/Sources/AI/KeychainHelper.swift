import Foundation
import Security

// MARK: - Keychain Helper

/// Secure storage helper using iOS Keychain for API keys and sensitive data.
public struct KeychainHelper {

    public init() {}
    
    // MARK: - Errors
    
    public enum KeychainError: Error, LocalizedError {
        case saveFailed(OSStatus)
        case loadFailed(OSStatus)
        case deleteFailed(OSStatus)
        case invalidData
        
        public var errorDescription: String? {
            switch self {
            case .saveFailed(let status):
                return "Failed to save to keychain: \(status)"
            case .loadFailed(let status):
                return "Failed to load from keychain: \(status)"
            case .deleteFailed(let status):
                return "Failed to delete from keychain: \(status)"
            case .invalidData:
                return "Invalid data format"
            }
        }
    }
    
    // MARK: - Public API
    
    /// Save a string value to the keychain.
    public static func save(key: String, value: String) throws {
        guard let data = value.data(using: .utf8) else {
            throw KeychainError.invalidData
        }
        
        // First, try to delete any existing item
        try? delete(key: key)
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        ]
        
        let status = SecItemAdd(query as CFDictionary, nil)
        
        guard status == errSecSuccess else {
            throw KeychainError.saveFailed(status)
        }
    }
    
    /// Load a string value from the keychain.
    public static func load(key: String) throws -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        
        guard status != errSecItemNotFound else {
            return nil
        }
        
        guard status == errSecSuccess else {
            throw KeychainError.loadFailed(status)
        }
        
        guard let data = result as? Data else {
            throw KeychainError.invalidData
        }
        
        return String(data: data, encoding: .utf8)
    }
    
    /// Delete a value from the keychain.
    public static func delete(key: String) throws {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        
        let status = SecItemDelete(query as CFDictionary)
        
        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw KeychainError.deleteFailed(status)
        }
    }
    
    // MARK: - Convenience Methods
    
    /// Save an API key for a specific provider.
    public static func saveAPIKey(_ key: String, for provider: LLMProvider) throws {
        try save(key: "cyberdiviner_api_key_\(provider.rawValue)", value: key)
    }
    
    /// Load an API key for a specific provider.
    public static func loadAPIKey(for provider: LLMProvider) throws -> String? {
        return try load(key: "cyberdiviner_api_key_\(provider.rawValue)")
    }
    
    /// Delete an API key for a specific provider.
    public static func deleteAPIKey(for provider: LLMProvider) throws {
        try delete(key: "cyberdiviner_api_key_\(provider.rawValue)")
    }
    
    /// Check if an API key exists for a provider.
    public static func hasAPIKey(for provider: LLMProvider) -> Bool {
        return (try? loadAPIKey(for: provider)) != nil
    }
}

// MARK: - Keychain Configuration

/// Configuration for keychain access.
public struct KeychainConfiguration {

    public init() {}
    /// Service identifier for the keychain items.
    public static let service = "com.cyberdiviner.keychain"
    
    /// Access group for sharing keychain items across apps (if needed).
    public static let accessGroup: String? = nil
}
