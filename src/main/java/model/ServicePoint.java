package model;

import eduni.distributions.ContinuousGenerator;
import framework.*;

import java.util.LinkedList;

public class ServicePoint {
    private final String name;
    private final LinkedList<Customer> queue = new LinkedList<>(); // Data Structure used
    private final ContinuousGenerator generator;
    private final EventList eventList;
    private final EventType eventTypeScheduled;

    private boolean reserved = false;
    private int longestQueueSize;
    private int servedCustomersHere;

    public ServicePoint(String name, ContinuousGenerator generator, EventList eventList, EventType type) {
        this.name = name;
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
        this.longestQueueSize = 0;
    }

    public int getQueueSize() {
        return this.queue.size();
    }

    public void setLongestQueueSize(int longestQueue) {
        this.longestQueueSize = longestQueue;
    }

    public int getLongestQueueSize() {
        return this.longestQueueSize;
    }

    public int getServedCustomersHere() {
        return this.servedCustomersHere;
    }

    public String getName() {
        return name;
    }

    public void increaseServedCustomersHereByOne() {
        this.servedCustomersHere++;
    }

    public void addQueue(Customer a) {    // The first customer of the queue is always in service
        this.queue.add(a);
        if (getLongestQueueSize() < getQueueSize()){
            setLongestQueueSize(getQueueSize());
        }
    }

    public Customer removeQueue() {        // Remove serviced customer
        this.reserved = false;
        increaseServedCustomersHereByOne();
        return this.queue.poll();
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
}
