package nachos.threads;

import nachos.machine.Lib;

/**
 * A scheduler that chooses threads using a lottery.
 * 
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 * 
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 * 
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking the
 * maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
	/**
	 * Allocate a new lottery scheduler.
	 */
	public LotteryScheduler() {
	}

	/**
	 * Allocate a new lottery thread queue.
	 * 
	 * @param transferPriority
	 *            <tt>true</tt> if this queue should transfer tickets from
	 *            waiting threads to the owning thread.
	 * @return a new lottery thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new LotteryQueue(transferPriority); 

	}
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new LotteryState(thread);

		return (LotteryState) thread.schedulingState;
	}

	@Override 
	public int getPriorityMaximum() {
		return priorityMaximum;
	}
	public int getPriorityMinimum() {
		return priorityMinimum;
	}
	
	protected class LotteryQueue extends PriorityQueue {
		public LotteryQueue (boolean tranferPriority) {
			super(tranferPriority);
		}
		
		@Override
		public KThread nextThread() {
			if (observer != null) 
				observer.unRegister(this);

			if (waitQueue.isEmpty())
				return null;
			int lottery = Lib.random(cachedDonation);
			ThreadState next = null;
			

			for (ThreadState t: waitQueue) {
				if (lottery < t.getEffectivePriority()) {
					next = t;
					break;
				}
				lottery -= t.getEffectivePriority();
			}
			
			if (next == null) 
				return null;
			waitQueue.remove(next);
			next.setObserver(null);
			this.cachedDonation = this.calcDonation();
			next.acquire(this);
			Lib.debug('p', next.thread + "leave queue " + id);
			return next.thread;
		}
		
		@Override
		public int getDonation() {
			if (transferPriority)
				return cachedDonation;
			else 
				return 0;
		}
		
		@Override 
		public void update() {
			cachedDonation = calcDonation();
			if (transferPriority && observer != null) {
				observer.update();
			}
		}
		
		int calcDonation() {
			int temp = 0;
			for(ThreadState t: waitQueue) {
				temp += t.getEffectivePriority();
			} 
			return temp;
		}	
		
		int cachedDonation = 0;
	}
	
	protected class LotteryState extends ThreadState {
		public LotteryState (KThread thread) {
			super(thread);
			this.donation = 0;
		}
		
		@Override 
		public void update() {
			donation = 0;
			for (PriorityQueue queue: servents) {
				donation += queue.getDonation();
			}
			if (observer != null)
				observer.update();
		}
		@Override 
		public int getEffectivePriority() {
			return priority + donation;
		}
	}
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = Integer.MAX_VALUE;

}
