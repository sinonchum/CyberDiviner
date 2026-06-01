import SwiftUI
import DesignSystem
import Persistence

public struct ArchiveScreen: View {

    public init() {}

    @State private var vm = ArchiveViewModel()
    @State private var expandedId: UUID?

    public var body: some View {
        VStack(spacing: 0) {
            SectionHeader(chineseTitle: "因果命簿", englishSubtitle: "KARMA ARCHIVE")
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.top, CyberSpacing.xs)

            DividerLine()

            // Filter chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: CyberSpacing.xs) {
                    FilterChip(label: "全部", isActive: vm.filterType == nil) {
                        vm.filterType = nil
                    }
                    FilterChip(label: "叩问", isActive: vm.filterType == .oracle) {
                        vm.filterType = .oracle
                    }
                    FilterChip(label: "六爻", isActive: vm.filterType == .liuyao) {
                        vm.filterType = .liuyao
                    }
                    FilterChip(label: "塔罗", isActive: vm.filterType == .tarot) {
                        vm.filterType = .tarot
                    }
                }
                .padding(.horizontal, CyberSpacing.sm)
                .padding(.vertical, CyberSpacing.xs)
            }

            DividerLine()

            if vm.filteredReadings.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(vm.filteredReadings) { reading in
                        ArchiveRow(
                            reading: reading,
                            isExpanded: expandedId == reading.id
                        )
                        .onTapGesture {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                expandedId = expandedId == reading.id ? nil : reading.id
                            }
                        }
                        .listRowBackground(CyberColors.cyberBlack)
                        .swipeActions(edge: .trailing) {
                            Button(role: .destructive) {
                                vm.delete(reading)
                            } label: {
                                Label("删除", systemImage: "trash")
                            }
                        }
                    }
                }
                .listStyle(.plain)
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

// MARK: - Archive Row

private struct ArchiveRow: View {
    let reading: SavedReading
    let isExpanded: Bool

    public var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(typeIcon)
                    .font(CyberTypography.bodyMedium)
                Text(reading.title)
                    .font(CyberTypography.bodyMedium)
                    .foregroundStyle(CyberColors.grayTitle)
                Spacer()
                Text(dateString)
                    .font(CyberTypography.monoCaption)
                    .foregroundStyle(CyberColors.grayMuted)
            }

            if !reading.question.isEmpty {
                Text(reading.question)
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayCaption)
                    .lineLimit(1)
            }

            if isExpanded {
                DividerLine()
                Text(reading.resultText)
                    .font(CyberTypography.bodySmall)
                    .foregroundStyle(CyberColors.grayBody)
                    .textSelection(.enabled)
            }
        }
        .padding(.vertical, 4)
    }

    private var typeIcon: String {
        switch reading.type {
        case .oracle: return "签"
        case .liuyao: return "爻"
        case .tarot:  return "牌"
        }
    }

    private var dateString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM/dd HH:mm"
        return formatter.string(from: reading.createdAt)
    }
}

// MARK: - Filter Chip

private struct FilterChip: View {
    let label: String
    let isActive: Bool
    let action: () -> Void

    public var body: some View {
        Text(label)
            .font(CyberTypography.monoSmall)
            .foregroundStyle(isActive ? CyberColors.cyberBlack : CyberColors.grayCaption)
            .padding(.horizontal, CyberSpacing.xs)
            .padding(.vertical, 4)
            .background(isActive ? CyberColors.cyberWhite : .clear)
            .overlay(
                Rectangle()
                    .stroke(CyberColors.grayBorder, lineWidth: 1)
            )
            .contentShape(Rectangle())
            .onTapGesture { action() }
    }
}
