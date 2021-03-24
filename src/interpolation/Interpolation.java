package interpolation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Interpolation {
    private ArrayList<Point> points;
    private Poly result;

    private static Poly interpolate(List<Point> points) {
        int n = points.size();
        if (n == 0) {
            return new Poly();
        }
        // We only need two tables for divided differences
        Rational[] prevDiffs = new Rational[n];
        Rational[] curDiffs = new Rational[n];
        for (int i = 0; i < n; ++i) {
            prevDiffs[i] = points.get(i).getY();
        }
        Rational[] coeffs = new Rational[n];
        coeffs[0] = prevDiffs[0];

        for (int k = 1; k < n; ++k) {
            for (int i = 0; i + k < n; ++i) {
                // f[x_i, ..., x_i+k] =
                //    (f[x_i+1, ..., x_i+k] - f[x_i, ..., x_i+k-1]) / (x_i+k - x_i)
                Rational p = prevDiffs[i+1].sub(prevDiffs[i]);
                Rational q = points.get(i+k).getX().sub(points.get(i).getX());
                curDiffs[i] = p.div(q);
            }
            // b_k = f[x_0, ..., x_k]
            coeffs[k] = curDiffs[0];
            Rational[] tmpDiffs = prevDiffs;
            prevDiffs = curDiffs;
            curDiffs = tmpDiffs;
        }

        Poly result = new Poly();
        for (int i = n-1; i >= 0; --i) {
            // P = P * (x - x_i) + b_i
            Rational x_i = points.get(i).getX();
            result = result.mul(new Poly(x_i.negate(), new Rational(1)));
            result = result.add(new Poly(coeffs[i]));
        }
        return result;
    }

    // Interpolates list of data points. List gets copied.
    // Interpolating an empty list yields the zero polynomial.
    public Interpolation(List<Point> points) {
        result = interpolate(points);
        this.points = new ArrayList<>(points);
    }

    // Returns data points used.
    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    // Returns the computed interpolation polynomial.
    public Poly getResult() {
        return result;
    }
}
