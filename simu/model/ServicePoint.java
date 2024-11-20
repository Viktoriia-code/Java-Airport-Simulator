package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.*;
import java.util.LinkedList;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
	private LinkedList<Customer> queue = new LinkedList<>(); // Data Structure used
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;
	//Queuestrategy strategy; // option: ordering of the customer
	private boolean reserved = false;


	public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type){
		this.eventList = eventList;
		this.generator = generator;
		this.eventTypeScheduled = type;
	}

	public void addQueue(Customer a) {	// The first customer of the queue is always in service
		queue.add(a);
	}

	public Customer removeQueue() {		// Remove serviced customer
		reserved = false;
		return queue.poll();
	}

	public void beginService() {		// Begins a new service, customer is on the queue during the service
		Trace.out(Trace.Level.INFO, "Starting a new service for the customer #" + queue.peek().getId());
		
		reserved = true;
		double serviceTime = generator.sample();
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getClock()+serviceTime));
	}

	public boolean isReserved(){
		return reserved;
	}

	public boolean isOnQueue(){
		return queue.size() != 0;
	}
}
