import framework.Engine;
import framework.Trace;
import framework.Trace.Level;
import model.MyEngine;
//import view.SimulatorView;

/* Command-line type User Interface */
public class Main {
    public static void main(String[] args) {
        Trace.setTraceLevel(Level.INFO);

        Engine m = new MyEngine();
        m.setSimulationTime(100);
        m.run();

        //SimulatorView.launch(SimulatorView.class);
    }
}
