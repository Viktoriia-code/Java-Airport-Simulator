import eduni.distributions.Normal;
import framework.EventList;
import framework.Trace;
import model.Customer;
import model.CustomerCreator;
import model.EventType;
import model.ServicePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServicePointTest {
    @BeforeEach
    public void setUp() {
        Trace.setTraceLevel(Trace.Level.WAR);
    }

    @Test
    public void testServedCustomersHere() {
        ServicePoint sp1 = new ServicePoint("Check-In", new Normal(3, 1), new EventList(), EventType.DEP_CHECKIN);
        ServicePoint sp2 = new ServicePoint("Check-In", new Normal(3, 1), new EventList(), EventType.DEP_CHECKIN);

        assertEquals(0, sp1.getServedCustomersHere());
        assertEquals(0, sp2.getServedCustomersHere());

        for (int i = 1; i < 10; i++) {
            sp1.increaseServedCustomersHereByOne();
            assertEquals(i, sp1.getServedCustomersHere());
        }

        assertEquals(0, sp2.getServedCustomersHere());
    }

    @Test
    public void testQueue() {
        ServicePoint sp = new ServicePoint("Security", new Normal(3, 1), new EventList(), EventType.DEP_SECURITY);
        CustomerCreator cc = new CustomerCreator(50, 50, 50);

        assertEquals(0, sp.getQueueSize());

        for (int i = 0; i < 10; i++) {
            sp.addQueue(cc.createCustomer());
        }

        assertEquals(10, sp.getQueueSize());

        for (int i = 0; i < 5; i++) {
            sp.removeQueue();
        }

        assertEquals(5, sp.getQueueSize());
    }

    @Test
    public void testReserved(){
        ServicePoint sp = new ServicePoint("Security", new Normal(3, 1), new EventList(), EventType.DEP_SECURITY);
        sp.addQueue(new Customer(false, false, false));

        assertFalse(sp.isReserved());
        sp.beginService();
        assertTrue(sp.isOnQueue());
    }
}