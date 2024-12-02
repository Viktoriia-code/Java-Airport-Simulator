package model;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Normal;
import eduni.distributions.Negexp;
import framework.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class MyEngine extends Engine {
    private final ArrivalProcess arrivalProcess;
    public static final boolean TEXTDEMO = true;
    public static final boolean FIXEDARRIVALTIMES = false;
    public static final boolean FIXEDSERVICETIMES = false;
    private int servedClients = 0;

    /* how many customers out of 100 approximately are in business class
     * -> using a different security check line */
    private final double PERCENTAGE_BUSINESS_CLASS = 20;

    /* how many customers out of 100 approximately are heading to an inside EU flight
     * -> customers inside EU skip border control */
    private final double PERCENTAGE_INSIDE_EU = 50;

    /* how many customers out of 100 approximately did online checkout
     * -> skip check out line and go straight to security*/
    private final double PERCENTAGE_ONLINE_CHECKOUT = 10;

    /* how many of each Service Points of each type are created (= how many queues)
     * NOTE: to disable fast track or outside EU boarding, both the stations and the corresponding
     * percentages above have to be 0 */
    private final int NUM_CHECKIN = 3;

    private final int NUM_SECURITY = 2;
    private final int NUM_SECURITY_FAST_TRACK = 1;

    private final int NUM_BORDERCTRL = 5;

    private final int NUM_INSIDE_EU_BOARDING = 3;
    private final int NUM_OUTSIDE_EU_BOARDING = 3;

    CustomerCreator customerCreator = new CustomerCreator(PERCENTAGE_BUSINESS_CLASS, PERCENTAGE_INSIDE_EU, PERCENTAGE_ONLINE_CHECKOUT);

    ArrayList<ServicePoint> checkInPoints = new ArrayList<>();

    ArrayList<ServicePoint> securityFastTrackPoints = new ArrayList<>();
    ArrayList<ServicePoint> securityPoints = new ArrayList<>();

    ArrayList<ServicePoint> borderControlPoints = new ArrayList<>();

    ArrayList<ServicePoint> boardingInEUPoints = new ArrayList<>();
    ArrayList<ServicePoint> boardingNotEUPoints = new ArrayList<>();

    ArrayList<ArrayList<ServicePoint>> allServicePoints = new ArrayList<>();

    /*
     * This is the place where you implement your own simulator
     *
     * Simulation case:
     * Simulate four service points, customer goes through all four service points to get serviced
     * 		--> SP1 --> SP2 --> SP3 --> SP4
     *      --> Check-in --> Security check --> Border Control --> Boarding
     */

    public MyEngine() {
        allServicePoints.add(checkInPoints);
        allServicePoints.add(securityFastTrackPoints);
        allServicePoints.add(securityPoints);
        allServicePoints.add(borderControlPoints);

        allServicePoints.add(boardingInEUPoints);
        allServicePoints.add(boardingNotEUPoints);

        if (TEXTDEMO) {
            /* special setup for the example in text
             * https://github.com/jacquesbergelius/PP-CourseMaterial/blob/master/1.1_Introduction_to_Simulation.md
             */
            Random r = new Random();

            ContinuousGenerator arrivalTime;
            if (FIXEDARRIVALTIMES) {
                /* version where the arrival times are constant (and greater than service times) */

                // make a special "random number distribution" which produces constant value for the customer arrival times
                arrivalTime = new ContinuousGenerator() {
                    @Override
                    public double sample() {
                        return 2;
                    }

                    @Override
                    public void setSeed(long seed) {
                    }

                    @Override
                    public long getSeed() {
                        return 0;
                    }

                    @Override
                    public void reseed() {
                    }
                };
            } else
                // exponential distribution is used to model customer arrivals times, to get variability between programs runs, give a variable seed
                arrivalTime = new Negexp(10, Integer.toUnsignedLong(r.nextInt()));

            ContinuousGenerator serviceTime;
            if (FIXEDSERVICETIMES) {
                // make a special "random number distribution" which produces constant value for the service time in service points
                serviceTime = new ContinuousGenerator() {
                    @Override
                    public double sample() {
                        return 9;
                    }

                    @Override
                    public void setSeed(long seed) {
                    }

                    @Override
                    public long getSeed() {
                        return 0;
                    }

                    @Override
                    public void reseed() {
                    }
                };
            } else {
                // normal distribution used to model service times
                serviceTime = new Normal(10, 6, Integer.toUnsignedLong(r.nextInt()));
            }

            checkInPoints.addAll(createServicePoints("Check-in", NUM_CHECKIN, serviceTime, EventType.DEP_CHECKIN));

            securityPoints.addAll(createServicePoints("Security check", NUM_SECURITY, serviceTime, EventType.DEP_SECURITY));
            securityFastTrackPoints.addAll(createServicePoints("Security check (Fast Track)", NUM_SECURITY_FAST_TRACK, serviceTime, EventType.DEP_SECURITY));

            borderControlPoints.addAll(createServicePoints("Border control", NUM_BORDERCTRL, serviceTime, EventType.DEP_BORDERCTRL));

            boardingInEUPoints.addAll(createServicePoints("Boarding (inside EU)", NUM_INSIDE_EU_BOARDING, serviceTime, EventType.DEP_BOARDING));
            boardingNotEUPoints.addAll(createServicePoints("Boarding (outside EU)", NUM_OUTSIDE_EU_BOARDING, serviceTime, EventType.DEP_BOARDING));

            arrivalProcess = new ArrivalProcess(arrivalTime, eventList, EventType.ARRIVAL, customerCreator);
        } else {
            checkInPoints.addAll(createServicePoints("Check-in", NUM_CHECKIN, new Normal(10, 6), EventType.DEP_CHECKIN));

            securityPoints.addAll(createServicePoints("Security check", NUM_SECURITY, new Normal(10, 10), EventType.DEP_SECURITY));
            securityFastTrackPoints.addAll(createServicePoints("Security check (Fast Track)", NUM_SECURITY_FAST_TRACK, new Normal(10, 10), EventType.DEP_SECURITY));

            borderControlPoints.addAll(createServicePoints("Border control", NUM_BORDERCTRL, new Normal(5, 3), EventType.DEP_BORDERCTRL));

            boardingInEUPoints.addAll(createServicePoints("Boarding (inside EU)", NUM_INSIDE_EU_BOARDING, new Normal(5, 3), EventType.DEP_BOARDING));
            boardingNotEUPoints.addAll(createServicePoints("Boarding (outside EU)", NUM_OUTSIDE_EU_BOARDING, new Normal(5, 3), EventType.DEP_BOARDING));

            arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARRIVAL, customerCreator);
        }
    }

    private ArrayList<ServicePoint> createServicePoints(String name, int count, ContinuousGenerator serviceTime, EventType eventType) {
        ArrayList<ServicePoint> servicePoints = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            servicePoints.add(new ServicePoint(name, serviceTime, eventList, eventType));
        }
        return servicePoints;
    }

    public ServicePoint findShortestQueue(ArrayList<ServicePoint> servicePointArr) {
        /* given an array of ServicePoints, returns the one with the shortest queue */
        return servicePointArr.stream()
                .min(Comparator.comparingInt(ServicePoint::getQueueSize))
                .orElse(servicePointArr.get(0));
    }

    @Override
    protected void initialize() {    // First arrival in the system
        arrivalProcess.generateNextEvent();
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        Customer c;
        ServicePoint q;

        switch ((EventType) t.getType()) {
            /* use customer that was created during event creation via t.getCustomer,
             * place them in the shortest check-in queue */
            case ARRIVAL:
                c = t.getCustomer();
                if (c.isOnlineCheckOut()) {
                    if (c.isBusinessClass()) {
                        q = findShortestQueue(securityFastTrackPoints);
                        q.addQueue(c);
                        c.setCurrentQueueIndex(securityFastTrackPoints.indexOf(q));
                    } else {
                        q = findShortestQueue(securityPoints);
                        q.addQueue(c);
                        c.setCurrentQueueIndex(securityPoints.indexOf(q));
                    }
                } else {
                    q = findShortestQueue(checkInPoints);
                    q.addQueue(c);
                    c.setCurrentQueueIndex(checkInPoints.indexOf(q));
                }
                arrivalProcess.generateNextEvent();
                break;

            /* using the customer that is associated with the event,
             * get the current index of the queue the associated customer is at
             * and remove them from the queue: place them in the shortest queue of next station */
            case DEP_CHECKIN:
                c = checkInPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();

                /* put the customer to the proper queue according to whether they're in
                 * business class or not */

                if (c.isBusinessClass()) {
                    q = findShortestQueue(securityFastTrackPoints);
                    c.setCurrentQueueIndex(securityFastTrackPoints.indexOf(q));
                } else {
                    q = findShortestQueue(securityPoints);
                    c.setCurrentQueueIndex(securityPoints.indexOf(q));
                }

                q.addQueue(c);
                break;

            case DEP_SECURITY:
                /* check if event-associated customer is in business class or not:
                 * remove from appropriate queue */
                if (t.getCustomer().isBusinessClass()) {
                    c = securityFastTrackPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                } else {
                    c = securityPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                }

                /* skip border control and go straight to in-EU boarding if customer is not
                 * flying outside europe: otherwise go to border control queue */
                if (t.getCustomer().isEUFlight()) {
                    q = findShortestQueue(boardingInEUPoints);
                    q.addQueue(c);
                    c.setCurrentQueueIndex(boardingInEUPoints.indexOf(q));
                } else {
                    q = findShortestQueue(borderControlPoints);
                    q.addQueue(c);
                    c.setCurrentQueueIndex(borderControlPoints.indexOf(q));
                }
                break;

            case DEP_BORDERCTRL:
                if (!t.getCustomer().isEUFlight()) {
                    c = borderControlPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                    q = findShortestQueue(boardingNotEUPoints);
                    q.addQueue(c);
                    c.setCurrentQueueIndex(boardingNotEUPoints.indexOf(q));
                }
                break;

            /* after DEP_BOARDING event is fired (= customer is done with boarding process),
             * the customer isn't placed in a new queue and is only removed from the appropriate
             * boarding queue. the time of leaving the system is saved to the customer */
            case DEP_BOARDING:
                if (t.getCustomer().isEUFlight()) {
                    boardingInEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                } else {
                    boardingNotEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                }
                t.getCustomer().setRemovalTime(Clock.getInstance().getClock());
                t.getCustomer().reportResults();
                servedClients++;
                break;
        }
    }

    @Override
    protected void tryCEvents() {
        for (ArrayList<ServicePoint> servicePointList : allServicePoints) {
            for (ServicePoint p : servicePointList) {
                if (!p.isReserved() && p.isOnQueue()) {
                    p.beginService();
                }
            }
        }
    }

    @Override
    protected void results() {
        System.out.println("Simulation ended at " + Clock.getInstance().getClock());
        System.out.println("Results:");
        System.out.println("Number of served clients: " + servedClients);
    }
}