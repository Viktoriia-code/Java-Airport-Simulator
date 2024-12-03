package model;

import framework.*;

// TODO:
// Customer to be implemented according to the requirements of the simulation model (data!)
public class Customer {
	private double arrivalTime;
	private double removalTime;

	private final int id;
	private static int i = 1;
	private static long sum = 0;
	private int currentQueue;

	private final boolean isBusinessClass;
	private final boolean isEUFlight;
	private final boolean isOnlineCheckOut;

	public Customer(boolean isBusinessClass, boolean isEUFlight, boolean isOnlineCheckOut) {
		this.isBusinessClass = isBusinessClass;
		this.isEUFlight = isEUFlight;
		this.isOnlineCheckOut = isOnlineCheckOut;

		id = i++;

		arrivalTime = Clock.getInstance().getClock();
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime +
				(this.isBusinessClass ? " (Business Class" : " (Economy Class") +
				(this.isEUFlight ? " || Inside EU ||" : " || Outside EU ||") +
				(this.isOnlineCheckOut ? " Online Check-Out)" : " Regular Check Out)"));
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

	public double getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

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

	public void reportResults(){
		Trace.out(Trace.Level.INFO, "\nCustomer #" + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer #" + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " stayed: " + (removalTime - arrivalTime));

		sum += (long) (removalTime - arrivalTime);
		double mean = (double) sum / id;
		System.out.println("Current mean of the customer service times " + mean);
		System.out.printf("Customer %s: %s, Flight: %s, Total Time: %.2f%n",
				getId(),
				this.isBusinessClass ? "Business" : "Economy",
				this.isEUFlight ? "Internal" : "External",
				removalTime - arrivalTime);
	}
}
