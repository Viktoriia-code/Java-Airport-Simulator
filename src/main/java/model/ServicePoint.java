package model;

import eduni.distributions.ContinuousGenerator;
import framework.*;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ServicePoint {
	private String name;
	private final PriorityQueue<Customer> queue;
	// Data Structure used
	private final PriorityQueue<Customer> boardingInternalQueue; // Queue for internal flights at boarding
	private final PriorityQueue<Customer> boardingExternalQueue; // Queue for external flights at boarding
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
		this.boardingInternalQueue = new PriorityQueue<>(Comparator.comparingInt(c -> c.isBusinessClass() ? -1 : 1));
		this.boardingExternalQueue = new PriorityQueue<>(Comparator.comparingInt(c -> c.isBusinessClass() ? -1 : 1));
	}

	// Add a customer to the queue
	public void addQueue(Customer a) {
		if ("Boarding".equals(name)) {
			// Only at boarding, differentiate based on flight type
			if (a.isEUFlight()) {
				boardingInternalQueue.add(a);
			} else {
				boardingExternalQueue.add(a);
			}
		} else {
			queue.add(a);
		}
		if (!reserved) {
			beginService();
		}
	}

	// Remove a serviced customer from the queue
	public Customer removeQueue() {
		Customer removedCustomer = null;
		if ("Boarding".equals(name)) {
			if (!boardingInternalQueue.isEmpty()) {
				removedCustomer = boardingInternalQueue.poll();
			} else if (!boardingExternalQueue.isEmpty()) {
				removedCustomer = boardingExternalQueue.poll();
			}
		} else {
			removedCustomer = queue.poll();
		}
		reserved = false;
		return removedCustomer;
	}

	// Begin service for the customer
	public void beginService() {
		Customer currentCustomer = null;
		if ("Boarding".equals(name)) {
			// At boarding, check internal first, if any, else external
			currentCustomer = boardingInternalQueue.peek();
			if (currentCustomer == null) {
				currentCustomer = boardingExternalQueue.peek();
			}
		} else {
			// For other service points, use the general queue
			currentCustomer = queue.peek();
		}

		if (currentCustomer != null) {
			Trace.out(Trace.Level.INFO, "Starting service for customer #" + currentCustomer.getId() + " at " + name );
			double serviceTime = generator.sample();
			// If business class, reduce service time (20% faster)
			if (currentCustomer.isBusinessClass()) {
				serviceTime *= 0.8; // 20% faster service time for business class
			}
			eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getClock() + serviceTime));
		}
	}

	// Check if the service point is reserved (i.e., a customer is being served)
	public boolean isReserved() {
		return reserved;
	}

	// Check if there are any customers in the queue
	public boolean isOnQueue() {
		if ("Boarding".equals(name)) {
			return !boardingInternalQueue.isEmpty() || !boardingExternalQueue.isEmpty();
		} else {
			return !queue.isEmpty();
		}
	}

	// Handle the logic of skipping passport control for internal flights
	public boolean isEUFlight(Customer customer) {
		return customer.isEUFlight(); // Assuming this method exists in Customer class
	}
}
