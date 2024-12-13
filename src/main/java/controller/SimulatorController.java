package controller;

import entity.Parameters;
import entity.Result;
import framework.Trace;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.Customer;
import model.MyEngine;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import javafx.animation.AnimationTimer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.ServicePoint;

/**
 * Controller class for the Airport Simulator application.
 * Handles user input, simulation setup, and results display.
 */
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

    // Speed control settings
    @FXML
    private Button playButton;
    @FXML
    private Button stopButton;
    @FXML
    private Slider speedSlider;
    @FXML
    private Label speedLabel;

    boolean isPaused = false;

    // Maps to store the number of service points for each type
    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();

    private final Map<String, Double> customerTypesMap = new LinkedHashMap<>();

    private final Map<String, Integer> maxServicePointsMap = new LinkedHashMap<>();
    private AnimationTimer animationTimer;
    private Map<String, Point2D> servicePointPositions = new LinkedHashMap<String, Point2D>();

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


    private MyEngine sim;

    /**
     * Initializes the Airport Simulator application.
     */
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

    /**
     * Toggles the pause state of the simulation.
     */
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

    /**
     * Stops the simulation.
     */
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

    /**
     * Draws a label for a specific point type on the canvas.
     *
     * @param gc the {@link GraphicsContext} used to draw on the canvas.
     * @param pointType the label text representing the type of the point to be drawn.
     * @param y the vertical position (Y-coordinate) on the canvas where the label will be drawn.
     */
    private void drawTypeLabel(GraphicsContext gc, String pointType, double y) {
        double xLeftEdge = 5;
        gc.setFill(Color.BLACK);
        gc.fillText(pointType, xLeftEdge, y-15);
    }

    /**
     * Shows the instructions for the Airport Simulator.
     */
    private void showInstructions() {
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

    /**
     * Validates the input for a spinner and checks if it falls within the specified range.
     *
     * @param spinner the spinner component whose input is being validated
     * @param newValue the value entered by the user in the spinner's text field
     * @param min the minimum acceptable value for the spinner
     * @param max the maximum acceptable value for the spinner
     * @param labelName the name of the label associated with the spinner, used for error message formatting
     */
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

    /**
     * Retrieves the frequency corresponding to the selected passenger arrival rate option.
     *
     * This method converts the selected option from a dropdown (such as "Fast", "Moderate",
     * "Normal", "Slow", or "Very Slow") into a frequency value, where the frequency is expressed
     * in minutes per cycle. The available options represent different time intervals for how often
     * passengers arrive, with each option corresponding to a specific time interval in seconds,
     * converted to minutes.
     *
     * @return the frequency in minutes corresponding to the selected option.
     * @throws IllegalArgumentException if the selected option does not match any of the predefined options.
     */
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

    /**
     * Starts the simulation with the specified settings.
     */
    @FXML
    private void startSimulation() {
        sim = new MyEngine();

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

            sim.setPositionProvider(key -> servicePointPositions.get(key));

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

            Platform.runLater(() -> {
                log("Simulation started...");
                startAnimation();
            });

            sim.run();

            sim.stopSimulation();
            Platform.runLater(() -> finishSim(sim, simulationParameters));

        }).start();
    }

    /**
     * Sets the speed of the simulation based on the selected speed mode.
     *
     * @param sim the simulation engine
     */
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

    /**
     * Finishes the simulation and displays the results.
     *
     * @param sim the simulation engine
     * @param simulationParameters the parameters used for the simulation
     */
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

    /**
     * Prints the simulation results to the screen.
     *
     * @param customersServed
     * @param meanServiceTime
     * @param simulationTime
     * @param avQueueLength
     * @param longestQueueName
     * @param longestQueueSize
     * @param servicePointResults
     */
    public void printResults(int customersServed, double meanServiceTime, double simulationTime, double avQueueLength, String longestQueueName, int longestQueueSize, String servicePointResults) {
        totalPassengersServedLabel.setText(String.valueOf(customersServed));
        avServiceTimeLabel.setText(String.format("%.0f mins", meanServiceTime));
        simulationTimeLabel.setText(String.format("%.0f mins", simulationTime));
        avQueueLabel.setText(String.format("%.0f mins", avQueueLength));
        longestQueueNameLabel.setText(longestQueueName);
        longestQueueSizeLabel.setText(String.format(longestQueueSize + " passenger" + (longestQueueSize != 1 ? "s" : "")));
        servicePointResultsTextArea.setText(servicePointResults);
    }

    /**
     * Saves the simulation results to a MariaDB database using JPA.
     *
     * This method records the results of a simulation. It creates a new Result entity, populates it with the provided data,
     * and persists it to the database.
     *
     * @param servedClients the total number of clients served during the simulation.
     * @param meanServiceTime the mean service time of the clients during the simulation.
     * @param simulationTime the total time the simulation ran, in seconds or minutes (depending on the context).
     * @param longestQueuename the name of the queue that had the longest waiting time during the simulation.
     * @param parameters the Parameters entity associated with the simulation run.
     */
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

    /**
     * Initializes the sliders for the service points.
     */
    private void initializeSliders() {
        setupSlider(checkInSlider, checkInLabel, "CheckIn");
        setupSlider(regularSecurityCheckSlider, regularSecurityCheckLabel, "RegularSecurityCheck");
        setupSlider(fastSecurityCheckSlider, fastSecurityCheckLabel, "FastSecurityCheck");
        setupSlider(borderControlSlider, borderControlLabel, "BorderControl");
        setupSlider(euOnboardingSlider, euOnboardingLabel, "EuOnboarding");
        setupSlider(outEuOnboardingSlider, outEuOnboardingLabel, "OutEuOnboarding");
    }

    /**
     * Sets up a slider with a label and a point type.
     *
     * @param slider
     * @param label
     * @param pointType
     */
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

//            if (pointType.equals("CheckIn") && typeIndex > 0) {
//                y += 2;
//            } else if (typeIndex > 0) {
//                y += yStep;
//            }
            if (pointType.equals("CheckIn") && typeIndex > 0) {
                y += 2;
            } else if (pointType.equals("EuOnboarding") && typeIndex > 0) {
                y += 85;
            } else if (typeIndex > 0) {
                y += yStep;
            }


            drawServicePoints(gc, pointType, activatedPoints, totalPoints, y);

            drawTypeLabel(gc, pointType, y - 15);
            typeIndex++;
        }
    }


    private void drawServicePoints(GraphicsContext gc, String pointType, int activatedCount, int totalPoints, double yStart) {
        double rectWidth = 15.0;
        double rectHeight = 10.0;
        double spacingX = 28.0;
        double spacingY = 30.0;
        int pointsPerRow = 20;

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

            gc.fillRect(x - rectWidth / 2, yOffset - rectHeight / 2, rectWidth, rectHeight);

            // Store the service point coordinates in the Map
            String key = pointType + "#" + i;
            servicePointPositions.put(key, new Point2D(x, yOffset));


        }
    }

    @FXML
    private Canvas labelCanvas;

    private void drawQueueLabels() {
        GraphicsContext gc = labelCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, labelCanvas.getWidth(), labelCanvas.getHeight()); // 清除标签区域

        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 12)); // 设置字体

        for (Map.Entry<String, Point2D> entry : servicePointPositions.entrySet()) {
            String spKey = entry.getKey();
            int queueSize = getQueueSizeForServicePoint(spKey);
            Point2D position = entry.getValue();

            if (queueSize > 0) {
                double x = position.getX();
                double y = position.getY() - 15;
                gc.fillText(String.valueOf(queueSize), x - 10, y);
            }
        }
    }


    private static final Map<String, String> normalizedKeyMap = new HashMap<>();
    static {
        normalizedKeyMap.put("Check-in", "checkin");
        normalizedKeyMap.put("Security check", "regularsecuritycheck");
        normalizedKeyMap.put("Border control", "bordercontrol");
        normalizedKeyMap.put("Security check (Fast Track)", "fastsecuritycheck");
        normalizedKeyMap.put("Boarding (inside EU)", "euonboarding");
        normalizedKeyMap.put("Boarding (outside EU)", "outeuonboarding");
    }



    private int getQueueSizeForServicePoint(String spKey) {
        for (ArrayList<ServicePoint> servicePointList : sim.getAllServicePoints()) {
            for (ServicePoint sp : servicePointList) {
//                String fullKey = sp.getName() + "#" + servicePointList.indexOf(sp);
                String normalizedFullKey = normalizedKeyMap.getOrDefault(sp.getName(), sp.getName().toLowerCase()) + "#" + servicePointList.indexOf(sp);
                if (spKey.toLowerCase().equals(normalizedFullKey)) {
                    return sp.getQueueSize();
                    }

            }
        }
        return 0;
    }







    /**
     * Logs a message to the log list view.
     *
     * @param s the message to log
     */
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






//    private void pauseAnimation() {
//        if (animationTimer != null) {
//            animationTimer.stop();
//            log("Passenger animation paused.");
//        }
//    }
//
//    private void resumeAnimation() {
//        if (animationTimer != null) {
//            animationTimer.start();
//            log("Passenger animation resumed.");
//        }
//    }

    @FXML
    private Canvas passengerCanvas;

    private void drawPassengers() {
        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, passengerCanvas.getWidth(), passengerCanvas.getHeight());

        for (Customer c : sim.getAllCustomers()) {
            gc.setFill(Color.RED); // Set the customer color
            gc.fillOval(c.getX() - 5, c.getY() - 5, 8, 8); // Draw the customer dot

        }
    }

    private void updatePassengerPositions() {
        if (sim == null) {
            System.err.println("Simulation engine (sim) is null.");
            return;
        }

        for (Customer c : sim.getAllCustomers()) {

            c.updatePosition();

        }
    }

    public void startAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        animationTimer = new AnimationTimer() {
            private long lastUpdateTime = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdateTime >= 33_000_000) {
                    updatePassengerPositions();
                    drawPassengers();
                    drawQueueLabels();
                    lastUpdateTime = now;
                }
            }
        };


        animationTimer.start();
    }

}