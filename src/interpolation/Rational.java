package interpolation;

import java.util.Objects;
import java.util.Optional;

// Immutable rational numbers.
// Note that constructors and methods in this class will always convert
// numbers to their canonical form, i.e. irreducible fractions with
// a non-negative denominator. So, for example, constructing 2/-4
// will actually yield -1/2 and multiplication 1/2 * 2/3 returns 1/3.
public class Rational implements Comparable<Rational> {
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
    public int compareTo(Rational x) {
        return Long.signum(sub(x).getNum());
    }

    public double toDouble() {
        return (double) num / (double) den;
    }

    public Optional<String> toDecimal() {
        // A fraction can be converted to a decimal iff its denominator
        // can be represented as 2^i * 5^j. In that case, the denominator
        // of the decimal fraction is (2*5)^max{i,j}.
        long q = den;
        long i = 0;
        long j = 0;
        while (q % 2 == 0) {
            q /= 2;
            ++i;
        }
        while (q % 5 == 0) {
            q /= 5;
            ++j;
        }
        if (q > 1) {
            return Optional.empty();
        }

        q = den;
        while (i < j) {
            q *= 2;
            ++i;
        }
        while (j < i) {
            q *= 5;
            ++j;
        }
        long p = Math.abs(num * (q / den));
        // Explicit "-" is required for numbers starting with 0.
        return Optional.of((num < 0 ? "-" : "") + p / q + "." + p % q);
    }

    @Override
    public String toString() {
        if (den == 1) {
            return Long.toString(num);
        }
        Optional<String> dec = toDecimal();
        if (dec.isPresent()) {
            return dec.get();
        }
        else if (Math.abs(num) < den){
            return num + "/" + den;
        }
        else {
            return (num / den) + " " + (Math.abs(num) % den) + "/" + den;
        }
    }

    private static class Parser {
        private final String input;
        private int position;

        private Parser(String input) {
            this.input = input;
            position = 0;
        }

        private boolean eof() {
            return position >= input.length();
        }

        private char get() {
            return eof() ? 0 : input.charAt(position);
        }

        private void next() {
            if (!eof()) {
                ++position;
            }
        }

        private void error(String msg) {
            throw new NumberFormatException(
                    "Invalid rational number: " + msg + " at " + position + " in `" + input + "`");
        }

        private boolean attempt(char c) {
            if (get() != c) {
                return false;
            }
            next();
            return true;
        }

        private void require(char c) {
            if (!attempt(c)) {
                error("expected " + c);
            }
        }

        private void skipSpace() {
            while (Character.isWhitespace(get())) {
                next();
            }
        }

        // 123
        private long parseNatural() {
            if (!Character.isDigit(get())) {
                error("expected digit");
            }
            long n = 0;
            do {
                n *= 10;
                n += Character.digit(get(), 10);
                next();
            } while (Character.isDigit(get()));
            return n;
        }

        // 1234 (but means 1234/10000)
        private Rational parseDecimals() {
            int start = position;
            long p = parseNatural();
            long q = (long) Math.pow(10, position - start);
            return new Rational(p, q);
        }

        // 12 / 34
        private Rational parseFraction() {
            long p = parseNatural();
            skipSpace();
            require('/');
            skipSpace();
            long q = parseNatural();
            return new Rational(p, q);
        }

        private Rational parse() {
            skipSpace();
            int sign = 1;
            if (attempt('-')) {
                sign = -1;
                skipSpace();
            }
            Rational out = new Rational(parseNatural());
            if (attempt('.')) {
                // decimal
                Rational b = parseDecimals();
                out = out.add(b);
            } else {
                skipSpace();
                if (attempt('/')) {
                    // fraction
                    skipSpace();
                    long q = parseNatural();
                    out = out.div(new Rational(q));
                } else if (!eof()) {
                    // mixed
                    if (attempt('_') || attempt('+')) {
                        skipSpace();
                    }
                    Rational b = parseFraction();
                    out = out.add(b);
                }
            }
            skipSpace();
            if (!eof()) {
                error("expected eof");
            }
            return sign > 0 ? out : out.negate();
        }
    }

    // Parses string representation of a rational.
    // The following forms are supported:
    //  - integers, e.g. "12"
    //  - decimals, e.g. "12.15"
    //  - fractions, e.g. "1/3"
    //  - mixed numbers, e.g. "1 2/3", "1_2/3", "1+2/3"
    // Numbers can be prepended with a "-" to negate them.
    // Leading and trailing whitespace as well as whitespace around
    // "-", "_", "+" and "/" symbols is allowed.
    // On invalid input a NumberFormatException is thrown.
    public static Rational parse(String input) {
        Parser p = new Parser(input);
        return p.parse();
    }
}
