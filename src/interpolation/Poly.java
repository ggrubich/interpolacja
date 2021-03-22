package interpolation;

import java.util.Objects;

// Immutable polynomial with Rational coefficients.
// Methods in this class assume that coefficients are ordered in the following way:
//   a0, a1, a2, ..., an
// where
//   P(x) = a0 + a1*x + a2*x^2 + a3*x^3 + ... + an*x^n
public class Poly {
    private final Rational[] coeffs;

    // Constructs the polynomial from given coefficients.
    public Poly(Rational... as) {
        int n = as.length;
        while (n > 0 && as[n-1].getNum() == 0) {
            --n;
        }
        coeffs = new Rational[n];
        for (int i = 0; i < n; ++i) {
            coeffs[i] = as[i];
        }
    }

    // Returns ith coefficient. If i > degree always returns 0.
    public Rational get(int i) {
        if (i >= coeffs.length) {
            return new Rational(0);
        }
        return coeffs[i];
    }

    // Returns polynomial's degree. We assume that the zero polynomial has a negative degree.
    public int degree() {
        return coeffs.length - 1;
    }

    // P + Q
    public Poly add(Poly other) {
        int deg = Math.max(degree(), other.degree());
        if (deg < 0) {
            return new Poly();
        }
        Rational[] out = new Rational[deg+1];
        for (int i = 0; i <= deg; ++i) {
            out[i] = get(i).add(other.get(i));
        }
        return new Poly(out);
    }

    // P * Q
    public Poly mul(Poly other) {
        if (degree() < 0 || other.degree() < 0) {
            return new Poly();
        }
        int deg = degree() + other.degree();
        Rational[] out = new Rational[deg+1];
        for (int i = 0; i <= deg; ++i) {
            out[i] = new Rational(0);
        }
        for (int i = 0; i <= degree(); ++i) {
            for (int j = 0; j <= other.degree(); ++j) {
                Rational a = get(i);
                Rational b = other.get(j);
                out[i+j] = out[i+j].add(a.mul(b));
            }
        }
        return new Poly(out);
    }

    // P(x)
    public Rational eval(Rational x) {
        Rational y = new Rational(0);
        for (int i = degree(); i >= 0; --i) {
            y = y.mul(x);
            y = y.add(get(i));
        }
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Poly other = (Poly)obj;
        if (degree() != other.degree()) {
            return false;
        }
        for (int i = 0; i < degree(); ++i) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object[])coeffs);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(get(0));
        for (int i = 1; i <= degree(); ++i) {
            Rational a = get(i);
            if (a.getNum() == 0) {
                continue;
            }
            buf.append(" ");
            buf.append(a.getNum() >= 0 ? "+" : "-");
            buf.append(" ");
            if (a.getDen() == 1) {
                buf.append(a.abs().toString());
            }
            else {
                buf.append("(");
                buf.append(a.abs().toString());
                buf.append(")");
            }
            buf.append("x");
            if (i > 1) {
                buf.append("^");
                buf.append(i);
            }
        }
        return buf.toString();
    }
}
