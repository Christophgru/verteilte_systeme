package uulm.in.vs.ex5.task1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author David Mödinger
 *
 */
public class BaseClock implements Runnable, Clock {
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong counter;
    private volatile ScheduledFuture<?> timeFuture;
    private volatile long tickrate;

    public BaseClock() {
        this(0);
    }

    public BaseClock(long start) {
        this(start,100);
    }

    public BaseClock(long start, int tickrate) {
        this.counter = new AtomicLong(start);
        this.tickrate = tickrate;
        this.timeFuture = this.timer.scheduleAtFixedRate(this, tickrate, tickrate, TimeUnit.MILLISECONDS);
    }

    private synchronized void updateTickrate(long speed) {
        // Limit the range of the tickrate
        tickrate = speed < 10 ? 10 : speed;
        tickrate = speed > 1000 ? 1000 : speed;

        // Update Clock
        long delay = timeFuture.getDelay(TimeUnit.MILLISECONDS);
        timeFuture.cancel(true);
        this.timeFuture = this.timer.scheduleAtFixedRate(this, delay, tickrate, TimeUnit.MILLISECONDS);
    }

    /*
    * Methods to gradually decrease/increase the tick rate
    * */
    public void decreaseSpeed() {
        updateTickrate((long)(1.1*tickrate));
    }

    public void increaseSpeed() {
        updateTickrate((long)(0.9*tickrate));
    }

    /*
     * Methods to set new absolute tick rates
     * */
    public void setVeryFastSpeed() {
        updateTickrate(10);
    }

    public void setFastSpeed() {
        updateTickrate(50);
    }

    public void setNormalSpeed() {
        updateTickrate(100);
    }

    public void setSlowSpeed() {
        updateTickrate(200);
    }

    public void setVerySlowSpeed() {
        updateTickrate(1000);
    }

    public void setTimeToFuture(long future) {
        // Only allow future times
        if(future > counter.get())
            counter.set(future);
    }

    public long getTickrate() {
        return tickrate;
    }

    public long getTime() {
        return counter.get();
    }

    public void stop() {
        timeFuture.cancel(true);
    }

    public void restart() {
        updateTickrate(tickrate);
    }

    // Use shutdown once you finished using your Clock.
    public void shutdown() {
        timer.shutdown();
    }

    @Override
    public void run() {
        counter.addAndGet(100);
    }
}
