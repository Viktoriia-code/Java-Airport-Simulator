package controller;

import framework.Trace;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.MyEngine;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimulatorController {
    // Input section (left part of the screen)
    // Service points settings
    @FXML
    private Label checkInLabel;
    @FXML
    private Slider checkInSlider;

    @FXML
    private Label regularSecurityCheckLabel;
    @FXML
    private Slider regularSecurityCheckSlider;

    @FXML
    private Label fastSecurityCheckLabel;
    @FXML
    private Slider fastSecurityCheckSlider;

    @FXML
    private Label borderControlLabel;
    @FXML
    private Slider borderControlSlider;

    @FXML
    private Label euOnboardingLabel;
    @FXML
    private Slider euOnboardingSlider;

    @FXML
    private Label outEuOnboardingLabel;
    @FXML
    private Slider outEuOnboardingSlider;

    // Clients settings
    @FXML
    private Spinner<Integer> passengerSpinner;

    @FXML
    private Label economClassPercLabel;
    @FXML
    private Slider classSlider;
    @FXML
    private Label businessClassPercLabel;

    @FXML
    private Label euFlightPercLabel;
    @FXML
    private Slider euFlightSlider;
    @FXML
    private Label outEuFlightPercLabel;

    @FXML
    private Label onlineCheckInPercLabel;
    @FXML
    private Slider onlineCheckInSlider;
    @FXML
    private Label offlineCheckInPercLabel;

    // General simulation settings
    @FXML
    private Label speedLabel;
    @FXML
    private Slider speedSlider;

    @FXML
    private Spinner<Integer> timeSpinner;


    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();
    private final Map<String, Double> customerTypesMap = new LinkedHashMap<>();

    // Bottom part of the screen
    @FXML
    private Label inputErrorLabel;
    @FXML
    private Button helpButton;

    // Central part of the screen
    @FXML
    private Canvas airportCanvas;
    @FXML
    private TextArea logArea;

    // Results section (right part of the screen)
    @FXML
    private Label totalPassengersServedLabel;
    @FXML
    private Label avServiceTimeLabel;
    @FXML
    private Label simulationTimeLabel;

    @FXML
    public void initialize() {
        servicePointsMap.put("CheckIn", (int) checkInSlider.getValue());
        servicePointsMap.put("RegularSecurityCheck", (int) regularSecurityCheckSlider.getValue());
        servicePointsMap.put("FastSecurityCheck", (int) fastSecurityCheckSlider.getValue());
        servicePointsMap.put("BorderControl", (int) borderControlSlider.getValue());
        servicePointsMap.put("EuOnboarding", (int) euOnboardingSlider.getValue());
        servicePointsMap.put("OutEuOnboarding", (int) outEuOnboardingSlider.getValue());

        customerTypesMap.put("EconomClass", (double) classSlider.getValue());

        initializeSliders();

        drawAllServicePoints();

        helpButton.setOnAction(event -> showInstructions());

        // Control for the time spinner
        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30000, 1000, 100);
        timeSpinner.setValueFactory(timeValueFactory);

        // Control for the passenger spinner
        SpinnerValueFactory<Integer> passengerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 100);
        passengerSpinner.setValueFactory(passengerValueFactory);

        // Control for the class slider
        classSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int econClassValue = newValue.intValue(); // Converts to int directly
            int businessClassValue = 100 - econClassValue;

            economClassPercLabel.setText(econClassValue + "%");
            businessClassPercLabel.setText(businessClassValue + "%");
        });

        // Control for the EU flight slider
        euFlightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int euFlightValue = newValue.intValue(); // Converts to int directly
            int outEuFlightValue = 100 - euFlightValue;

            euFlightPercLabel.setText(euFlightValue + "%");
            outEuFlightPercLabel.setText(outEuFlightValue + "%");
        });

        // Control for the online check-in slider
        onlineCheckInSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int onlineCheckInValue = newValue.intValue(); // Converts to int directly
            int offlineCheckInValue = 100 - onlineCheckInValue;

            onlineCheckInPercLabel.setText(onlineCheckInValue + "%");
            offlineCheckInPercLabel.setText(offlineCheckInValue + "%");
        });

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
        int checkInPoints = Integer.valueOf((int) checkInSlider.getValue());
        int regularSecurityCheckPoints = Integer.valueOf((int) regularSecurityCheckSlider.getValue());
        int fastSecurityCheckPoints = Integer.valueOf((int) fastSecurityCheckSlider.getValue());
        int borderControlPoints = Integer.valueOf((int) borderControlSlider.getValue());
        int euOnboardingPoints = Integer.valueOf((int) euOnboardingSlider.getValue());
        int outEuOnboardingPoints = Integer.valueOf((int) outEuOnboardingSlider.getValue());
        Trace.setTraceLevel(Trace.Level.INFO);
        MyEngine sim = new MyEngine();
        // Set time for the simulation
        sim.setSimulationTime(timeValue);
        // Set SPs for the simulation
        sim.setAllServicePoints(
                checkInPoints,
                regularSecurityCheckPoints,
                fastSecurityCheckPoints,
                borderControlPoints,
                euOnboardingPoints,
                outEuOnboardingPoints
        );
        sim.run();
        printResults(sim.getServedClients(), sim.getMeanServiceTime(), sim.getSimulationTime());
    }

    public void printResults(int customersServed, double meanServiceTime, double simulationTime) {
        totalPassengersServedLabel.setText(String.valueOf(customersServed));
        avServiceTimeLabel.setText(String.valueOf(meanServiceTime));
        simulationTimeLabel.setText(String.valueOf(simulationTime));
    }

    private void initializeSliders() {
        setupSlider(checkInSlider, checkInLabel, "CheckIn");
        setupSlider(regularSecurityCheckSlider, regularSecurityCheckLabel, "RegularSecurityCheck");
        setupSlider(fastSecurityCheckSlider, fastSecurityCheckLabel, "FastSecurityCheck");
        setupSlider(borderControlSlider, borderControlLabel, "BorderControl");
        setupSlider(euOnboardingSlider, euOnboardingLabel, "EuOnboarding");
        setupSlider(outEuOnboardingSlider, outEuOnboardingLabel, "OutEuOnboarding");

        //setupSlider(classSlider, economClassPercLabel, "EconomClass");
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