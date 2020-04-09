package cecs277.events;

import cecs277.buildings.Building;
import cecs277.passengers.Passenger;
import cecs277.Simulation;
import cecs277.passengers.VisitorPassenger;
import cecs277.passengers.WorkerPassenger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A simulation event that adds a new random passenger on floor 1, and then schedules the next spawn event.
 */
public class SpawnPassengerEvent extends SimulationEvent {
	private static long SPAWN_MEAN_DURATION = 10_800;
	private static long SPAWN_STDEV_DURATION = 3_600;

	// After executing, will reference the Passenger object that was spawned.
	private Passenger mPassenger;
	private Building mBuilding;
	
	public SpawnPassengerEvent(long scheduledTime, Building building) {
		super(scheduledTime);
		mBuilding = building;
	}
	
	@Override
	public String toString() {
		return super.toString() + "Adding " + mPassenger + " to floor 1.";
	}
	
	@Override
	public void execute(Simulation sim) {
		Random r = mBuilding.getSimulation().getRandom();
		
		// 75% of all passengers are normal Visitors.
		if (r.nextInt(4) <= 2) {
			mPassenger = getVisitor();
		}
		else {
			mPassenger = getWorker();
		}
		mBuilding.getFloor(1).addWaitingPassenger(mPassenger);

		/*
		 DONE: schedule the new SpawnPassengerEvent with the simulation. Construct a new SpawnPassengerEvent
		 with a scheduled time that is X seconds in the future, where X is a uniform random integer from
		 1 to 30 inclusive.
		*/
		mBuilding.getSimulation().scheduleEvent(new SpawnPassengerEvent(r.nextInt(30) + 1, mBuilding));

	}
	
	
	private Passenger getVisitor() {
		/*
		 DONE: construct a VisitorPassenger and return it.
		 The visitor should have a random destination floor that is not floor 1 (generate a random int from 2 to N).
		 The visitor's visit duration should follow a NORMAL (GAUSSIAN) DISTRIBUTION with a mean of 1 hour
		 and a standard deviation of 20 minutes.
		 */
		Random r = mBuilding.getSimulation().getRandom();
		int x = r.nextInt(mBuilding.getFloorCount() - 1) + 2;
		// Look up the documentation for the .nextGaussian() method of the Random class.
		//source: https://www.javamex.com/tutorials/random_numbers/gaussian_distribution_2.shtml
		int y = (int) (r.nextGaussian() * 20 + 60);
		return new VisitorPassenger(x, y);
	}
	
	private Passenger getWorker() {
		/*
		DONE: construct and return a WorkerPassenger. A Worker requires a list of destinations and a list of durations.
		To generate the list of destinations, first generate a random number from 2 to 5 inclusive. Call this "X",
		how many floors the worker will visit before returning to floor 1.
		X times, generate an integer from 2 to N (number of floors) that is NOT THE SAME as the previously-generated floor.
		Add those X integers to a list.
		//TODO this part with the duration may be wrong
		To generate the list of durations, generate X integers using a NORMAL DISTRIBUTION with a mean of 10 minutes
		and a standard deviation of 3 minutes.
		 */
		Random r = mBuilding.getSimulation().getRandom();
		ArrayList<Integer> destinations = new ArrayList<>();
		ArrayList<Long> durations = new ArrayList<>();

		int x = r.nextInt(4) + 2;

		int last = -1, j;
		for (int i = 0; i < x; i++){
			do {
				j = r.nextInt(mBuilding.getFloorCount() - 1) + 2;
			} while (j == last);
			destinations.add(j);
			last = j;
		}
		//adds final floor 1, Im not sure if he wants it here or in visitor/worker class file
		destinations.add(1);

		//TODO i may have not done this right bc look at the private static long variables i didnt use and the other imported stuff not used
		long y;
		for (int i = 0; i < x; i++){
			y = (long) (r.nextGaussian() * 3 + 10);
			durations.add(y);
		}

		return new WorkerPassenger(destinations, durations);
	}
}
