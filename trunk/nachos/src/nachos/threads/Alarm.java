package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	static PriorityQueue<KThreadWithTime> sleepers = new PriorityQueue<KThreadWithTime>();
	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		boolean intStatus = Machine.interrupt().disable();
		
		while (true) {
			KThreadWithTime t = sleepers.peek();
			if (t != null && t.time <= Machine.timer().getTime()) {
				t.thread.ready();
				sleepers.poll();
			}
			else break;
		}
		
		Machine.interrupt().restore(intStatus);
		
		KThread.yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x
	 *            the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		boolean intStatus = Machine.interrupt().disable();
		
		long wakeTime = Machine.timer().getTime() + x;
		sleepers.add(new KThreadWithTime(KThread.currentThread(), wakeTime));
		if (wakeTime > Machine.timer().getTime())
			KThread.sleep();
		
		Machine.interrupt().restore(intStatus);
	}
	
	class KThreadWithTime implements Comparable<KThreadWithTime>{
		KThread thread;
		long time;
		public KThreadWithTime(KThread thread, long time) {
			this.thread = thread;
			this.time = time;
		}
		public int compareTo(KThreadWithTime twt) {
			if (this.time < twt.time)
				return -1;
			if (this.time == twt.time)
				return 0;
			return 1;
		}
	}
}
