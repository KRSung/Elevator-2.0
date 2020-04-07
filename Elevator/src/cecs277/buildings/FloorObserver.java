package cecs277.buildings;

import cecs277.buildings.Floor;
import cecs277.elevators.Elevator;

public interface FloorObserver {
	/**
	 * Triggered when an elevator begins decelerating as it is approaching the given floor.
	 * @param sender the Floor being approached
	 * @param elevator the Elevator that is decelerating.
	 */
	void elevatorArriving(Floor sender, Elevator elevator);
	
	/**
	 * Triggered when a direction button has been pressed on the given floor.
	 */
	void directionRequested(Floor sender, Elevator.Direction direction);
}
