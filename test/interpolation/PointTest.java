package interpolation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PointTest {
    @Test
    public void testGetX() {
        assertEquals(new Rational(13),
                new Point(new Rational(13), new Rational(31)).getX(),
                "x getter");
    }

    @Test
    public void testGetY() {
        assertEquals(new Rational(31),
                new Point(new Rational(13), new Rational(31)).getY(),
                "y getter");
    }

    @Test
    public void testEquality() {
        Point a1 = new Point(new Rational(1), new Rational(13));
        Point a2 = new Point(new Rational(1), new Rational(13));
        Point b = new Point(new Rational(1), new Rational(1));
        TestUtils.testEquality(a1, a2, b);
    }
}
