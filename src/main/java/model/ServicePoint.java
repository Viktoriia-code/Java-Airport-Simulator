package model;

import eduni.distributions.ContinuousGenerator;
import framework.*;

import java.util.LinkedList;

/**
 * Service Point implements the functionalities, calculations and reporting.
 *
 *
 * Service point has a queue where customers are waiting to be serviced.
 * Service point simulated the servicing time using the given random number generator which
 * generated the given event (customer serviced) for that time.
 *
 * Service point collects measurement parameters.
 */
public class ServicePoint {
    private final String name;
    private final LinkedList<Customer> queue = new LinkedList<>(); // Data Structure used
    private final ContinuousGenerator generator;
    private final EventList eventList;
    private final EventType eventTypeScheduled;

    private boolean reserved = false;
    private int longestQueueSize;
    private int servedCustomersHere;
    private double totalQueueTime;

    /**
     * Create the service point with a waiting queue.
     *
     * @param generator Random number generator for service time simulation
     * @param eventList Simulator event list, needed for the insertion of service ready event
     * @param type Event type for the service end event
     */
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

    /**
     * Add a customer to the service point queue.
     *
     * @param a Customer to be queued
     */
    public void addQueue(Customer a) {    // The first customer of the queue is always in service
        this.queue.add(a);
        a.setQueueEntryTime(Clock.getInstance().getClock());
        if (getLongestQueueSize() < getQueueSize()){
            setLongestQueueSize(getQueueSize());
        }
    }

    /**
     * Remove customer from the waiting queue.
     * Here we calculate also the appropriate measurement values.
     *
     * @return Customer retrieved from the waiting queue
     */
    public Customer removeQueue() {        // Remove serviced customer
        this.reserved = false;
        increaseServedCustomersHereByOne();
        Customer a = this.queue.poll();
        this.totalQueueTime += Clock.getInstance().getClock() - a.getQueueEntryTime();
        return a;
    }

    /**
     * Begins a new service, customer is on the queue during the service
     *
     * Inserts a new event to the event list when the service should be ready.
     */
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

    /**
     * Check whether the service point is busy
     *
     * @return logical value indicating service state
     */
    public boolean isReserved() {
        return reserved;
    }

    /**
     * Check whether there is customers on the waiting queue
     *
     * @return logical value indicating queue status
     */
    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    public double getAverageQueueTime() {
        return totalQueueTime / servedCustomersHere;
    }
}
