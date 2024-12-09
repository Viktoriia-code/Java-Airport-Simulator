package model;

import java.util.Random;

/**
 * Customer Creator ensures that new created customers are distributed along the given
 * percentages for their types (business/economic class, in/out EU, online/on-site checkIn)
 */

public class CustomerCreator {
    private final double businessClassPercentage;
    private final double euFlightPercentage;
    private final double onlineCheckInPercentage;
    private final Random random;

    /**
     * Initializing the customer creator with the wanted percentages
     * @param businessClassPercentage How many customers out of 100 are in business Class
     * @param euFlightPercentage How many customers out of 100 are traveling inside EU
     * @param onlineCheckInPercentage How many customers out of 100 are checking in online
     * */
    public CustomerCreator(double businessClassPercentage, double euFlightPercentage, double onlineCheckInPercentage) {
        this.businessClassPercentage = businessClassPercentage;
        this.euFlightPercentage = euFlightPercentage;
        this.onlineCheckInPercentage = onlineCheckInPercentage;
        this.random = new Random();
    }

    /**
     * Randomly makes three boolean values that Customer class uses and creates and returns a
     * Customer with them. The randomization adheres to the percentages of the CustomerCreator.
     * @return A new Customer
     */
    public Customer createCustomer() {
        boolean isBusinessClass = this.random.nextDouble() < (this.businessClassPercentage / 100.0);
        boolean isEUFlight = this.random.nextDouble() < (this.euFlightPercentage / 100.0);
        boolean isOnlineCheckIn = this.random.nextDouble() < (this.onlineCheckInPercentage / 100.0);

        return new Customer(isBusinessClass, isEUFlight, isOnlineCheckIn);
    }
}