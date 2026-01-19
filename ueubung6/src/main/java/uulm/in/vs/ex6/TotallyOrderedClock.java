package uulm.in.vs.ex6;

import java.math.BigInteger;

public class TotallyOrderedClock {
    private long PID;
    private long seq = 0;
    public TotallyOrderedClock(long PID) {
        this.PID = PID;
    }

    public TotallyOrderedTimestamp createTimestamp() throws IllegalArgumentException  {
        return createTimestamp(System.currentTimeMillis());
    }

    public TotallyOrderedTimestamp createTimestamp(long time) throws IllegalArgumentException {
    	if(time>System.currentTimeMillis())throw new IllegalArgumentException("passed timestamp too big");
        return new TotallyOrderedTimestamp(time, PID, ++seq);
    }
}
