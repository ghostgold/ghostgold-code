package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	static BoatGrader bg;
	static Lock lock;
	static Condition adultOnOahu;
	static Condition childOnMolokai;
	static Condition waitingBoat;
	static Condition waitingPassenger;
	
	static int peopleOnOahu;
	static int peopleOnBoat;
	static int adultNum;
	
	static boolean boatOnOahu;
	
	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// begin(1, 2, b);

		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		// begin(3, 3, b);
	}

	public static void begin(int adults, int children, BoatGrader b) {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		lock = new Lock();
		adultOnOahu = new Condition(lock);
		childOnMolokai = new Condition(lock);
		waitingBoat = new Condition(lock);
		waitingPassenger = new Condition(lock);
		
		peopleOnOahu = adults + children;
		peopleOnBoat = 0;
		adultNum = 0;
		
		boatOnOahu = true;
		
		Runnable child = new Runnable() {
			public void run() {
				ChildItinerary();
			}
		};
		Runnable adult = new Runnable() {
			public void run() {
				AdultItinerary();
			}
			
		};
		for(int i = 0; i < adults; i++) {
			KThread t = new KThread(adult);
			t.fork();
		}
		for(int i = 0; i < children; i++) {
			KThread t = new KThread(child);
			t.fork();
		}
		while(peopleOnOahu != 0)
			KThread.yield();
//		KThread t = new KThread(r);
//		t.setName("Sample Boat Thread");
//		t.fork();

	}

	static void AdultItinerary() {
		/*
		 * This is where you should put your solutions. Make calls to the
		 * BoatGrader to show that it is synchronized. For example:
		 * bg.AdultRowToMolokai(); indicates that an adult has rowed the boat
		 * across to Molokai
		 */
		lock.acquire();
		while (adultNum == 0)
			adultOnOahu.sleep();
		
		adultNum--;
		
		while (peopleOnBoat > 0 || !boatOnOahu) {
			waitingBoat.sleep();
		}
		
		peopleOnBoat = 2;
		peopleOnOahu--;
		boatOnOahu = false;
		peopleOnBoat = 0;		
		bg.AdultRowToMolokai();
		childOnMolokai.wake();
		lock.release();
	}

	static void ChildItinerary() {
		lock.acquire();
		while (true) {
			while (peopleOnBoat > 1 || !boatOnOahu) {
				waitingBoat.sleep();
			}
			
			if (peopleOnBoat == 0) {
				peopleOnBoat++;
				peopleOnOahu--;
				waitingBoat.wakeAll();
				waitingPassenger.sleep();
				boatOnOahu = true;
				peopleOnBoat--;
				bg.ChildRowToOahu();
				waitingBoat.wake();
				waitingBoat.sleep();
			}

			else {
				peopleOnBoat++;
				if (adultNum++ == 0)
					adultOnOahu.wake();
				peopleOnOahu--;
				if (peopleOnOahu > 0)
					waitingPassenger.wake();
				boatOnOahu = false;
				peopleOnBoat--;
				bg.ChildRowToMolokai();
				bg.ChildRideToMolokai();
				childOnMolokai.sleep();
				while (peopleOnBoat > 0 || boatOnOahu) {
					childOnMolokai.sleep();
				}
				peopleOnBoat++;
				bg.ChildRowToOahu();
				boatOnOahu = true;
				peopleOnBoat--;
				waitingBoat.wake();
				waitingBoat.sleep();
			}
		}
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out
				.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

}
