import framework.Trace;
import framework.Trace.Level;
import model.MyEngine;
//import view.SimulatorView;

/* Command-line type User Interface */
public class Main {
    public static void main(String[] args) {
        Trace.setTraceLevel(Level.INFO);

        MyEngine m = new MyEngine();
        m.setCustomerPercentagesAtOnce(10, 5, 15);
        m.setAmountOfAllServicePoints(10, 10, 10, 10, 10, 10);
        m.setSimulationTime(10000);
        m.run();

        //SimulatorView.launch(SimulatorView.class);
    }
}
