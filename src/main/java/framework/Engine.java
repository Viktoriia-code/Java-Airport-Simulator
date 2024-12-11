package framework;

/**
 * Engine implement three-phase simulator. See <a href="https://www.jstor.org/stable/2584330">Three-Phase Simulator</a>
 * <p>
 * This is a skeleton of a three-phase simulator. You need to implement abstract methods for your
 * purpose.
 */
public abstract class Engine {
    private double simulationTime = 0;    // time when the simulation will be stopped
    private final Clock clock;                // to simplify the code (clock.getClock() instead Clock.getInstance().getClock())
    protected EventList eventList;        // events to be processed are stored here

    private boolean isPaused = false;
    private boolean isStopped = false;
    private double simulationSpeed = 1000;

    /**
     * Service Points are created in simu.model-package's class inheriting the Engine class
     */
    public Engine() {
        clock = Clock.getInstance();
        eventList = new EventList();
    }

    /**
     * Define how long we will run the simulation
     *
     * @param time Ending time of the simulation
     */
    public void setSimulationTime(double time) {    // define how long we will run the simulation
        simulationTime = time;
    }

    /**
     * Pause simulation
     */
    public void togglePause() {
        isPaused = !isPaused;
        if (!isPaused) {
            synchronized (this) {
                notify();
            }
        }
    }

    /**
     * End simulation
     */
    public void stopSimulation() {
        isStopped = true;
        isPaused = false;

        synchronized (this) {
            notify();
        }
    }

    /**
     * Change simulation speed
     */
    public void setSimulationSpeed(double speed) {
        simulationSpeed = speed;
    }

    /**
     * The starting point of the simulator. We will return when the simulation ends.
     */
    public void run() {
        initialize(); // creating, e.g., the first event

        while (simulate() && !isStopped) {
            synchronized (this) {
                while (isPaused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            Trace.out(Trace.Level.INFO, "\nA-phase: time is " + currentTime());
            clock.setClock(currentTime());

            Trace.out(Trace.Level.INFO, "\nB-phase:");
            runBEvents();

            Trace.out(Trace.Level.INFO, "\nC-phase:");
            tryCEvents();

            synchronized (this) {
                if (simulationSpeed != 100){
                    try {
                        Thread.sleep((long) (simulationSpeed));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        results();
        clock.resetClock();
    }

    /**
     * Execute all B-events (bounded to time) at the current time removing them from the event list.
     */
    private void runBEvents() {
        while (eventList.getNextEventTime() == clock.getClock()) {
            runEvent(eventList.remove());
        }
    }

    /**
     * @return Earliest event time at the event list
     */
    private double currentTime() {
        return eventList.getNextEventTime();
    }

    /**
     * @return logical value whether we should continue simulation
     */
    private boolean simulate() {
        return clock.getClock() < simulationTime;
    }

    /**
     * Execute event actions (e.g., removing customer from the queue)
     * Defined in simu.model-package's class who is inheriting the Engine class
     *
     * @param t The event to be executed
     */
    protected abstract void runEvent(Event t);    // Defined in simu.model-package's class who is inheriting the Engine class

    /**
     * Execute all possible C-events (conditional events)
     * Defined in simu.model-package's class who is inheriting the Engine class
     */
    protected abstract void tryCEvents();        // Defined in simu.model-package's class who is inheriting the Engine class

    /**
     * Set all data structures to initial values
     * Defined in simu.model-package's class who is inheriting the Engine class
     */
    protected abstract void initialize();        // Defined in simu.model-package's class who is inheriting the Engine class

    /**
     * Show/analyze measurement parameters collected during the simulation.
     * This method is called at the end of the simulation.
     * Defined in simu.model-package's class who is inheriting the Engine class
     */
    protected abstract void results();            // Defined in simu.model-package's class who is inheriting the Engine class
}