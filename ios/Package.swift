// swift-tools-version: 5.10
import PackageDescription

let package = Package(
    name: "CyberDivinerLibs",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "DesignSystem",      targets: ["DesignSystem"]),
        .library(name: "DivinationCore",    targets: ["DivinationCore"]),
        .library(name: "AI",                targets: ["AI"]),
        .library(name: "Persistence",       targets: ["Persistence"]),
        .library(name: "Oracle",            targets: ["Oracle"]),
        .library(name: "Liuyao",            targets: ["Liuyao"]),
        .library(name: "Tarot",             targets: ["Tarot"]),
        .library(name: "Archive",           targets: ["Archive"]),
        .library(name: "Settings",          targets: ["Settings"]),
        .library(name: "ShareKit",          targets: ["ShareKit"]),
    ],
    targets: [
        .target(name: "DesignSystem", path: "Sources/DesignSystem"),
        .target(name: "DivinationCore", path: "Sources/DivinationCore"),
        .target(name: "AI", dependencies: ["DivinationCore"], path: "Sources/AI"),
        .target(name: "Persistence", dependencies: ["DivinationCore"], path: "Sources/Persistence"),
        .target(name: "Oracle", dependencies: ["AI", "DivinationCore", "Persistence", "DesignSystem"], path: "Sources/Features/Oracle"),
        .target(name: "Liuyao", dependencies: ["DivinationCore", "AI", "Persistence", "DesignSystem"], path: "Sources/Features/Liuyao"),
        .target(name: "Tarot", dependencies: ["DivinationCore", "AI", "Persistence", "DesignSystem"], path: "Sources/Features/Tarot"),
        .target(name: "Archive", dependencies: ["Persistence", "DesignSystem"], path: "Sources/Features/Archive"),
        .target(name: "Settings", dependencies: ["AI", "DesignSystem"], path: "Sources/Features/Settings"),
        .target(name: "ShareKit", dependencies: ["Persistence", "DesignSystem"], path: "Sources/ShareKit"),
    ]
)
