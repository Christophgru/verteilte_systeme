package uulm.in.vs.ex5.task1;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cristian synchronized clock.
 */
public class SynchronizedClock implements Clock {

    private final BaseClock baseClock;
    private final ZMQ.Socket socket;
    private final ScheduledExecutorService scheduler;

    private final int numRequests;

    // last estimated offset: server_estimated_now - local_now
    private volatile long offsetMs = 0;

    // Tuning
    // Synchronisation
    private static final long SYNC_PERIOD_MS = 1000;

    // Do nothing inside this band (tick quantization is 100ms, plus latency noise)
    private static final long DEAD_BAND_MS = 120;

    // Region thresholds (offset magnitude)
    private static final long SMALL_OFFSET_MS = 500;
    private static final long MEDIUM_OFFSET_MS = 2000;

    // Maximum forward step when we are close (prevents overshoot/oscillation)
    private static final long MAX_NEAR_FORWARD_STEP_MS = 100;

    // Medium region: limited forward step, still gentle
    private static final long MAX_MEDIUM_FORWARD_STEP_MS = 200;

    // How many gradual slow-down steps in the medium-ahead region
    private static final int MEDIUM_AHEAD_SLOWDOWN_STEPS = 2;

    public SynchronizedClock(ZContext context, String host, int numRequests) {
        this(context, host, numRequests, 0L, true);
    }

    public SynchronizedClock(ZContext context, String host, int numRequests, long start) {
        this(context, host, numRequests, start, false);
    }

    private SynchronizedClock(ZContext context, String host, int numRequests, long start, boolean syncAtStart) {
        if (numRequests <= 0)
            throw new IllegalArgumentException("numRequests must be > 0");
        this.numRequests = numRequests;

        this.baseClock = new BaseClock(start, 50);

        this.socket = context.createSocket(SocketType.REQ);
        this.socket.connect("tcp://" + host);

        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // do one initial sync (recommended)
        if (syncAtStart) {
            syncOnce();
        }

        // periodic sync
        scheduler.scheduleAtFixedRate(() -> {
            try {
                syncOnce();
            } catch (Exception ignored) {
                // keep running even if server occasionally fails
            }
        }, SYNC_PERIOD_MS, SYNC_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getTime() {
        return baseClock.getTime();
    }

    /** Positive -> local behind. Negative -> local ahead. */
    public long getOffset() {
        return offsetMs;
    }

    private void syncOnce() {
        Sample best = null;

        for (int i = 0; i < numRequests; i++) {
            Sample s = sampleServerTime();
            if (s == null)
                continue;

            if (best == null || s.rtt < best.rtt)
                best = s;
        }

        if (best == null)
            return;

        long oneWay = best.rtt / 2;
        long serverAtReceive = best.serverTime + oneWay;

        long localNow = best.t1;
        long newOffset = serverAtReceive - localNow;
        offsetMs = newOffset;

        // Debug (optional): uncomment to see what's happening
        // Util.logTime("sync: rtt=" + best.rtt + "ms offset=" + newOffset + "ms local="
        // + localNow + " server~=" + serverAtReceive);

        applyAdjustment(newOffset);
    }

    private static final class Sample {
        final long t0;
        final long t1;
        final long rtt;
        final long serverTime;

        Sample(long t0, long t1, long serverTime) {
            this.t0 = t0;
            this.t1 = t1;
            this.rtt = Math.max(0, t1 - t0);
            this.serverTime = serverTime;
        }
    }

    private Sample sampleServerTime() {
        long t0 = baseClock.getTime();

        // Most servers accept an empty request.
        // If yours needs a keyword, replace with:
        // socket.send("TIME".getBytes(StandardCharsets.UTF_8), 0);
        socket.send(new byte[0], 0);

        byte[] reply = socket.recv(0);
        long t1 = baseClock.getTime();
        if (reply == null)
            return null;

        String msg = new String(reply, StandardCharsets.UTF_8).trim();

        // Many teaching servers return a single long in ASCII.
        // If your server returns something else, print msg and adjust parsing.
        try {
            long serverTime = Long.parseLong(msg);
            return new Sample(t0, t1, serverTime);
        } catch (NumberFormatException e) {
            // If parsing fails, log once (optional)
            // Util.logTime("Cannot parse server reply: '" + msg + "'");
            return null;
        }
    }

    private void applyAdjustment(long offset) {
        long abs = Math.abs(offset);

        // 1) Deadband to ignore noise + BaseClock quantization
        if (abs <= DEAD_BAND_MS) {
            baseClock.setNormalSpeed();
            return;
        }

        if (offset > 0) {
            // local clock is behind server

            if (abs <= SMALL_OFFSET_MS) {
                // Close: do NOT jump aggressively. Tiny step + gentle speed-up.
                long now = baseClock.getTime();
                long step = Math.min(offset, MAX_NEAR_FORWARD_STEP_MS);
                baseClock.setTimeToFuture(now + step);
                baseClock.increaseSpeed();
                return;
            }

            if (abs <= MEDIUM_OFFSET_MS) {
                // Medium: bounded step + gentle speed-up
                long now = baseClock.getTime();
                long step = Math.min(offset, MAX_MEDIUM_FORWARD_STEP_MS);
                baseClock.setTimeToFuture(now + step);
                baseClock.increaseSpeed();
                return;
            }

            // Far behind: one-time jump + faster base speed (coarse correction is OK)
            long now = baseClock.getTime();
            baseClock.setTimeToFuture(now + offset);
            baseClock.setFastSpeed();
            return;

        } else {
            // local clock is ahead of server -> NEVER go backwards, only slow down

            if (abs <= SMALL_OFFSET_MS) {
                // Close: gentle slow-down
                baseClock.decreaseSpeed();
                return;
            }

            if (abs <= MEDIUM_OFFSET_MS) {
                // Medium ahead: a couple of gentle slow-down steps
                for (int i = 0; i < MEDIUM_AHEAD_SLOWDOWN_STEPS; i++) {
                    baseClock.decreaseSpeed();
                }
                return;
            }

            // Far ahead: strong slow-down
            baseClock.setVerySlowSpeed();
        }
    }

    // optional cleanup if you ever need it
    public void shutdown() {
        scheduler.shutdownNow();
        baseClock.shutdown();
        socket.close();
    }
}
