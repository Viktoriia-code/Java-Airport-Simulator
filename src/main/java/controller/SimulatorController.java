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

    @FXML
    private Label speedLabel;
    @FXML
    private Slider speedSlider;

    @FXML
    private Spinner<Integer> timeSpinner;

    @FXML
    private Spinner<Integer> passengerSpinner;

    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();
    private final Map<String, Integer> maxServicePointsMap = new LinkedHashMap<>();

    // Bottom part of the screen
    @FXML
    private Label inputErrorLabel;
    @FXML
    private Button helpButton;

    // Central part of the screen
    @FXML
    private SplitPane splitPane;
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


        maxServicePointsMap.put("CheckIn", 65);
        maxServicePointsMap.put("RegularSecurityCheck", 14);
        maxServicePointsMap.put("FastSecurityCheck", 10);
        maxServicePointsMap.put("BorderControl", 26);
        maxServicePointsMap.put("EuOnboarding", 20);
        maxServicePointsMap.put("OutEuOnboarding", 15);

        initializeSliders();

        drawAllServicePoints();

        helpButton.setOnAction(event -> showInstructions());

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

    private double lastCanvasHeight = -1;
    private double lastCanvasWidth = -1;

    private void adjustCanvasSize() {
        double canvasHeight = splitPane.getDividerPositions()[0] * splitPane.getHeight();
        double canvasWidth = splitPane.getWidth();

        if (canvasHeight != lastCanvasHeight || canvasWidth != lastCanvasWidth) {
            airportCanvas.setHeight(canvasHeight);
            airportCanvas.setWidth(canvasWidth);

            lastCanvasHeight = canvasHeight;
            lastCanvasWidth = canvasWidth;

            drawAllServicePoints();
        }
    }

    private void drawTypeLabel(GraphicsContext gc, String pointType, double y) {
        double xLeftEdge = 5;
        gc.setFill(Color.BLACK);
        gc.fillText(pointType, xLeftEdge, y);
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
    }

    private void setupSlider(Slider slider, Label label, String pointType) {
        int defaultValue = (int) slider.getValue();
        int maxPoints = maxServicePointsMap.getOrDefault(pointType, 0);
        slider.setMax(maxPoints);
        servicePointsMap.put(pointType, defaultValue);
        label.setText(String.valueOf(defaultValue));

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newCount = newValue.intValue();
            servicePointsMap.put(pointType, newCount);
            label.setText(String.valueOf(newCount));
            drawAllServicePoints();
        });
    }

    private void drawAllServicePoints() {
        GraphicsContext gc = airportCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, airportCanvas.getWidth(), airportCanvas.getHeight());

        double yStep = 75;
        int typeIndex = 0;

        for (Map.Entry<String, Integer> entry : maxServicePointsMap.entrySet()) {
            String pointType = entry.getKey();
            int totalPoints = entry.getValue();
            int activatedPoints = servicePointsMap.getOrDefault(pointType, 0);

            double y = yStep * (typeIndex + 1);

            if (pointType.equals("CheckIn") && typeIndex > 0) {
                y += 2;
            } else if (typeIndex > 0) {
                y += yStep;
            }

            drawServicePoints(gc, activatedPoints, totalPoints, y);
            drawTypeLabel(gc, pointType, y - 15);
            typeIndex++;
        }
    }



    private void drawServicePoints(GraphicsContext gc,  int activatedCount, int totalPoints, double yStart) {
        double pointDiameter = 10.0;
        double spacingX = 20.0;
        double spacingY = 30.0;
        int pointsPerRow = 26;

        int currentRow = 0;
        for (int i = 0; i < totalPoints; i++) {
            int rowPosition = i % pointsPerRow;
            if (i > 0 && rowPosition == 0) {
                currentRow++;
            }

            double x = spacingX * (rowPosition + 1);
            double yOffset = yStart  + spacingY * currentRow;

            if (i < activatedCount) {
                gc.setFill(Color.BLUE);
            } else {
                gc.setFill(Color.DARKGRAY);
            }

            gc.fillOval(x - pointDiameter / 2, yOffset - pointDiameter / 2, pointDiameter, pointDiameter);
        }
    }
}