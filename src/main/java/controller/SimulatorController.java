package controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// An airport simulator controller class, ONLY for example purposes and NOT part of the final application

public class SimulatorController {

    @FXML
    private Canvas airportCanvas;

    @FXML
    private TextArea logArea;

    private final List<ServicePoint> servicePoints = new ArrayList<>();
    private final Random random = new Random();

    @FXML
    public void initialize() {
        initializeServicePoints(5);
        logMessage("Initialized with 5 service points.");
    }

    private void initializeServicePoints(int count) {
        servicePoints.clear();
        GraphicsContext gc = airportCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, airportCanvas.getWidth(), airportCanvas.getHeight());

        double spacing = airportCanvas.getWidth() / (count + 1);
        double y = airportCanvas.getHeight() / 2;

        for (int i = 0; i < count; i++) {
            double x = spacing * (i + 1);
            servicePoints.add(new ServicePoint(x, y));
            drawServicePoint(gc, x, y, i + 1);
        }
    }

    private void drawServicePoint(GraphicsContext gc, double x, double y, int id) {
        gc.setFill(Color.BLUE);
        gc.fillOval(x - 15, y - 15, 30, 30);
        gc.setFill(Color.WHITE);
        gc.fillText("SP" + id, x - 10, y + 5);
    }

    public void generatePassenger() {
        GraphicsContext gc = airportCanvas.getGraphicsContext2D();
        double startX = random.nextDouble() * airportCanvas.getWidth();
        double startY = random.nextDouble() * airportCanvas.getHeight();

        Passenger passenger = new Passenger(startX, startY);
        ServicePoint target = servicePoints.get(random.nextInt(servicePoints.size()));

        logMessage("Passenger created at (" + (int) startX + ", " + (int) startY + ") -> Target: SP" + (servicePoints.indexOf(target) + 1));
        movePassenger(gc, passenger, target);
    }

    private void movePassenger(GraphicsContext gc, Passenger passenger, ServicePoint target) {
        double dx = (target.getX() - passenger.getX()) / 50;
        double dy = (target.getY() - passenger.getY()) / 50;

        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                passenger.setX(passenger.getX() + dx);
                passenger.setY(passenger.getY() + dy);

                Platform.runLater(() -> {
                    gc.clearRect(passenger.getX() - 5, passenger.getY() - 5, 10, 10);
                    gc.setFill(Color.RED);
                    gc.fillOval(passenger.getX() - 5, passenger.getY() - 5, 10, 10);
                });

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logMessage("Passenger arrived at SP" + (servicePoints.indexOf(target) + 1));
        }).start();
    }

    private void logMessage(String message) {
        logArea.appendText(message + "\n");
    }

    private static class ServicePoint {
        private final double x;
        private final double y;

        public ServicePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private static class Passenger {
        private double x;
        private double y;

        public Passenger(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}


//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//
//public class SimulatorController {
//
//    @FXML
//    private Spinner<Integer> timeSpinner;
//
//    @FXML
//    private Spinner<Integer> passengerSpinner;
//
//    @FXML
//    public void initialize() {
//        // control for the time spinner
//        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30000, 1000, 100);
//        timeSpinner.setValueFactory(timeValueFactory);
//
//        // control for the passenger spinner
//        SpinnerValueFactory<Integer> passengerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 100);
//        passengerSpinner.setValueFactory(passengerValueFactory);
//    }
//}
