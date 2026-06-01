import SwiftUI
import DesignSystem
import Oracle
import Liuyao
import Tarot
import Archive
import Settings

@main
struct CyberDivinerApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(.dark)
        }
    }
}

// MARK: - ContentView with TabView

struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            // Tab 1: 叩问天机 (Oracle)
            NavigationStack {
                OracleScreen()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("叩问天机", systemImage: "sparkles")
            }
            .tag(0)

            // Tab 2: 术数推演 (Rituals menu → Liuyao / Tarot)
            NavigationStack {
                RitualsMenuView()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("术数推演", systemImage: "die.face.5")
            }
            .tag(1)

            // Tab 3: 因果命簿 (Archive)
            NavigationStack {
                ArchiveScreen()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("因果命簿", systemImage: "book.closed")
            }
            .tag(2)

            // Tab 4: Settings
            NavigationStack {
                SettingsScreen()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("设置", systemImage: "gearshape")
            }
            .tag(3)
        }
        .tint(CyberColors.accentRed)
    }
}

// MARK: - Rituals Menu (Liuyao + Tarot entry)

struct RitualsMenuView: View {
    var body: some View {
        ZStack {
            CyberColors.cyberBlack
                .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                SectionHeader(chineseTitle: "术数推演", englishSubtitle: "RITUALS")

                Spacer().frame(height: 48)

                NavigationLink {
                    LiuyaoScreen()
                } label: {
                    RitualMenuItem(
                        title: "周易六爻",
                        subtitle: "I CHING · LIUYAO",
                        icon: "line.3.horizontal"
                    )
                }

                Spacer().frame(height: 48)

                NavigationLink {
                    TarotScreen()
                } label: {
                    RitualMenuItem(
                        title: "赛博塔罗",
                        subtitle: "CYBER TAROT",
                        icon: "rectangle.on.rectangle"
                    )
                }

                Spacer()
            }
            .padding(.horizontal, 48)
            .padding(.top, 32)
        }
        .navigationTitle("术数推演")
    }
}

struct RitualMenuItem: View {
    let title: String
    let subtitle: String
    let icon: String

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundStyle(CyberColors.grayCaption)
                .frame(width: 24)

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 20, weight: .medium))
                    .foregroundStyle(CyberColors.cyberWhite)

                Text(subtitle)
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundStyle(CyberColors.grayCaption)
            }

            Spacer()
        }
        .padding(.vertical, 8)
    }
}

// MARK: - Preview

#Preview {
    ContentView()
        .preferredColorScheme(.dark)
}
