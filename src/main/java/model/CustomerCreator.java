package model;

import java.util.Random;

public class CustomerCreator {
    private final double businessClassPercentage;
    private final double euFlightPercentage;
    private final Random random;

    public CustomerCreator(double businessClassPercentage, double euFlightPercentage) {
        this.businessClassPercentage = businessClassPercentage;
        this.euFlightPercentage = euFlightPercentage;
        this.random = new Random();
    }

    public Customer createCustomer() {
        boolean isBusinessClass = random.nextDouble() < businessClassPercentage;
        boolean isEUFlight = random.nextDouble() < euFlightPercentage;
        return new Customer(isBusinessClass, isEUFlight);
    }
}