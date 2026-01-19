package uulm.in.vs.ex6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotallyOrderedTimestampTest {
    private TotallyOrderedClock clock, clock2;
    private TotallyOrderedTimestamp a1,a2,b1,b2;

    @BeforeEach
    void setUp() {
        clock = new TotallyOrderedClock(5);
        clock2 = new TotallyOrderedClock(10);

        a1 = clock.createTimestamp(150l);
        a2 = clock.createTimestamp(150l);

        b1 = clock2.createTimestamp(150l);
        b2 = clock2.createTimestamp(250l);
    }

    @Test
    void testCompareTo() {
        assertEquals(0, a1.compareTo(a1));
        assertEquals(a2.compareTo(a1), -a1.compareTo(a2));

        assertTrue(a2.compareTo(a1) > 0);
        assertTrue(b1.compareTo(a1) > 0);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a1.compareTo(b1) < 0);
        assertTrue(b2.compareTo(b1) > 0);
    }

    @Test
    void testAsBigInteger() {
        var BIa1 = a1.asBigInteger();
        var BIa2 = a2.asBigInteger();
        var BIb1 = b1.asBigInteger();

        assertEquals(0, BIa1.compareTo(BIa1));
        assertEquals(BIa2.compareTo(BIa1), -BIa1.compareTo(BIa2));

        assertTrue(BIa2.compareTo(BIa1) > 0);
        assertTrue(BIb1.compareTo(BIa1) > 0);
        assertTrue(BIa1.compareTo(BIa2) < 0);
        assertTrue(BIa1.compareTo(BIb1) < 0);
    }

    @Test
    void testGetTimestamp() {
        assertEquals(150l, a1.getTimestamp());
        assertEquals(a1.getTimestamp(),a2.getTimestamp());
    }
}