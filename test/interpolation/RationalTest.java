package interpolation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RationalTest {
    @Test
    public void testInitZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rational(12, 0),
                "creating rational with denominator 0");
    }

    private void assertRationalEquals(long p, long q, Rational sut, String desc) {
        assertEquals(p, sut.getNum(), "numerator of " + desc);
        assertEquals(q, sut.getDen(), "denominator of " + desc);
    }

    @Test
    public void testInitNatural() {
        Rational sut = new Rational(13);
        assertRationalEquals(13, 1, sut, "13");
    }

    @Test
    public void testInitBothPositive() {
        Rational sut = new Rational(12, 34);
        assertRationalEquals(6, 17, sut, "12/34");
    }

    @Test
    public void testInitBothNegative() {
        Rational sut = new Rational(-42, -28);
        assertRationalEquals(3, 2, sut, "-42/-28");
    }

    @Test
    public void testInitPositiveNegative() {
        Rational sut = new Rational(12, -34);
        assertRationalEquals(-6, 17, sut, "12/-34");
    }

    @Test
    public void testEqualsTrue() {
        Rational x = new Rational(3, 4);
        Rational y = new Rational(9, 12);
        assertTrue(x.equals(x), "self equals self");
        assertTrue(x.equals(y), "self equals other");
        assertTrue(y.equals(x), "other equals self");
    }

    @Test
    public void testEqualsFalse() {
        Rational x = new Rational(3, 5);
        Rational y = new Rational(4, 5);
        assertFalse(x.equals(null), "self not equals null");
        assertFalse(x.equals(y), "self not equals other");
        assertFalse(y.equals(x), "other not equals self");
    }

    @Test
    public void testHashCode() {
        Rational x = new Rational(3, 4);
        Rational y = new Rational(9, 12);
        assertEquals(x.hashCode(), y.hashCode(),
                "hash codes of equal objects are equal");
    }

    @Test
    public void testAbs() {
        assertEquals(new Rational(3, 4),
                new Rational(3, -4).abs(),
                "abs of 3/-4");
    }

    @Test
    public void testNegate() {
        assertEquals(new Rational(4, 5),
                new Rational(-4, 5).negate(),
                "negate -4/5");
    }

    @Test
    public void testAdd() {
        assertEquals(new Rational(13, 12),
                new Rational(3, 4).add(new Rational(1, 3)),
                "3/4 + 1/3");
    }

    @Test
    public void testSub() {
        assertEquals(new Rational(-1, 12),
                new Rational(3, 4).sub(new Rational(5, 6)),
                "3/4 - 5/6");
    }

    @Test
    public void testInvert() {
        assertEquals(new Rational(-3, 4),
                new Rational(-4, 3).invert(),
                "invert -4/3");
    }

    @Test
    public void testMul() {
        assertEquals(new Rational(2),
                new Rational(3, 4).mul(new Rational(8, 3)),
                "3/4 * 8/3");
    }

    @Test
    public void testDiv() {
        assertEquals(new Rational(2, 3),
                new Rational(1, 2).div(new Rational(3, 4)),
                "1/2 / 3/4");
    }
}
