package simu.framework;

public abstract class Engine {
	private double simulationTime = 0;	// time when the simulation will be stopped
	private Clock clock;				// to simplify the code (clock.getClock() instead Clock.getInstance().getClock())
	protected EventList eventList;		// events to be processed are stored here

	public Engine() {
		clock = Clock.getInstance();
		
		eventList = new EventList();
		
		// Service Points are created in simu.model-package's class who is inheriting the Engine class
	}

	public void setSimulationTime(double time) {	// define how long we will run the simulation
		simulationTime = time;
	}

	public void run(){
		initialize(); // creating, e.g., the first event

		while (simulate()) {
			Trace.out(Trace.Level.INFO, "\nA-phase: time is " + currentTime());
			clock.setClock(currentTime());
			
			Trace.out(Trace.Level.INFO, "\nB-phase:" );
			runBEvents();
			
			Trace.out(Trace.Level.INFO, "\nC-phase:" );
			tryCEvents();

		}

		results();
	}
	
	private void runBEvents() {
		while (eventList.getNextEventTime() == clock.getClock()){
			runEvent(eventList.remove());
		}
	}

	private double currentTime(){
		return eventList.getNextEventTime();
	}
	
	private boolean simulate(){
		return clock.getClock() < simulationTime;
	}

	protected abstract void runEvent(Event t);	// Defined in simu.model-package's class who is inheriting the Engine class

	protected abstract void tryCEvents();		// Defined in simu.model-package's class who is inheriting the Engine class

	protected abstract void initialize(); 		// Defined in simu.model-package's class who is inheriting the Engine class

	protected abstract void results(); 			// Defined in simu.model-package's class who is inheriting the Engine class
}