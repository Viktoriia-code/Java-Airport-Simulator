package model;

import framework.*;

/**
 * Customer in a simulator
 *
 */
public class Customer {
	private double arrivalTime;
	private double removalTime;

	private final int id;
	private static int i = 1;
	private static double serviceTimeSum = 0;
	private int currentQueue;

	private final boolean isBusinessClass;
	private final boolean isEUFlight;
	private final boolean isOnlineCheckOut;
	private double queueEntryTime;

	/**
	 * Create a unique customer
	 */
	public Customer(boolean isBusinessClass, boolean isEUFlight, boolean isOnlineCheckOut) {
		this.isBusinessClass = isBusinessClass;
		this.isEUFlight = isEUFlight;
		this.isOnlineCheckOut = isOnlineCheckOut;

		id = i++;

		setArrivalTime(Clock.getInstance().getClock());
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime +
				(this.isBusinessClass ? " (Business Class" : " (Economy Class") +
				(this.isEUFlight ? " || Inside EU ||" : " || Outside EU ||") +
				(this.isOnlineCheckOut ? " Online Check-Out)" : " Regular Check Out)"));
	}

	public void setQueueEntryTime(double queueEntryTime) {
		this.queueEntryTime = queueEntryTime;
	}

	public double getQueueEntryTime() {
		return queueEntryTime;
	}

	public boolean isBusinessClass() {
		return isBusinessClass;
	}

	public boolean isEUFlight() {
		return isEUFlight;
	}

	public boolean isOnlineCheckOut() {
		return isOnlineCheckOut;
	}

	/**
	 * Give the time when customer has been removed (from the system to be simulated)
	 * @return Customer removal time
	 */
	public double getRemovalTime() {
		return removalTime;
	}

	/**
	 * Mark the time when the customer has been removed (from the system to be simulated)
	 * @param removalTime Customer removal time
	 */
	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	/**
	 * Give the time when the customer arrived to the system to be simulated
	 * @return Customer arrival time
	 */
	public double getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Mark the time when the customer arrived to the system to be simulated
	 * @param arrivalTime Customer arrival time
	 */
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	/**
	 * Get the (unique) customer id
	 * @return Customer id
	 */
	public int getId() {
		return id;
	}

    public void setCurrentQueueIndex(int i) {
        this.currentQueue = i;
    }

    public int getCurrentQueueIndex() {
        return this.currentQueue;
    }

	public static void resetId() { i = 1; }

	/**
	 * Report the measured variables of the customer. In this case to the diagnostic output.
	 */
	public void reportResults(){
		Trace.out(Trace.Level.INFO, "\nCustomer #" + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer #" + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " stayed: " + (removalTime - arrivalTime));

		serviceTimeSum += (getRemovalTime() - getArrivalTime());
		double mean = serviceTimeSum / id;
		System.out.println("Current mean of the customer service times " + mean);
		System.out.printf("Customer %s: %s, Flight: %s, Total Time: %.2f%n",
				getId(),
				this.isBusinessClass ? "Business" : "Economy",
				this.isEUFlight ? "Internal" : "External",
				removalTime - arrivalTime);
	}

	public static double getServiceTimeSum(){
		return serviceTimeSum;
	}

	public static void resetServiceTimeSum(){
		serviceTimeSum = 0;
	}
}
