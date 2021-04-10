package interpolation;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {
    // Tests equals and hashCode implementation.
    // Assumes that a1 == a2 and a1 != b.
    public static <T> void testEquality(T a1, T a2, T b) {
        assertTrue(a1.equals(a1), "a1 equals a1");
        assertTrue(a1.equals(a2), "a1 equals a2");
        assertTrue(a2.equals(a1), "a2 equals a1");

        assertFalse(a1.equals(null), "a1 not equals null");
        assertFalse(a1.equals(new TestUtils()), "a1 not equals different class");
        assertFalse(a1.equals(b), "a1 not equals b");
        assertFalse(b.equals(a1), "b not equals a1");

        assertEquals(a1.hashCode(), a1.hashCode(), "a1 hashCode equals a1 hashCode");
        assertEquals(a1.hashCode(), a2.hashCode(), "a1 hashCode equals a2 hashCode");
    }
}
