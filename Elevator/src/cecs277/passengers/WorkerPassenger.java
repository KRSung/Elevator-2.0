package cecs277.passengers;

import cecs277.elevators.Elevator;
import cecs277.events.PassengerNextDestinationEvent;

import cecs277.Simulation;
import java.util.ArrayList;
import java.util.List;

/**
 * A WorkerPassenger visits many floors in succession. They have a list of destination floors and a list of durations,
 * each duration corresponding to the time they "disappear" after reaching each of the destination floors.
 */
public class WorkerPassenger extends Passenger {
	// DONE: add fields for the list of destination floors, and the list of duration amounts.
	private ArrayList<Integer> mDestinations;
	private ArrayList<Long> mDurations;

	public WorkerPassenger(List<Integer> destinations, List<Long> durations) {
		super();
	
		// DONE: finish the constructor.
		this.mDestinations = (ArrayList<Integer>) destinations;
		this.mDurations = (ArrayList<Long>) durations;
	}

	// DONE: implement this method. Return the current destination, which is the first element of the destinations list.
	@Override
	public int getDestination() {
		return mDestinations.get(0);
	}
	
	// DONE: implement this template method variant. A Worker will only join an elevator with at most 3 people on it.
	@Override
	protected boolean willBoardElevator(Elevator elevator) {
		if (elevator.getPassengerCount() > 3){
			return false;
		} else {
			return true;
		}
	}
	
	/*
	 DONE: implement this template method variant, which is called when the worker is leaving the elevator it
	 is on. A Worker that is departing on floor 1 just leaves the building, printing a message to System.out.
	 A Worker that is departing on any other floor removes the first destination in their list, and then schedules a
	 PassengerNextDestinationEvent to occur when they are supposed to "reappear" (the first element of the durations list,
	 which is also removed.)
	*/
	@Override
	protected void leavingElevator(Elevator elevator) {

		//FIXME we probably have to remove the passenger as an observer of the elevator when the get out to the floor so i did that
		mDestinations.remove(0);
		elevator.removeObserver(this);
		elevator.removePassenger(this);
		if (elevator.getCurrentFloor().getNumber() == 1){
			System.out.println("Worker " + getId() + " is leaving the building.");
		} else {
			Simulation s = elevator.getBuilding().getSimulation();
			//FIXME i think we need to add the sim time to the duration
			PassengerNextDestinationEvent ev = new PassengerNextDestinationEvent(s.currentTime() + mDurations.get(0), this, elevator.getCurrentFloor());
			mDurations.remove(0);
			s.scheduleEvent(ev);
		}
	}
	
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// Don't care.
	}
	
	// DONE: return "Worker heading to floor {destination}", replacing {destination} with the first destination floor number.
	@Override
	public String toString() {
		return "Worker " + this.getId() + " heading to floor " + getDestination();
	}
	
}
