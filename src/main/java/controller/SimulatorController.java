package controller;

import entity.Parameters;
import entity.Result;
import framework.Trace;
import model.Customer;

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
import javafx.animation.AnimationTimer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.PassengerMover;

public class SimulatorController implements PassengerMover {
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


        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            animationSpeed = newValue.doubleValue();
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
        sim.setPassengerMover(this);
        // Set time for the simulation
        sim.setSimulationTime(timeValue);
        // Set SPs for the simulation
        sim.setAnimationSpeed(animationSpeed);
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
//        sim.run();
        new Thread(() -> sim.run()).start();


        printResults(sim.getServedClients(), sim.getMeanServiceTime(), sim.getSimulationTime());

        // Save simulation results
        saveSimuResult(
                sim.getServedClients(),
                sim.getMeanServiceTime(),
                sim.getSimulationTime(),
                sim.findLongestQueueSPName(),
                simulationParameters // Pass the Parameters object
        );
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
        // Clear the list of coordinates for the current service point type
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

    @Override
    public void movePassengerToServicePoint(Customer customer, String type, int index) {
        double[] coords = getServicePointCoordinates(type, index);
        animatePassengerMovement(customer, coords, null); // 不使用阻塞逻辑
    }


//    @Override
//    public void movePassengerToServicePoint(Customer customer, String type, int index) {
//        double[] coords = getServicePointCoordinates(type, index);
//        animatePassengerMovement(customer, coords, () -> {
//            synchronized (this) {
//                this.notify(); // 通知事件逻辑继续执行
//            }
//        });
//
//        // 等待动画完成
//        synchronized (this) {
//            try {
//                this.wait(); // 阻塞直到动画完成
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }

    private double[] getServicePointCoordinates(String type, int index) {
        return servicePointCoordinates.get(type).get(index);
    }

    private void animatePassengerMovement(Customer customer, double[] targetCoords, Runnable onFinish) {
        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();

        double[] currentPosition = customer.getCurrentPosition() != null ?
                customer.getCurrentPosition() : targetCoords;

        new AnimationTimer() {
            private final double step = 0.02;
            private double progress = 0;

            @Override
            public void handle(long now) {
                if (progress >= 1) {
                    double radius = 2;
                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
                    gc.setFill(Color.RED);
                    gc.fillOval(targetCoords[0] - radius, targetCoords[1] - radius, radius * 2, radius * 2);

                    customer.setCurrentPosition(targetCoords);
                    stop();

                    if (onFinish != null) {
                        onFinish.run();
                    }
                } else {
                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);

                    double radius = 2;
                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
                    gc.setFill(Color.RED);
                    gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

                    currentPosition[0] = x;
                    currentPosition[1] = y;
                    progress += step;
                }
            }
        }.start();
    }


//    public void movePassengerToServicePoint(Customer customer, String type, int index) {
//        double[] coords = getServicePointCoordinates(type, index);
//        animatePassengerMovement(customer, coords);
//    }
//
//    @Override
//    public void movePassengerToServicePoint(Customer customer, String type, int index) {
//        double[] coords = getServicePointCoordinates(type, index);
//        animatePassengerMovement(customer, coords, () -> {
//            synchronized (this) {
//                this.notify(); // 通知事件逻辑继续执行
//            }
//        });
//
//        // 等待动画完成
//        synchronized (this) {
//            try {
//                this.wait(); // 阻塞直到动画完成
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    private void animatePassengerMovement(Customer customer, double[] targetCoords, Runnable onFinish) {
//        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
//
//        // 获取乘客的当前位置，默认初始位置为目标位置
//        double[] currentPosition = customer.getCurrentPosition() != null ?
//                customer.getCurrentPosition() : targetCoords;
//
//        new AnimationTimer() {
//            private final double step = 0.02; // 步长
//            private double progress = 0;     // 当前进度
//
//            @Override
//            public void handle(long now) {
//                if (progress >= 1) {
//                    // 动画完成时绘制目标位置
//                    double radius = 2;
//                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(targetCoords[0] - radius, targetCoords[1] - radius, radius * 2, radius * 2);
//
//                    // 更新乘客当前位置并停止动画
//                    customer.setCurrentPosition(targetCoords);
//                    stop();
//
//                    // 动画完成时回调通知
//                    if (onFinish != null) {
//                        onFinish.run();
//                    }
//                } else {
//                    // 动画中逐帧更新当前位置
//                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
//                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);
//
//                    double radius = 2;
//                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
//
//                    // 更新当前位置和进度
//                    currentPosition[0] = x;
//                    currentPosition[1] = y;
//                    progress += step;
//                }
//            }
//        }.start();
//    }
//

//    @Override
//    public void movePassengerToServicePoint(Customer customer, String type, int index) {
//        double[] coords = getServicePointCoordinates(type, index);
//        animatePassengerMovement(customer, coords);
//    }
//
//    // 获取服务点坐标
//    private double[] getServicePointCoordinates(String type, int index) {
//        List<double[]> coords = servicePointCoordinates.get(type);
//        return coords != null && index < coords.size() ? coords.get(index) : new double[] {0, 0};
//    }
//
//    private void animatePassengerMovement(Customer customer, double[] targetCoords) {
//        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
//
//        // 获取乘客的当前位置，默认初始位置为目标位置
//        double[] currentPosition = customer.getCurrentPosition() != null ?
//                customer.getCurrentPosition() : targetCoords;
//
//        // 使用 AnimationTimer 动画逐帧更新
//        new AnimationTimer() {
//            private final double step = 0.02; // 步长
//            private double progress = 0;     // 当前进度
//
//            @Override
//            public void handle(long now) {
//                if (progress >= 1) {
//// 动画完成时绘制目标位置
//                    double radius = 2; // 将圆点的半径调整为更小的值，例如 3
//                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(targetCoords[0] - radius, targetCoords[1] - radius, radius * 2, radius * 2);
//
//
//                    // 更新乘客当前位置并停止动画
//                    customer.setCurrentPosition(targetCoords);
//                    stop();
//                } else {
//                    // 动画中逐帧更新当前位置
//                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
//                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);
//
//                    // 清理旧位置并绘制新位置
//// 动画完成时绘制目标位置
//                    double radius = 2; // 将圆点的半径调整为更小的值，例如 3
//                    gc.clearRect(currentPosition[0] - radius - 1, currentPosition[1] - radius - 1, radius * 2 + 2, radius * 2 + 2);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(targetCoords[0] - radius, targetCoords[1] - radius, radius * 2, radius * 2);
//
//
//                    // 更新当前显示位置和进度
//                    currentPosition[0] = x;
//                    currentPosition[1] = y;
//                    progress += step;
//                }
//            }
//        }.start();
//    }



//    private void animatePassengerMovement(Customer customer, double[] targetCoords) {
//        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
//
//        // 使用数组包装 currentPosition
//        double[] currentPosition = customer.getCurrentPosition() != null ?
//                customer.getCurrentPosition() :
//                new double[]{targetCoords[0], targetCoords[1]};
//        customer.setCurrentPosition(currentPosition);
//
//        new AnimationTimer() {
//            private final double step = 0.02; // 步长，值越小动画越平滑
//            private double progress = 0; // 当前进度
//
//            @Override
//            public void handle(long now) {
//                if (progress >= 1) {
//                    gc.clearRect(currentPosition[0] - 5, currentPosition[1] - 5, 10, 10);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(targetCoords[0] - 5, targetCoords[1] - 5, 10, 10);
//                    customer.setCurrentPosition(targetCoords);
//                    stop(); // 停止动画
//                } else {
//                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
//                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);
//                    gc.clearRect(currentPosition[0] - 5, currentPosition[1] - 5, 10, 10);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(x - 5, y - 5, 10, 10);
//                    customer.setCurrentPosition(new double[]{x, y}); // 同步更新
//                    progress += step;
//                }
//            }
//
//
////            @Override
////            public void handle(long now) {
////                if (progress >= 1) {
////                    // 动画结束，绘制最终位置
////                    gc.clearRect(currentPosition[0] - 5, currentPosition[1] - 5, 10, 10);
////                    gc.setFill(Color.RED);
////                    gc.fillOval(targetCoords[0] - 5, targetCoords[1] - 5, 10, 10);
////
////                    // 更新乘客当前位置
////                    currentPosition[0] = targetCoords[0];
////                    currentPosition[1] = targetCoords[1];
////                    customer.setCurrentPosition(currentPosition);
////                    stop(); // 停止动画
////                } else {
////                    // 插值计算当前位置
////                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
////                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);
////
////                    // 清除之前位置
////                    gc.clearRect(currentPosition[0] - 5, currentPosition[1] - 5, 10, 10);
////
////                    // 绘制新位置
////                    gc.setFill(Color.RED);
////                    gc.fillOval(x - 5, y - 5, 10, 10);
////
////                    // 更新当前位置
////                    currentPosition[0] = x;
////                    currentPosition[1] = y;
////                    progress += step; // 增加进度
////                }
////            }
//        }.start();
//    }


//    // 动画乘客移动
//    private void animatePassengerMovement(Customer customer, double[] targetCoords) {
//        GraphicsContext gc = passengerCanvas.getGraphicsContext2D();
//
//        double[] currentPosition = customer.getCurrentPosition();
//        if (currentPosition == null) {
//            // 如果当前位置为空，初始化为目标位置
//            currentPosition = new double[]{targetCoords[0], targetCoords[1]};
//            customer.setCurrentPosition(currentPosition);
//        }
//
//        // 动画定时器
//        new AnimationTimer() {
//            private final double step = 0.02; // 步长，值越小动画越平滑
//            private double progress = 0; // 当前进度
//
//            @Override
//            public void handle(long now) {
//                if (progress >= 1) {
//                    // 动画结束，绘制最终位置
//                    gc.clearRect(currentPosition[0] - 5, currentPosition[1] - 5, 10, 10);
//                    gc.setFill(Color.RED);
//                    gc.fillOval(targetCoords[0] - 5, targetCoords[1] - 5, 10, 10);
//
//                    // 更新乘客当前位置
//                    customer.setCurrentPosition(targetCoords);
//                    stop(); // 停止动画
//                } else {
//                    // 插值计算当前位置
//                    double x = currentPosition[0] + progress * (targetCoords[0] - currentPosition[0]);
//                    double y = currentPosition[1] + progress * (targetCoords[1] - currentPosition[1]);
//
//                    // 清除之前位置
//                    gc.clearRect(currentPosition[0] - 5, currentPosition[1] - 5, 10, 10);
//
//                    // 绘制新位置
//                    gc.setFill(Color.RED);
//                    gc.fillOval(x - 5, y - 5, 10, 10);
//
//                    // 更新当前位置
//                    customer.setCurrentPosition(new double[]{x, y});
//                    progress += step; // 增加进度
//                }
//            }
//        }.start();
//    }


}
