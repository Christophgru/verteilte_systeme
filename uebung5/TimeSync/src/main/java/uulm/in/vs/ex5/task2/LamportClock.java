package uulm.in.vs.ex5.task2;

public class LamportClock implements Comparable<LamportClock>{
        private long time;

    public LamportClock() {
        time=0;
    }

    public LamportClock(long init) {
        time=(int)init;
    }

    public long getTime() {
        return time;
    }

    /**
    * Also returns incremented time.
    */
    public long increment() {
        time+=1;
        return time;
    }

    public long merge(LamportClock b) {
        time = Math.max(this.time, b.time) + 1;
        return time;
    }

    public static LamportClock merge(LamportClock a, LamportClock b) {
        LamportClock c= new LamportClock( Math.max(a.time, b.time) + 1);
        return c;
    }

    public static int compare(LamportClock a, LamportClock b) {
        return Long.compare(a.time, b.time);
    }

    public boolean equals(LamportClock b) {
        return this.time == b.time;
    }

    @Override
    public int compareTo(LamportClock l) {
        return Long.compare(this.time, l.time);
    }
}
