import SwiftUI
import DesignSystem

public struct SettingsScreen: View {

    public init() {}

    @State private var vm = SettingsViewModel()

    public var body: some View {
        VStack(spacing: 0) {
            SectionHeader(chineseTitle: "设置", englishSubtitle: "SETTINGS")
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.top, CyberSpacing.xs)

            DividerLine()

            ScrollView {
                VStack(alignment: .leading, spacing: CyberSpacing.md) {
                    // Provider
                    settingGroup("服务提供者") {
                        Picker("Provider", selection: $vm.provider) {
                            Text("OpenAI-Compatible").tag("openai_compatible")
                            Text("OpenAI").tag("openai")
                            Text("Anthropic").tag("anthropic")
                            Text("Ollama").tag("ollama")
                        }
                        .pickerStyle(.segmented)
                        .colorScheme(.dark)
                    }

                    // API Key
                    settingGroup("API Key") {
                        SecureField("sk-...", text: $vm.apiKey)
                            .font(CyberTypography.monoSmall)
                            .foregroundStyle(CyberColors.cyberWhite)
                            .textFieldStyle(.plain)
                            .padding(CyberSpacing.xs)
                            .background(CyberColors.graySurface)
                            .overlay(
                                Rectangle()
                                    .stroke(CyberColors.grayBorder, lineWidth: 1)
                            )
                    }

                    // Base URL
                    settingGroup("Base URL") {
                        TextField("https://api.openai.com/v1", text: $vm.baseURL)
                            .font(CyberTypography.monoSmall)
                            .foregroundStyle(CyberColors.cyberWhite)
                            .textFieldStyle(.plain)
                            .padding(CyberSpacing.xs)
                            .background(CyberColors.graySurface)
                            .overlay(
                                Rectangle()
                                    .stroke(CyberColors.grayBorder, lineWidth: 1)
                            )
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                    }

                    // Model ID
                    settingGroup("Model ID") {
                        TextField("gpt-4o", text: $vm.modelID)
                            .font(CyberTypography.monoSmall)
                            .foregroundStyle(CyberColors.cyberWhite)
                            .textFieldStyle(.plain)
                            .padding(CyberSpacing.xs)
                            .background(CyberColors.graySurface)
                            .overlay(
                                Rectangle()
                                    .stroke(CyberColors.grayBorder, lineWidth: 1)
                            )
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                    }

                    // Inference Mode
                    settingGroup("推理模式") {
                        Picker("Mode", selection: $vm.inferenceMode) {
                            Text("Auto").tag("auto")
                            Text("Online").tag("online")
                            Text("Offline").tag("offline")
                        }
                        .pickerStyle(.segmented)
                        .colorScheme(.dark)
                    }

                    // Privacy note
                    VStack(alignment: .leading, spacing: 4) {
                        Text("隐私声明")
                            .font(CyberTypography.monoSmall)
                            .foregroundStyle(CyberColors.grayCaption)
                        Text("API Key 仅存储于本机 Keychain，不会上传至任何第三方服务器。占卜结果仅保存在本地。")
                            .font(CyberTypography.bodySmall)
                            .foregroundStyle(CyberColors.grayMuted)
                    }

                    // Save
                    CyberButton("保存设置") {
                        vm.save()
                    }
                    .padding(.top, CyberSpacing.xs)
                }
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.vertical, CyberSpacing.sm)
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private func settingGroup(_ label: String, @ViewBuilder content: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(CyberTypography.monoSmall)
                .foregroundStyle(CyberColors.grayCaption)
            content()
        }
    }
}
