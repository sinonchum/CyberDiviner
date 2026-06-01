import SwiftUI
import DesignSystem
import Oracle
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

// MARK: - App Phase

enum AppPhase {
    case splash
    case epiphany
    case main
}

// MARK: - ContentView — Splash → Epiphany → TabView

struct ContentView: View {
    @State private var phase: AppPhase = .splash
    @State private var selectedTab = 0
    @State private var showConfig = false

    var body: some View {
        ZStack {
            CyberColors.cyberBlack
                .ignoresSafeArea()

            switch phase {
            case .splash:
                SplashScreen {
                    withAnimation(.easeOut(duration: 0.3)) {
                        phase = .epiphany
                    }
                }
                .transition(.opacity)

            case .epiphany:
                EpiphanyScreen {
                    withAnimation(.easeOut(duration: 0.3)) {
                        phase = .main
                    }
                }
                .transition(.opacity)

            case .main:
                mainContent
                    .transition(.opacity)
            }
        }
        .animation(.easeOut(duration: 0.3), value: phase)
    }

    // MARK: - Main Tab Content

    private var mainContent: some View {
        ZStack(alignment: .topTrailing) {
            TabView(selection: $selectedTab) {
                // Tab 1: 叩问天机 (Oracle)
                NavigationStack {
                    OracleScreen()
                        .navigationBarTitleDisplayMode(.inline)
                }
                .tabItem {
                    OracleTabLabel(isSelected: selectedTab == 0)
                }
                .tag(0)

                // Tab 2: 术数推演 (Rituals)
                NavigationStack {
                    RitualsMenuView()
                        .navigationBarTitleDisplayMode(.inline)
                }
                .tabItem {
                    TrigramTabLabel(isSelected: selectedTab == 1)
                }
                .tag(1)

                // Tab 3: 修习之路 (Learn placeholder)
                NavigationStack {
                    LearnPlaceholderView()
                        .navigationBarTitleDisplayMode(.inline)
                }
                .tabItem {
                    LearnTabLabel(isSelected: selectedTab == 2)
                }
                .tag(2)

                // Tab 4: 因果命簿 (Archive)
                NavigationStack {
                    ArchiveScreen()
                        .navigationBarTitleDisplayMode(.inline)
                }
                .tabItem {
                    ScrollTabLabel(isSelected: selectedTab == 3)
                }
                .tag(3)
            }
            .tint(CyberColors.cyberWhite)
            .onAppear {
                // Style the tab bar appearance
                let appearance = UITabBarAppearance()
                appearance.configureWithOpaqueBackground()
                appearance.backgroundColor = UIColor(CyberColors.cyberBlack)
                appearance.shadowColor = .clear

                // Normal (unselected)
                appearance.stackedLayoutAppearance.normal.iconColor = UIColor(CyberColors.grayCaption)
                appearance.stackedLayoutAppearance.normal.titleTextAttributes = [
                    .foregroundColor: UIColor(CyberColors.grayCaption),
                    .font: UIFont(name: "Huiwen-mincho", size: 11) ?? UIFont.systemFont(ofSize: 11)
                ]

                // Selected
                appearance.stackedLayoutAppearance.selected.iconColor = .white
                appearance.stackedLayoutAppearance.selected.titleTextAttributes = [
                    .foregroundColor: UIColor.white,
                    .font: UIFont(name: "Huiwen-mincho", size: 11) ?? UIFont.systemFont(ofSize: 11)
                ]

                UITabBar.appearance().standardAppearance = appearance
                UITabBar.appearance().scrollEdgeAppearance = appearance
            }

            // Config gear icon overlay — visible only on main tab screens
            Button {
                showConfig = true
            } label: {
                GearSettingsIcon(size: 20, color: CyberColors.grayCaption)
                    .padding(.top, 8)
                    .padding(.trailing, 8)
            }
            .padding(.top, 8)
            .padding(.trailing, 12)
            .sheet(isPresented: $showConfig) {
                NavigationStack {
                    SettingsScreen()
                        .navigationBarTitleDisplayMode(.inline)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarLeading) {
                                Button("关闭") {
                                    showConfig = false
                                }
                                .foregroundStyle(CyberColors.grayCaption)
                            }
                        }
                }
                .presentationDetents([.medium, .large])
            }
        }
    }
}

// MARK: - Tab Labels with Canvas Icons

/// Oracle tab — almond eye icon + "叩问天机"
struct OracleTabLabel: View {
    let isSelected: Bool
    private var color: Color { isSelected ? CyberColors.cyberWhite : CyberColors.grayCaption }

    var body: some View {
        VStack(spacing: 4) {
            Canvas { context, size in
                let sw: CGFloat = 1.5
                let cx = size.width / 2
                let cy = size.height / 2
                let rx = size.width * 0.42
                let ry = size.height * 0.22

                // Eye outline (almond shape using two arcs)
                let eyePath = Path { p in
                    p.move(to: CGPoint(x: cx - rx, y: cy))
                    p.addQuadCurve(to: CGPoint(x: cx + rx, y: cy),
                                   control: CGPoint(x: cx, y: cy - ry * 2.5))
                    p.addQuadCurve(to: CGPoint(x: cx - rx, y: cy),
                                   control: CGPoint(x: cx, y: cy + ry * 2.5))
                    p.closeSubpath()
                }
                context.stroke(eyePath, with: .color(color),
                               style: StrokeStyle(lineWidth: sw, lineCap: .square))

                // Iris circle
                context.stroke(
                    Path { p in
                        p.addEllipse(in: CGRect(
                            x: cx - ry * 0.7, y: cy - ry * 0.7,
                            width: ry * 1.4, height: ry * 1.4
                        ))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw, lineCap: .square)
                )
            }
            .frame(width: 24, height: 24)

            Text("叩问天机")
                .font(.custom("Huiwen-mincho", size: 11))
        }
    }
}

/// Trigram tab — 3 horizontal lines (middle broken) + "术数推演"
struct TrigramTabLabel: View {
    let isSelected: Bool
    private var color: Color { isSelected ? CyberColors.cyberWhite : CyberColors.grayCaption }

    var body: some View {
        VStack(spacing: 4) {
            Canvas { context, size in
                let sw: CGFloat = 1.5
                let lineLen = size.width * 0.7
                let cx = size.width / 2
                let left = cx - lineLen / 2
                let right = cx + lineLen / 2
                let midGap: CGFloat = 4

                for i in 0...2 {
                    let y = size.height * 0.25 + CGFloat(i) * (size.height * 0.25)
                    if i == 1 {
                        // Yin line (broken)
                        context.stroke(
                            Path { p in
                                p.move(to: CGPoint(x: left, y: y))
                                p.addLine(to: CGPoint(x: cx - midGap, y: y))
                            },
                            with: .color(color),
                            style: StrokeStyle(lineWidth: sw, lineCap: .square)
                        )
                        context.stroke(
                            Path { p in
                                p.move(to: CGPoint(x: cx + midGap, y: y))
                                p.addLine(to: CGPoint(x: right, y: y))
                            },
                            with: .color(color),
                            style: StrokeStyle(lineWidth: sw, lineCap: .square)
                        )
                    } else {
                        // Yang line (solid)
                        context.stroke(
                            Path { p in
                                p.move(to: CGPoint(x: left, y: y))
                                p.addLine(to: CGPoint(x: right, y: y))
                            },
                            with: .color(color),
                            style: StrokeStyle(lineWidth: sw, lineCap: .square)
                        )
                    }
                }
            }
            .frame(width: 24, height: 24)

            Text("术数推演")
                .font(.custom("Huiwen-mincho", size: 11))
        }
    }
}

/// Learn tab — open book icon + "修习之路"
struct LearnTabLabel: View {
    let isSelected: Bool
    private var color: Color { isSelected ? CyberColors.cyberWhite : CyberColors.grayCaption }

    var body: some View {
        VStack(spacing: 4) {
            Canvas { context, size in
                let sw: CGFloat = 1.5
                let cx = size.width / 2
                let cy = size.height / 2
                let pageW = size.width * 0.35
                let pageH = size.height * 0.35
                let top = cy - pageH
                let bottom = cy + pageH * 0.4

                // Left page
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx, y: top))
                        p.addLine(to: CGPoint(x: cx, y: bottom))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw, lineCap: .square)
                )
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx, y: top))
                        p.addLine(to: CGPoint(x: cx - pageW, y: top + pageH * 0.2))
                        p.addLine(to: CGPoint(x: cx - pageW, y: bottom))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw, lineCap: .square)
                )
                // Right page
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx, y: top))
                        p.addLine(to: CGPoint(x: cx + pageW, y: top + pageH * 0.2))
                        p.addLine(to: CGPoint(x: cx + pageW, y: bottom))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw, lineCap: .square)
                )
                // Bottom spine
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx - pageW, y: bottom))
                        p.addLine(to: CGPoint(x: cx + pageW, y: bottom))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw * 0.7, lineCap: .square)
                )
            }
            .frame(width: 24, height: 24)

            Text("修习之路")
                .font(.custom("Huiwen-mincho", size: 11))
        }
    }
}

/// Scroll tab — document icon + "因果命簿"
struct ScrollTabLabel: View {
    let isSelected: Bool
    private var color: Color { isSelected ? CyberColors.cyberWhite : CyberColors.grayCaption }

    var body: some View {
        VStack(spacing: 4) {
            Canvas { context, size in
                let sw: CGFloat = 1.5
                let pad: CGFloat = 3
                let left = pad
                let top = pad
                let right = size.width - pad
                let bottom = size.height - pad

                // Document outline
                let outline = Path { p in
                    p.move(to: CGPoint(x: left, y: top))
                    p.addLine(to: CGPoint(x: right, y: top))
                    p.addLine(to: CGPoint(x: right, y: bottom))
                    p.addLine(to: CGPoint(x: left, y: bottom))
                    p.closeSubpath()
                }
                context.stroke(outline, with: .color(color),
                               style: StrokeStyle(lineWidth: sw, lineCap: .square))

                // Inner lines (text representation)
                let lineY1 = top + size.height * 0.28
                let lineY2 = top + size.height * 0.5
                let lineY3 = top + size.height * 0.72
                let innerLeft = left + 5
                let innerRight = right - 5

                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: innerLeft, y: lineY1))
                        p.addLine(to: CGPoint(x: innerRight, y: lineY1))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw * 0.7, lineCap: .square)
                )
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: innerLeft, y: lineY2))
                        p.addLine(to: CGPoint(x: innerRight * 0.8, y: lineY2))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw * 0.7, lineCap: .square)
                )
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: innerLeft, y: lineY3))
                        p.addLine(to: CGPoint(x: innerRight * 0.6, y: lineY3))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw * 0.7, lineCap: .square)
                )
            }
            .frame(width: 24, height: 24)

            Text("因果命簿")
                .font(.custom("Huiwen-mincho", size: 11))
        }
    }
}

// MARK: - Learn Placeholder

struct LearnPlaceholderView: View {
    var body: some View {
        ZStack {
            CyberColors.cyberBlack
                .ignoresSafeArea()

            VStack(spacing: 16) {
                Spacer()

                Text("修习之路")
                    .font(.custom("Huiwen-mincho", size: 32))
                    .foregroundStyle(CyberColors.grayMuted)

                Text("LEARNING PATH")
                    .font(CyberTypography.monoSmall)
                    .foregroundStyle(CyberColors.grayCaption)
                    .kerning(4)

                Text("即将开放")
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.grayMuted)
                    .padding(.top, 8)

                Spacer()
            }
        }
        .navigationTitle("修习之路")
    }
}

// MARK: - Preview

#Preview {
    ContentView()
        .preferredColorScheme(.dark)
}
