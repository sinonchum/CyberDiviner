import SwiftUI
import DesignSystem
import Persistence

public struct ArchiveScreen: View {

    public init() {}

    @State private var vm = ArchiveViewModel()
    @State private var expandedId: UUID?

    public var body: some View {
        VStack(spacing: 0) {
            // Header
            SectionHeader(chineseTitle: "因果命簿", englishSubtitle: "CAUSAL LEDGER")
                .padding(.horizontal, CyberSpacing.lg)
                .padding(.top, CyberSpacing.lg)

            Spacer().frame(height: CyberSpacing.sm)

            DividerLine()

            if vm.filteredReadings.isEmpty {
                emptyState
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(vm.filteredReadings) { reading in
                            ArchiveCard(
                                reading: reading,
                                isExpanded: expandedId == reading.id,
                                onDelete: { vm.delete(reading) },
                                onToggle: {
                                    withAnimation(.easeInOut(duration: 0.2)) {
                                        expandedId = expandedId == reading.id ? nil : reading.id
                                    }
                                }
                            )
                            DividerLine()
                        }
                    }
                }
            }
        }
        .background(CyberColors.cyberBlack)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var emptyState: some View {
        VStack(spacing: CyberSpacing.sm) {
            Spacer()
            Text("命簿为空")
                .font(CyberTypography.titleMedium)
                .foregroundStyle(CyberColors.grayMuted)
            Text("去叩问天机，因果自会记载于此")
                .font(CyberTypography.bodySmall)
                .foregroundStyle(CyberColors.grayCaption)
            Spacer()
        }
    }
}

// MARK: - Archive Card

private struct ArchiveCard: View {
    let reading: SavedReading
    let isExpanded: Bool
    let onDelete: () -> Void
    let onToggle: () -> Void

    @State private var showDeleteConfirm = false
    @State private var dragOffset: CGFloat = 0

    var body: some View {
        ZStack(alignment: .trailing) {
            // Delete reveal (red background)
            if dragOffset < -20 {
                HStack {
                    Spacer()
                    Button(action: onDelete) {
                        Text("删除")
                            .font(CyberTypography.monoSmall)
                            .foregroundStyle(CyberColors.cyberWhite)
                            .frame(width: 80)
                            .frame(maxHeight: .infinity)
                            .background(CyberColors.accentRed)
                    }
                }
            }

            // Card content
            VStack(alignment: .leading, spacing: 8) {
                // Top row: Ganzhi date + solar date | type badge
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        // Ganzhi date (simplified: use formatted date)
                        Text(ganzhiDate)
                            .font(CyberTypography.bodySmall) // Huiwen 14sp Bold
                            .fontWeight(.bold)
                            .foregroundStyle(CyberColors.grayTitle)

                        // Solar date
                        Text(solarDate)
                            .font(CyberTypography.monoCaption) // Mono 10sp
                            .foregroundStyle(CyberColors.grayMuted)
                    }

                    Spacer()

                    // Type badge
                    Text(typeBadge)
                        .font(CyberTypography.monoCaption) // Mono 11sp
                        .foregroundStyle(CyberColors.accentRed)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .overlay(
                            Rectangle()
                                .stroke(CyberColors.accentRed, lineWidth: 1)
                        )
                }

                // Title
                Text(reading.title)
                    .font(CyberTypography.titleLarge) // Huiwen 32sp Bold
                    .fontWeight(.bold)
                    .foregroundStyle(CyberColors.cyberWhite)
                    .tracking(6) // letterSpacing 6

                // Interpretation preview
                Text(isExpanded ? reading.resultText : reading.resultText)
                    .font(CyberTypography.bodySmall) // WenKai 14sp
                    .foregroundStyle(CyberColors.grayBody)
                    .lineSpacing(4) // lineHeight 24 ≈ 14sp + 10
                    .lineLimit(isExpanded ? nil : 3)

                // Bottom row: share + expand hint
                if isExpanded {
                    HStack {
                        Spacer()
                        Button(action: shareReading) {
                            Text("分享")
                                .font(CyberTypography.monoSmall)
                                .foregroundStyle(CyberColors.grayCaption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .overlay(
                                    Rectangle()
                                        .stroke(CyberColors.grayBorder, lineWidth: 1)
                                )
                        }
                    }
                    .padding(.top, 4)
                }
            }
            .padding(.horizontal, CyberSpacing.lg)
            .padding(.vertical, 12)
            .background(CyberColors.cyberBlack)
            .offset(x: dragOffset)
            .gesture(
                DragGesture(minimumDistance: 30)
                    .onChanged { value in
                        let dx = value.translation.width
                        dragOffset = dx < 0 ? max(dx, -100) : 0
                    }
                    .onEnded { value in
                        withAnimation(.easeOut(duration: 0.2)) {
                            if value.translation.width < -60 {
                                showDeleteConfirm = true
                                dragOffset = -80
                            } else {
                                dragOffset = 0
                            }
                        }
                    }
            )
            .contentShape(Rectangle())
            .onTapGesture {
                onToggle()
            }
        }
        .confirmationDialog("确认删除？", isPresented: $showDeleteConfirm, titleVisibility: .visible) {
            Button("删除", role: .destructive) {
                onDelete()
            }
            Button("取消", role: .cancel) {
                withAnimation { dragOffset = 0 }
            }
        }
    }

    // MARK: - Date formatting

    private var ganzhiDate: String {
        // Simplified Ganzhi: use the reading's date with Chinese calendar elements
        let formatter = DateFormatter()
        formatter.dateFormat = "M月d日"
        let solarStr = formatter.string(from: reading.createdAt)

        // Basic Ganzhi approximation using day of year
        let dayOfYear = Calendar.current.ordinality(of: .day, in: .year, for: reading.createdAt) ?? 1
        let stems = ["甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"]
        let branches = ["子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"]
        let stemIdx = (dayOfYear - 1) % 10
        let branchIdx = (dayOfYear - 1) % 12
        return "\(stems[stemIdx])\(branches[branchIdx])日 · \(solarStr)"
    }

    private var solarDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        return formatter.string(from: reading.createdAt)
    }

    private var typeBadge: String {
        reading.type.icon
    }

    // MARK: - Share

    private func shareReading() {
        let text = "\(reading.title)\n\n\(reading.resultText)"
        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = scene.windows.first?.rootViewController {
            root.present(av, animated: true)
        }
    }
}
