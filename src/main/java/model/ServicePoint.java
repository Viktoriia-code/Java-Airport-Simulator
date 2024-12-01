package model;

import eduni.distributions.ContinuousGenerator;
import framework.*;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ServicePoint {
	private String name;
	private final PriorityQueue<Customer> queue; // Data Structure used
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;
	private boolean reserved = false;

	// Constructor
	public ServicePoint(String name, ContinuousGenerator generator, EventList eventList, EventType type) {
		this.name = name;
		this.eventList = eventList;
		this.generator = generator;
		this.eventTypeScheduled = type;

		// Prioritize business class customers
		queue = new PriorityQueue<>(Comparator.comparingInt(c -> c.isBusinessClass() ? -1 : 1));
	}

	// Add a customer to the queue
	public void addQueue(Customer a) {
		queue.add(a);
		if (!reserved) {
			beginService();
		}
	}

	// Remove a serviced customer from the queue
	public Customer removeQueue() {
		reserved = false;
		return queue.isEmpty() ? null : queue.poll(); // Check if queue is empty
	}

	// Begin service for the customer
	public void beginService() {
		if (queue.isEmpty()) {
			return; // No customers to serve
		}

		Customer currentCustomer = queue.peek();
		Trace.out(Trace.Level.INFO, "Starting " + name + " for the customer #" + currentCustomer.getId());

		reserved = true;

		// Adjust service time if the customer is business class
		double serviceTime = generator.sample();
		if (currentCustomer.isBusinessClass()) {
			// Business class might have faster service time (example)
			serviceTime *= 0.8; // Adjust this as needed
		}

		// Schedule the next event based on the service time
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getClock() + serviceTime));
	}

	// Check if the service point is reserved (i.e., a customer is being served)
	public boolean isReserved() {
		return reserved;
	}

	// Check if there are any customers in the queue
	public boolean isOnQueue() {
		return !queue.isEmpty();
	}

	// Handle the logic of skipping passport control for internal flights
	public boolean isInternalFlight(Customer customer) {
		return customer.isEUFlight(); // Assuming this method exists in Customer class
	}
}
