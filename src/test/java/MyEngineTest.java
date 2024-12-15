import framework.Event;
import framework.Trace;
import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MyEngineTest {
    @Test
    public void testCreateSPs(){
        Trace.setTraceLevel(Trace.Level.WAR);
        MyEngine sim = new MyEngine();

        assertEquals(0, sim.getAllServicePoints().size());

        for (int i = 0; i < 10; i++){
            sim.setAllServicePoints(i, i, i, i, i, i);
            sim.initialize();

            int sum = 0;
            for (ArrayList<ServicePoint> arr : sim.getAllServicePoints()) {
                sum += arr.size();
            }

            assertEquals(i * 6, sum);
        }
    }
}