package framework;

import eduni.distributions.ContinuousGenerator;
import model.Customer;

public class ArrivalProcess {
	private final ContinuousGenerator generator;
	private final EventList eventList;
	private final IEventType type;

	public ArrivalProcess(ContinuousGenerator g, EventList tl, IEventType type) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
	}

	public void generateNextEvent() {
		Customer c = new Customer();
		Event t = new Event(type, Clock.getInstance().getClock() + generator.sample(), c);
		eventList.add(t);
	}
}
