package src.main.java;

import src.main.java.framework.Engine;
import src.main.java.framework.Trace;
import src.main.java.framework.Trace.Level;
import src.main.java.model.MyEngine;

/* Command-line type User Interface */
public class Main {
    public static void main(String[] args) {
        Trace.setTraceLevel(Level.INFO);

        Engine m = new MyEngine();
        m.setSimulationTime(100);
        m.run();
    }
}
