package cecs277.events;

import cecs277.Simulation;
import cecs277.buildings.Floor;
import cecs277.passengers.Passenger;

/**
 * A simulation event that adds an existing passenger to a given floor, as if they have finished with their
 * task on that floor and are now waiting for an elevator to go to their next destination.
 */
public class PassengerNextDestinationEvent extends SimulationEvent {
	private Passenger mPassenger;
	private Floor mStartingFloor;
	
	public PassengerNextDestinationEvent(long scheduledTime, Passenger passenger, Floor startingFloor) {
		super(scheduledTime);
		mPassenger = passenger;
		mStartingFloor = startingFloor;
	}
	
	
	@Override
	public void execute(Simulation sim) {
		mPassenger.setState(Passenger.PassengerState.WAITING_ON_FLOOR);
		mStartingFloor.addWaitingPassenger(mPassenger);
	}
	
	@Override
	public String toString() {
		return super.toString() + "Passenger " + mPassenger.getId() + " joining floor " + mStartingFloor.getNumber() +
		 ", heading to floor " + mPassenger.getDestination();
	}
}
