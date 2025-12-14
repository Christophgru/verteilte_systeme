package uulm.in.vs.ex5.task2;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VectorTest {
    @org.junit.jupiter.api.Test
    void increment() {
        VectorClock a,b,c;
        a = new VectorClock(Arrays.asList(0L,4L,8L),0);
        b = new VectorClock(Arrays.asList(0L,4L,8L),1);
        c = new VectorClock(Arrays.asList(0L,4L,8L),2);

        assertEquals(1,a.increment());
        assertEquals(5,b.increment());
        assertEquals(9,c.increment());
    }

    @org.junit.jupiter.api.Test
    void testGetTime() {
        VectorClock a,b,c;
        a = new VectorClock(Arrays.asList(0L,4L,8L),0);
        b = new VectorClock(Arrays.asList(0L,4L,8L),1);
        c = new VectorClock(Arrays.asList(0L,4L,8L),2);

        long[] test = {0L, 4L, 8L};

        assertEquals(0,a.getTime(0));
        assertEquals(4,b.getTime(1));
        assertEquals(8,c.getTime(2));

        assertTrue(Arrays.equals(a.getTime(),c.getTime()));
        assertTrue(Arrays.equals(test ,c.getTime()));
    }

    @org.junit.jupiter.api.Test
    void merge() {
        VectorClock a,b,x;
        a = new VectorClock(Arrays.asList(0L,4L,8L),0);
        b = new VectorClock(Arrays.asList(1L,3L,5L),0);

        long[] c = {2L,4L,8L};
        x = new VectorClock(5,0);

        assertEquals(2, b.merge(a));
        assertTrue(Arrays.equals(c, b.getTime()));
        assertThrows(IllegalArgumentException.class, () -> {x.merge(a);});
    }

    @org.junit.jupiter.api.Test
    void size() {
        VectorClock a,b;
        a = new VectorClock(3,2);
        b = new VectorClock(Arrays.asList(2L,5L,8L),2);

        assertEquals(3, a.size());
        assertEquals(3, b.size());
    }

    @org.junit.jupiter.api.Test
    void geq() {
        VectorClock a,c,n1,n2,x;
        a = new VectorClock(Arrays.asList(0L,4L,8L),0);
        c = new VectorClock(Arrays.asList(2L,5L,8L),2);

        n1 = new VectorClock(Arrays.asList(0L,0L,0L),2);
        n2 = new VectorClock(3,0);

        x = new VectorClock(5,0);

        assertTrue(n1.geq(n2) && n2.geq(n1));
        assertTrue(c.geq(a));
        
        assertFalse(a.geq(c));
        
        assertThrows(IllegalArgumentException.class, () -> {x.geq(a);});
    }

    @org.junit.jupiter.api.Test
    void compare() {
        VectorClock a,b,c,n1,n2,x;
        a = new VectorClock(Arrays.asList(0L,4L,8L),0);
        b = new VectorClock(Arrays.asList(0L,4L,8L),1);
        c = new VectorClock(Arrays.asList(2L,5L,8L),2);

        n1 = new VectorClock(Arrays.asList(0L,0L,0L),2);
        n2 = new VectorClock(3,0);

        x = new VectorClock(Arrays.asList(8L,4L,0L),2);

        assertEquals(0, VectorClock.compare(a,b).get());
        assertEquals(0, VectorClock.compare(n1,n2).get());

        assertTrue( VectorClock.compare(c,a).get()>0);
        assertTrue( VectorClock.compare(a,c).get()<0);
        assertTrue( VectorClock.compare(a,x).isEmpty());
        assertTrue( VectorClock.compare(a,c).isPresent());
    }

    @org.junit.jupiter.api.Test
    void testEquals() {
        VectorClock a,b,n1,n2,x;
        a = new VectorClock(Arrays.asList(0L,4L,8L),0);
        b = new VectorClock(Arrays.asList(0L,4L,8L),2);

        n1 = new VectorClock(Arrays.asList(0L,0L,0L),2);
        n2 = new VectorClock(3,0);

        x = new VectorClock(Arrays.asList(8L,4L,0L),2);

        assertTrue( a.equals(b));
        assertTrue( n2.equals(n1) && n1.equals(n2));

        assertFalse( a.equals(x));
    }
}