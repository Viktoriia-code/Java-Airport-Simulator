package framework;

/**
 * Singleton for holding global simulation time
 */
public class Clock {
	private double clock;
	private static Clock instance;

	/**
	 * Constructor for the Clock class.
	 * The constructor is private to prevent instantiation of the class.
	 */
	private Clock(){
		clock = 0;
	}

	/**
	 * Retrieves the singleton instance of the Clock.
	 * If the instance does not exist, it will be created.
	 *
	 * @return the singleton instance of the Clock.
	 */
	public static Clock getInstance(){
		if (instance == null){
			instance = new Clock();
		}
		return instance;
	}

	/**
	 * Sets the global simulation time.
	 *
	 * @param clock the new value for the simulation time.
	 */
	public void setClock(double clock){
		this.clock = clock;
	}

	/**
	 * Retrieves the current global simulation time.
	 *
	 * @return the current value of the simulation time.
	 */
	public double getClock(){
		return clock;
	}

	/**
	 * Resets the global simulation time to zero.
	 */
	public void resetClock() { clock = 0; }
}
