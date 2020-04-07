package cecs277.passengers;

import cecs277.elevators.Elevator;
import cecs277.events.PassengerNextDestinationEvent;

import java.util.List;

/**
 * A WorkerPassenger visits many floors in succession. They have a list of destination floors and a list of durations,
 * each duration corresponding to the time they "disappear" after reaching each of the destination floors.
 */
public class WorkerPassenger extends Passenger {
	// TODO: add fields for the list of destination floors, and the list of duration amounts.
	
	public WorkerPassenger(List<Integer> destinations, List<Long> durations) {
		super();
	
		// TODO: finish the constructor.
	}
	// TODO: implement this method. Return the current destination, which is the first element of the destinations list.
	@Override
	public int getDestination() {
		return -1;
	}
	
	// TODO: implement this template method variant. A Worker will only join an elevator with at most 3 people on it.
	@Override
	protected boolean willBoardElevator(Elevator elevator) {
		return false;
	}
	
	/*
	 TODO: implement this template method variant, which is called when the worker is leaving the elevator it
	 is on. A Worker that is departing on floor 1 just leaves the building, printing a message to System.out.
	 A Worker that is departing on any other floor removes the first destination in their list, and then schedules a
	 PassengerNextDestinationEvent to occur when they are supposed to "reappear" (the first element of the durations list,
	 which is also removed.)
	*/
	@Override
	protected void leavingElevator(Elevator elevator) {
	
	}
	
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// Don't care.
	}
	
	// TODO: return "Worker heading to floor {destination}", replacing {destination} with the first destination floor number.
	@Override
	public String toString() {
		return "";
	}
	
}
