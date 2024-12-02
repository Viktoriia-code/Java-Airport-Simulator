package model;

import eduni.distributions.ContinuousGenerator;
import framework.*;

import java.util.LinkedList;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
    private final String name;
    private final LinkedList<Customer> queue = new LinkedList<>(); // Data Structure used
    private final ContinuousGenerator generator;
    private final EventList eventList;
    private final EventType eventTypeScheduled;

    private boolean reserved = false;

    public ServicePoint(String name, ContinuousGenerator generator, EventList eventList, EventType type) {
        this.name = name;
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
    }

    public void addQueue(Customer a) {    // The first customer of the queue is always in service
        queue.add(a);
    }

    public Customer removeQueue() {        // Remove serviced customer
        reserved = false;
        return queue.poll();
    }

    public void beginService() {        // Begins a new service, customer is on the queue during the service
        try {
            Trace.out(Trace.Level.INFO, "Starting " + name + " for the customer #" + queue.peek().getId() + " at queue #" + queue.peek().getCurrentQueueIndex());

            reserved = true;
            double serviceTime = generator.sample();
            eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getClock() + serviceTime, queue.getLast()));
        } catch (NullPointerException e) {
            Trace.out(Trace.Level.WAR, "[WARNING] Tried to do ServicePoint.beginService() while queue was empty.");
        }
    }

    public boolean isReserved() {
        return reserved;
    }

    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    public int getQueueSize() {
        return queue.size();
    }
}
