import SwiftUI
import DesignSystem

public struct SettingsScreen: View {

    public init() {}

    @State private var vm = SettingsViewModel()
    @Environment(\.dismiss) private var dismiss

    public var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    // CONFIG header
                    Text("CONFIG")
                        .font(CyberTypography.monoMedium) // Mono 14sp
                        .foregroundStyle(CyberColors.grayCaption)
                        .tracking(4) // letterSpacing 4
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 20)
                        .padding(.bottom, 18)
                        .padding(.top, 16)

                    // API KEY
                    fieldLabel("API KEY")
                        .padding(.horizontal, 20)

                    apiKeyField
                        .padding(.horizontal, 20)

                    Spacer().frame(height: 14)

                    // BASE URL
                    fieldLabel("BASE URL")
                        .padding(.horizontal, 20)

                    baseField
                        .padding(.horizontal, 20)

                    Spacer().frame(height: 14)

                    // MODEL ID
                    fieldLabel("MODEL ID")
                        .padding(.horizontal, 20)

                    modelField
                        .padding(.horizontal, 20)

                    Spacer().frame(height: 14)

                    // INFERENCE MODE
                    fieldLabel("INFERENCE MODE")
                        .padding(.horizontal, 20)

                    modeDropdown
                        .padding(.horizontal, 20)

                    Spacer().frame(height: 32)

                    // Bottom buttons
                    HStack(spacing: 12) {
                        CyberButton("SAVE") {
                            vm.save()
                        }

                        CyberButton("BACK") {
                            dismiss()
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 32)
                }
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
    }

    // MARK: - Field Label

    private func fieldLabel(_ text: String) -> some View {
        Text(text)
            .font(CyberTypography.monoSmall) // Mono 12sp
            .foregroundStyle(CyberColors.grayBody)
            .tracking(2) // letterSpacing 2
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.bottom, 8)
    }

    // MARK: - API Key Field (with SHOW/HIDE toggle)

    private var apiKeyField: some View {
        ZStack(alignment: .trailing) {
            Group {
                if vm.showAPIKey {
                    TextField("sk-...", text: $vm.apiKey)
                } else {
                    SecureField("sk-...", text: $vm.apiKey)
                }
            }
            .font(CyberTypography.monoSmall)
            .foregroundStyle(CyberColors.cyberWhite)
            .textFieldStyle(.plain)
            .padding(CyberSpacing.xs)
            .background(CyberColors.graySurface)
            .overlay(
                Rectangle()
                    .stroke(CyberColors.grayBorder, lineWidth: 1)
            )

            Button(action: { vm.showAPIKey.toggle() }) {
                Text(vm.showAPIKey ? "HIDE" : "SHOW")
                    .font(CyberTypography.monoCaption) // Mono 10sp
                    .foregroundStyle(CyberColors.grayCaption)
                    .tracking(1)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
            }
        }
    }

    // MARK: - Base URL Field

    private var baseField: some View {
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

    // MARK: - Model ID Field

    private var modelField: some View {
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

    // MARK: - Mode Dropdown

    private var modeDropdown: some View {
        Menu {
            Button("AUTO") { vm.inferenceMode = "auto" }
            Button("ONLINE") { vm.inferenceMode = "online" }
            Button("OFFLINE") { vm.inferenceMode = "offline" }
        } label: {
            HStack {
                Text(vm.inferenceMode.uppercased())
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.cyberWhite)
                Spacer()
                Image(systemName: "chevron.down")
                    .font(.system(size: 10))
                    .foregroundStyle(CyberColors.grayCaption)
            }
            .padding(CyberSpacing.xs)
            .background(CyberColors.graySurface)
            .overlay(
                Rectangle()
                    .stroke(CyberColors.grayBorder, lineWidth: 1)
            )
        }
    }
}
