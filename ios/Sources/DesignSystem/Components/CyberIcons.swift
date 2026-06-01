import SwiftUI

// MARK: - Oracle Icon (almond eye shape with iris, 24dp)

public struct OracleIcon: View {
    let selected: Bool

    public init(selected: Bool) {
        self.selected = selected
    }

    public var body: some View {
        Canvas { context, size in
            let cx = size.width / 2
            let cy = size.height / 2
            let w = size.width * 0.8
            let h = size.height * 0.5

            // Almond / eye shape — two arcs
            var eye = Path()
            eye.move(to: CGPoint(x: cx - w/2, y: cy))
            eye.addQuadCurve(
                to: CGPoint(x: cx + w/2, y: cy),
                control: CGPoint(x: cx, y: cy - h)
            )
            eye.addQuadCurve(
                to: CGPoint(x: cx - w/2, y: cy),
                control: CGPoint(x: cx, y: cy + h)
            )
            eye.closeSubpath()

            context.stroke(eye, with: .color(selected ? CyberColors.cyberWhite : CyberColors.grayCaption),
                           style: StrokeStyle(lineWidth: 1.5, lineCap: .square))

            // Iris circle
            let irisR = size.width * 0.12
            let iris = Path(ellipseIn: CGRect(
                x: cx - irisR, y: cy - irisR,
                width: irisR * 2, height: irisR * 2
            ))
            context.fill(iris, with: .color(selected ? CyberColors.cyberWhite : CyberColors.grayCaption))
        }
        .frame(width: 24, height: 24)
    }
}

// MARK: - Trigram Icon (3 lines, middle broken, 24dp)

public struct TrigramIcon: View {
    let selected: Bool

    public init(selected: Bool) {
        self.selected = selected
    }

    public var body: some View {
        Canvas { context, size in
            let color = selected ? CyberColors.cyberWhite : CyberColors.grayCaption
            let style = StrokeStyle(lineWidth: 1.5, lineCap: .square)
            let gap = size.height / 4
            let lineW = size.width * 0.8
            let startX = (size.width - lineW) / 2

            for i in 0..<3 {
                let y = gap * CGFloat(i + 1)
                if i == 1 {
                    // Middle line broken into two segments
                    let midGap = lineW * 0.15
                    let mid = size.width / 2
                    let seg1 = Path { p in
                        p.move(to: CGPoint(x: startX, y: y))
                        p.addLine(to: CGPoint(x: mid - midGap, y: y))
                    }
                    let seg2 = Path { p in
                        p.move(to: CGPoint(x: mid + midGap, y: y))
                        p.addLine(to: CGPoint(x: startX + lineW, y: y))
                    }
                    context.stroke(seg1, with: .color(color), style: style)
                    context.stroke(seg2, with: .color(color), style: style)
                } else {
                    let line = Path { p in
                        p.move(to: CGPoint(x: startX, y: y))
                        p.addLine(to: CGPoint(x: startX + lineW, y: y))
                    }
                    context.stroke(line, with: .color(color), style: style)
                }
            }
        }
        .frame(width: 24, height: 24)
    }
}

// MARK: - Learn Icon (open book shape, 24dp)

public struct LearnIcon: View {
    let selected: Bool

    public init(selected: Bool) {
        self.selected = selected
    }

    public var body: some View {
        Canvas { context, size in
            let color = selected ? CyberColors.cyberWhite : CyberColors.grayCaption
            let style = StrokeStyle(lineWidth: 1.5, lineCap: .square)
            let cx = size.width / 2
            let inset = size.width * 0.15
            let top = size.height * 0.2
            let bottom = size.height * 0.8

            // Left page
            var leftPage = Path()
            leftPage.move(to: CGPoint(x: cx, y: top))
            leftPage.addLine(to: CGPoint(x: inset, y: top))
            leftPage.addLine(to: CGPoint(x: inset, y: bottom))
            leftPage.addLine(to: CGPoint(x: cx, y: bottom))

            // Right page
            var rightPage = Path()
            rightPage.move(to: CGPoint(x: cx, y: top))
            rightPage.addLine(to: CGPoint(x: size.width - inset, y: top))
            rightPage.addLine(to: CGPoint(x: size.width - inset, y: bottom))
            rightPage.addLine(to: CGPoint(x: cx, y: bottom))

            // Spine
            let spine = Path { p in
                p.move(to: CGPoint(x: cx, y: top - 2))
                p.addLine(to: CGPoint(x: cx, y: bottom + 2))
            }

            context.stroke(leftPage, with: .color(color), style: style)
            context.stroke(rightPage, with: .color(color), style: style)
            context.stroke(spine, with: .color(color), style: style)
        }
        .frame(width: 24, height: 24)
    }
}

// MARK: - Scroll Icon (document with 3 inner lines, 24dp)

public struct ScrollIcon: View {
    let selected: Bool

    public init(selected: Bool) {
        self.selected = selected
    }

    public var body: some View {
        Canvas { context, size in
            let color = selected ? CyberColors.cyberWhite : CyberColors.grayCaption
            let style = StrokeStyle(lineWidth: 1.5, lineCap: .square)
            let inset = size.width * 0.2
            let margin = size.width * 0.1

            // Outer rectangle
            let rect = Path(CGRect(
                x: inset, y: margin,
                width: size.width - inset * 2,
                height: size.height - margin * 2
            ))
            context.stroke(rect, with: .color(color), style: style)

            // 3 inner horizontal lines
            let lineStartX = inset + 3
            let lineEndX = size.width - inset - 3
            let areaTop = margin + 4
            let areaHeight = size.height - margin * 2 - 8

            for i in 0..<3 {
                let y = areaTop + areaHeight * CGFloat(i + 1) / 4
                let line = Path { p in
                    p.move(to: CGPoint(x: lineStartX, y: y))
                    p.addLine(to: CGPoint(x: lineEndX, y: y))
                }
                context.stroke(line, with: .color(color), style: style)
            }
        }
        .frame(width: 24, height: 24)
    }
}

// MARK: - IChing Icon (3 lines, 48dp)

public struct IChingIcon: View {
    let selected: Bool

    public init(selected: Bool) {
        self.selected = selected
    }

    public var body: some View {
        Canvas { context, size in
            let color = selected ? CyberColors.cyberWhite : CyberColors.grayCaption
            let style = StrokeStyle(lineWidth: 1.5, lineCap: .square)
            let gap = size.height / 4
            let lineW = size.width * 0.7
            let startX = (size.width - lineW) / 2

            for i in 0..<3 {
                let y = gap * CGFloat(i + 1)
                let line = Path { p in
                    p.move(to: CGPoint(x: startX, y: y))
                    p.addLine(to: CGPoint(x: startX + lineW, y: y))
                }
                context.stroke(line, with: .color(color), style: style)
            }
        }
        .frame(width: 48, height: 48)
    }
}

// MARK: - Tarot Icon (rectangle + diamond, 48dp)

public struct TarotIcon: View {
    let selected: Bool

    public init(selected: Bool) {
        self.selected = selected
    }

    public var body: some View {
        Canvas { context, size in
            let color = selected ? CyberColors.cyberWhite : CyberColors.grayCaption
            let style = StrokeStyle(lineWidth: 1.5, lineCap: .square)
            let cx = size.width / 2
            let cy = size.height / 2
            let cardW = size.width * 0.5
            let cardH = size.height * 0.75

            // Card rectangle
            let card = Path(CGRect(
                x: cx - cardW / 2, y: cy - cardH / 2,
                width: cardW, height: cardH
            ))
            context.stroke(card, with: .color(color), style: style)

            // Diamond in center
            let dSize = min(cardW, cardH) * 0.3
            var diamond = Path()
            diamond.move(to: CGPoint(x: cx, y: cy - dSize))
            diamond.addLine(to: CGPoint(x: cx + dSize, y: cy))
            diamond.addLine(to: CGPoint(x: cx, y: cy + dSize))
            diamond.addLine(to: CGPoint(x: cx - dSize, y: cy))
            diamond.closeSubpath()
            context.stroke(diamond, with: .color(color), style: style)
        }
        .frame(width: 48, height: 48)
    }
}
