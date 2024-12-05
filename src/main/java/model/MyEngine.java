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
    private ArrivalProcess arrivalProcess;
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

    double arrival_mean;
    double checkIn_mean;
    double security_mean;
    double borderControl_mean;
    double boarding_mean;

    public MyEngine() {
        /* customer distribution percentages (0-100) */
        this.percentage_business_class = 40;
        this.percentage_inside_EU = 50;
        this.percentage_online_checkin = 50;

        /* amount of each service point station */
        this.num_checkin = 9;
        this.num_security = 2;
        this.num_security_fast = 3;
        this.num_border_control = 2;
        this.num_in_EU_boarding = 2;
        this.num_out_EU_boarding = 2;

        /* timing parameters */
        this.arrival_mean = 5;
        this.checkIn_mean = 30;
        this.security_mean = 10;
        this.borderControl_mean = 10;
        this.boarding_mean = 10;

        /* resetting amount of served clients and simulation time */
        this.servedClients = 0;
        this.simulationTime = 0;
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
        /* creating and adding ServicePoints to the appropriate lists with varying service time */
        this.checkInPoints.addAll(createServicePoints("Check-in", num_checkin, new Normal(checkIn_mean, 6), EventType.DEP_CHECKIN));

        this.securityPoints.addAll(createServicePoints("Security check", num_security, new Normal(security_mean, 20), EventType.DEP_SECURITY));
        this.securityFastTrackPoints.addAll(createServicePoints("Security check (Fast Track)", num_security_fast, new Normal(security_mean, 10), EventType.DEP_SECURITY));

        this.borderControlPoints.addAll(createServicePoints("Border control", num_border_control, new Normal(borderControl_mean, 3), EventType.DEP_BORDERCTRL));

        this.boardingInEUPoints.addAll(createServicePoints("Boarding (inside EU)", num_in_EU_boarding, new Normal(boarding_mean, 3), EventType.DEP_BOARDING));
        this.boardingNotEUPoints.addAll(createServicePoints("Boarding (outside EU)", num_out_EU_boarding, new Normal(boarding_mean, 3), EventType.DEP_BOARDING));

        /* creating the customerCreator according to the percentages */
        CustomerCreator customerCreator = new CustomerCreator(this.percentage_business_class, this.percentage_inside_EU, this.percentage_online_checkin);
        /* making arrivalProcess, customerCreator is also sent here */
        this.arrivalProcess = new ArrivalProcess(new Negexp(arrival_mean), eventList, EventType.ARRIVAL, customerCreator);

        /* gather and add all security points to the allServicePoints master array */
        this.allServicePoints.add(checkInPoints);
        this.allServicePoints.add(securityPoints);
        this.allServicePoints.add(securityFastTrackPoints);
        this.allServicePoints.add(borderControlPoints);
        this.allServicePoints.add(boardingInEUPoints);
        this.allServicePoints.add(boardingNotEUPoints);

        Customer.resetId();
        Customer.resetServiceTimeSum();
        this.arrivalProcess.generateNextEvent();
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        switch ((EventType) t.getType()) {
            case ARRIVAL:
                handleArrival(t);
                break;
            case DEP_CHECKIN:
                handleDepCheckin(t);
                break;
            case DEP_SECURITY:
                handleDepSecurity(t);
                break;
            case DEP_BORDERCTRL:
                handleDepBorderCtrl(t);
                break;
            case DEP_BOARDING:
                handleDepBoarding(t);
                break;
        }
    }

    private void handleArrival(Event t) {
        Customer c = t.getCustomer();
        ServicePoint q;
        if (c.isOnlineCheckOut()) {
            q = findShortestQueue(c.isBusinessClass() ? securityFastTrackPoints : securityPoints);
            c.setCurrentQueueIndex(c.isBusinessClass() ? securityFastTrackPoints.indexOf(q) : securityPoints.indexOf(q));
        } else {
            q = findShortestQueue(checkInPoints);
            c.setCurrentQueueIndex(checkInPoints.indexOf(q));
        }
        q.addQueue(c);
        arrivalProcess.generateNextEvent();
    }

    private void handleDepCheckin(Event t) {
        Customer c = checkInPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        ServicePoint q = findShortestQueue(c.isBusinessClass() ? securityFastTrackPoints : securityPoints);
        q.addQueue(c);
        c.setCurrentQueueIndex(c.isBusinessClass() ? securityFastTrackPoints.indexOf(q) : securityPoints.indexOf(q));
    }

    private void handleDepSecurity(Event t) {
        Customer c = t.getCustomer().isBusinessClass() ?
                securityFastTrackPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue() :
                securityPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        ServicePoint q = findShortestQueue(c.isEUFlight() ? boardingInEUPoints : borderControlPoints);
        q.addQueue(c);
        c.setCurrentQueueIndex(c.isEUFlight() ? boardingInEUPoints.indexOf(q) : borderControlPoints.indexOf(q));
    }

    private void handleDepBorderCtrl(Event t) {
        Customer c = borderControlPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        ServicePoint q = findShortestQueue(boardingNotEUPoints);
        q.addQueue(c);
        c.setCurrentQueueIndex(boardingNotEUPoints.indexOf(q));
    }

    private void handleDepBoarding(Event t) {
        Customer c = t.getCustomer().isEUFlight() ?
                boardingInEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue() :
                boardingNotEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        c.setRemovalTime(Clock.getInstance().getClock());
        c.reportResults();
        servedClients++;
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
        int maxQueueSize = 0;
        String longestQueueSPName = ""; 
        
        for (ArrayList<ServicePoint> servicePointList : allServicePoints) {
            for (ServicePoint p : servicePointList) {
                Trace.out(Trace.Level.INFO, p.getName() + " #" + servicePointList.indexOf(p));
                Trace.out(Trace.Level.INFO, "  - " + p.getServedCustomersHere() + " customers served");
                Trace.out(Trace.Level.INFO, "  - Longest queue: " + p.getLongestQueueSize() + " customer" + (p.getLongestQueueSize() > 1 ? "s" : ""));
                Trace.out(Trace.Level.INFO, "  - Average Queue Time: " + p.getAverageQueueTime());

                if (p.getLongestQueueSize() > maxQueueSize){
                    maxQueueSize = p.getLongestQueueSize();
                    longestQueueSPName = (p.getName() + " #" + servicePointList.indexOf(p));
                }
            }
        }

        simulationTime = Clock.getInstance().getClock();
        Trace.out(Trace.Level.INFO, "Results:");
        Trace.out(Trace.Level.INFO, "Number of served clients: " + servedClients);
        Trace.out(Trace.Level.INFO, "Simulation ended at: " + Clock.getInstance().getClock());
        Trace.out(Trace.Level.INFO, "Mean service time: " + Customer.getServiceTimeSum() / servedClients);
        Trace.out(Trace.Level.INFO, "Longest queue: " + maxQueueSize + " customers at " + longestQueueSPName);
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

    public void setArrivalMean(double mean){
        arrival_mean = mean;
    }

    public void setCheckInMean(double mean){
        checkIn_mean = mean;
    }

    public void setBorderControlMean(double mean){
        borderControl_mean = mean;
    }

    public void setSecurityMean(double mean){
        security_mean = mean;
    }

    public void setBoardingMean(double mean){
        boarding_mean = mean;
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

    public void setAllTimingMeans(double arrival, double checkIn, double borderControl, double security, double boarding){
        setArrivalMean(arrival);
        setCheckInMean(checkIn);
        setBorderControlMean(borderControl);
        setSecurityMean(security);
        setBoardingMean(boarding);
    }
}