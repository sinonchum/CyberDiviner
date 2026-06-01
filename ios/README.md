# CyberDiviner iOS

## Architecture

Swift Package Manager (SPM) modular architecture targeting **iOS 17+**.

### Module Structure

```
CyberDiviner/
├── Package.swift
└── Sources/
    ├── CyberDivinerApp/      ← @main executable entry point
    ├── DesignSystem/          ← Colors, typography, shared UI tokens
    ├── DivinationCore/        ← Core divination models & logic (no deps)
    ├── AI/                    ← AI/LLM integration layer
    ├── Persistence/           ← Data storage (SwiftData/CoreData)
    ├── Features/
    │   ├── Oracle/            ← Oracle (叩问天机) feature
    │   ├── Liuyao/            ← Liuyao (六爻) feature
    │   ├── Tarot/             ← Tarot feature
    │   ├── Archive/           ← Reading history (因果命簿)
    │   └── Settings/          ← App settings
    └── ShareKit/              ← Share extension support
```

### Dependency Graph

```
CyberDivinerApp (executable)
├── DesignSystem
├── DivinationCore
├── AI ──────────→ DivinationCore
├── Persistence ─→ DivinationCore
├── Oracle ──────→ AI, DivinationCore, Persistence, DesignSystem
├── Liuyao ──────→ DivinationCore, AI, Persistence, DesignSystem
├── Tarot ───────→ DivinationCore, AI, Persistence, DesignSystem
├── Archive ─────→ Persistence, DesignSystem
├── Settings ────→ AI, DesignSystem
└── ShareKit ────→ Persistence, DesignSystem
```

## Building

### Prerequisites

- Xcode 15.4+ (or any version supporting Swift 5.10 / iOS 17)
- macOS 14+

### Build from Command Line

```bash
cd ios/

# Build for iOS Simulator
xcodebuild -scheme CyberDivinerApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  build

# Or use swift build (limited — SPM alone can't target iOS natively)
swift build
```

### Build in Xcode

1. Open the `ios/` directory in Xcode (File → Open → select `ios/`)
2. Xcode will resolve the Package.swift and show all targets
3. Select the **CyberDivinerApp** scheme
4. Choose an iOS 17+ simulator or device
5. Build & Run (⌘R)

### Adding a New Module

1. Create `Sources/YourModule/` directory
2. Add at least one `.swift` file (SPM ignores empty targets)
3. Add the `.target()` entry in `Package.swift`
4. Add the `.library()` product if it should be consumable
5. Add the dependency to any targets that need it

## Notes

- All library products are **dynamic** (`type: .dynamic`) for now to support
  SwiftUI previews and faster incremental builds. Switch to static later for
  release optimization.
- Feature modules under `Features/` are flat targets with path-based routing
  (e.g., `Sources/Features/Oracle/`), not nested packages.
- `DesignSystem` defines `CyberDivinerColors` and other shared UI tokens.
  Import it in any SwiftUI view that needs the app's color palette.
