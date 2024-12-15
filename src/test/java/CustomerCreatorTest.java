import framework.Trace;
import model.Customer;
import model.CustomerCreator;

import model.MyEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomerCreatorTest {
    @BeforeEach
    public void setUp(){
        Trace.setTraceLevel(Trace.Level.WAR);
    }

    @Test
    public void testBusinessClassPercentage(){
        CustomerCreator cc1 = new CustomerCreator(100, 50, 50);
        CustomerCreator cc2 = new CustomerCreator(0, 50, 50);

        for (int i = 0; i < 100; i++){
            Customer customer1 = cc1.createCustomer();
            assertTrue(customer1.isBusinessClass());

            Customer customer2 = cc2.createCustomer();
            assertFalse(customer2.isBusinessClass());
        }
    }

    @Test
    public void testEuFlight(){
        CustomerCreator cc1 = new CustomerCreator(50, 100, 50);
        CustomerCreator cc2 = new CustomerCreator(50, 0, 50);

        for (int i = 0; i < 100; i++){
            Customer customer1 = cc1.createCustomer();
            assertTrue(customer1.isEUFlight());

            Customer customer2 = cc2.createCustomer();
            assertFalse(customer2.isEUFlight());
        }
    }

    @Test
    public void testOnlineCheckIn(){
        CustomerCreator cc1 = new CustomerCreator(50, 50, 100);
        CustomerCreator cc2 = new CustomerCreator(50, 50, 0);

        for (int i = 0; i < 100; i++){
            Customer customer1 = cc1.createCustomer();
            assertTrue(customer1.isOnlineCheckIn());

            Customer customer2 = cc2.createCustomer();
            assertFalse(customer2.isOnlineCheckIn());
        }
    }
}