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
 * Demo simulation case:
 * Simulate three service points, customer goes through all three service points to get serviced
 * 		--> SP1 --> SP2 --> SP3 --> SP4
 */
public class MyEngine extends Engine {
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

    String maxQueueName = "";

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

    // Added fields for tracking average queue size
    private double totalQueueSizeSum = 0;
    private int queueMeasurementCount = 0;

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

    /**
     * Creates a given amount of Service Points
     * @param name The name shared between all the Service Points created here (e.g. "Border Control")
     * @param count How many Service Points like this should the returned array contain
     * @param serviceTime ContinuousGenerator that tells the ServicePoint class how long service should take 
     * @param eventType The EventType of Event that occurs when the service is done (e.g. DEP_CHECKIN)
     * @return An ArrayList that contains the Service Points
     */
    private ArrayList<ServicePoint> createServicePoints(String name, int count, ContinuousGenerator serviceTime, EventType eventType) {
        ArrayList<ServicePoint> servicePoints = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            servicePoints.add(new ServicePoint(name, serviceTime, eventList, eventType));
        }
        return servicePoints;
    }

    /**
     * Finds the Service Point that the Customer is put into: either takes the first shortest one, or
     * in case of queue sizes being the same, picks a random Service Point
     * @param servicePointArr ArrayList of Service Points that the ServicePoint should be from
     * @return Service Point with either the shortest queue or a random one if shortest can't be found
     */
    public ServicePoint findShortestQueue(ArrayList<ServicePoint> servicePointArr) {
        return servicePointArr.stream()
                .min(Comparator.comparingInt(ServicePoint::getQueueSize))
                .orElse(servicePointArr.get(new Random().nextInt(servicePointArr.size())));
    }

    /**
     * Generates first arrival in the system, creates Service Points according to given numbers,
     * creates CustomerCreator with proper percentages, resets ServiceTimeSum and id of Customer Class
     */
    @Override
    protected void initialize() {
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

    /**
     * B-Phase events: takes the event and calls the method that corresponds its EventType
     * @param t The event to be executed
     */
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

    /**
     * Handling of ARRIVAL event. If associated Customer has done online Check-In, places them
     * to Security: if not, places them to Check-In. Generates next arrival event.
     * @param t The event that is being handled
     */
    private void handleArrival(Event t) {
        Customer c = t.getCustomer();
        ServicePoint q;
        if (c.isOnlineCheckIn()) {
            q = findShortestQueue(c.isBusinessClass() ? securityFastTrackPoints : securityPoints);
            c.setCurrentQueueIndex(c.isBusinessClass() ? securityFastTrackPoints.indexOf(q) : securityPoints.indexOf(q));
        } else {
            q = findShortestQueue(checkInPoints);
            c.setCurrentQueueIndex(checkInPoints.indexOf(q));
        }
        q.addQueue(c);
        arrivalProcess.generateNextEvent();
    }

    /**
     * Handling of DEP_CHECKIN. Customer is removed from the Check-In queue and placed
     * in Security queue (either Fast Track or regular, depending on if Customer is in Business Class)
     * @param t The event that is being handled
     */
    private void handleDepCheckin(Event t) {
        Customer c = checkInPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        ServicePoint q = findShortestQueue(c.isBusinessClass() ? securityFastTrackPoints : securityPoints);
        q.addQueue(c);
        c.setCurrentQueueIndex(c.isBusinessClass() ? securityFastTrackPoints.indexOf(q) : securityPoints.indexOf(q));
    }

    /**
     * Handling of DEP_SECURITY. Removes Customer from the right queue and places them to
     * In-EU Boarding or Border Control, depending on if their destination is in or out of EU.
     * @param t The event that is being handled
     */
    private void handleDepSecurity(Event t) {
        Customer c = t.getCustomer().isBusinessClass() ?
                securityFastTrackPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue() :
                securityPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        ServicePoint q = findShortestQueue(c.isEUFlight() ? boardingInEUPoints : borderControlPoints);
        q.addQueue(c);
        c.setCurrentQueueIndex(c.isEUFlight() ? boardingInEUPoints.indexOf(q) : borderControlPoints.indexOf(q));
    }

    /**
     * Handling of DEP_BORDERCTRL. Removes Customer from Border Control queue, places them
     * in out of EU Boarding.
     * @param t The event that is being handled
     */
    private void handleDepBorderCtrl(Event t) {
        Customer c = borderControlPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        ServicePoint q = findShortestQueue(boardingNotEUPoints);
        q.addQueue(c);
        c.setCurrentQueueIndex(boardingNotEUPoints.indexOf(q));
    }

    /**
     * Handling of DEP_BOARDING. Removes Customer from the boarding queue, sets the Customer's removal
     * time to the current time, adds +1 to the total served clients in this simulation run.
     * @param t The event that is being handled
     */
    private void handleDepBoarding(Event t) {
        Customer c = t.getCustomer().isEUFlight() ?
                boardingInEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue() :
                boardingNotEUPoints.get(t.getCustomer().getCurrentQueueIndex()).removeQueue();
        c.setRemovalTime(Clock.getInstance().getClock());
        c.reportResults();
        servedClients++;
    }

    /**
     * C-Phase Events: checks through every ServicePoint in the system and checks if they're currently
     * free and if there is someone in the queue. If so, ServicePoint's beginService() method is called.
     */
    @Override
    protected void tryCEvents() {
        for (ArrayList<ServicePoint> servicePointList : allServicePoints) {
            for (ServicePoint p : servicePointList) {
                if (!p.isReserved() && p.isOnQueue()) {
                    p.beginService();
                }
            }
            collectQueueSizes();
        }
    }

    /**
     * Displays Simulation results with Trace
     * */
    @Override
    public void results() {
        for (ArrayList<ServicePoint> servicePointList : allServicePoints) {
            for (ServicePoint p : servicePointList) {
                Trace.out(Trace.Level.INFO, p.getName() + " #" + servicePointList.indexOf(p));
                Trace.out(Trace.Level.INFO, "  - " + p.getServedCustomersHere() + " customers served");
                Trace.out(Trace.Level.INFO, "  - Longest queue: " + p.getLongestQueueSize() + " customer" + (p.getLongestQueueSize() > 1 ? "s" : ""));
                Trace.out(Trace.Level.INFO, "  - Average Queue Time: " + p.getAverageQueueTime());
            }
        }

        simulationTime = Clock.getInstance().getClock();

        Trace.out(Trace.Level.INFO, "Results:");
        Trace.out(Trace.Level.INFO, "Number of served clients: " + getServedClients());
        Trace.out(Trace.Level.INFO, "Simulation ended at: " + Clock.getInstance().getClock());
        Trace.out(Trace.Level.INFO, "Mean service time: " + Customer.getServiceTimeSum() / getServedClients());
        Trace.out(Trace.Level.INFO, "Longest queue: " + getLongestQueueSize() + " customers at " + getLongestQueueSPName());
        Trace.out(Trace.Level.INFO, "Average Queue Size: " + getAverageQueueSize());
    }

    /**
     * Gets all Service Points
     * @return all Service Points: notice it returns 6 ArrayLists inside one ArrayList
     */

    public ArrayList<ArrayList<ServicePoint>> getAllServicePoints(){
        return allServicePoints;
    }

    /**
     * Find the name of the SP with the longest queue
     * @return string that consists of SP name + index in the array of similar SPs (e.g. "Check-In #4")
     */

    public String getLongestQueueSPName(){
        int maxQueueSize = 0;
        String longestQueueSPName = "";

        for (ArrayList<ServicePoint> arr : allServicePoints){
            for (ServicePoint sp : arr){
                if (maxQueueSize <= sp.getLongestQueueSize()){
                    maxQueueSize = sp.getLongestQueueSize();
                    longestQueueSPName = sp.getName() + " #" + arr.indexOf(sp);
                }
            }
        }

        return longestQueueSPName;
    }

    /**
     * Find the size of the longest queue
     * @return size of the longest queue
     */

    public int getLongestQueueSize(){
        int maxQueueSize = 0;

        for (ArrayList<ServicePoint> arr : allServicePoints){
            for (ServicePoint sp : arr){
                if (maxQueueSize <= sp.getLongestQueueSize()){
                    maxQueueSize = sp.getLongestQueueSize();
                }
            }
        }

        return maxQueueSize;
    }

    /**
     * Gets the number of served clients.
     * @return the number of served clients
     */
    public int getServedClients() {
        return servedClients;
    }

    /**
     * Gets the mean service time.
     * @return the mean service time
     */
    public double getAvServiceTime() {
        return Customer.getServiceTimeSum() / getServedClients();
    }

    /**
     * Gets the simulation time.
     * @return the simulation time
     */
    public double getSimulationTime() {
        return simulationTime;
    }

    /**
     * Sets the number of check-in points.
     * @param amt the number of check-in points
     */
    public void setCheckInPoints(int amt) {
        num_checkin = amt;
    }

    /**
     * Sets the number of regular security points.
     * @param amt the number of regular security points
     */
    public void setRegularSecurityPoints(int amt) {
        num_security = amt;
    }

    /**
     * Sets the number of Fast Track security points.
     * @param amt the number of Fast Track security points
     */
    public void setFastSecurityPoints(int amt) {
        num_security_fast = amt;
    }

    /**
     * Sets the number of border control points.
     * @param amt the number of border control points
     */
    public void setBorderControlPoints(int amt) {
        num_border_control = amt;
    }

    /**
     * Sets the number of out-EU boarding points.
     * @param amt the number of out-EU boarding points
     */
    public void setOutEUBoardingPoints(int amt) {
        num_out_EU_boarding = amt;
    }

    /**
     * Sets the number of in-EU boarding points.
     * @param amt the number of in-EU boarding points
     */
    public void setInEUBoardingPoints(int amt) {
        num_in_EU_boarding = amt;
    }

    /**
     * Sets the percentage of online check-in.
     * @param percentage the percentage of online check-in
     */
    public void setOnlineCheckInPercentage(double percentage) {
        percentage_online_checkin = percentage;
    }

    /**
     * Sets the percentage of inside EU flights.
     * @param percentage the percentage of inside EU flights
     */
    public void setInsideEUPercentage(double percentage) {
        percentage_inside_EU = percentage;
    }

    /**
     * Sets the percentage of business class customers.
     * @param percentage the percentage of business class customers
     */
    public void setBusinessClassPercentage(double percentage) {
        percentage_business_class = percentage;
    }

    /**
     * Sets the mean arrival time.
     * @param mean the mean arrival time
     */
    public void setArrivalMean(double mean){
        arrival_mean = mean;
    }

    /**
     * Sets the mean check-in time.
     * @param mean the mean check-in time
     */
    public void setCheckInMean(double mean){
        checkIn_mean = mean;
    }

    /**
     * Sets the mean border control time.
     * @param mean the mean border control time
     */
    public void setBorderControlMean(double mean){
        borderControl_mean = mean;
    }

    /**
     * Sets the mean security time.
     * @param mean the mean security time
     */
    public void setSecurityMean(double mean){
        security_mean = mean;
    }

    /**
     * Periodically collects queue sizes across all service points.
     * This should be called during simulation ticks or at regular intervals.
     */
    private void collectQueueSizes() {
        for (ArrayList<ServicePoint> servicePointList : allServicePoints) {
            for (ServicePoint p : servicePointList) {
                totalQueueSizeSum += p.getQueueSize();
            }
        }
        queueMeasurementCount++;
    }

    /**
     * Computes the average queue size at the end of the simulation.
     * @return the average queue size across all service points
     */
    public int getAverageQueueSize() {
        return queueMeasurementCount == 0 ? 0 : (int)totalQueueSizeSum / queueMeasurementCount;
    }

    /**
     * Sets the mean boarding time.
     * @param mean the mean boarding time
     */
    public void setBoardingMean(double mean){
        boarding_mean = mean;
    }

    /* method for setting all the Service Point amounts in one line */

    /**
     * Set amounts of all Service Points at once
     * @param amount_of_check_in_points amount of check-in service points
     * @param amount_of_regular_security amount of security (Non-Fast Track) service points
     * @param amount_of_fast_security amount of security (Fast Track) service points
     * @param amount_of_border_control amount of Border Control service points
     * @param amount_of_IN_EU_boarding amount of boarding (inside EU) service points
     * @param amount_of_OUT_EU_boarding amount of boarding (outside EU) service points
     */
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

    /**
     * Sets all Customer related percentages at once. Note that values should be between 0-100.
     * @param onlineCheckInCustomers percentage of customers checking in online (skipping on-site check-in)
     * @param innerEUCustomers percentage of customers traveling inside of EU (skipping Border Control)
     * @param businessClassCustomers percentage of business class customers (put in fast track security)
     */
    public void setAllCustomerPercentages(double onlineCheckInCustomers, double innerEUCustomers, double businessClassCustomers) {
        setOnlineCheckInPercentage(onlineCheckInCustomers);
        setInsideEUPercentage(innerEUCustomers);
        setBusinessClassPercentage(businessClassCustomers);
    }

    /**
     * Set all timing means at once
     * @param arrival mean time between customer arrivals
     * @param checkIn mean for the time of check-in service
     * @param borderControl mean for the time of border control service
     * @param security mean for the time of security service
     * @param boarding mean for the time of boarding service
     */
    public void setAllTimingMeans(double arrival, double checkIn, double borderControl, double security, double boarding){
        setArrivalMean(arrival);
        setCheckInMean(checkIn);
        setBorderControlMean(borderControl);
        setSecurityMean(security);
        setBoardingMean(boarding);
    }
}