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



//public class SimulatorController {
//
//    @FXML
//    private Canvas airportCanvas;
//
//    @FXML
//    private TextArea logArea;
//
//    @FXML
//    private Slider checkInSlider;
//
//    @FXML
//    private Label checkInNumber;
//
//    private final List<ServicePoint> servicePoints = new ArrayList<>();
//    private final Random random = new Random();
//
//    @FXML
//    public void initialize() {
//        // 初始化默认服务点数量为滑动条的默认值
//        int defaultPoints = (int) checkInSlider.getValue();
//        initializeServicePoints(defaultPoints);
//        logMessage("Initialized with " + defaultPoints + " service points.");
//
//        // 添加滑动条的值变化监听器
//        checkInSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            // 更新右侧数字显示
//            checkInNumber.setText(String.valueOf(newValue.intValue()));
//
//            // 根据滑动条的值动态更新服务点
//            initializeServicePoints(newValue.intValue());
//            logMessage("Updated to " + newValue.intValue() + " service points.");
//        });
//    }
//
//    // 初始化服务点
//    private void initializeServicePoints(int count) {
//        servicePoints.clear(); // 清空现有服务点
//        GraphicsContext gc = airportCanvas.getGraphicsContext2D();
//        gc.clearRect(0, 0, airportCanvas.getWidth(), airportCanvas.getHeight()); // 清空画布
//
//        double spacing = airportCanvas.getWidth() / (count + 1); // 均匀分布
//        double y = airportCanvas.getHeight() / 2; // 服务点的垂直位置
//
//        for (int i = 0; i < count; i++) {
//            double x = spacing * (i + 1); // 水平分布
//            servicePoints.add(new ServicePoint(x, y));
//            drawServicePoint(gc, x, y, i + 1);
//        }
//    }
//
//    // 绘制服务点
//    private void drawServicePoint(GraphicsContext gc, double x, double y, int id) {
//        gc.setFill(Color.BLUE);
//        gc.fillOval(x - 15, y - 15, 30, 30); // 绘制蓝色圆形服务点
//        gc.setFill(Color.WHITE);
//        gc.fillText("SP" + id, x - 10, y + 5); // 显示服务点编号
//    }
//
//    // 生成乘客
//    public void generatePassenger() {
//        GraphicsContext gc = airportCanvas.getGraphicsContext2D();
//        double startX = random.nextDouble() * airportCanvas.getWidth();
//        double startY = random.nextDouble() * airportCanvas.getHeight();
//
//        Passenger passenger = new Passenger(startX, startY);
//        ServicePoint target = servicePoints.get(random.nextInt(servicePoints.size())); // 随机选择一个目标服务点
//
//        logMessage("Passenger created at (" + (int) startX + ", " + (int) startY + ") -> Target: SP" + (servicePoints.indexOf(target) + 1));
//        movePassenger(gc, passenger, target);
//    }
//
//    // 移动乘客
//    private void movePassenger(GraphicsContext gc, Passenger passenger, ServicePoint target) {
//        double dx = (target.getX() - passenger.getX()) / 50; // 每步移动的 X 距离
//        double dy = (target.getY() - passenger.getY()) / 50; // 每步移动的 Y 距离
//
//        new Thread(() -> {
//            for (int i = 0; i < 50; i++) {
//                passenger.setX(passenger.getX() + dx);
//                passenger.setY(passenger.getY() + dy);
//
//                // 更新图形需要在 JavaFX 主线程中运行
//                Platform.runLater(() -> {
//                    gc.clearRect(passenger.getX() - 5, passenger.getY() - 5, 10, 10); // 清除旧位置
//                    gc.setFill(Color.RED);
//                    gc.fillOval(passenger.getX() - 5, passenger.getY() - 5, 10, 10); // 绘制乘客新位置
//                });
//
//                try {
//                    Thread.sleep(50); // 控制移动速度
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            logMessage("Passenger arrived at SP" + (servicePoints.indexOf(target) + 1));
//        }).start();
//    }
//
//    // 日志输出
//    private void logMessage(String message) {
//        logArea.appendText(message + "\n");
//    }
//
//    // 内部类：服务点
//    private static class ServicePoint {
//        private final double x;
//        private final double y;
//
//        public ServicePoint(double x, double y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        public double getX() {
//            return x;
//        }
//
//        public double getY() {
//            return y;
//        }
//    }
//
//    // 内部类：乘客
//    private static class Passenger {
//        private double x;
//        private double y;
//
//        public Passenger(double x, double y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        public double getX() {
//            return x;
//        }
//
//        public void setX(double x) {
//            this.x = x;
//        }
//
//        public double getY() {
//            return y;
//        }
//
//        public void setY(double y) {
//            this.y = y;
//        }
//    }
//}












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
