package interpolation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PolyTest {
    @Test
    public void testGet() {
        Poly a = new Poly(
            new Rational(1),
            new Rational(2),
            new Rational(0),
            new Rational(4),
            new Rational(0)
        );
        assertEquals(new Rational(1), a.get(0), "a0");
        assertEquals(new Rational(2), a.get(1), "a1");
        assertEquals(new Rational(0), a.get(2), "a2");
        assertEquals(new Rational(4), a.get(3), "a3");
        assertEquals(new Rational(0), a.get(4), "a4");
        assertEquals(new Rational(0), a.get(10), "a10");
    }

    @Test
    public void testDegreeZero() {
        assertTrue(new Poly(new Rational(0)).degree() < 0,
                "degree of zero polynomial < 0");
    }

    @Test
    public void testDegreeConst() {
        assertEquals(0, new Poly(new Rational(3)).degree(),
                "degree of const polynomial = 0");
    }

    @Test
    public void testDegreeTwo() {
        Poly a = new Poly(new Rational(1), new Rational(0), new Rational(3), new Rational(0));
        assertEquals(2, a.degree(), "degree of [1 + 3x^2] = 0");
    }

    @Test
    public void testEqualsTrue() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        assertTrue(a.equals(a), "self equals self");
        assertTrue(a.equals(b), "self equals other");
        assertTrue(b.equals(a), "other equals self");
    }

    @Test
    public void testEqualsFalse() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly(new Rational(2), new Rational(1), new Rational(1, 2));
        assertFalse(a.equals(null), "self not equals null");
        assertFalse(a.equals(12), "self not equals int");
        assertFalse(a.equals(b), "self not equals other");
        assertFalse(b.equals(a), "other not equals self");
    }

    @Test
    public void testAddZero() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly();
        assertEquals(a, a.add(b), "[" + a + "] + " + b);
    }

    @Test
    public void testAddConst() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly(new Rational(-1));
        Poly y = new Poly(new Rational(0), new Rational(2), new Rational(1, 2));
        assertEquals(y, a.add(b), "[" + a + "] + [" + b + "]");
    }

    @Test
    public void testAddHigher() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly(new Rational(0), new Rational(1, 2), new Rational(-1, 2));
        Poly y = new Poly(new Rational(1), new Rational(5, 2));
        assertEquals(y, a.add(b), "[" + a + "] + [" + b + "]");
    }

    @Test
    public void testMulZero() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly(new Rational(0));
        assertEquals(b, a.mul(b), "[" + a + "] * 0");
    }

    @Test
    public void testMulConst() {
        Poly a = new Poly(new Rational(1), new Rational(2), new Rational(1, 2));
        Poly b = new Poly(new Rational(1, 2));
        Poly y = new Poly(new Rational(1, 2), new Rational(1), new Rational(1, 4));
        assertEquals(y, a.mul(b), "[" + a + "] * [" + b + "]");
    }

    @Test
    public void testMulSquare() {
        Poly a = new Poly(new Rational(3), new Rational(-2));
        Poly y = new Poly(new Rational(9), new Rational(-12), new Rational(4));
        assertEquals(y, a.mul(a), "[" + a + "]^2");
    }

    @Test
    public void testEval() {
        Poly p = new Poly(new Rational(-3), new Rational(-1), new Rational(2));
        Rational x = new Rational(-2);
        Rational y = new Rational(7);
        assertEquals(y, p.eval(x), "[" + p + "](" + x + ")");
    }
}
