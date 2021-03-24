package interpolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class InterpolationTest {
    @Test
    public void testGetPoints() {
        List<Point> expected = Arrays.asList(
                new Point(new Rational(1), new Rational(0)),
                new Point(new Rational(2), new Rational(2)),
                new Point(new Rational(4), new Rational(12)),
                new Point(new Rational(5), new Rational(20))
        );
        Interpolation interp = new Interpolation(expected);
        assertIterableEquals(expected, interp.getPoints(), "points getter");
    }

    private void testInterpolation(List<Point> points) {
        Interpolation interp = new Interpolation(points);
        Poly poly = interp.getResult();
        String comment = " (input = " + points + ", poly = " + poly + ")";
        assertTrue(poly.degree() < points.size(),
                "degree < number of data points" + comment);
        ArrayList<Point> actual = new ArrayList<>();
        for (Point p : points) {
            Rational x = p.getX();
            Rational y = poly.eval(x);
            actual.add(new Point(x, y));
        }
        assertIterableEquals(points, actual,
            "calculated values in nodes" + comment);
    }

    @Test
    public void testZero() {
        List<Point> points = Arrays.asList();
        testInterpolation(points);
    }

    @Test
    public void testOne() {
        List<Point> points = Arrays.asList(new Point(new Rational(1), new Rational(13)));
        testInterpolation(points);
    }

    @Test
    public void testThree() {
        List<Point> points = Arrays.asList(
            new Point(new Rational(1), new Rational(1, 3)),
            new Point(new Rational(2), new Rational(2, 3)),
            new Point(new Rational(3), new Rational(5, 6))
        );
        testInterpolation(points);
    }

    @Test
    public void testFour() {
        List<Point> points = Arrays.asList(
            new Point(new Rational(1), new Rational(0)),
            new Point(new Rational(2), new Rational(2)),
            new Point(new Rational(4), new Rational(12)),
            new Point(new Rational(5), new Rational(20))
        );
    }

    @Test
    public void testInvalidNodes() {
        List<Point> points = Arrays.asList(
            new Point(new Rational(1), new Rational(0)),
            new Point(new Rational(5), new Rational(2)),
            new Point(new Rational(4), new Rational(12)),
            new Point(new Rational(5), new Rational(20))
        );
        // this should cause division by 0
        assertThrows(Exception.class, () -> new Interpolation(points),
                "interpolating data with duplicate node");
    }

}
