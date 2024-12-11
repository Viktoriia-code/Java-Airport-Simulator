package controller;

import entity.Parameters;
import entity.Result;
import framework.Trace;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.PassengerMover;

public class SimulatorController  implements PassengerMover {
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

    @FXML
    private Canvas passengerCanvas;

    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();

    private final Map<String, Double> customerTypesMap = new LinkedHashMap<>();

    private final Map<String, Integer> maxServicePointsMap = new LinkedHashMap<>();
    private final Map<String, List<double[]>> servicePointCoordinates = new LinkedHashMap<>();


    private double animationSpeed = 1.0;

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
    private ListView logListView;

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

        initializeSliders();

        drawAllServicePoints();

        helpButton.setOnAction(event -> showInstructions());

        // Configure the speed slider
        speedSlider.setMin(0); // Start at 0
        speedSlider.setMax(5.0); // End at 5.0
        speedSlider.setValue(1.0); // Default value
        speedSlider.setMajorTickUnit(1.0); // Major ticks at 1, 2, 3, 4, 5
        speedSlider.setMinorTickCount(4); // No minor ticks
        speedSlider.setSnapToTicks(true); // Snap to defined ticks
        speedSlider.setShowTickMarks(true); // Show tick marks
        speedSlider.setShowTickLabels(true); // Show tick labels

        // Bind the slider's value to update the label and simulation speed
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double speed = Math.round(newValue.doubleValue() * 10) / 10.0; // Round to 1 decimal
            speedLabel.setText("Speed: " + speed + "x");
            setSimulationSpeed(speed);
        });





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

        log("Welcome to the Airport simulation!");
    }


    private void setSimulationSpeed(double speed) {
        if (speed == 0.0) {
            pauseSimulation(); // Pause when speed is 0
        } else {
            animationSpeed = speed; // Update the animation speed multiplier
            adjustSimulationSpeed(); // Apply the new speed to the simulation engine
        }
    }

    // Pause simulation logic
    private void pauseSimulation() {
        System.out.println("Simulation paused.");
        animationSpeed = 0.0;
        // Add logic to pause the animation timer or simulation engine
    }

    // Apply speed multiplier logic
    private void adjustSimulationSpeed() {
        System.out.println("Simulation speed adjusted to " + animationSpeed + "x.");
        // Update the simulation engine with the new animation speed
        // Example: pass `animationSpeed` to animation timers or engine logic
    }










    /**
     * Binds a slider to a label and updates the label with the slider's value.
     * @param slider the slider to observe
     * @param label the label to update
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

    @FXML
    private void startSimulation() {
        int timeValue = timeSpinner.getValue();
        int checkInPoints = Integer.valueOf((int) checkInSlider.getValue());
        int regularSecurityCheckPoints = Integer.valueOf((int) regularSecurityCheckSlider.getValue());
        int fastSecurityCheckPoints = Integer.valueOf((int) fastSecurityCheckSlider.getValue());
        int borderControlPoints = Integer.valueOf((int) borderControlSlider.getValue());
        int euOnboardingPoints = Integer.valueOf((int) euOnboardingSlider.getValue());
        int outEuOnboardingPoints = Integer.valueOf((int) outEuOnboardingSlider.getValue());

        int businessClassValue = 100 - Integer.valueOf((int) classSlider.getValue());
        int euFlightValue = Integer.valueOf((int) euFlightSlider.getValue());
        int onlineCheckInValue = Integer.valueOf((int) onlineCheckInSlider.getValue());

        int checkInTime = Integer.valueOf((int) checkInTimeSlider.getValue());
        int securityTime = Integer.valueOf((int) securityTimeSlider.getValue());
        int borderTime = Integer.valueOf((int) borderTimeSlider.getValue());
        int onboardingTime = Integer.valueOf((int) onboardingTimeSlider.getValue());

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
        // Set time for SP
        sim.setAllTimingMeans(
                5,
                checkInTime,
                securityTime,
                borderTime,
                onboardingTime
        );
        // Set PassengerMover
        sim.setPassengerMover(this);

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
        log(String.format(
                "Simulation started with: Time=%d, CheckIn=%d, RegularSec=%d, FastSec=%d,\n" +
                " BorderControl=%d, EUOnboard=%d, OutEUOnboard=%d, OnlineCheckIn=%d%%, EUFlights=%d%%,\n" +
                " BusinessClass=%d%%",
                timeValue, checkInPoints, regularSecurityCheckPoints, fastSecurityCheckPoints,
                borderControlPoints, euOnboardingPoints, outEuOnboardingPoints,
                onlineCheckInValue, euFlightValue, businessClassValue
        ));
        sim.run();

        printResults(
                sim.getServedClients(),
                sim.getAvServiceTime(),
                sim.getSimulationTime(),
                sim.getAverageQueueSize(),
                sim.getLongestQueueSPName(),
                sim.getLongestQueueSize()
        );

        // Save simulation results
        saveSimuResult(
                sim.getServedClients(),
                sim.getAvServiceTime(),
                sim.getSimulationTime(),
                sim.getLongestQueueSPName(),
                simulationParameters // Pass the Parameters object
        );
    }

    public void printResults(int customersServed, double meanServiceTime, double simulationTime, int avQueueSize, String longestQueueName, int longestQueueSize) {
        totalPassengersServedLabel.setText(String.valueOf(customersServed));
        avServiceTimeLabel.setText(String.format("%.0f mins", meanServiceTime));
        simulationTimeLabel.setText(String.format("%.0f mins", simulationTime));
        avQueueLabel.setText(String.format(avQueueSize + " passengers"));
        longestQueueNameLabel.setText(longestQueueName);
        longestQueueSizeLabel.setText(String.format(longestQueueSize + " passengers"));
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
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
        log("Simulation ended");

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

            drawServicePoints(gc, activatedPoints, totalPoints, y, pointType);
            drawTypeLabel(gc, pointType, y - 15);
            typeIndex++;
        }
    }



    private void drawServicePoints(GraphicsContext gc,  int activatedCount, int totalPoints, double yStart, String type) {
        double pointDiameter = 10.0;
        double spacingX = 20.0;
        double spacingY = 30.0;
        int pointsPerRow = 26;

        List<double[]> coords = servicePointCoordinates.computeIfAbsent(type, k -> new ArrayList<>());
        coords.clear();

        int currentRow = 0;
        for (int i = 0; i < totalPoints; i++) {
            int rowPosition = i % pointsPerRow;
            if (i > 0 && rowPosition == 0) {
                currentRow++;
            }

            double x = spacingX * (rowPosition + 1);
            double yOffset = yStart  + spacingY * currentRow;

            coords.add(new double[]{x, yOffset});

            if (i < activatedCount) {
                gc.setFill(Color.BLUE);
            } else {
                gc.setFill(Color.DARKGRAY);
            }

            gc.fillOval(x - pointDiameter / 2, yOffset - pointDiameter / 2, pointDiameter, pointDiameter);
        }
        System.out.println("Drawing type: " + type + ", Activated Count: " + activatedCount + ", Total Points: " + totalPoints);
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

    // PassengerMover interface implementation
    private double[] getServicePointCoordinates(String type, int index) {
        List<double[]> coords = servicePointCoordinates.get(type);
        if (coords == null || index < 0 || index >= coords.size()) {
            System.err.println("Invalid coordinates for type: " + type + ", index: " + index);
            return null;
        }
        return coords.get(index);
    }

    private void clearPassengers() {
        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, passengerCanvas.getWidth(), passengerCanvas.getHeight());
    }



    @Override
    public void movePassengerToServicePoint(Customer customer, String type, int index) {
        System.out.println("Attempting to move customer #" + customer.getId() + " to type: " + type + ", index: " + index);

        double[] targetCoords = getServicePointCoordinates(type, index);
        if (targetCoords == null) {
            System.out.println("Cannot proceed with animation because targetCoords is null for type: " + type + ", index: " + index);
            return;
        }

//        animatePassengerMovement(customer, targetCoords, index, null);
//        customer.setCurrentPosition(targetCoords);
//        customer.setCurrentQueueIndex(index);
//        System.out.println("Customer #" + customer.getId() + " moved to: " + type + ", index: " + index);
//
        animatePassengerMovement(customer, targetCoords, index, () -> {
            customer.setCurrentPosition(targetCoords);
            customer.setCurrentQueueIndex(index);
            System.out.println("Customer #" + customer.getId() + " moved to: " + type + ", index: " + index);
        });
    }



    private void animatePassengerMovement(Customer customer, double[] targetCoords,int targetIndex, Runnable onFinish) {
        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();

        double[] currentPosition = customer.getCurrentPosition() != null ?
                customer.getCurrentPosition() : targetCoords;

        if (currentPosition == null) {
            System.out.println("Error: currentPosition is null, cannot start animation.");
            return;
        }

        if (targetCoords == null) {
            System.out.println("Error: targetCoords is null, cannot start animation.");
            return;
        }


        new AnimationTimer() {
            private final double step = 0.005;
            private double progress = 0;


            @Override
            public void handle(long now) {
                double radius = 4;

                if (progress >= 1) {
                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
                    gc.setFill(Color.RED);
                    gc.fillOval(targetCoords[0] - radius, targetCoords[1] - radius, radius * 2, radius * 2);

//                    currentPosition[0] = targetCoords[0];
//                    currentPosition[1] = targetCoords[1];
                    customer.setCurrentPosition(targetCoords);
                    stop();

//                    if (simulationEnded) {
//                        clearPassengers();
//                    }
                    if (onFinish != null) {
                        onFinish.run();
                    }
                } else {
                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);

                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);

                    gc.setFill(Color.RED);
                    gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

                    currentPosition[0] = x;
                    currentPosition[1] = y;

//                    drawAllServicePoints();
                    progress += step * animationSpeed; // Scale progress by animationSpeed
                }
            }
        }.start();
    }
}


//
//private void animatePassengerMovement(Customer customer, double[] targetCoords, int targetIndex, Runnable onFinish) {
//    GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
//
//    double[] currentPosition = customer.getCurrentPosition() != null ?
//            customer.getCurrentPosition() : targetCoords;
//
//    if (currentPosition == null || targetCoords == null) {
//        System.out.println("Error: Invalid position for animation.");
//        return;
//    }
//
//    new AnimationTimer() {
//        private final double step = 0.005; // Animation speed step
//        private double progress = 0; // Animation progress
//
//        @Override
//        public void handle(long now) {
//            // Animation complete
//            if (progress >= 1) {
//                gc.setFill(Color.RED);
//                gc.fillOval(targetCoords[0] - 2, targetCoords[1] - 2, 8, 8); // Final position
//                customer.setCurrentPosition(targetCoords);
//                stop();
//                if (onFinish != null) onFinish.run();
//            } else {
//                // Calculate interpolated position
//                double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
//                double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);
//
//                // Clear previous position
//                gc.clearRect(currentPosition[0] - 4, currentPosition[1] - 4, 8, 8);
//
//                // Draw passenger
//                gc.setFill(Color.RED);
//                gc.fillOval(x - 2, y - 2, 4, 4);
//
//                // Update current position and progress
//                currentPosition[0] = x;
//                currentPosition[1] = y;
//                progress += step * animationSpeed; // Adjust for speed
//            }
//        }
//    }.start();
//}