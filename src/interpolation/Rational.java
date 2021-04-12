package interpolation;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

// Immutable rational numbers.
// Note that constructors and methods in this class will always convert
// numbers to their canonical form, i.e. irreducible fractions with
// a non-negative denominator. So, for example, constructing 2/-4
// will actually yield -1/2 and multiplication 1/2 * 2/3 returns 1/3.
public class Rational implements Comparable<Rational> {
    private final BigInteger num;
    private final BigInteger den;

    public BigInteger getNum() {
        return num;
    }

    public BigInteger getDen() {
        return den;
    }

    // Least common multiple.
    private BigInteger lcm(BigInteger a, BigInteger b) {
        BigInteger d = a.gcd(b);
        if (d.signum() == 0) {
            return d;
        }
        return a.divide(d).multiply(b);
    }

    // Construct a Rational from its fractional parts.
    public Rational(BigInteger p, BigInteger q) {
        if (q.signum() == 0) {
            throw new IllegalArgumentException("Denominator can't be 0");
        }
        if (q.signum() < 0) {
            p = p.negate();
            q = q.negate();
        }
        BigInteger d = p.gcd(q);
        num = p.divide(d);
        den = q.divide(d);
    }

    public Rational(long p, long q) {
        this(BigInteger.valueOf(p), BigInteger.valueOf(q));
    }

    // Construct a Rational from an integer.
    public Rational(BigInteger n) {
        this(n, BigInteger.ONE);
    }

    public Rational(long n) {
        this(n, 1);
    }

    // |a|
    public Rational abs() {
        return new Rational(num.abs(), den);
    }

    // -a
    public Rational negate() {
        return new Rational(num.negate(), den);
    }

    // a + b
    public Rational add(Rational x) {
        // We use the lcm to minimize the likelihood of overflows.
        BigInteger q = lcm(den, x.den);
        BigInteger p = num.multiply(q.divide(den)).add(x.num.multiply(q.divide(x.den)));
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
        BigInteger d1 = num.gcd(x.den);
        BigInteger d2 = x.num.gcd(den);
        return new Rational(
                num.divide(d1).multiply(x.num.divide(d2)),
                den.divide(d2).multiply(x.den.divide(d1))
        );
    }

    // a / b
    public Rational div(Rational x) {
        return mul(x.invert());
    }

    // sgn(a)
    public int signum() {
        return num.signum();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Rational other = (Rational)obj;
        return Objects.equals(num, other.num) && Objects.equals(den, other.den);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    @Override
    public int compareTo(Rational x) {
        return sub(x).signum();
    }

    public double toDouble() {
        return num.doubleValue() / den.doubleValue();
    }

    private String zeroPad(long width, String str) {
        StringBuilder buf = new StringBuilder();
        for (int i = str.length(); i < width; ++i) {
            buf.append("0");
        }
        buf.append(str);
        return buf.toString();
    }

    public Optional<String> toDecimal() {
        // A fraction can be converted to a decimal iff its denominator
        // can be represented as 2^i * 5^j. In that case, the denominator
        // of the decimal fraction is (2*5)^max{i,j}.
        BigInteger q = den;
        long i = 0;
        long j = 0;
        BigInteger TWO = BigInteger.valueOf(2);
        BigInteger FIVE = BigInteger.valueOf(5);
        while (q.remainder(TWO).signum() == 0) {
            q = q.divide(TWO);
            ++i;
        }
        while (q.remainder(FIVE).signum() == 0) {
            q = q.divide(FIVE);
            ++j;
        }
        if (!q.equals(BigInteger.ONE)) {
            return Optional.empty();
        }

        q = den;
        while (i < j) {
            q = q.multiply(TWO);
            ++i;
        }
        while (j < i) {
            q = q.multiply(FIVE);
            ++j;
        }
        BigInteger p = num.multiply(q.divide(den)).abs();
        // Explicit "-" is required for numbers starting with 0.
        // Padding is necessary to properly display numbers with leading zeros
        // in fractional part, e.g. "1.0003".
        StringBuilder buf = new StringBuilder();
        if (num.signum() < 0) {
            buf.append("-");
        }
        buf.append(p.divide(q));
        buf.append(".");
        buf.append(zeroPad(i, p.mod(q).toString()));
        return Optional.of(buf.toString());
    }

    @Override
    public String toString() {
        if (den.equals(BigInteger.ONE)) {
            return num.toString();
        }
        Optional<String> dec = toDecimal();
        if (dec.isPresent()) {
            return dec.get();
        }
        else if (num.abs().compareTo(den) < 0) {
            return num + "/" + den;
        }
        else {
            return num.divide(den) + " " + (num.abs().mod(den)) + "/" + den;
        }
    }

    private static class Parser {
        private static final BigInteger TEN = BigInteger.valueOf(10);

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
        private BigInteger parseNatural() {
            if (!Character.isDigit(get())) {
                error("expected digit");
            }
            BigInteger n = BigInteger.ZERO;
            do {
                BigInteger d = BigInteger.valueOf(Character.digit(get(), 10));
                n = n.multiply(TEN).add(d);
                next();
            } while (Character.isDigit(get()));
            return n;
        }

        // 1234 (but means 1234/10000)
        private Rational parseDecimals() {
            int start = position;
            BigInteger p = parseNatural();
            BigInteger q = TEN.pow(position - start);
            return new Rational(p, q);
        }

        // 12 / 34
        private Rational parseFraction() {
            BigInteger p = parseNatural();
            skipSpace();
            require('/');
            skipSpace();
            BigInteger q = parseNatural();
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
                    BigInteger q = parseNatural();
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
