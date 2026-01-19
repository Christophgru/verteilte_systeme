package uulm.in.vs.ex6;

import java.math.BigInteger;

public class TotallyOrderedTimestamp implements Comparable<TotallyOrderedTimestamp> {
    private final BigInteger time;
    private final long PID;
    private final long seq;

    public TotallyOrderedTimestamp(long time, long PID, long seq) {
        this.time=new BigInteger(String.valueOf(time));
        this.PID=PID;
        this.seq=seq;
    }

    @Override
    public int compareTo(TotallyOrderedTimestamp other) {
        int cmp = this.time.compareTo( other.time);
        if (cmp != 0) {
            return cmp;
        }
        cmp= Long.compare(this.PID, other.get_PID());
        if (cmp != 0) return cmp;

        return Long.compare(this.seq, other.seq);
    }

    public BigInteger asBigInteger() {
        BigInteger p = BigInteger.valueOf(PID - Long.MIN_VALUE); // make non-negative
        BigInteger s = BigInteger.valueOf(seq);

        return time.shiftLeft(64).add(p.shiftLeft(64)).add(s);

    }

    public long getTimestamp() {
        return time.longValue();
    }
    public long get_PID(){
        return this.PID;
    }
}
