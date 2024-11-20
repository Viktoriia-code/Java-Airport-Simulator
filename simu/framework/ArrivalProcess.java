package simu.framework;

import eduni.distributions.ContinuousGenerator;

public class ArrivalProcess {
	private ContinuousGenerator generator;
	private EventList eventList;
	private IEventType type;

	public ArrivalProcess(ContinuousGenerator g, EventList tl, IEventType type) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
	}

	public void generateNextEvent() {
		Event t = new Event(type, Clock.getInstance().getClock() + generator.sample());
		eventList.add(t);
	}
}
