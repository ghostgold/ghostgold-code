package nachos.threads;

import nachos.machine.Lib;
import nachos.machine.Machine;

import java.util.Iterator;
import java.util.LinkedList;
/**
 * A scheduler that chooses threads based on their priorities.
 * 
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the thread
 * that has been waiting longest.
 * 
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has the
 * potential to starve a thread if there's always a thread waiting with higher
 * priority.
 * 
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 * 
	 * @param transferPriority
	 *            <tt>true</tt> if this queue should transfer priority from
	 *            waiting threads to the owning thread.
	 * @return a new priority thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= getPriorityMinimum()
				&& priority <= getPriorityMaximum());
		
		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == getPriorityMaximum())
			return false;

		setPriority(thread, priority + 1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == getPriorityMinimum())
			return false;

		setPriority(thread, priority - 1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;

	/**
	 * Return the scheduling state of the specified thread.
	 * 
	 * @param thread
	 *            the thread whose scheduling state to return.
	 * @return the scheduling state of the specified thread.
	 */

	public int getPriorityMaximum() {
		return priorityMaximum;
	}
	
	public int getPriorityMinimum() {
		return priorityMinimum;
	}
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	
	private static int seqnum = 0;	
	protected class PriorityQueue extends ThreadQueue {
		int id;
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
			id = seqnum++;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			waitQueue.add(getThreadState(thread));
			getThreadState(thread).setObserver(this);
			Lib.debug('p',thread + "wait on queue  " + id);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			Lib.debug('p', thread + "acquire queue " + id);
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			ThreadState result = this.nextCandidate;
			if (observer != null) 
				observer.unRegister(this);

			if (result == null) 
				return null;
			waitQueue.remove(result);
			result.setObserver(null);
			this.nextCandidate = this.pickNextThread();

			result.acquire(this);
			Lib.debug('p', result.thread + "leave queue " + id);
			return result.thread;
		}

		
		
		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 * 
		 * @return the next thread that <tt>nextThread()</tt> would return.
		 */
		protected ThreadState pickNextThread() {
			int priority = priorityMinimum - 1;
			ThreadState candidate = null;
			for (ThreadState t: waitQueue) {
				if (t.getEffectivePriority() > priority) {
					priority = t.getEffectivePriority();
					candidate = t;
				}
			}
			return candidate;
		}

		
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			for (Iterator<ThreadState> i = waitQueue.iterator(); i.hasNext();)
				System.out.print(i.next().thread + " ");
		}

		
		int getDonation() {
			if(!transferPriority)
				return priorityMinimum;
			if (nextCandidate != null)
				return nextCandidate.getEffectivePriority();
			else 
				return priorityMinimum;
		}
		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
		
		ThreadState observer;
		ThreadState nextCandidate;
		LinkedList<ThreadState> waitQueue = new LinkedList<ThreadState>();
		public void setObserver(ThreadState threadState) {
			this.observer = threadState;
			if (observer != null)
				observer.update();
		}

		public void update() {
			this.nextCandidate = pickNextThread();
			
			if (transferPriority && observer != null)
				observer.update();
			
		}
	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue it's
	 * waiting for, if any.
	 * 
	 * @see nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 * 
		 * @param thread
		 *            the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;
			donation = priorityMinimum;
			setPriority(priorityDefault);
		}
		
		/**
		 * Observer Pattern for ThreadState observe Queues
		 * Inform the thread a queue owned by it has changed
		 */
		public void update() {
			donation = priorityMinimum;
			for (PriorityQueue queue: servents) {
				if (queue.getDonation() > donation)
					donation = queue.getDonation();
			}
			
			if (observer != null)
				observer.update();
			
		}

		/**
		 * Observer Pattern for Queue observe Thread
		 * After thread change, observer.update() should be called
		 * @param priorityQueue
		 * 	the Observer
		 */
		public void setObserver(PriorityQueue priorityQueue) {
			this.observer = priorityQueue;
			if (observer != null)
				observer.update();
		}

		/**
		 * Return the priority of the associated thread.
		 * 
		 * @return the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 * 
		 * @return the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			return (priority > donation)? priority:donation;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 * 
		 * @param priority
		 *            the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;
			
			if (observer != null)
				observer.update();

		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the resource
		 * guarded by <tt>waitQueue</tt>. This method is only called if the
		 * associated thread cannot immediately obtain access.
		 * 
		 * @param waitQueue
		 *            the queue that the associated thread is now waiting on.
		 * 
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			
		}


		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 * 
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			if (!waitQueue.transferPriority)
				return;
			servents.add(waitQueue);
			waitQueue.setObserver(this);
		}
		
		/**
		 * Observer Pattern for Thread observe Queues
		 * should be called when a queue is no longer owned by this thread 
		 * @param waitQueue
		 * the queue no longer belongs to the thread
		 */
		void unRegister(PriorityQueue waitQueue) {
			servents.remove(waitQueue);
			update();
			Lib.debug('p', thread + "(" + priority +":" + donation + ")" + " release queue " + waitQueue.id );
		}

		/** The thread with which this object is associated. */
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		
		protected int donation;
		
		protected PriorityQueue observer;
		
		protected LinkedList<PriorityQueue> servents = new LinkedList<PriorityQueue>(); 
		
	}
}
