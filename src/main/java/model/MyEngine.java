package model;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Normal;
import eduni.distributions.Negexp;
import framework.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

/**
 * Main simulator engine.
 *
 * Demo simulation case:
 * Simulate three service points, customer goes through all three service points to get serviced
 * 		--> SP1 --> SP2 --> SP3 --> SP4
 */
public class MyEngine extends Engine {
    /* basic variable initializing */
    private final ArrivalProcess arrivalProcess;
    private int servedClients;
    private double simulationTime;

    ArrayList<ServicePoint> checkInPoints = new ArrayList<>();

    ArrayList<ServicePoint> securityFastTrackPoints = new ArrayList<>();
    ArrayList<ServicePoint> securityPoints = new ArrayList<>();

    ArrayList<ServicePoint> borderControlPoints = new ArrayList<>();

    ArrayList<ServicePoint> boardingInEUPoints = new ArrayList<>();
    ArrayList<ServicePoint> boardingNotEUPoints = new ArrayList<>();

    ArrayList<ArrayList<ServicePoint>> allServicePoints = new ArrayList<>();

    double percentage_business_class;
    double percentage_inside_EU;
    double percentage_online_checkin;

    int num_checkin;
    int num_security;
    int num_security_fast;
    int num_border_control;
    int num_out_EU_boarding;
    int num_in_EU_boarding;

    public MyEngine() {
        /* some default values: feel free to replace, these have their own set-methods too */
        percentage_business_class = 20;
        percentage_inside_EU = 50;
        percentage_online_checkin = 10;

        num_checkin = 1;
        num_security = 1;
        num_security_fast = 1;
        num_border_control = 1;
        num_in_EU_boarding = 1;
        num_out_EU_boarding = 1;

        /* creates the customerCreator according to the percentages */
        CustomerCreator customerCreator = new CustomerCreator(percentage_business_class, percentage_inside_EU, percentage_online_checkin);

        /* resetting amount of served clients and simulation time */
        servedClients = 0;
        simulationTime = 0;

        /* creating and adding ServicePoints to the appropriate lists with varying service time */
        checkInPoints.addAll(createServicePoints("Check-in", num_checkin, new Normal(10, 6), EventType.DEP_CHECKIN));

        securityPoints.addAll(createServicePoints("Security check", num_security, new Normal(10, 10), EventType.DEP_SECURITY));
        securityFastTrackPoints.addAll(createServicePoints("Security check (Fast Track)", num_security_fast, new Normal(10, 10), EventType.DEP_SECURITY));

        borderControlPoints.addAll(createServicePoints("Border control", num_border_control, new Normal(5, 3), EventType.DEP_BORDERCTRL));

        boardingInEUPoints.addAll(createServicePoints("Boarding (inside EU)", num_in_EU_boarding, new Normal(5, 3), EventType.DEP_BOARDING));
        boardingNotEUPoints.addAll(createServicePoints("Boarding (outside EU)", num_out_EU_boarding, new Normal(5, 3), EventType.DEP_BOARDING));

        /* making arrivalProcess, customerCreator is also sent here */
        arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARRIVAL, customerCreator);

        /* gather and add all security points to the allServicePoints master array */
        allServicePoints.add(checkInPoints);
        allServicePoints.add(securityPoints);
        allServicePoints.add(securityFastTrackPoints);
        allServicePoints.add(borderControlPoints);
        allServicePoints.add(boardingInEUPoints);
        allServicePoints.add(boardingNotEUPoints);
    }

    /* method used in the to create the specific amount of service points without having to repeat code */
    private ArrayList<ServicePoint> createServicePoints(String name, int count, ContinuousGenerator serviceTime, EventType eventType) {
        ArrayList<ServicePoint> servicePoints = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            servicePoints.add(new ServicePoint(name, serviceTime, eventList, eventType));
        }
        return servicePoints;
    }

    /* in runEvent, this method is used to find which queue is the shortest.
     * this is how it is decided which queue customer goes next */
    public ServicePoint findShortestQueue(ArrayList<ServicePoint> servicePointArr) {
        /* given an array of ServicePoints, returns the one with the shortest queue */
        return servicePointArr.stream()
                .min(Comparator.comparingInt(ServicePoint::getQueueSize))
                .orElse(servicePointArr.get(new Random().nextInt(servicePointArr.size())));
    }

    @Override
    protected void initialize() {    // First arrival in the system
        Customer.resetId();
        arrivalProcess.generateNextEvent();
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        Customer c;
        ServicePoint q;

        /* due to changes in the Event Class, the Event now contains association to the customer (see usage of t.getCustomer())*/
        switch ((EventType) t.getType()) {
            /* if event type is ARRIVAL:
             * - customer is sent to the shortest check in queue
             * - in case customer has done online check in, they're sent to the appropriate security queue instead
             * - next arrivalProcess event is generated
             * - the index of which line customer is sent to is saved to customer (see Customer.getCurrentQueueIndex()) */
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

            case DEP_CHECKIN:
                /* using the saved index value, customer is retrieved from the right check-in queue */
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
                /* customers exiting border control are traveling out of EU:
                 * send to non-EU boarding queue */
                c = borderControlPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                q = findShortestQueue(boardingNotEUPoints);
                q.addQueue(c);
                c.setCurrentQueueIndex(boardingNotEUPoints.indexOf(q));
                break;

            /* after customer is done with boarding process,
             * the customer isn't placed in a new queue and is only removed from the appropriate
             * boarding queue. the time of leaving the system is saved to the customer and results are reported */
            case DEP_BOARDING:
                servedClients++;
                if (t.getCustomer().isEUFlight()) {
                    boardingInEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                } else {
                    boardingNotEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
                }
                t.getCustomer().setRemovalTime(Clock.getInstance().getClock());
                t.getCustomer().reportResults();
                break;
        }
    }

    /* C-events: checks if any of the service points have availability and if there's someone in the queue:
     * begins service if appropriate */
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

    /* displays simulation results */
    @Override
    public void results() {
        simulationTime = Clock.getInstance().getClock();
        Trace.out(Trace.Level.INFO, "Results:");
        Trace.out(Trace.Level.INFO, "Number of served clients: " + servedClients);
        Trace.out(Trace.Level.INFO, "Simulation ended at: " + Clock.getInstance().getClock());
        Trace.out(Trace.Level.INFO, "Mean service time: " + Clock.getInstance().getClock() / servedClients);
    }

    /* general getters and setters */
    public int getServedClients() {
        return servedClients;
    }

    public double getMeanServiceTime() {
        return simulationTime / servedClients;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public void setCheckInPoints(int amt) {
        num_checkin = amt;
    }

    public void setRegularSecurityPoints(int amt) {
        num_security = amt;
    }

    public void setFastSecurityPoints(int amt) {
        num_security_fast = amt;
    }

    public void setBorderControlPoints(int amt) {
        num_border_control = amt;
    }

    public void setOutEUBoardingPoints(int amt) {
        num_out_EU_boarding = amt;
    }

    public void setInEUBoardingPoints(int amt) {
        num_in_EU_boarding = amt;
    }


    public void setOnlineCheckInPercentage(double percentage) {
        percentage_online_checkin = percentage;
    }

    public void setInsideEUPercentage(double percentage) {
        percentage_inside_EU = percentage;
    }

    public void setBusinessClassPercentage(double percentage) {
        percentage_business_class = percentage;
    }


    /* method for setting all the Service Point amounts in one line */
    public void setAllServicePoints(int amount_of_check_in_points, int amount_of_regular_security,
                                         int amount_of_fast_security, int amount_of_border_control,
                                         int amount_of_IN_EU_boarding, int amount_of_OUT_EU_boarding) {
        setCheckInPoints(amount_of_check_in_points);
        setRegularSecurityPoints(amount_of_regular_security);
        setFastSecurityPoints(amount_of_fast_security);
        setBorderControlPoints(amount_of_border_control);
        setInEUBoardingPoints(amount_of_IN_EU_boarding);
        setOutEUBoardingPoints(amount_of_OUT_EU_boarding);
    }

    /* method for setting all the CustomerCreator percentages at once: note that the values should be between 0-100 */
    public void setAllCustomerPercentages(double onlineCheckInCustomers, double innerEUCustomers, double businessClassCustomers) {
        setOnlineCheckInPercentage(onlineCheckInCustomers);
        setInsideEUPercentage(innerEUCustomers);
        setBusinessClassPercentage(businessClassCustomers);
    }
}
