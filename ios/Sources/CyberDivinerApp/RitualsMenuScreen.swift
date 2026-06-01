import SwiftUI
import DesignSystem
import Liuyao
import Tarot

struct RitualsMenuView: View {
    @State private var showPlaceholderAlert = false
    @State private var placeholderTitle = ""

    var body: some View {
        ZStack {
            CyberColors.cyberBlack
                .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                SectionHeader(chineseTitle: "术数推演", englishSubtitle: "RITUAL EXECUTION")

                Spacer().frame(height: 48)

                StaggeredItem(index: 0) {
                    NavigationLink {
                        LiuyaoScreen()
                    } label: {
                        CyberMenuItem(
                            title: "周易六爻",
                            subtitle: "I-CHING",
                            description: "摇钱起卦，六爻断事",
                            action: {}
                        )
                    }
                }

                Spacer().frame(height: 24)

                StaggeredItem(index: 1) {
                    NavigationLink {
                        TarotScreen()
                    } label: {
                        CyberMenuItem(
                            title: "赛博塔罗",
                            subtitle: "CYBER TAROT",
                            description: "七十八牌，阵法推演",
                            action: {}
                        )
                    }
                }

                Spacer().frame(height: 24)

                StaggeredItem(index: 2) {
                    CyberMenuItem(
                        title: "视界摸骨",
                        subtitle: "FACE SCAN",
                        description: "镜阵观相，五官推演",
                        action: {
                            placeholderTitle = "视界摸骨 · FACE SCAN"
                            showPlaceholderAlert = true
                        }
                    )
                }

                Spacer().frame(height: 24)

                StaggeredItem(index: 3) {
                    CyberMenuItem(
                        title: "电子颂钵",
                        subtitle: "SINGING BOWL",
                        description: "一击清音，静心调息",
                        action: {
                            placeholderTitle = "电子颂钵 · SINGING BOWL"
                            showPlaceholderAlert = true
                        }
                    )
                }

                Spacer().frame(height: 24)

                StaggeredItem(index: 4) {
                    CyberMenuItem(
                        title: "赛博黄历",
                        subtitle: "ALMANAC",
                        description: "干支黄历，每日宜忌",
                        action: {
                            placeholderTitle = "赛博黄历 · ALMANAC"
                            showPlaceholderAlert = true
                        }
                    )
                }

                Spacer()
            }
            .padding(.horizontal, 32)
        }
        .navigationTitle("术数推演")
        .alert(placeholderTitle, isPresented: $showPlaceholderAlert) {
            Button("确定", role: .cancel) {}
        } message: {
            Text("即将开放")
        }
    }
}
