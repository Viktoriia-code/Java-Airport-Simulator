package framework;
import model.Customer;

public class Event implements Comparable<Event> {
	private IEventType type;
	private double time;
	private Customer customer;

	public Event(IEventType type, double time, Customer customer){
		this.type = type;
		this.time = time;
		this.customer = customer;
	}
	
	public void setType(IEventType type) {
		this.type = type;
	}
	public IEventType getType() {
		return type;
	}

	public void setTime(double time) {
		this.time = time;
	}
	public double getTime() {
		return time;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public Customer getCustomer() {
		return customer;
	}


	@Override
	public int compareTo(Event arg) {
		if (this.time < arg.time) return -1;
		else if (this.time > arg.time) return 1;
		return 0;
	}
}
