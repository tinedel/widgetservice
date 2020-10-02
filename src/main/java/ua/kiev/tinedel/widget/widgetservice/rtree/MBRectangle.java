package ua.kiev.tinedel.widget.widgetservice.rtree;

import lombok.Value;
import ua.kiev.tinedel.widget.widgetservice.models.Widget;

@Value
public class MBRectangle {

    // bottom left
    int x1, y1;
    // top right
    int x2, y2;

    public static MBRectangle from(Widget w) {
        return new MBRectangle(w.getX(), w.getY(), w.getX() + w.getWidth(), w.getY() + w.getHeight());
    }

    public boolean contains(MBRectangle other) {
        if (other == null) return false;
        return x1 <= other.x1 && other.x2 <= x2
                && y1 <= other.y1 && other.y2 <= y2;
    }

    // strictly
    public boolean containsPoint(int x, int y) {
        return x1 <= x && x <= x2
                && y1 <= y && y <= y2;
    }

    public boolean intersects(MBRectangle other) {
        if (other == null) return false;
        // main diagonal
        boolean containsBottomLeft = containsPoint(other.x1, other.y1);
        boolean containsTopRight = containsPoint(other.x2, other.y2);
        // side diagonal
        boolean containsBottomRight = containsPoint(other.x2, other.y1);
        boolean containsTopLeft = containsPoint(other.x1, other.y2);

        return
                // partial intersections
                (containsBottomLeft != containsTopRight) || (containsBottomRight != containsTopLeft)
                        // full line intersections horizontal
                        || (other.x1 < x1 && x2 < other.x2
                        && (y1 < other.y1 && other.y1 < y2 || y1 < other.y2 && other.y2 < y2))
                        // full line intersection vertical
                        || (other.y1 < y1 && y2 < other.y2
                        && (x1 < other.x1 && other.x1 < x2 || x1 < other.x2 && other.x2 < x2));
    }

    public long area() {
        return (x2 - x1) * (y2 - y1);
    }

    public MBRectangle append(MBRectangle other) {
        if (other == null) return this;
        return new MBRectangle(
                Math.min(x1, other.x1),
                Math.min(y1, other.y1),
                Math.max(x2, other.x2),
                Math.max(y2, other.y2)
        );
    }
}
