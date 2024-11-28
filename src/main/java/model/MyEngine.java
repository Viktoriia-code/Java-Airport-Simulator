package model;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Normal;
import eduni.distributions.Negexp;
import framework.*;

import java.util.Random;

public class MyEngine extends Engine {
	private ArrivalProcess arrivalProcess;
	private ServicePoint[] servicePoints;
	public static final boolean TEXTDEMO = true;
	public static final boolean FIXEDARRIVALTIMES = false;
	public static final boolean FIXEDSERVICETIMES = false;
	private int servedClients = 0;

	/*
	 * This is the place where you implement your own simulator
	 *
	 * Simulation case:
	 * Simulate four service points, customer goes through all four service points to get serviced
	 * 		--> SP1 --> SP2 --> SP3 --> SP4
	 *      --> Check-in --> Security check --> Border Control --> Boarding
	 */
	public MyEngine() {
		servicePoints = new ServicePoint[4];

		if (TEXTDEMO) {
			/* special setup for the example in text
			 * https://github.com/jacquesbergelius/PP-CourseMaterial/blob/master/1.1_Introduction_to_Simulation.md
			 */
			Random r = new Random();

			ContinuousGenerator arrivalTime = null;
			if (FIXEDARRIVALTIMES) {
				/* version where the arrival times are constant (and greater than service times) */

				// make a special "random number distribution" which produces constant value for the customer arrival times
				arrivalTime = new ContinuousGenerator() {
					@Override
					public double sample() {
						return 10;
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

			ContinuousGenerator serviceTime = null;
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
			} else
				// normal distribution used to model service times
				serviceTime = new Normal(10, 6, Integer.toUnsignedLong(r.nextInt()));

			servicePoints[0] = new ServicePoint("Check-in", serviceTime, eventList, EventType.DEP1);
			servicePoints[1] = new ServicePoint("Security check", serviceTime, eventList, EventType.DEP2);
			servicePoints[2] = new ServicePoint("Border control", serviceTime, eventList, EventType.DEP3);
			servicePoints[3] = new ServicePoint("Boarding", serviceTime, eventList, EventType.DEP4);

			arrivalProcess = new ArrivalProcess(arrivalTime, eventList, EventType.ARR1);
		} else {
			/* more realistic simulation case with variable customer arrival times and service times */
			servicePoints[0] = new ServicePoint("Check-in", new Normal(10, 6), eventList, EventType.DEP1);
			servicePoints[1] = new ServicePoint("Security check", new Normal(10, 10), eventList, EventType.DEP2);
			servicePoints[2] = new ServicePoint("Border control", new Normal(5, 3), eventList, EventType.DEP3);
			servicePoints[3] = new ServicePoint("Boarding", new Normal(5, 3), eventList, EventType.DEP4);

			arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARR1);
		}
	}

	@Override
	protected void initialize() {	// First arrival in the system
		arrivalProcess.generateNextEvent();
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		Customer a;

		switch ((EventType)t.getType()) {
		case ARR1:
			servicePoints[0].addQueue(new Customer());
			arrivalProcess.generateNextEvent();
			break;

		case DEP1:
			a = servicePoints[0].removeQueue();
			servicePoints[1].addQueue(a);
			break;

		case DEP2:
			a = servicePoints[1].removeQueue();
			servicePoints[2].addQueue(a);
			break;

		case DEP3:
			a = servicePoints[2].removeQueue();
			servicePoints[3].addQueue(a);
			break;

		case DEP4:
			a = servicePoints[3].removeQueue();
			a.setRemovalTime(Clock.getInstance().getClock());
		    a.reportResults();
			servedClients++;
			break;
		}
	}

	@Override
	protected void tryCEvents() {
		for (ServicePoint p: servicePoints){
			if (!p.isReserved() && p.isOnQueue()){
				p.beginService();
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
