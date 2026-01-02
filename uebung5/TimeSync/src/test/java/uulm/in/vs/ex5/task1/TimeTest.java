package uulm.in.vs.ex5.task1;

import org.junit.jupiter.api.Test;
import org.zeromq.ZContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Clock and SynchronizedClock.
 */
public class TimeTest {

    @Test
    void synchronizedClockConverges() throws InterruptedException {
        ZContext context = new ZContext();
        String host = "vs.lxd-vs.uni-ulm.de:3322";

        // Clock that synchronizes at the start
        Clock correct = new SynchronizedClock(context, host, 5);

        // Clock with wrong initial time
        Clock wrong = new SynchronizedClock(
                context, host, 5, correct.getTime() + 2000
        );

        long initialDiff = Math.abs(correct.getTime() - wrong.getTime());

        // wait a few sync cycles
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
        }

        long finalDiff = Math.abs(correct.getTime() - wrong.getTime());

        // clocks should get closer over time
        assertTrue(
                finalDiff < initialDiff,
                "Clock difference should decrease after synchronization"
        );

        context.close();
    }
}
