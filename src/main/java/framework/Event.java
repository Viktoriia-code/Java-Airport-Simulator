package framework;
import model.Customer;

/**
 * Event holds three-phase simulation event information; type and time of the event.
 * Events are compared according to time.
 */
public class Event implements Comparable<Event> {
	private IEventType type;
	private double time;
	private Customer customer;

	/**
	 * Create an event with the given type, time and customer.
	 *
	 * @param type the type of the event.
	 * @param time the time of the event.
	 * @param customer the customer associated with the event.
	 */
	public Event(IEventType type, double time, Customer customer){
		this.type = type;
		this.time = time;
		this.customer = customer;
	}

	/**
	 * Set the type of the event.
	 *
	 * @param type the type of the event.
	 */
	public void setType(IEventType type) {
		this.type = type;
	}

	/**
	 * Retrieve the type of the event.
	 *
	 * @return the type of the event.
	 */
	public IEventType getType() {
		return type;
	}

	/**
	 * Set the time of the event.
	 *
	 * @param time the time of the event.
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * Retrieve the time of the event.
	 *
	 * @return the time of the event.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Set the customer associated with the event.
	 *
	 * @param customer the customer associated with the event.
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	/**
	 * Retrieve the customer associated with the event.
	 *
	 * @return the customer associated with the event.
	 */
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * Compare two events according to time.
	 *
	 * @param arg the event to be compared to this event.
	 * @return 0 if the time of the two events are equal, -1 if this event is earlier, 1 if this event is later.
	 */
	@Override
	public int compareTo(Event arg) {
		if (this.time < arg.time) return -1;
		else if (this.time > arg.time) return 1;
		return 0;
	}
}
