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
	private int passengerChangeCount = 0;
	private List<Passenger> mPassengers = new ArrayList<>();
	private ArrayList<ElevatorObserver> mObservers = new ArrayList<>();
	
	// Done: declare a field to keep track of which floors have been requested by passengers.
	private boolean mRequestedFloors[];
	
	
	public Elevator(int number, Building bld) {
		mNumber = number;
		mBuilding = bld;
		mCurrentFloor = bld.getFloor(1);
		
		scheduleStateChange(ElevatorState.IDLE_STATE, 0);
		mRequestedFloors = new boolean[mBuilding.getFloorCount()];
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
		//FIXME +1 didnt work so i changed it to -1
		mRequestedFloors[passenger.getDestination() - 1] = true;
		passengerChangeCount++;
	}

	public void removePassenger(Passenger passenger) {
		mPassengers.remove(passenger);
		//FIXME +1 didnt work so i changed it to -1
		mRequestedFloors[passenger.getDestination() - 1] = false;
		passengerChangeCount++;
	}
	
	
	/**
	 * Schedules the elevator's next state change based on its current state.
	 */
	public void tick() {
		// TODO: port the logic of your state changes from Project 1, accounting for the adjustments in the spec.
		// TODO: State changes are no longer immediate; they are scheduled using scheduleStateChange().
		Simulation s = this.getBuilding().getSimulation();
		switch (mCurrentState) {
			case IDLE_STATE:
				mCurrentFloor.addObserver(this);

				for (ElevatorObserver o:mObservers) {
					o.elevatorWentIdle(this);
				}

				return;

			case DOORS_OPENING:
//				s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//						ElevatorState.DOORS_OPEN, this));
				scheduleStateChange(ElevatorState.DOORS_OPEN, s.currentTime() + 2);

				return;

			case DOORS_OPEN:
				passengerChangeCount = 0;

//				for (ElevatorObserver o:mObservers) {
				for (int i = 0; i < mObservers.size(); i++){
					mObservers.get(i).elevatorDoorsOpened(this);
				}

//				System.out.println(passengerChangeCount);
//				System.out.println(passengerChangeCount /2 + 1);
//				s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + (passengerChangeCount / 2) + 1,
//						ElevatorState.DOORS_CLOSING, this));
				scheduleStateChange(ElevatorState.DOORS_CLOSING, s.currentTime() + (passengerChangeCount / 2) + 1);

				return;

			case DOORS_CLOSING:
				if (mCurrentDirection == Direction.MOVING_DOWN ) {
					if (hasRequestedFloorsDown()){
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.ACCELERATING, this));
						scheduleStateChange(ElevatorState.ACCELERATING, s.currentTime() + 2);

					}
					else if (hasRequestedFloorsUp()) {
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.DOORS_OPENING, this));
						scheduleStateChange(ElevatorState.DOORS_OPENING, s.currentTime() + 2);
						mCurrentDirection = Direction.MOVING_UP;
					}
					else{
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.IDLE_STATE, this));
						scheduleStateChange(ElevatorState.IDLE_STATE, s.currentTime() + 2);
						mCurrentDirection = Direction.NOT_MOVING;
					}
				}
				else if (mCurrentDirection == Direction.MOVING_UP) {
					if (hasRequestedFloorsUp()){
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.ACCELERATING, this));
						scheduleStateChange(ElevatorState.ACCELERATING, s.currentTime() + 2);
					}
					else if (hasRequestedFloorsDown()) {
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.DOORS_OPENING, this));
						scheduleStateChange(ElevatorState.DOORS_OPENING, s.currentTime() + 2);
						mCurrentDirection = Direction.MOVING_DOWN;
					}
					else{
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.IDLE_STATE, this));
						scheduleStateChange(ElevatorState.IDLE_STATE, s.currentTime() + 2);
						mCurrentDirection = Direction.NOT_MOVING;
					}
				}
				return;

			case ACCELERATING:
				//go to moving
//				s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//						ElevatorState.MOVING, this));
				scheduleStateChange(ElevatorState.MOVING, s.currentTime() + 2);

				return;

			case MOVING:
				if (mCurrentDirection == Direction.MOVING_UP) {
					mCurrentFloor = mBuilding.getFloor(mCurrentFloor.getNumber() + 1);
					if (mRequestedFloors[mCurrentFloor.getNumber() - 1] ||
							mCurrentFloor.directionIsPressed(Direction.MOVING_UP)) {
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.DECELERATING, this));
						scheduleStateChange(ElevatorState.DECELERATING, s.currentTime() + 2);
					}
					else{
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.MOVING, this));
						scheduleStateChange(ElevatorState.MOVING, s.currentTime() + 2);
					}
				}
				else if (mCurrentDirection == Direction.MOVING_DOWN) {
					mCurrentFloor = mBuilding.getFloor(mCurrentFloor.getNumber() - 1);
					if (mRequestedFloors[mCurrentFloor.getNumber() - 1] ||
							mCurrentFloor.directionIsPressed(Direction.MOVING_DOWN)) {
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.DECELERATING, this));
						scheduleStateChange(ElevatorState.DECELERATING, s.currentTime() + 2);
					}
					else{
//						s.scheduleEvent(new ElevatorStateEvent(s.currentTime() + 2,
//								ElevatorState.MOVING, this));
						scheduleStateChange(ElevatorState.MOVING, s.currentTime() + 2);
					}
				}
				return;

			case DECELERATING:
				mRequestedFloors[mCurrentFloor.getNumber() - 1] = false;

				if ( mCurrentDirection == Direction.MOVING_UP ) {
					mCurrentFloor.clearDirection(Direction.MOVING_UP);
					if ( !(mCurrentFloor.directionIsPressed(Direction.MOVING_UP) || hasRequestedFloorsUp())
							&& mCurrentFloor.directionIsPressed(Direction.MOVING_DOWN) ){
						mCurrentDirection = Direction.MOVING_DOWN;
					}
					else {
						mCurrentDirection = Direction.NOT_MOVING;
					}
				}
				else {
					if ( !(mCurrentFloor.directionIsPressed(Direction.MOVING_DOWN) || hasRequestedFloorsDown())
							&& mCurrentFloor.directionIsPressed(Direction.MOVING_UP) ){
						mCurrentDirection = Direction.MOVING_UP;
					}
					else {
						mCurrentDirection = Direction.NOT_MOVING;
					}
				}

				return;

			default:
				return;
		}
	}
	

	private boolean hasRequestedFloorsUp(){
		for(int i = mCurrentFloor.getNumber(); i < mBuilding.getFloorCount(); i++){
			if (mRequestedFloors[i] == true) {
				return true;
			}
		}
		return false;
	}

	private boolean hasRequestedFloorsDown(){
		for(int i = mCurrentFloor.getNumber() - 2; i >= 0; i--){
			if (mRequestedFloors[i] == true) {
				return true;
			}
		}
		return false;
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
			// Done: set a floor request for the given floor, and schedule a state change to ACCELERATING immediately.
			scheduleStateChange(ElevatorState.ACCELERATING, 0);
		}
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

//		ArrayList<ElevatorObserver> temp = mObservers;
//		for (ElevatorObserver e : mObservers) {
		for (int i = 0; i < mObservers.size(); i++){
			mObservers.get(i).elevatorDecelerating(this);
		}
		// Done: then schedule an immediate state change to DOORS_OPENING.
		scheduleStateChange(ElevatorState.DOORS_OPENING, 0);
	}
	
	
	
	
	// Voodoo magic.
	@Override
	public String toString() {
		return "Elevator " + mNumber + " - " + mCurrentFloor + " - " + mCurrentState + " - " + mCurrentDirection + " - "
		 + "[" + mPassengers.stream().map(p -> Integer.toString(p.getDestination())).collect(Collectors.joining(", "))
		 + "]";
	}
	
}