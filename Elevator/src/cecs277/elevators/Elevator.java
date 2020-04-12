package cecs277.elevators;

import cecs277.Simulation;
import cecs277.buildings.Building;
import cecs277.buildings.Floor;
import cecs277.buildings.FloorObserver;
import cecs277.events.ElevatorStateEvent;
import cecs277.passengers.Passenger;

import java.util.*;
import java.util.stream.Collectors;

public class Elevator implements FloorObserver {
	
	public enum ElevatorState {
		IDLE_STATE,
		DOORS_OPENING,
		DOORS_CLOSING,
		DOORS_OPEN,
		ACCELERATING,
		DECELERATING,
		MOVING
	}
	
	public enum Direction {
		NOT_MOVING,
		MOVING_UP,
		MOVING_DOWN
	}
	
	
	private int mNumber;
	private Building mBuilding;

	private ElevatorState mCurrentState = ElevatorState.IDLE_STATE;
	private Direction mCurrentDirection = Direction.NOT_MOVING;
	private Floor mCurrentFloor;
	private List<Passenger> mPassengers = new ArrayList<>();
	
	private List<ElevatorObserver> mObservers = new ArrayList<>();
	
	// Done: declare a field to keep track of which floors have been requested by passengers.
	private Set<Integer> mRequestedFloors = new HashSet<Integer>();
	
	
	public Elevator(int number, Building bld) {
		mNumber = number;
		mBuilding = bld;
		mCurrentFloor = bld.getFloor(1);
		
		scheduleStateChange(ElevatorState.IDLE_STATE, 0);
	}
	
	/**
	 * Helper method to schedule a state change in a given number of seconds from now.
	 */
	private void scheduleStateChange(ElevatorState state, long timeFromNow) {
		Simulation sim = mBuilding.getSimulation();
		sim.scheduleEvent(new ElevatorStateEvent(sim.currentTime() + timeFromNow, state, this));
	}
	
	/**
	 * Adds the given passenger to the elevator's list of passengers, and requests the passenger's destination floor.
	 */
	public void addPassenger(Passenger passenger) {
		// Done: add the passenger's destination to the set of requested floors.
		mPassengers.add(passenger);
		mRequestedFloors.add(passenger.getDestination());
	}
	
	public void removePassenger(Passenger passenger) {
		mPassengers.remove(passenger);
	}
	
	
	/**
	 * Schedules the elevator's next state change based on its current state.
	 */
	public void tick() {
		// TODO: port the logic of your state changes from Project 1, accounting for the adjustments in the spec.
		// TODO: State changes are no longer immediate; they are scheduled using scheduleStateChange().
		ArrayList<Integer> tempList = mBuilding.getFloor(mCurrentFloor);
		switch (mCurrentState) {
			case IDLE_STATE:
				if (!tempList.isEmpty()) {
					//checks to see if anyone is waiting for elevator on current floor
					mCurrentState = ElevatorState.DOORS_OPENING;
				}
				return;
			case DOORS_OPENING:
				if (mPassengers.contains(mCurrentFloor)) {
					//checks to see if anyone in elevator wants to get off on current floor
					mCurrentState = ElevatorState.UNLOADING_PASSENGERS;
				}
				else {
					mCurrentState = ElevatorState.LOADING_PASSENGERS;
				}
				return;
			case UNLOADING_PASSENGERS:
				while (mPassengers.contains(mCurrentFloor)) {
					//removes passengers if people want to get off
					mPassengers.remove((Integer) (mCurrentFloor));
				}
				if (mPassengers.isEmpty() && !tempList.isEmpty()) {
					//if no passengers and people are waiting
					mCurrentDirection = Direction.NOT_MOVING;
					mCurrentState = ElevatorState.LOADING_PASSENGERS;
				} else if (mPassengers.isEmpty() && tempList.isEmpty()){
					//else if no passengers and no people waiting
					mCurrentState = ElevatorState.DOORS_CLOSING;
					mCurrentDirection = Direction.NOT_MOVING;
				} else if (mCurrentDirection == Direction.MOVING_UP){
					//if passengers going up and waiting going up
					if (tempList.isEmpty()) {
						mCurrentState = ElevatorState.DOORS_CLOSING;
					} else {
						for (int person : tempList) {
							if (person > mCurrentFloor) {
								mCurrentState = ElevatorState.LOADING_PASSENGERS;
								break;
							} else {
								mCurrentState = ElevatorState.DOORS_CLOSING;
							}
						}
					}
				} else if (mCurrentDirection == Direction.MOVING_DOWN) {
					//if passengers going down and waiting going down
					if (tempList.isEmpty()) {
						mCurrentState = ElevatorState.DOORS_CLOSING;
					} else {
						for (int person : tempList) {
							if (person < mCurrentFloor) {
								mCurrentState = ElevatorState.LOADING_PASSENGERS;
								break;
							} else {
								mCurrentState = ElevatorState.DOORS_CLOSING;
							}
						}
					}
				}
				return;
			case LOADING_PASSENGERS:
				if (mCurrentDirection == Direction.MOVING_UP) {
					//going up get all going up
					for (int waiter : tempList) {
						if (waiter > mCurrentFloor) {
							mPassengers.add(waiter);
						}
					}
					//removing
					for (int i = tempList.size() - 1; i >= 0; i--){
						if (tempList.get(i) > mCurrentFloor){
							tempList.remove(i);
						}
					}
				} else if (mCurrentDirection == Direction.MOVING_DOWN) {
					//going down get all going down
					for (int waiter : tempList) {
						if (waiter < mCurrentFloor) {
							mPassengers.add(waiter);
						}
					}
					//removing
					for (int i = tempList.size() - 1; i >= 0; i--){
						if (tempList.get(i) < mCurrentFloor){
							tempList.remove(i);
						}
					}
				} else if (mCurrentDirection == Direction.NOT_MOVING && !tempList.isEmpty()){
					//not moving
					int next = tempList.get(0);
					//get new direction
					if (next > mCurrentFloor) {
						mCurrentDirection = Direction.MOVING_UP;
						//going up get all going up
						for (int waiter : tempList) {
							if (waiter > mCurrentFloor) {
								mPassengers.add(waiter);
							}
						}
						//removing
						for (int i = tempList.size() - 1; i >= 0; i--){
							if (tempList.get(i) > mCurrentFloor){
								tempList.remove(i);
							}
						}
					} else{
						//get new direction
						mCurrentDirection = Direction.MOVING_DOWN;
						//going down get all going down
						for (int waiter : tempList) {
							if (waiter < mCurrentFloor) {
								mPassengers.add(waiter);
							}
						}
						//removing
						for (int i = tempList.size() - 1; i >= 0; i--){
							if (tempList.get(i) < mCurrentFloor){
								tempList.remove(i);
							}
						}
					}
				}
				//doors close will always be next
				mCurrentState = ElevatorState.DOORS_CLOSING;
				return;
			case DOORS_CLOSING:
				if (mPassengers.isEmpty()){
					//if empty
					mCurrentState = ElevatorState.IDLE_STATE;
				} else {
					//people on accelerate
					mCurrentState = ElevatorState.ACCELERATING;
				}
				return;
			case ACCELERATING:
				//go to moving
				mCurrentState = ElevatorState.MOVING;
				return;
			case MOVING:
				ArrayList<Integer> nextFloor;
				if (mCurrentDirection == Direction.MOVING_UP){
					mCurrentFloor++;
					nextFloor = mBuilding.getFloor(mCurrentFloor);
					if (mPassengers.contains(mCurrentFloor)){
						mCurrentState = ElevatorState.DECELERATING;
						break;
					}
					for (int waiter : nextFloor) {
						if (waiter > mCurrentFloor) {
							mCurrentState = ElevatorState.DECELERATING;
						}
					}
				} else {
					mCurrentFloor--;
					nextFloor = mBuilding.getFloor(mCurrentFloor);
					if (mPassengers.contains(mCurrentFloor)){
						mCurrentState = ElevatorState.DECELERATING;
						break;
					}
					for (int waiter : nextFloor) {
						if (waiter < mCurrentFloor) {
							mCurrentState = ElevatorState.DECELERATING;
						}
					}
				}
				return;
			case DECELERATING:
				mCurrentState = ElevatorState.DOORS_OPENING;
				if (mPassengers.isEmpty()){
					mCurrentDirection = Direction.NOT_MOVING;
				}
				return;
		}
		// Example of how to trigger a state change:
		// scheduleStateChange(ElevatorState.MOVING, 3); // switch to MOVING and call tick(), 3 seconds from now.
	}
	
	
	/**
	 * Sends an idle elevator to the given floor.
	 */
	public void dispatchTo(Floor floor) {
		// Done: if we are currently idle and not on the given floor, change our direction to move towards the floor.
		if (mCurrentState == ElevatorState.IDLE_STATE && floor != getCurrentFloor()) {
			if (floor.getNumber() > mCurrentFloor.getNumber()){
				mCurrentDirection = Direction.MOVING_UP;
			}
			else{
				mCurrentDirection = Direction.MOVING_DOWN;
			}
		}

		// Done: set a floor request for the given floor, and schedule a state change to ACCELERATING immediately.
		scheduleStateChange(ElevatorState.ACCELERATING, 0);
		
	}
	
	// Simple accessors
	public Floor getCurrentFloor() {
		return mCurrentFloor;
	}
	
	public Direction getCurrentDirection() {
		return mCurrentDirection;
	}
	
	public Building getBuilding() {
		return mBuilding;
	}
	
	/**
	 * Returns true if this elevator is in the idle state.
	 */
	public boolean isIdle() {
		if (mCurrentState == ElevatorState.IDLE_STATE){
			return true;
		}
		return false;
	}
	
	// All elevators have a capacity of 10, for now.
	public int getCapacity() {
		return 10;
	}
	
	public int getPassengerCount() {
		return mPassengers.size();
	}
	
	// Simple mutators
	public void setState(ElevatorState newState) {
		mCurrentState = newState;
	}
	
	public void setCurrentDirection(Direction direction) {
		mCurrentDirection = direction;
	}
	
	public void setCurrentFloor(Floor floor) {
		mCurrentFloor = floor;
	}
	
	// Observers
	public void addObserver(ElevatorObserver observer) {
		mObservers.add(observer);
	}
	
	public void removeObserver(ElevatorObserver observer) {
		mObservers.remove(observer);
	}
	
	
	// FloorObserver methods
	@Override
	public void elevatorArriving(Floor floor, Elevator elevator) {
		// Not used.
	}
	
	/**
	 * Triggered when our current floor receives a direction request.
	 */
	@Override
	public void directionRequested(Floor sender, Direction direction) {
		// Done: if we are currently idle, change direction to match the request. Then alert all our observers that we are decelerating,
		if (mCurrentState == ElevatorState.IDLE_STATE){
			mCurrentDirection = direction;
		}
		for (ElevatorObserver e:mObservers) {
			e.elevatorDecelerating(this);
		}
		// Done: then schedule an immediate state change to DOORS_OPENING.
		scheduleStateChange(ElevatorState.DOORS_OPEN, 0);
	}
	
	
	
	
	// Voodoo magic.
	@Override
	public String toString() {
		return "Elevator " + mNumber + " - " + mCurrentFloor + " - " + mCurrentState + " - " + mCurrentDirection + " - "
		 + "[" + mPassengers.stream().map(p -> Integer.toString(p.getDestination())).collect(Collectors.joining(", "))
		 + "]";
	}
	
}