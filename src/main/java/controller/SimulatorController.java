package controller;

import javafx.fxml.FXML;
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
    private Canvas airportCanvas;

    @FXML
    private TextArea logArea;

    @FXML
    private Slider checkInSlider, securityCheckSlider, securityCheckFastSlider, borderControlSlider, onboardingSlider;

    @FXML
    private Label checkInNumber, securityCheckNumber, securityCheckFastNumber, borderControlNumber, onboardingNumber;

    private final Map<String, Integer> servicePointsMap = new LinkedHashMap<>();
    private final Random random = new Random();


    @FXML
    public void initialize() {
        servicePointsMap.put("CheckIn", (int) checkInSlider.getValue());
        servicePointsMap.put("SecurityCheck", (int) securityCheckSlider.getValue());
        servicePointsMap.put("SecurityCheckFast", (int) securityCheckFastSlider.getValue());
        servicePointsMap.put("BorderControl", (int) borderControlSlider.getValue());
        servicePointsMap.put("Onboarding", (int) onboardingSlider.getValue());

        initializeSliders();

        drawAllServicePoints();
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