package model;

import java.util.Random;

public class CustomerCreator {
    private final double businessClassPercentage;
    private final double euFlightPercentage;
    private final double onlineCheckOutPercentage;
    private final Random random;

    public CustomerCreator(double businessClassPercentage, double euFlightPercentage, double onlineCheckOutPercentage) {
        this.businessClassPercentage = businessClassPercentage;
        this.euFlightPercentage = euFlightPercentage;
        this.onlineCheckOutPercentage = onlineCheckOutPercentage;
        this.random = new Random();
    }

    public Customer createCustomer() {
        boolean isBusinessClass = this.random.nextDouble() < (this.businessClassPercentage / 100.0);
        boolean isEUFlight = this.random.nextDouble() < (this.euFlightPercentage / 100.0);
        boolean isOnlineCheckOut = this.random.nextDouble() < (this.onlineCheckOutPercentage / 100.0);

        return new Customer(isBusinessClass, isEUFlight, isOnlineCheckOut);
    }
}