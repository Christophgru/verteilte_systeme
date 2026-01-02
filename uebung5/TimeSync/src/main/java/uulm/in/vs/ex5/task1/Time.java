package uulm.in.vs.ex5.task1;

import org.zeromq.ZContext;

/**
 * @author David Mödinger
 *
 */
public class Time {

    /**
     * Demonstrate Clock and SynchronizedClock.
     */
    public static void main(String[] args) {
        ZContext context = new ZContext();
        String host = "vs.lxd-vs.uni-ulm.de:3322";

        // Clock that synchronizes at the start
        Clock initial = new SynchronizedClock(context, host, 5);

        // Clock with initial time
        Clock wrong = new SynchronizedClock(context, host, 5, initial.getTime()+2000);

        Util.logTime(
                String.format("%10s\t%10s\t%10s","Wrong","Correct","Difference")
        );

        while(!Thread.currentThread().isInterrupted()) {
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                // This is exactly what we want, isn't it?
            }

            Util.logTime(
                    String.format("%10d\t%10d\t%10d",wrong.getTime(),initial.getTime(),(initial.getTime()-wrong.getTime()))
            );
        }

    }
}
