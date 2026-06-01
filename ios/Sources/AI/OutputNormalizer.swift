import Foundation

// MARK: - Output Normalizer

/// Cleans and normalizes LLM output, removing artifacts and extracting structured content.
public struct OutputNormalizer {

    public init() {}
    
    // MARK: - Regex Patterns (ported from Android PersonaEngine)
    
    /// Matches emoji characters (Unicode supplementary planes)
    private static let regexEmoji = try! NSRegularExpression(
        pattern: "[\\x{10000}-\\x{10FFFF}]",
        options: []
    )
    
    /// Matches parenthesized action descriptions: (上下打量), (摇晃签筒)
    private static let regexParenActions = try! NSRegularExpression(
        pattern: "[（(][^）)]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑嘿嘿哈哈哼嗯啊哎呀叹]{1,20}[）)]",
        options: []
    )
    
    /// Matches bracketed action descriptions: [点头示意]
    private static let regexBracketActions = try! NSRegularExpression(
        pattern: "\\[[^\\]]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑]{1,20}]",
        options: []
    )
    
    /// Matches asterisk-wrapped actions: *点点头*
    private static let regexAsteriskActions = try! NSRegularExpression(
        pattern: "\\*[^*]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑嘿嘿哈哈]{1,20}\\*",
        options: []
    )
    
    /// Matches speaker lines with action descriptions
    private static let regexSpeakerLine = try! NSRegularExpression(
        pattern: "^.*?(?:算命师|先生|老人|大师|师傅|仙人|先知|老者).{0,10}(?:笑着|点点头|沉思|端详|掐指|皱眉|叹气|摇头|微笑|打量|看了看|注视|凝视|闭目|捋须|睁开|抬头|低头|轻声|低声).{0,30}(?:后|才|道|说|答|开口).{0,5}(?:说|道|答)?[：:,]",
        options: [.anchorsMatchLines]
    )
    
    /// Matches standalone action sentences
    private static let regexStandaloneAction = try! NSRegularExpression(
        pattern: "^.*?(?:笑着|点点头|沉思片刻|端详|掐指一算|皱眉|叹气|摇头|微笑|打量|嘿嘿|哈哈).{0,20}[。.]\\s*$",
        options: [.anchorsMatchLines]
    )
    
    /// Matches 3+ consecutive blank lines
    private static let regexMultiBlankLines = try! NSRegularExpression(
        pattern: "\\n{3,}",
        options: []
    )
    
    /// Matches garbled encoding prefixes (non-CJK non-ASCII before CJK)
    private static let regexGarbledPrefix = try! NSRegularExpression(
        pattern: "[\\u0080-\\u00ff\\u0100-\\u024f\\u0250-\\u02af\\u2000-\\u206f\\u2070-\\u209f\\u20a0-\\u20cf\\u2100-\\u214f]{1,8}(?=[\\u4e00-\\u9fff\\u3400-\\u4dbf])",
        options: []
    )
    
    /// Matches markdown headers: # ## ### etc.
    private static let regexMarkdownHeaders = try! NSRegularExpression(
        pattern: "#{1,6}\\s*",
        options: []
    )
    
    /// Matches markdown bold: **
    private static let regexMarkdownBold = try! NSRegularExpression(
        pattern: "\\*\\*",
        options: []
    )
    
    /// Matches numbered list items: 1. 2. 3.
    private static let regexMarkdownListNum = try! NSRegularExpression(
        pattern: "^\\d+[.．、]\\s+",
        options: [.anchorsMatchLines]
    )
    
    /// Matches bullet list items: - or *
    private static let regexMarkdownListBullet = try! NSRegularExpression(
        pattern: "^[-*]\\s+",
        options: [.anchorsMatchLines]
    )
    
    /// Matches LLM self-generated section headers
    private static let regexLLMSectionHeaders = try! NSRegularExpression(
        pattern: "^\\s*(?:诗意签文|白话解释|直接建议|签诗解读|签文解读)：?\\s*$",
        options: [.anchorsMatchLines]
    )
    
    // MARK: - Special Token Patterns
    
    private static let specialTokenPatterns = [
        "<\\|?end_of_turn\\|?>",
        "<\\|?endoftext\\|?>",
        "<\\|?im_start\\|?>",
        "<\\|?im_end\\|?>",
        "<bos>",
        "<eos>"
    ].map { try! NSRegularExpression(pattern: $0, options: []) }
    
    // MARK: - Oracle Section Markers
    
    private static let oracleSectionMarkers = [
        "载入签文",
        "逻辑解析",
        "最终断语"
    ]
    
    // MARK: - Public API
    
    /// Clean LLM output by removing emoji, action descriptions, markdown, and special tokens.
    public static func cleanOutput(_ text: String) -> String {
        var result = text
        
        // Remove emoji
        result = regexEmoji.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Remove action descriptions
        result = regexParenActions.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexBracketActions.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexAsteriskActions.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexSpeakerLine.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexStandaloneAction.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Clean garbled encoding
        result = regexGarbledPrefix.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Clean markdown artifacts
        result = regexMarkdownHeaders.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexMarkdownBold.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexMarkdownListNum.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexMarkdownListBullet.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexLLMSectionHeaders.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Remove special tokens
        for pattern in specialTokenPatterns {
            result = pattern.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        }
        
        // Collapse multiple blank lines
        result = regexMultiBlankLines.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "\n\n")
        
        return result.trimmingCharacters(in: .whitespacesAndNewlines)
    }
    
    /// Clean output for offline models (with additional garbled encoding cleanup).
    public static func cleanOfflineOutput(_ text: String) -> String {
        var result = text
        
        // Clean garbled encoding
        result = regexGarbledPrefix.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Clean markdown artifacts
        result = regexMarkdownHeaders.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexMarkdownBold.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexMarkdownListNum.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexMarkdownListBullet.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        result = regexLLMSectionHeaders.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Remove loading/waiting messages
        let loadingPattern = try! NSRegularExpression(pattern: "\\[[^\\]]*请稍等[^\\]]*\\]", options: [])
        result = loadingPattern.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        let preparingPattern = try! NSRegularExpression(pattern: "\\[[^\\]]*正在[^\\]]*准备[^\\]]*\\]", options: [])
        result = preparingPattern.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        
        // Remove special tokens
        for pattern in specialTokenPatterns {
            result = pattern.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "")
        }
        
        // Collapse multiple blank lines
        result = regexMultiBlankLines.stringByReplacingMatches(in: result, range: NSRange(location: 0, length: result.utf16.count), withTemplate: "\n\n")
        
        return result.trimmingCharacters(in: .whitespacesAndNewlines)
    }
    
    // MARK: - Oracle Section Parsing
    
    /// Parse oracle output into three sections: verse, analysis, and verdict.
    public static func parseOracleSections(_ text: String) -> (verse: String, analysis: String, verdict: String) {
        let cleaned = cleanOutput(text)
        
        // Find section boundaries
        let verseMarker = "[ 载入签文 ]"
        let analysisMarker = "[ 逻辑解析 ]"
        let verdictMarker = "[ 最终断语 ]"
        
        var verse = ""
        var analysis = ""
        var verdict = ""
        
        // Extract verse section
        if let verseRange = cleaned.range(of: verseMarker),
           let analysisRange = cleaned.range(of: analysisMarker) {
            let start = cleaned.index(verseRange.upperBound, offsetBy: 0)
            let end = analysisRange.lowerBound
            verse = String(cleaned[start..<end]).trimmingCharacters(in: .whitespacesAndNewlines)
        }
        
        // Extract analysis section
        if let analysisRange = cleaned.range(of: analysisMarker),
           let verdictRange = cleaned.range(of: verdictMarker) {
            let start = cleaned.index(analysisRange.upperBound, offsetBy: 0)
            let end = verdictRange.lowerBound
            analysis = String(cleaned[start..<end]).trimmingCharacters(in: .whitespacesAndNewlines)
        }
        
        // Extract verdict section (everything after verdict marker)
        if let verdictRange = cleaned.range(of: verdictMarker) {
            let start = cleaned.index(verdictRange.upperBound, offsetBy: 0)
            verdict = String(cleaned[start...]).trimmingCharacters(in: .whitespacesAndNewlines)
        }
        
        // Fallback: if no markers found, try to parse by structure
        if verse.isEmpty && analysis.isEmpty && verdict.isEmpty {
            return parseOracleSectionsFallback(cleaned)
        }
        
        return (verse: verse, analysis: analysis, verdict: verdict)
    }
    
    /// Fallback parser for when section markers are missing.
    private static func parseOracleSectionsFallback(_ text: String) -> (verse: String, analysis: String, verdict: String) {
        let lines = text.components(separatedBy: .newlines).filter { !$0.isEmpty }
        
        // Try to identify 7-character verse lines (typical Chinese poetry)
        var verseLines: [String] = []
        var remainingLines: [String] = []
        
        for line in lines {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            // Check if it looks like a verse line (4-7 Chinese characters with punctuation)
            if trimmed.count >= 4 && trimmed.count <= 20 && 
               trimmed.range(of: "[，。、；：]", options: .regularExpression) != nil {
                verseLines.append(trimmed)
            } else {
                remainingLines.append(trimmed)
            }
        }
        
        let verse = verseLines.joined(separator: "\n")
        let remaining = remainingLines.joined(separator: "\n")
        
        // Split remaining into analysis and verdict (rough heuristic)
        let midpoint = remaining.count / 2
        let analysis = String(remaining.prefix(midpoint)).trimmingCharacters(in: .whitespacesAndNewlines)
        let verdict = String(remaining.suffix(remaining.count - midpoint)).trimmingCharacters(in: .whitespacesAndNewlines)
        
        return (verse: verse, analysis: analysis, verdict: verdict)
    }
}
