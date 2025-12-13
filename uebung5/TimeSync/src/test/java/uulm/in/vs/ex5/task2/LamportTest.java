package uulm.in.vs.ex5.task2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LamportTest {

    @Test
    void getTime() {
        LamportClock a,b;
        a = new LamportClock();
        b = new LamportClock(12);

        assertEquals(0, a.getTime());
        assertEquals(12, b.getTime());
    }

    @Test
    void increment() {
        LamportClock a,b;
        a = new LamportClock();
        b = new LamportClock(12);

        assertEquals(1, a.increment());
        assertEquals(13, b.increment());
    }

    @Test
    void merge() {
        LamportClock a,b,c;
        a = new LamportClock();
        b = new LamportClock(6);
        c = new LamportClock(12);

        assertEquals(7, a.merge(b));
        assertEquals(13, c.merge(b));
        assertEquals(7, b.merge(b));

        a = new LamportClock();
        b = new LamportClock(6);
        c = new LamportClock(12);
        assertEquals(7, LamportClock.merge(a,b).getTime());
        assertEquals(13, LamportClock.merge(c,b).getTime());
        assertEquals(7, LamportClock.merge(b,b).getTime());
    }

    @Test
    void compare() {
        LamportClock a,b,c;
        a = new LamportClock();
        b = new LamportClock(6);
        c = new LamportClock(12);

        assertEquals(7, a.merge(b));
        assertEquals(13, c.merge(b));
        assertEquals(7, b.merge(b));
    }

    @Test
    void testEquals() {
        LamportClock a,b,c;
        a = new LamportClock();
        b = new LamportClock(0);
        c = new LamportClock(12);

        assertTrue(a.equals(a));
        assertTrue(c.equals(c));
        assertTrue(a.equals(b));

        assertFalse(a.equals(c));
        assertFalse(c.equals(a));
    }

    @Test
    void compareTo() {
        LamportClock a,b,c;
        a = new LamportClock();
        b = new LamportClock(0);
        c = new LamportClock(12);

        assertEquals(Long.compare(a.getTime(), a.getTime()), a.compareTo(a));
        assertEquals(Long.compare(a.getTime(), b.getTime()), a.compareTo(b));
        assertEquals(Long.compare(a.getTime(), c.getTime()), a.compareTo(c));
        assertEquals(Long.compare(c.getTime(), a.getTime()), c.compareTo(a));
    }
}