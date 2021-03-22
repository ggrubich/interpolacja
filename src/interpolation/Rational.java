package interpolation;

import java.util.Objects;

// Immutable rational numbers.
// Note that constructors and methods in this class will always convert
// numbers to their canonical form, i.e. irreducible fractions with
// a non-negative denominator. So, for example, constructing 2/-4
// will actually yield -1/2 and multiplication 1/2 * 2/3 returns 1/3.
public class Rational {
    private final long num;
    private final long den;

    public long getNum() {
        return num;
    }

    public long getDen() {
        return den;
    }

    // Greatest common divisor.
    private long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long tmp_b = b;
            b = a % b;
            a = tmp_b;
        }
        return a;
    }

    // Least common multiple.
    private long lcm(long a, long b) {
        long d = gcd(a, b);
        if (d == 0) {
            return 0;
        }
        return (a / d) * b;
    }

    // Construct a Rational from its fractional parts.
    public Rational(long p, long q) {
        if (q == 0) {
            throw new IllegalArgumentException("Denominator can't be 0");
        }
        if (q < 0) {
            p *= -1;
            q *= -1;
        }
        long d = gcd(p, q);
        num = p / d;
        den = q / d;
    }

    // Construct a Rational from an integer.
    public Rational(long n) {
        this(n, 1);
    }

    // |a|
    public Rational abs() {
        return new Rational(Math.abs(num), den);
    }

    // -a
    public Rational negate() {
        return new Rational(-num, den);
    }

    // a + b
    public Rational add(Rational x) {
        // We use the lcm to minimize the likelihood of overflows.
        long q = lcm(den, x.den);
        long p = num * (q / den) + x.num * (q / x.den);
        return new Rational(p, q);
    }

    // a - b
    public Rational sub(Rational x) {
        return add(x.negate());
    }

    // 1/a
    public Rational invert() {
        return new Rational(den, num);
    }

    // a * b
    public Rational mul(Rational x) {
        // We reduce the fraction before doing the multiplication to minimize
        // the likelihood of overflows.
        long d1 = gcd(num, x.den);
        long d2 = gcd(x.num, den);
        return new Rational((num / d1) * (x.num / d2), (den / d2) * (x.den / d1));
    }

    // a / b
    public Rational div(Rational x) {
        return mul(x.invert());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Rational other = (Rational)obj;
        return num == other.num && den == other.den;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    @Override
    public String toString() {
        if (den == 1) {
            return Long.toString(num);
        }
        else {
            return num + "/" + den;
        }
    }
}
