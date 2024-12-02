package controller;

import framework.Trace;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.MyEngine;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import java.util.Random;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimulatorController {

    @FXML
    private Label checkInLabel;
    @FXML
    private Slider checkInSlider;

    @FXML
    private Label securityCheckLabel;
    @FXML
    private Slider securityCheckSlider;

    @FXML
    private Label borderControlLabel;
    @FXML
    private Slider borderControlSlider;

    @FXML
    private Label onboardingLabel;
    @FXML
    private Slider onboardingSlider;

    @FXML
    private Label speedLabel;
    @FXML
    private Slider speedSlider;

    @FXML
    private Canvas airportCanvas;

    @FXML
    private TextArea logArea;

    @FXML
    private Spinner<Integer> timeSpinner;

    @FXML
    private Spinner<Integer> passengerSpinner;

    @FXML
    private Slider securityCheckFastSlider;

    @FXML
    private Label checkInNumber, securityCheckNumber, securityCheckFastNumber, borderControlNumber, onboardingNumber;

    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();
    private final Random random = new Random();


    @FXML
    private Label inputErrorLabel;

    @FXML
    private Button helpButton;

    // Results section
    @FXML
    private Label totalPassengersServedLabel;

    @FXML
    private Label avServiceTimeLabel;

    @FXML
    private Label simulationTimeLabel;

    @FXML
    public void initialize() {
        servicePointsMap.put("CheckIn", (int) checkInSlider.getValue());
        servicePointsMap.put("SecurityCheck", (int) securityCheckSlider.getValue());
        servicePointsMap.put("SecurityCheckFast", (int) securityCheckFastSlider.getValue());
        servicePointsMap.put("BorderControl", (int) borderControlSlider.getValue());
        servicePointsMap.put("Onboarding", (int) onboardingSlider.getValue());

        initializeSliders();

        drawAllServicePoints();

        helpButton.setOnAction(event -> showInstructions());

        // control for the check-in slider
        checkInSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                checkInLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the security check slider
        securityCheckSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                securityCheckLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the border control slider
        borderControlSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                borderControlLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the onboarding slider
        onboardingSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                onboardingLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the speed slider
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                speedLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // Control for the time spinner
        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30000, 1000, 100);
        timeSpinner.setValueFactory(timeValueFactory);

        // Control for the passenger spinner
        SpinnerValueFactory<Integer> passengerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 100);
        passengerSpinner.setValueFactory(passengerValueFactory);

        // Validate input for the time and passenger spinners
        timeSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> validateInput(timeSpinner, newValue, 1, 30000, "Time"));
        passengerSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> validateInput(passengerSpinner, newValue, 1, 1000, "Passenger count"));

    }

    private void showInstructions() {
        // Create a simple alert with instructions
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("Airport Simulator Instructions");
        alert.setWidth(300);
        alert.setHeight(400);
        alert.setContentText("1. Adjust the sliders to set the number of points for each stage of the process.\n"
                + "2. Set the simulation time and passenger count.\n"
                + "3. Click Start to begin the simulation.\n"
                + "4. The results will be displayed in the Results section.\n"
                + "5. Make sure to follow the valid input ranges.\n"
                + "6. If there's an invalid input, it will be highlighted in red.");

        alert.showAndWait();  // Show the alert and wait for user interaction
    }

    private void validateInput(Spinner<Integer> spinner, String newValue, int min, int max, String labelName) {
        String errorMessage = "";

        try {
            // Parse the value entered by the user in the text field
            int value = Integer.parseInt(newValue);

            // Check if the value is within the acceptable range
            if (value < min || value > max) {
                errorMessage = labelName + " must be between " + min + " and " + max + ".";
                spinner.getEditor().setStyle("-fx-text-fill: red;"); // Highlight invalid input
            } else {
                spinner.getEditor().setStyle(""); // Reset border color if valid
            }

        } catch (NumberFormatException e) {
            errorMessage = labelName + " must be a valid number.";
            spinner.getEditor().setStyle("-fx-text-fill: red;"); // Highlight invalid input
        }

        // Display the error message or clear it
        inputErrorLabel.setText(errorMessage);
    }

    @FXML
    private void startSimulation() {
        int timeValue = timeSpinner.getValue();
        Trace.setTraceLevel(Trace.Level.INFO);
        MyEngine sim = new MyEngine();
        sim.setSimulationTime(timeValue);
        sim.run();
        printResults(sim.getServedClients(), sim.getMeanServiceTime(), sim.getSimulationTime());
    }

    public void printResults(int customersServed, double meanServiceTime, double simulationTime) {
        totalPassengersServedLabel.setText(String.valueOf(customersServed));
        avServiceTimeLabel.setText(String.valueOf(meanServiceTime));
        simulationTimeLabel.setText(String.valueOf(simulationTime));
    }


    private void initializeSliders() {
        setupSlider(checkInSlider, checkInNumber, "CheckIn");
        setupSlider(securityCheckSlider, securityCheckNumber, "SecurityCheck");
        setupSlider(securityCheckFastSlider, securityCheckFastNumber, "SecurityCheckFast");
        setupSlider(borderControlSlider, borderControlNumber, "BorderControl");
        setupSlider(onboardingSlider, onboardingNumber, "Onboarding");
    }

    private void setupSlider(Slider slider, Label label, String pointType) {
        int defaultValue = (int) slider.getValue();
        servicePointsMap.put(pointType, defaultValue);
        label.setText(String.valueOf(defaultValue));

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newCount = newValue.intValue();
            servicePointsMap.put(pointType, newCount);
            label.setText(String.valueOf(newCount));

            logMessage(pointType + " updated to " + newCount + " service points.");
            drawAllServicePoints();
        });
    }

    private void drawAllServicePoints() {
        GraphicsContext gc = airportCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, airportCanvas.getWidth(), airportCanvas.getHeight());

        double yStep = airportCanvas.getHeight() / (servicePointsMap.size() + 1);
        int typeIndex = 0;

        for (Map.Entry<String, Integer> entry : servicePointsMap.entrySet()) {
            String pointType = entry.getKey();
            int count = entry.getValue();

            double y = yStep * (typeIndex + 1);
            drawServicePoints(gc, count, y, pointType);
            typeIndex++;
        }
    }

    private void drawServicePoints(GraphicsContext gc, int count, double y, String pointType) {
        double spacing = airportCanvas.getWidth() / (count + 1);

        for (int i = 0; i < count; i++) {
            double x = spacing * (i + 1);

            gc.setFill(Color.BLUE);
            gc.fillOval(x - 15, y - 15, 30, 30);

            gc.setFill(Color.WHITE);
            gc.fillText(pointType + " " + (i + 1), x - 20, y + 5);
        }
    }

    private void logMessage(String message) {
        logArea.appendText(message + "\n");
    }
}