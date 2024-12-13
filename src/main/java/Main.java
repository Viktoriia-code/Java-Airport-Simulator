import framework.Trace;
import framework.Trace.Level;
import model.MyEngine;

/**
 * Command line entry point for the simulation.
 * This class sets up the simulation parameters and runs the simulation.
 * The simulation results are output to the console.
 */
public class Main {
    public static void main(String[] args) {
        Trace.setTraceLevel(Level.INFO);

        MyEngine m = new MyEngine();
        m.setAllCustomerPercentages(10, 5, 15);
        m.setAllServicePoints(10, 10, 10, 10, 10, 10);
        m.setSimulationTime(10000);
        m.run();
    }
}
