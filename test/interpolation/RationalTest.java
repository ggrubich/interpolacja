package interpolation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class RationalTest {
    @Test
    public void testInitZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rational(12, 0),
                "creating rational with denominator 0");
    }

    private void assertRationalEquals(long p, long q, Rational sut, String desc) {
        assertEquals(BigInteger.valueOf(p), sut.getNum(), "numerator of " + desc);
        assertEquals(BigInteger.valueOf(q), sut.getDen(), "denominator of " + desc);
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
    public void testEquality() {
        Rational a1 = new Rational(3, 4);
        Rational a2 = new Rational(9, 12);
        Rational b = new Rational(2, 4);
        TestUtils.testEquality(a1, a2, b);
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

    @Test
    public void testSignumMinus() {
        assertEquals(-1, new Rational(-3, 4).signum(), "sgn(-3/4) = -1");
    }

    @Test
    public void testSignumZero() {
        assertEquals(0, new Rational(0).signum(), "sgn(0) = 0");
    }

    @Test
    public void testSignumPlus() {
        assertEquals(1, new Rational(2, 7).signum(), "sgn(2/7) = 1");
    }

    @Test
    public void testCompareLT() {
        assertTrue(new Rational(3, 4).compareTo(new Rational(4, 3)) < 0,
                "3/4 < 4/3");
    }

    @Test
    public void testCompareEQ() {
        assertTrue(new Rational(3, 4).compareTo(new Rational(3, 4)) == 0,
                "3/4 == 3/4");
    }

    @Test
    public void testCompareGT() {
        assertTrue(new Rational(3, 4).compareTo(new Rational(-3, 4)) > 0,
                "3/4 > -3/4");
    }

    @Test
    public void testToStringInt() {
        assertEquals("13", new Rational(13).toString(), "to string");
    }

    @Test
    public void testToStringDecimal() {
        assertEquals("-0.375", new Rational(-3, 8).toString(), "to string");
    }

    @Test
    public void testToStringLeadingZero() {
        assertEquals("1.0003", new Rational(10003, 10000).toString(), "to string");
    }

    @Test
    public void testToStringFraction() {
        assertEquals("-5/7", new Rational(-5, 7).toString(), "to string");
    }

    @Test
    public void testToStringMixed() {
        assertEquals("-1 2/3", new Rational(-5, 3).toString(), "to string");
    }

    private void assertParse(Rational expected, String input) {
        Rational actual = Rational.parse(input);
        assertEquals(expected, actual, "parse `" + input + "`");
    }

    @Test
    public void testParseInt() {
        assertParse(new Rational(12), "12");
    }

    @Test
    public void testParseDecimal() {
        assertParse(new Rational(-243, 20), "- 12.15 ");
    }

    @Test
    public void testParseFraction() {
        assertParse(new Rational(-12, 34), "  -12 /34");
    }

    @Test
    public void testParseMixedSpace() {
        assertParse(new Rational(7, 4), "1 3 / 4");
    }

    @Test
    public void testParseMixedUnderscore() {
        assertParse(new Rational(7, 4), " 1_3/4  ");
    }

    @Test
    public void testParseMixedPlus() {
        assertParse(new Rational(7, 4), "1 +3/ 4");
    }

    @Test
    public void testParseBig() {
        String input = "-92233720368547758070000.1234";
        String p = "-922337203685477580700001234";
        String q = "10000";
        assertParse(new Rational(new BigInteger(p), new BigInteger(q)), input);
    }

    private void assertParseThrows(String input) {
        assertThrows(NumberFormatException.class,
                () -> Rational.parse(input),
                "parse " + input);
    }

    @Test
    public void testParseInvalidInt() {
        assertParseThrows("12 a");
    }

    @Test
    public void testParseInvalidDecimal() {
        assertParseThrows("-12.");
    }

    @Test
    public void testParseInvalidFraction() {
        assertParseThrows("/4");
    }

    @Test
    public void testParseInvalidMixed() {
        assertParseThrows("- 1 3/");
    }
}
