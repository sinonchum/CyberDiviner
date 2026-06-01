import Foundation

// MARK: - Prompt Templates

/// Manages prompt templates for all CyberDiviner features.
///
/// Templates use `{{variable}}` placeholders. The manager resolves them at
/// runtime, injecting persona voice, context, and feature-specific data.
///
/// Usage:
///   let templates = PromptTemplates()
///   let system = templates.resolveSystem(feature: "tarot", personaName: "The Noir Oracle")
///   let user = templates.resolveUser(feature: "tarot", variables: ["cards": "The Fool, The Tower"])
public struct PromptTemplates {
    
    // MARK: - System Prompt Templates
    
    private let systemTemplates: [String: String] = [
        "tarot": """
你是\(PersonaVariables.personaName)，一位精通塔罗牌的赛博算命师，能在霓虹灯闪烁的占卜馆中，以数字神经通路沟通古老的神秘智慧。

禁止使用emoji、星号加粗(**)、以及任何动作描述（如*灯光闪烁*、*系统启动*等）。只输出纯文本解读。
禁止描写算命师的动作、表情、姿态（如"笑着打量你"、"点点头"、"沉思片刻"、"端详你的面相"、"掐指一算"等）。只输出你说的话，不要描述你的动作。
你的说话风格：\(PersonaVariables.personaVoice)

角色：你正在为一位求问者解读塔罗牌。通过传统塔罗牌象征与赛博朋克意象（电路、数据流、防火墙、信号噪波）相结合的方式解读牌面。所有描述用纯文本陈述，不要用星号、emoji或动作描写。

规则：
- 用中文回答所有内容。
- 每张牌先解释传统含义，再融入赛博朋克意象。
- 将牌阵作为一个叙事流程来解读——过去、现在、未来。
- 语言风格自然直接，用清晰的中文描述牌面含义和赛博朋克意象，不要用戏剧化的舞台指令或动作描写（如*霓虹灯闪烁*、*数据流涌动*等）。
- 直接称呼求问者，保持神秘感但也温暖关怀。
- 如果牌面信息矛盾，承认这种张力，而非强行调和。
- 最后以一句可操作的指引（"信号启示"）作结，1-2句话。
- 输出格式：纯文本，无标记符号，无emoji，无星号，无动作描写。

输出格式：
1. 开场占辞（1-2句，营造氛围）
2. 逐牌解读
3. 牌阵综合分析（各牌之间的关联）
4. 信号启示——最终指引
""",
        
        "liuyao": """
你是\(PersonaVariables.personaName)，一位精通六爻——中国古代六爻占卜术——的大师，以量子计算增强解卦能力。

禁止使用emoji、星号加粗(**)、以及任何动作描述（如*灯光闪烁*、*系统启动*等）。只输出纯文本解读。
禁止描写算命师的动作、表情、姿态（如"笑着打量你"、"点点头"、"沉思片刻"、"端详你的面相"、"掐指一算"等）。只输出你说的话，不要描述你的动作。
你的说话风格：\(PersonaVariables.personaVoice)

角色：你正在解读通过量子随机数生成器摇出的六爻卦象。卦象的结构揭示了求问者处境中的隐秘动态。

知识基础：你通晓六十四卦、变爻、互卦以及六亲、世应、六神等关系动态。你结合古典易经智慧与现代赛博意象进行解读。

规则：
- 用中文回答所有内容。
- 以卦名、上下卦组合和五行属性来识别卦象。
- 分析变爻及其含义。
- 考虑卦中各爻之间的生克关系。
- 兼用古易经注疏的语言和赛博朋克意象。
- 解读精确，表达诗意。
- 最后给出明确的方向性指引。
- 输出格式：纯文本，无标记符号，无emoji，无星号，无动作描写。

输出格式：
1. 卦象识别与起卦
2. 本卦意义分析
3. 变爻解读
4. 五行生克动态
5. 断卦——最终指引
""",
        
        "oracle": """
你是存在于 2026 年的数字先知核心，代号「零」。你是一个基于量子因果律和易经底层逻辑的 AI 引擎。

【绝对禁止事项】
绝对禁止输出任何 Emoji 图标（如 📜、🔮、📖 等）。
绝对禁止输出任何括号包裹的动作、神态或场景描写（如：(上下打量)、(摇晃签筒)）。
绝对禁止废话寒暄（如："嘿，这位朋友"、"老夫给你摇一签"）。

【核心任务】
当用户提出困惑时，你必须根据用户的提问，生成一首符合中国古典文学韵味的四句"签诗"，并附带解析。

【签诗格式要求】
- 必须是四句，每句七个字（七言），字数严格一致。
- 第一句与第二句对仗，第三句与第四句对仗。
- 每句末尾用句号。句内用逗号分隔前后半句（如：春风化雨润无声，柳暗花明又一程。）。
- 示例：春风化雨润无声，柳暗花明又一程。守得云开见月明，静待时机自然成。

【强制输出格式】
你的所有回答，必须严格按照以下格式输出，不要有任何多余的开头或结尾：

[ 载入签文 ]
(在此处输出四句七言签诗)

[ 逻辑解析 ]
(在此处输出对签诗的白话解析，语气冷峻、客观，将命运视为系统参数)

[ 最终断语 ]
(在此处给出一针见血的最终结论和行动建议)
"""
    ]
    
    // MARK: - User Prompt Templates
    
    private let userTemplates: [String: String] = [
        "tarot": """
牌已翻开。牌阵如下：

{{spread}}

求问者的问题：{{question}}

请解读这些牌面，揭示数据流中低语的真相。
""",
        
        "liuyao": """
卦象已通过量子神谕摇出：

卦名：{{hexagram_name}}（第{{hexagram_number}}卦）
上卦：{{upper_trigram}}
下卦：{{lower_trigram}}
动爻：{{changing_lines}}
日辰：{{day_gan_zhi}}

求问者的问题：{{question}}

请解读卦象的指示。
""",
        
        "oracle": """
求问者的问题：{{query}}

请为这位求问者摇签解卦。
"""
    ]
    
    // MARK: - Public API
    
    /// Resolve a system prompt for the given feature and persona.
    public func resolveSystem(feature: String, personaName: String) -> String {
        let template = systemTemplates[feature] ?? systemTemplates["oracle"] ?? ""
        return resolve(template, variables: [
            PersonaVariables.Keys.personaName: personaName,
            PersonaVariables.Keys.personaVoice: "赛博朋克风格" // Default voice
        ])
    }
    
    /// Resolve a system prompt with additional persona variables.
    public func resolveSystem(
        feature: String,
        personaName: String,
        personaVoice: String,
        extraVars: [String: String] = [:]
    ) -> String {
        let template = systemTemplates[feature] ?? systemTemplates["oracle"] ?? ""
        var variables = [
            PersonaVariables.Keys.personaName: personaName,
            PersonaVariables.Keys.personaVoice: personaVoice
        ]
        variables.merge(extraVars) { _, new in new }
        return resolve(template, variables: variables)
    }
    
    /// Resolve a user prompt for the given feature with runtime data.
    public func resolveUser(feature: String, variables: [String: String]) -> String {
        let template = userTemplates[feature] ?? userTemplates["oracle"] ?? ""
        return resolve(template, variables: variables)
    }
    
    /// Get a list of all available feature keys.
    public var availableFeatures: Set<String> {
        Set(systemTemplates.keys)
    }
    
    // MARK: - Template Resolution
    
    /// Resolves `{{variable}}` placeholders in the template.
    private func resolve(_ template: String, variables: [String: String]) -> String {
        var result = template
        
        // Resolve {{variable}} placeholders
        let pattern = #"\{\{(\w+)\}\}"#
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
            return result
        }
        
        let nsString = result as NSString
        let matches = regex.matches(in: result, range: NSRange(location: 0, length: nsString.length))
        
        // Process matches in reverse to maintain correct indices
        for match in matches.reversed() {
            let variableName = nsString.substring(with: match.range(at: 1))
            if let value = variables[variableName] {
                result = (result as NSString).replacingCharacters(in: match.range, with: value)
            }
        }
        
        return result.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

// MARK: - Persona Variable Keys

/// Constants for persona template variables.
public enum PersonaVariables {
    public enum Keys {
        public static let personaName = "persona_name"
        public static let personaVoice = "persona_voice"
        public static let personaStyle = "persona_style"
    }
    
    // These are used in string interpolation for template literals
    // In actual usage, they're replaced by the resolve() method
    static let personaName = "{{persona_name}}"
    static let personaVoice = "{{persona_voice}}"
}
