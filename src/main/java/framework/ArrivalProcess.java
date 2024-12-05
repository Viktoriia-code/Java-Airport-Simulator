package framework;

import eduni.distributions.ContinuousGenerator;
import model.Customer;
import model.CustomerCreator;

/**
 * ArrivalProcess produces the time when next customer arrivals. This is based on the
 * current time and random number
 */
public class ArrivalProcess {
	private final ContinuousGenerator generator;
	private final EventList eventList;
	private final IEventType type;
	private final CustomerCreator customerCreator;

	/**
	 * Create the service point with a waiting queue.
	 *
	 * @param g Random number generator for customer arrival time simulation
	 * @param tl Simulator event list, needed for the insertion of customer arrival event
	 * @param type Event type for the customer arrival event
	 */
	public ArrivalProcess(ContinuousGenerator g, EventList tl, IEventType type, CustomerCreator creator) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
		this.customerCreator = creator;
	}

	/**
	 * Create a new customer (Generate customer arrival event and put it to the event list)
	 */
	public void generateNextEvent() {
		Customer c = customerCreator.createCustomer();
		Event t = new Event(type, Clock.getInstance().getClock() + generator.sample(), c);
		eventList.add(t);
	}
}
