package controller;

import entity.Parameters;
import entity.Result;
import framework.Trace;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.MyEngine;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.ServicePoint;

public class SimulatorController {
    // Input section (left part of the screen)
    // Service points settings
    @FXML
    private Label checkInLabel;
    @FXML
    private Slider checkInSlider;

    @FXML
    private Label checkInTimeLabel;
    @FXML
    private Slider checkInTimeSlider;

    @FXML
    private Label regularSecurityCheckLabel;
    @FXML
    private Slider regularSecurityCheckSlider;

    @FXML
    private Label fastSecurityCheckLabel;
    @FXML
    private Slider fastSecurityCheckSlider;

    @FXML
    private Label securityTimeLabel;
    @FXML
    private Slider securityTimeSlider;

    @FXML
    private Label borderControlLabel;
    @FXML
    private Slider borderControlSlider;

    @FXML
    private Label borderTimeLabel;
    @FXML
    private Slider borderTimeSlider;

    @FXML
    private Label euOnboardingLabel;
    @FXML
    private Slider euOnboardingSlider;

    @FXML
    private Label outEuOnboardingLabel;
    @FXML
    private Slider outEuOnboardingSlider;

    @FXML
    private Label onboardingTimeLabel;
    @FXML
    private Slider onboardingTimeSlider;

    // Clients settings
    @FXML
    private ChoiceBox<String> passengerSelect;
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
    private Spinner<Integer> timeSpinner;

    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();

    private final Map<String, Double> customerTypesMap = new LinkedHashMap<>();

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
    private ListView<TextFlow> logListView;

    // Results section (right part of the screen)
    @FXML
    private Label totalPassengersServedLabel;
    @FXML
    private Label avServiceTimeLabel;
    @FXML
    private Label simulationTimeLabel;
    @FXML
    private Label avQueueLabel;
    @FXML
    private Label longestQueueNameLabel;
    @FXML
    private Label longestQueueSizeLabel;
    @FXML
    private TextArea servicePointResultsTextArea;

    @FXML
    private Button playButton;

    @FXML
    private Button stopButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label speedLabel;

    boolean isPaused = false;

    @FXML
    public void initialize() {
        servicePointsMap.put("CheckIn", (int) checkInSlider.getValue());
        servicePointsMap.put("RegularSecurityCheck", (int) regularSecurityCheckSlider.getValue());
        servicePointsMap.put("FastSecurityCheck", (int) fastSecurityCheckSlider.getValue());
        servicePointsMap.put("BorderControl", (int) borderControlSlider.getValue());
        servicePointsMap.put("EuOnboarding", (int) euOnboardingSlider.getValue());
        servicePointsMap.put("OutEuOnboarding", (int) outEuOnboardingSlider.getValue());

        customerTypesMap.put("EconomClass", (double) classSlider.getValue());

        maxServicePointsMap.put("CheckIn", 65);
        maxServicePointsMap.put("RegularSecurityCheck", 14);
        maxServicePointsMap.put("FastSecurityCheck", 10);
        maxServicePointsMap.put("BorderControl", 26);
        maxServicePointsMap.put("EuOnboarding", 20);
        maxServicePointsMap.put("OutEuOnboarding", 15);

        // SP Time inputs
        bindSliderToLabel(checkInTimeSlider, checkInTimeLabel);
        bindSliderToLabel(securityTimeSlider, securityTimeLabel);
        bindSliderToLabel(borderTimeSlider, borderTimeLabel);
        bindSliderToLabel(onboardingTimeSlider, onboardingTimeLabel);

        bindSliderToLabel(speedSlider, speedLabel);

        initializeSliders();

        drawAllServicePoints();

        helpButton.setOnAction(event -> showInstructions());

        // Control for the time spinner
        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30000, 500, 100);
        timeSpinner.setValueFactory(timeValueFactory);

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

        // Validate input for the passenger frequency
        passengerSelect.getItems().addAll(
                "Fast (Every 1 second)",
                "Moderate (Every 3 seconds)",
                "Normal (Every 5 seconds)",
                "Slow (Every 10 seconds)",
                "Very Slow (Every 20 seconds)"
        );

        // Set default selection
        passengerSelect.setValue("Normal (Every 5 seconds)");

        playButton.setDisable(true);
        stopButton.setDisable(true);

        log("Welcome to the Airport simulation!");
    }

    private void togglePause(MyEngine sim) {
        isPaused = !isPaused;
        sim.togglePause();
        Trace.out(Trace.Level.INFO, "*** PAUSE BUTTON PRESSED ***");
        if (isPaused) {
            log("Simulation paused");
            log(String.format("%.0f minutes simulated: %s completely handled", sim.getCurrentTime(), sim.getServedClients()));
        } else {
            log("Continued Simulation");
        }
    }

    private void stopSim(MyEngine sim) {
        Trace.out(Trace.Level.INFO, "*** STOP BUTTON PRESSED ***");
        sim.stopSimulation();
        log("Simulation Stopped.");
    }

    /**
     * Binds a slider to a label and updates the label with the slider's value.
     *
     * @param slider the slider to observe
     * @param label  the label to update
     */
    public static void bindSliderToLabel(Slider slider, Label label) {
        // Set the initial label value based on the slider's current value
        updateLabel((int) slider.getValue(), label);

        // Add a listener to update the label whenever the slider value changes
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateLabel(newValue.intValue(), label);
        });
    }

    /**
     * Updates the label with the slider's current value.
     *
     * @param value the slider's value
     * @param label the label to update
     */
    private static void updateLabel(int value, Label label) {
        label.setText(String.valueOf(value));
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

    private double getSelectedFrequency() {
        String selectedOption = passengerSelect.getValue(); // Get the selected item
        switch (selectedOption) {
            case "Fast (Every 1 second)":
                return 1.0 / 60.0; // 1 second = 1/60 minutes
            case "Moderate (Every 3 seconds)":
                return 3.0 / 60.0; // 3 seconds = 3/60 minutes
            case "Normal (Every 5 seconds)":
                return 5.0 / 60.0; // 5 seconds = 5/60 minutes
            case "Slow (Every 10 seconds)":
                return 10.0 / 60.0; // 10 seconds = 10/60 minutes
            case "Very Slow (Every 20 seconds)":
                return 20.0 / 60.0; // 20 seconds = 20/60 minutes
            default:
                throw new IllegalArgumentException("Unexpected selection: " + selectedOption);
        }
    }

    @FXML
    private void startSimulation() {
        MyEngine sim = new MyEngine();

        new Thread(() -> {
            int timeValue = timeSpinner.getValue();
            int checkInPoints = (int) checkInSlider.getValue();
            int regularSecurityCheckPoints = (int) regularSecurityCheckSlider.getValue();
            int fastSecurityCheckPoints = (int) fastSecurityCheckSlider.getValue();
            int borderControlPoints = (int) borderControlSlider.getValue();
            int euOnboardingPoints = (int) euOnboardingSlider.getValue();
            int outEuOnboardingPoints = (int) outEuOnboardingSlider.getValue();

            int businessClassValue = 100 - (int) classSlider.getValue();
            int euFlightValue = (int) euFlightSlider.getValue();
            int onlineCheckInValue = (int) onlineCheckInSlider.getValue();

            int checkInTime = (int) checkInTimeSlider.getValue();
            int securityTime = (int) securityTimeSlider.getValue();
            int borderTime = (int) borderTimeSlider.getValue();
            int onboardingTime = (int) onboardingTimeSlider.getValue();

            Platform.runLater(() -> {
                log(String.format(
                        "Simulation Started With: Time=%d, CheckIn=%d, RegularSec=%d, FastSec=%d,\n" +
                                " BorderControl=%d, EUOnboard=%d, OutEUOnboard=%d, OnlineCheckIn=%d%%, EUFlights=%d%%,\n" +
                                " BusinessClass=%d%%, PassFrequency=%.2f, SpeedMode=%s",
                        timeValue, checkInPoints, regularSecurityCheckPoints, fastSecurityCheckPoints,
                        borderControlPoints, euOnboardingPoints, outEuOnboardingPoints,
                        onlineCheckInValue, euFlightValue, businessClassValue, getSelectedFrequency(), speedSlider.getValue()
                ));
            });

            Trace.setTraceLevel(Trace.Level.INFO);

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
            // Set time for SP
            sim.setAllTimingMeans(
                    getSelectedFrequency(),
                    checkInTime,
                    securityTime,
                    borderTime,
                    onboardingTime
            );

            setSpeed(sim);

            // Create Parameters object
            Parameters simulationParameters = new Parameters();
            simulationParameters.setCheck_in(checkInPoints);
            simulationParameters.setSecurity_check(regularSecurityCheckPoints);
            simulationParameters.setFasttrack(fastSecurityCheckPoints);
            simulationParameters.setBorder_control(borderControlPoints);
            simulationParameters.setEU_boarding(euOnboardingPoints);
            simulationParameters.setNon_EU_Boarding(outEuOnboardingPoints);

            // Set customer percentages for the simulation
            sim.setAllCustomerPercentages(
                    onlineCheckInValue,
                    euFlightValue,
                    businessClassValue
            );

            playButton.setDisable(false);
            playButton.setOnAction(event -> togglePause(sim));
            stopButton.setDisable(false);
            stopButton.setOnAction(event -> stopSim(sim));

            speedSlider.setOnMouseReleased(event -> setSpeed(sim));

            sim.run();

            sim.stopSimulation();
            Platform.runLater(() -> finishSim(sim, simulationParameters));

        }).start();
    }

    private void setSpeed(MyEngine sim){
        int speedMode = (int) speedSlider.getValue();
        double millis = switch (speedMode) {
            case 1 -> 2000;
            case 2 -> 500;
            case 4 -> 100;
            case 5 -> 0;
            default -> // including case 3
                    200;
        };
        sim.setSimulationSpeed(millis);
        Platform.runLater(() ->         log(String.format("Speed Mode %s%s", speedMode, millis == 0 ? ": no delay" : ": delay of " + millis / 1000 + " s")));
    }

    private void finishSim(MyEngine sim, Parameters simulationParameters) {
        log(String.format("Simulation done running: %.2f minutes simulated", sim.getSimulationTime()));

        printResults(
                sim.getServedClients(),
                sim.getAvServiceTime(),
                sim.getSimulationTime(),
                sim.getAverageQueueSize(),
                sim.getLongestQueueSPName(),
                sim.getLongestQueueSize(),
                sim.getServicePointResults()
        );

        playButton.setDisable(true);
        stopButton.setDisable(true);

        // Save simulation results
        saveSimuResult(
                sim.getServedClients(),
                sim.getAvServiceTime(),
                sim.getSimulationTime(),
                sim.getLongestQueueSPName(),
                simulationParameters // Pass the Parameters object
        );
    }

    public void printResults(int customersServed, double meanServiceTime, double simulationTime, double avQueueLength, String longestQueueName, int longestQueueSize, String servicePointResults) {
        totalPassengersServedLabel.setText(String.valueOf(customersServed));
        avServiceTimeLabel.setText(String.format("%.0f mins", meanServiceTime));
        simulationTimeLabel.setText(String.format("%.0f mins", simulationTime));
        avQueueLabel.setText(String.format("%.0f mins", avQueueLength));
        longestQueueNameLabel.setText(longestQueueName);
        longestQueueSizeLabel.setText(String.format(longestQueueSize + " passenger" + (longestQueueSize != 1 ? "s" : "")));
        servicePointResultsTextArea.setText(servicePointResults);
    }

    private void saveSimuParameters(int check_in, int security_check, int fasttrack, int border_control, int EU_boarding, int non_EU_Boarding) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Create and populate Parameters entity
            Parameters simulationParameters = new Parameters();
            simulationParameters.setCheck_in(check_in);
            simulationParameters.setSecurity_check(security_check);
            simulationParameters.setFasttrack(fasttrack);
            simulationParameters.setBorder_control(border_control);
            simulationParameters.setEU_boarding(EU_boarding);
            simulationParameters.setNon_EU_Boarding(non_EU_Boarding);

            // Persist Parameters entity
            em.persist(simulationParameters);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

    private void saveSimuResult(int servedClients, double meanServiceTime, double simulationTime, String longestQueuename, Parameters parameters) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Create and populate Result entity
            Result simulationResults = new Result();
            simulationResults.setServedPassenger(servedClients);
            simulationResults.setSimulationTime(simulationTime);
            simulationResults.setAverageServiceTime(meanServiceTime);
            simulationResults.setLongestQueue(longestQueuename);

            // Link the Parameters entity to the Result entity
            simulationResults.setParameters(parameters);

            // Persist the Result entity
            em.persist(simulationResults);

            em.getTransaction().commit();
            Trace.out(Trace.Level.INFO, "Results saved to the database");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Trace.out(Trace.Level.INFO, String.format("Results not saved to database (%s)", e.getMessage()));
        } finally {
            em.close();
            emf.close();
        }
        log("Simulation ended: results on the right side");
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


    private void drawServicePoints(GraphicsContext gc, int activatedCount, int totalPoints, double yStart) {
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
            double yOffset = yStart + spacingY * currentRow;

            if (i < activatedCount) {
                gc.setFill(Color.BLUE);
            } else {
                gc.setFill(Color.DARKGRAY);
            }

            gc.fillOval(x - pointDiameter / 2, yOffset - pointDiameter / 2, pointDiameter, pointDiameter);
        }
    }

    public void log(String s) {
        // Get the current time in HH:mm:ss format
        LocalTime currentTime = LocalTime.now();
        String timeString = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Create a Text object for the time and set it to bold
        Text timeText = new Text(timeString + "  ");
        timeText.setStyle("-fx-font-weight: bold;");

        // Create a Text object for the message
        Text messageText = new Text(s);

        // Combine both into a TextFlow
        TextFlow textFlow = new TextFlow(timeText, messageText);

        // Add the TextFlow to the ListView
        logListView.getItems().add(textFlow);
    }
}