package simu.framework;

import java.util.PriorityQueue;

public class EventList {
	private PriorityQueue<Event> eventlist;
	
	public EventList() {
		eventlist = new PriorityQueue<>();
	}
	
	public Event remove() {
		Trace.out(Trace.Level.INFO,"Removing from the event list " + eventlist.peek().getType() + " " + eventlist.peek().getTime());
		return eventlist.remove();
	}
	
	public void add(Event t) {
		Trace.out(Trace.Level.INFO,"Adding to the event list " + t.getType() + " " + t.getTime());
		eventlist.add(t);
	}
	
	public double getNextEventTime(){
		return eventlist.peek().getTime();
	}
}
