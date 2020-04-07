package cecs277.elevators;

public interface ElevatorObserver {
	/**
	 * Triggered when an elevator enters the DECELERATING state.
	 */
	void elevatorDecelerating(Elevator sender);
	
	/**
	 * Triggered when an elevator enters the DOORS_OPEN state.
	 */
	void elevatorDoorsOpened(Elevator sender);
	
	/**
	 * Triggered when an elevator enters the IDLE_STATE state.
	 */
	void elevatorWentIdle(Elevator sender);
}
