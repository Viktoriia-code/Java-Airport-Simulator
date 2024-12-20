package framework;

import controller.SimulatorController;

/**
 * General output for the diagnostic messages. Every diagnostic message has a severity level.
 * It is possible to control which level of diagnostic messages is printed.
 */
public class Trace {
	/**
	 * Diagnostic message severity level
	 * @see #INFO
	 * @see #DEBUG
	 * @see #WAR
	 * @see #ERR
	 */
	public enum Level {
		/**
		 * Debug messages
		 */
		DEBUG,
		/**
		 * Messages just for your information only
		 */
		INFO,
		/**
		 * Warning messages
		 */
		WAR,
		/**
		 * Error messages
		 */
		ERR }
	private static Level traceLevel;		// current severity level filtering
	private static SimulatorController controller;

	/**
	 * Set the filtering level of the diagnostic messages
	 *
	 * @param lvl filtering level. Severity level messages lower than this filtering level are not printed
	 */
	public static void setTraceLevel(Level lvl){
		traceLevel = lvl;
	}

	/**
	 * Set the Controller instance for delegation
	 *
	 * @param ctrl Controller instance to handle log calls
	 */
	public static void setController(SimulatorController ctrl) { controller = ctrl; }

	/**
	 * Print the given diagnostic message to the console
	 *
	 * @param lvl severity level of the diagnostic message
	 * @param txt diagnostic message to be printed
	 */
	public static void out(Level lvl, String txt){
		if (lvl.ordinal() >= traceLevel.ordinal()){
			System.out.println(txt);
		}
	}

	public static void log(Level lvl, String txt){
		if (lvl.ordinal() >= traceLevel.ordinal()){
			if (controller != null) {
				controller.log(txt); // Delegate to Controller's log
			}
		}
	}
}