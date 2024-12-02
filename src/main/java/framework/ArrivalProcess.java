package framework;

import eduni.distributions.ContinuousGenerator;
import model.Customer;
import model.CustomerCreator;

import java.util.Random;

public class ArrivalProcess {
	private final ContinuousGenerator generator;
	private final EventList eventList;
	private final IEventType type;
	private final CustomerCreator customerCreator;

	public ArrivalProcess(ContinuousGenerator g, EventList tl, IEventType type, CustomerCreator creator) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
		this.customerCreator = creator;
	}

	public void generateNextEvent() {
		Customer c = customerCreator.createCustomer();
		Event t = new Event(type, Clock.getInstance().getClock() + generator.sample(), c);
		eventList.add(t);
	}
}
