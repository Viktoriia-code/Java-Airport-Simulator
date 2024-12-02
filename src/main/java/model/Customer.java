package model;

import framework.*;

public class Customer {
	private double arrivalTime;
	private double removalTime;

	private final int id;
	private static int i = 1;
	private static long sum = 0;
	private int currentQueue;

	private final boolean isBusinessClass;
	private final boolean isEUFlight;

	public Customer(boolean isBusinessClass, boolean isEUFlight) {
		this.isBusinessClass = isBusinessClass;
		this.isEUFlight = isEUFlight;
		id = i++;

		arrivalTime = Clock.getInstance().getClock();
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime);
	}

	public boolean isBusinessClass() {
		return isBusinessClass;
	}

	public boolean isEUFlight() {
		return isEUFlight;
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

	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer #" + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer #" + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " stayed: " + (removalTime - arrivalTime));

		sum += (long) (removalTime - arrivalTime);
		double mean = (double) sum / id;
		System.out.println("Current mean of the customer service times " + mean);
		System.out.printf("Customer: %s, Flight: %s, Total Time: %.2f%n",
				isBusinessClass ? "Business" : "Economy",
				isEUFlight ? "Internal" : "External",
				removalTime - arrivalTime);
	}
}
