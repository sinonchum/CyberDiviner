// swift-tools-version: 5.10
import PackageDescription

let package = Package(
    name: "CyberDiviner",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        // App executable
        .executable(name: "CyberDivinerApp", targets: ["CyberDivinerApp"]),

        // Dynamic libraries
        .library(name: "DesignSystem",     type: .dynamic, targets: ["DesignSystem"]),
        .library(name: "DivinationCore",   type: .dynamic, targets: ["DivinationCore"]),
        .library(name: "AI",               type: .dynamic, targets: ["AI"]),
        .library(name: "Persistence",      type: .dynamic, targets: ["Persistence"]),
        .library(name: "Oracle",           type: .dynamic, targets: ["Oracle"]),
        .library(name: "Liuyao",           type: .dynamic, targets: ["Liuyao"]),
        .library(name: "Tarot",            type: .dynamic, targets: ["Tarot"]),
        .library(name: "Archive",          type: .dynamic, targets: ["Archive"]),
        .library(name: "Settings",         type: .dynamic, targets: ["Settings"]),
        .library(name: "ShareKit",         type: .dynamic, targets: ["ShareKit"]),
    ],
    targets: [
        // App executable
        .executableTarget(
            name: "CyberDivinerApp",
            dependencies: [
                "DesignSystem",
                "DivinationCore",
                "AI",
                "Persistence",
                "Oracle",
                "Liuyao",
                "Tarot",
                "Archive",
                "Settings",
                "ShareKit",
            ],
            path: "Sources/CyberDivinerApp"
        ),

        // Core modules
        .target(
            name: "DesignSystem",
            path: "Sources/DesignSystem"
        ),
        .target(
            name: "DivinationCore",
            path: "Sources/DivinationCore"
        ),
        .target(
            name: "AI",
            dependencies: ["DivinationCore"],
            path: "Sources/AI"
        ),
        .target(
            name: "Persistence",
            dependencies: ["DivinationCore"],
            path: "Sources/Persistence"
        ),

        // Feature modules
        .target(
            name: "Oracle",
            dependencies: ["AI", "DivinationCore", "Persistence", "DesignSystem"],
            path: "Sources/Features/Oracle"
        ),
        .target(
            name: "Liuyao",
            dependencies: ["DivinationCore", "AI", "Persistence", "DesignSystem"],
            path: "Sources/Features/Liuyao"
        ),
        .target(
            name: "Tarot",
            dependencies: ["DivinationCore", "AI", "Persistence", "DesignSystem"],
            path: "Sources/Features/Tarot"
        ),
        .target(
            name: "Archive",
            dependencies: ["Persistence", "DesignSystem"],
            path: "Sources/Features/Archive"
        ),
        .target(
            name: "Settings",
            dependencies: ["AI", "DesignSystem"],
            path: "Sources/Features/Settings"
        ),

        // Share extension
        .target(
            name: "ShareKit",
            dependencies: ["Persistence", "DesignSystem"],
            path: "Sources/ShareKit"
        ),
    ]
)
