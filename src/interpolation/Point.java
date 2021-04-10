package interpolation;

import java.util.Objects;

public class Point {
    private final Rational x;
    private final Rational y;

    public Point(Rational x, Rational y) {
        this.x = x;
        this.y = y;
    }

    public Rational getX() {
        return x;
    }

    public Rational getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Point p = (Point)obj;
        return Objects.equals(x, p.x) && Objects.equals(y, p.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
