package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SimulatorController {

    @FXML
    private Spinner<Integer> timeSpinner;

    @FXML
    private Spinner<Integer> passengerSpinner;

    @FXML
    public void initialize() {
        // control for the time spinner
        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30000, 1000, 100);
        timeSpinner.setValueFactory(timeValueFactory);

        // control for the passenger spinner
        SpinnerValueFactory<Integer> passengerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 100);
        passengerSpinner.setValueFactory(passengerValueFactory);
    }
}
