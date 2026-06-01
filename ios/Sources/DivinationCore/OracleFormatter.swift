import Foundation

// MARK: - Oracle Result

public struct OracleResult: Sendable {
    public let verse: String       // 签文 (the poem/verse)
    public let analysis: String    // 逻辑解析
    public let verdict: String     // 最终断语
    public let rawText: String

    public init(verse: String, analysis: String, verdict: String, rawText: String) {
        self.verse = verse
        self.analysis = analysis
        self.verdict = verdict
        self.rawText = rawText
    }
}

// MARK: - Oracle Formatter

/// Parses and normalizes LLM oracle (签) output.
public enum OracleFormatter {

    // MARK: - Section Markers

    private static let markerVerse   = "[ 载入签文 ]"
    private static let markerAnalysis = "[ 逻辑解析 ]"
    private static let markerVerdict  = "[ 最终断语 ]"

    // MARK: - Parse Oracle Output

    /// Parse raw LLM output into structured OracleResult.
    public static func parseOracleOutput(_ text: String) -> OracleResult {
        let cleaned = cleanText(text)
        let sections = splitIntoSections(cleaned)

        return OracleResult(
            verse: sections["verse"] ?? extractFirstQuatrain(cleaned),
            analysis: sections["analysis"] ?? "",
            verdict: sections["verdict"] ?? "",
            rawText: text
        )
    }

    // MARK: - Section Splitting

    /// Split text by [载入签文], [逻辑解析], [最终断语] markers.
    public static func splitIntoSections(_ text: String) -> [String: String] {
        var result: [String: String] = [:]

        // Try bracket markers first
        let patterns: [(key: String, markers: [String])] = [
            ("verse",    ["[ 载入签文 ]", "[载入签文]", "载入签文"]),
            ("analysis", ["[ 逻辑解析 ]", "[逻辑解析]", "逻辑解析"]),
            ("verdict",  ["[ 最终断语 ]", "[最终断语]", "最终断语"]),
        ]

        var found = false
        for (key, markers) in patterns {
            for marker in markers {
                if let range = text.range(of: marker) {
                    found = true
                    let afterMarker = text[range.upperBound...]
                    let content = afterMarker.trimmingCharacters(in: .whitespacesAndNewlines)

                    // Find the end (next marker or end of text)
                    var endIdx = content.endIndex
                    for (_, nextMarkers) in patterns {
                        for nextMarker in nextMarkers {
                            if nextMarker != marker,
                               let nextRange = content.range(of: nextMarker) {
                                if nextRange.lowerBound < endIdx {
                                    endIdx = nextRange.lowerBound
                                }
                            }
                        }
                    }

                    result[key] = String(content[content.startIndex..<endIdx])
                        .trimmingCharacters(in: .whitespacesAndNewlines)
                    break
                }
            }
        }

        if !found {
            // No markers — try to extract a four-line poem as verse
            result["verse"] = extractFirstQuatrain(text)
        }

        return result
    }

    // MARK: - Text Cleaning

    /// Full cleaning pipeline: emoji, action descriptions, markdown, special tokens.
    public static func cleanText(_ text: String) -> String {
        var result = text

        // Remove emoji
        result = result.replacingOccurrences(
            of: "[\\x{1F600}-\\x{1F64F}\\x{1F300}-\\x{1F5FF}\\x{1F680}-\\x{1F6FF}\\x{1F1E0}-\\x{1F1FF}\\x{2600}-\\x{27BF}\\x{FE00}-\\x{FE0F}\\x{1F900}-\\x{1F9FF}\\x{1FA00}-\\x{1FA6F}\\x{1FA70}-\\x{1FAFF}]",
            with: "", options: .regularExpression
        )

        // Remove parenthesized action descriptions: (上下打量), （摇晃签筒）
        result = result.replacingOccurrences(
            of: "[（(][^）)]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑嘿嘿哈哈哼嗯啊哎呀叹]{1,20}[）)]",
            with: "", options: .regularExpression
        )

        // Remove bracketed action descriptions: [灯光闪烁]
        result = result.replacingOccurrences(
            of: "\\[[^\\]]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑]{1,20}\\]",
            with: "", options: .regularExpression
        )

        // Remove asterisk actions: *点点头*
        result = result.replacingOccurrences(
            of: "\\*[^*]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑嘿嘿哈哈]{1,20}\\*",
            with: "", options: .regularExpression
        )

        // Remove speaker lines (e.g. "算命师笑着打量你后说：")
        result = result.replacingOccurrences(
            of: "(?m)^.*?(?:算命师|先生|老人|大师|师傅|仙人|先知|老者).{0,10}(?:笑着|点点头|沉思|端详|掐指|皱眉|叹气|摇头|微笑|打量|看了看|注视|凝视|闭目|捋须|睁开|抬头|低头|轻声|低声).{0,30}(?:后|才|道|说|答|开口).{0,5}(?:说|道|答)?[：:,]",
            with: "", options: .regularExpression
        )

        // Remove standalone action lines
        result = result.replacingOccurrences(
            of: "(?m)^.*?(?:笑着|点点头|沉思片刻|端详|掐指一算|皱眉|叹气|摇头|微笑|打量|嘿嘿|哈哈).{0,20}[。.]\\s*$",
            with: "", options: .regularExpression
        )

        // Clean garbled encoding (non-ASCII non-CJK before CJK)
        result = result.replacingOccurrences(
            of: "[\\u{0080}-\\u{00FF}\\u{0100}-\\u{024F}\\u{0250}-\\u{02AF}]{1,8}(?=[\\u{4E00}-\\u{9FFF}\\u{3400}-\\u{4DBF}])",
            with: "", options: .regularExpression
        )

        // Remove markdown headers
        result = result.replacingOccurrences(of: "#{1,6}\\s*", with: "", options: .regularExpression)

        // Remove markdown bold
        result = result.replacingOccurrences(of: "\\*\\*", with: "", options: .regularExpression)

        // Remove numbered list markers
        result = result.replacingOccurrences(of: "(?m)^\\d+[.．、]\\s+", with: "", options: .regularExpression)

        // Remove bullet list markers
        result = result.replacingOccurrences(of: "(?m)^[-*]\\s+", with: "", options: .regularExpression)

        // Remove LLM-generated section headers
        result = result.replacingOccurrences(
            of: "(?m)^\\s*(?:诗意签文|白话解释|直接建议|签诗解读|签文解读)：?\\s*$",
            with: "", options: .regularExpression
        )

        // Remove special tokens from various model families
        let specialTokens = ["<|end_of_turn|>", "<|endoftext|>", "<|im_start|>", "<|im_end|>",
                             "<bos>", "<eos>", "end_of_turn", "endoftext"]
        for token in specialTokens {
            result = result.replacingOccurrences(of: token, with: "")
        }

        // Collapse multiple blank lines
        result = result.replacingOccurrences(of: "\\n{3,}", with: "\n\n", options: .regularExpression)

        return result.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    // MARK: - Poem Extraction

    /// Try to extract a four-line seven-character poem from unstructured text.
    private static func extractFirstQuatrain(_ text: String) -> String {
        let lines = text.components(separatedBy: .newlines)
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        // Look for lines that are roughly 7 characters and end with punctuation
        var poemLines: [String] = []
        for line in lines {
            let stripped = line.replacingOccurrences(of: "[，。！？、；：]", with: "", options: .regularExpression)
            if stripped.count >= 5 && stripped.count <= 10 {
                poemLines.append(line)
                if poemLines.count == 4 { break }
            } else if !poemLines.isEmpty {
                // Non-matching line after poem started — stop
                break
            }
        }

        return poemLines.joined(separator: "\n")
    }

    // MARK: - Clean Offline Model Output

    /// Clean output from offline/local models (garbled encoding + markdown).
    public static func cleanOfflineOutput(_ text: String) -> String {
        var result = text

        // Garbled encoding
        result = result.replacingOccurrences(
            of: "[\\u{0080}-\\u{00FF}\\u{0100}-\\u{024F}]{1,8}(?=[\\u{4E00}-\\u{9FFF}])",
            with: "", options: .regularExpression
        )

        // Remove loading messages
        result = result.replacingOccurrences(of: "\\[[^\\]]*请稍等[^\\]]*\\]", with: "", options: .regularExpression)
        result = result.replacingOccurrences(of: "\\[[^\\]]*正在[^\\]]*准备[^\\]]*\\]", with: "", options: .regularExpression)

        // Markdown
        result = result.replacingOccurrences(of: "#{1,6}\\s*", with: "", options: .regularExpression)
        result = result.replacingOccurrences(of: "\\*\\*", with: "", options: .regularExpression)
        result = result.replacingOccurrences(of: "(?m)^\\d+[.．、]\\s+", with: "", options: .regularExpression)
        result = result.replacingOccurrences(of: "(?m)^[-*]\\s+", with: "", options: .regularExpression)

        // Special tokens
        let tokens = ["<|end_of_turn|>", "<|endoftext|>", "<|im_start|>", "<|im_end|>", "<bos>", "<eos>"]
        for t in tokens { result = result.replacingOccurrences(of: t, with: "") }

        result = result.replacingOccurrences(of: "\\n{3,}", with: "\n\n", options: .regularExpression)
        return result.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    // MARK: - Strip Action Descriptions (from PersonaEngine)

    /// Strip character action/expression descriptions from LLM output.
    public static func stripActionDescriptions(_ text: String) -> String {
        cleanText(text)
    }
}
