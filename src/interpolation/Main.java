package interpolation;

import java.util.ArrayList;

// USAGE: interpolation [<point>]...
// where <point> is a comma separated pair of rationals, which represents
// a data point used for interpolation.

public class Main {
	private static void fatal(String msg) {
		System.err.println(msg);
		System.exit(1);
	}

	public static void main(String[] args) {
		ArrayList<Point> points = new ArrayList<>();
		for (String arg : args) {
			String[] parts = arg.split(",");
			if (parts.length != 2) {
				fatal("Invalid point `" + arg + "`");
			}
			try {
				Rational x = Rational.parse(parts[0]);
				Rational y = Rational.parse(parts[1]);
				points.add(new Point(x, y));
			}
			catch (NumberFormatException e) {
				fatal(e.getMessage());
			}
		}
		Poly poly = new Interpolation(points).getResult();
		assert poly.degree() < args.length;
		for (Point p : points) {
			assert poly.eval(p.getX()) == p.getY();
		}
		System.out.println(poly);
	}
}
