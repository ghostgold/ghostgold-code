package nachos.threads;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	
	Lock lock;
	Condition waitingSpeaker;
	Condition waitingListener;
	Condition waitingClean;
	int waitS;
	int waitL;
	int accS;
	int accL;
	int message;
	int empty;
	int waitE;
//	Condition waitingResponce;
	public Communicator() {
		lock = new Lock();
		waitingSpeaker = new Condition(lock);
		waitingListener = new Condition(lock);
		waitingClean = new Condition(lock);
		waitS = 0;
		waitL = 0;
		waitE = 0;
		accS = 0;
		accL = 0;
		empty = 1;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word
	 *            the integer to transfer.
	 */
	public void speak(int word) {
		
		lock.acquire();
		if (waitL == 0){
			waitS++;
			waitingSpeaker.sleep();
		}
		while (empty == 0) {
			waitingClean.sleep();
		}
		empty--;
		message = word;
		waitL--;		
		waitingListener.wake();
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		lock.acquire();
		if (waitS > 0){
			waitS--;
			waitingSpeaker.wake();
		}
		waitL++;        
		waitingListener.sleep();
		int save = message;
		empty++;
		waitingClean.wake();
		lock.release();
		return save;

	}
}
