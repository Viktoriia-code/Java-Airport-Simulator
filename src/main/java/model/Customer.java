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
	private final boolean isOnlineCheckIn;

	private double queueEntryTime;

	private double x; // x and y coordinates of the customer
	private double y;
	private double targetX; // x and y coordinates of the target
	private double targetY;
	private double speed = 45.0; // speed of the customer

	private boolean visible = true;

	/**
	 * Create a unique customer
	 *
	 * @param isBusinessClass if customer is in the Business Class or Economy
	 * @param isEUFlight      if customer's destination is inside EU or out
	 * @param isOnlineCheckIn if customer is checking in online or on-site
	 */
	public Customer(boolean isBusinessClass, boolean isEUFlight, boolean isOnlineCheckIn) {
		this.isBusinessClass = isBusinessClass;
		this.isEUFlight = isEUFlight;
		this.isOnlineCheckIn = isOnlineCheckIn;

		id = i++;

		setArrivalTime(Clock.getInstance().getClock());
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime +
				(this.isBusinessClass ? " (Business Class" : " (Economy Class") +
				(this.isEUFlight ? " || Inside EU ||" : " || Outside EU ||") +
				(this.isOnlineCheckIn ? " Online Check-Out)" : " Regular Check Out)"));
	}

	/**
	 * Sets the time customer enters the Queue
	 *
	 * @param queueEntryTime time to be set as customer's arrival to a queue
	 */
	public void setQueueEntryTime(double queueEntryTime) {
		this.queueEntryTime = queueEntryTime;
	}

	/**
	 * Gets the time customer entered the Queue (for the latest queue they've entered)
	 *
	 * @return queueEntryTime time of the customer's arrival to a queue
	 */
	public double getQueueEntryTime() {
		return queueEntryTime;
	}

	/**
	 * True: customer is in Business Class || False: customer is in Economy Class
	 *
	 * @return boolean that indicates if customer is in Business Class or not
	 */
	public boolean isBusinessClass() {
		return isBusinessClass;
	}

	/**
	 * True: customer's destination is in EU || False: customer's destination is out of EU
	 *
	 * @return boolean that indicates if customer is traveling inside EU or not
	 */
	public boolean isEUFlight() {
		return isEUFlight;
	}

	/**
	 * True: customer is checking in online || False: customer is checking in on-site
	 *
	 * @return boolean that indicates if customer is checking in online or not
	 */
	public boolean isOnlineCheckIn() {
		return isOnlineCheckIn;
	}

	/**
	 * Give the time when customer has been removed (from the system to be simulated)
	 *
	 * @return Customer removal time
	 */
	public double getRemovalTime() {
		return removalTime;
	}

	/**
	 * Mark the time when the customer has been removed (from the system to be simulated)
	 *
	 * @param removalTime Customer removal time
	 */
	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	/**
	 * Give the time when the customer arrived to the system to be simulated
	 *
	 * @return Customer arrival time
	 */
	public double getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Mark the time when the customer arrived to the system to be simulated
	 *
	 * @param arrivalTime Customer arrival time
	 */
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	/**
	 * Get the (unique) customer id
	 *
	 * @return Customer id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the queue number the Customer is currently on
	 *
	 * @param i queue number
	 */
	public void setCurrentQueueIndex(int i) {
		this.currentQueue = i;
	}

	/**
	 * Gets the number of the queue the Customer is currently on: used in MyEngine to
	 * remove from the queue correctly. Customer does not contain the current Service Point otherwise
	 *
	 * @return current queue index
	 */
	public int getCurrentQueueIndex() {
		return this.currentQueue;
	}

	/**
	 * Static method to reset the id var of the Customer Class: ensures that re-running simulation
	 * starts Customer ids from 1 again
	 */
	public static void resetId() {
		i = 1;
	}

	/**
	 * Static Method to get the sum of all service times within the Customer Class
	 *
	 * @return double that represents the sum of all service times of all customers
	 */
	public static double getServiceTimeSum() {
		return serviceTimeSum;
	}

	/**
	 * Static method to reset the Service Time Sum of the Customer Class
	 */
	public static void resetServiceTimeSum() {
		serviceTimeSum = 0;
	}

	/**
	 * Report the measured variables of the customer. In this case to the diagnostic output.
	 */
	public void reportResults() {
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

	/**
	 * Sets the customer's current position to the specified coordinates.
	 * This method also aligns the customer's target position with the given coordinates,
	 * effectively placing the customer directly at their target destination without requiring any movement.
	 *
	 * @param x the x-coordinate of the new position
	 * @param y the y-coordinate of the new position
	 */
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		this.targetX = x;
		this.targetY = y;
	}

	/**
	 * Set the target position of the customer
	 * @param tx
	 * @param ty
	 */
	public void setTargetPosition(double tx, double ty) {
		this.targetX = tx;
		this.targetY = ty;
	}

	/**
	 * Update the position of the customer
	 */
	public void updatePosition() {
		double dx = targetX - x;
		double dy = targetY - y;
		double dist = Math.sqrt(dx*dx + dy*dy);
		if(dist > speed) {
			x += speed * (dx/dist);
			y += speed * (dy/dist);
		} else {
			x = targetX;
			y = targetY;
		}
	}

	/**
	 * Get the x coordinate of the customer
	 *
	 * @return x coordinate
	 */
	public double getX() { return x; }

	/**
	 * Get the y coordinate of the customer
	 *
	 * @return y coordinate
	 */
	public double getY() { return y; }


	public boolean isVisible() {return visible;}


	public void setVisible(boolean visible) {this.visible = visible;}

}