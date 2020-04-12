package cecs277.buildings;

import cecs277.passengers.Passenger;
import cecs277.Simulation;
import cecs277.elevators.Elevator;
import cecs277.elevators.ElevatorObserver;

import java.util.*;

public class Building implements ElevatorObserver, FloorObserver {
	private List<Elevator> mElevators = new ArrayList<>();
	private List<Floor> mFloors = new ArrayList<>();
	private Simulation mSimulation;
	private Queue<Integer> mWaitingFloors = new ArrayDeque<>();
	
	public Building(int floors, int elevatorCount, Simulation sim) {
		mSimulation = sim;
		
		// Construct the floors, and observe each one.
		for (int i = 0; i < floors; i++) {
			Floor f = new Floor(i + 1, this);
			f.addObserver(this);
			mFloors.add(f);
		}
		
		// Construct the elevators, and observe each one.
		for (int i = 0; i < elevatorCount; i++) {
			Elevator elevator = new Elevator(i + 1, this);
			elevator.addObserver(this);
			for (Floor f : mFloors) {
				elevator.addObserver(f);
			}
			mElevators.add(elevator);
		}
	}
	

	// DONE: recreate your toString() here.
	public String toString(){
		StringBuilder visualRepresentation = new StringBuilder();
		for (int i = getFloorCount(); i > 0; i--){

//            adds a padding so the floor numbers line up visually
			if (i < 10){
				visualRepresentation.append("  ").append(i).append(":  |");
			}
			else if(i < 100){
				visualRepresentation.append(" ").append(i).append(":  |");
			}
			else{
				visualRepresentation.append(i).append(":  |");
			}

			for(int j = 0; j < mElevators.size(); j++){
				if(mElevators.get(j).getCurrentFloor().getNumber() == i){
					visualRepresentation.append(" X |");
				}
				else{
					visualRepresentation.append("   |");
				}
			}

			visualRepresentation.append(" ");

//            displays the passengers waiting on the building floor
			for(int k = 0; k < getFloorCount(); k++){
				visualRepresentation.append(" ").append(mFloors.get(k));
			}

			visualRepresentation.append("\n");
		}

		for (int i = 0; i < mElevators.size(); i++){
			visualRepresentation.append(mElevators.get(i)).append("\n");
		}
		return visualRepresentation.toString();
	}
	
	public int getFloorCount() {
		return mFloors.size();
	}
	
	public Floor getFloor(int floor) {
		return mFloors.get(floor - 1);
	}
	
	public Simulation getSimulation() {
		return mSimulation;
	}
	
	
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// Have to implement all interface methods even if we don't use them.
	}
	
	@Override
	public void elevatorDoorsOpened(Elevator elevator) {
		// Don't care.
	}
	
	@Override
	public void elevatorWentIdle(Elevator elevator) {
		// DONE: if mWaitingFloors is not empty, remove the first entry from the queue and dispatch the elevator to that floor.
		//added the !
		if (!mWaitingFloors.isEmpty()){
			elevator.dispatchTo(mFloors.get(mWaitingFloors.remove()));
		}
	}
	
	@Override
	public void elevatorArriving(Floor sender, Elevator elevator) {
		if (!mWaitingFloors.contains(sender.getNumber())){
			mWaitingFloors.add(sender.getNumber());
		}
		// DONE: add the floor mWaitingFloors if it is not already in the queue.
	}
	
	@Override
	public void directionRequested(Floor floor, Elevator.Direction direction) {
		boolean elevatorDispatched = false;


		//TODO i wonder if we should break after dispatching one elevator or if it should be a race between elevators lol
		for(Elevator e: mElevators){
			if (e.isIdle()){
				e.dispatchTo(floor);
				elevatorDispatched = true;
				break;
			}
		}

		if (!elevatorDispatched) {
			mWaitingFloors.add(floor.getNumber());
		}

		// DONE: go through each elevator. If an elevator is idle, dispatch it to the given floor.
		// DONE: if no elevators are idle, then add the floor number to the mWaitingFloors queue.
	}
}
