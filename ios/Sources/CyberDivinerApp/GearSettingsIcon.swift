import SwiftUI
import DesignSystem

/// Shared gear/settings icon — a circle with inner hole and 4 short teeth at cardinal points.
/// Matches Android GearSettingsIcon in CyberIcons.kt exactly.
public struct GearSettingsIcon: View {
    let size: CGFloat
    let color: Color

    public init(size: CGFloat = 20, color: Color = CyberColors.grayCaption) {
        self.size = size
        self.color = color
    }

    public var body: some View {
        Canvas { context, canvasSize in
            let sw: CGFloat = size * 0.075
            let cx = canvasSize.width / 2
            let cy = canvasSize.height / 2
            let outerR = canvasSize.width * 0.4
            let innerR = canvasSize.width * 0.15
            let toothLen = size * 0.15

            // Outer circle
            context.stroke(
                Path { p in
                    p.addEllipse(in: CGRect(
                        x: cx - outerR, y: cy - outerR,
                        width: outerR * 2, height: outerR * 2
                    ))
                },
                with: .color(color),
                style: StrokeStyle(lineWidth: sw, lineCap: .square)
            )

            // Inner circle
            context.stroke(
                Path { p in
                    p.addEllipse(in: CGRect(
                        x: cx - innerR, y: cy - innerR,
                        width: innerR * 2, height: innerR * 2
                    ))
                },
                with: .color(color),
                style: StrokeStyle(lineWidth: sw, lineCap: .square)
            )

            // 4 cardinal teeth
            for angle in [0.0, 90.0, 180.0, 270.0] {
                let rad = angle * .pi / 180
                let startX = cx + outerR * cos(rad)
                let startY = cy + outerR * sin(rad)
                let endX = cx + (outerR + toothLen) * cos(rad)
                let endY = cy + (outerR + toothLen) * sin(rad)

                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: startX, y: startY))
                        p.addLine(to: CGPoint(x: endX, y: endY))
                    },
                    with: .color(color),
                    style: StrokeStyle(lineWidth: sw, lineCap: .square)
                )
            }
        }
        .frame(width: size, height: size)
    }
}

#Preview {
    ZStack {
        CyberColors.cyberBlack
        GearSettingsIcon(size: 20)
    }
}
