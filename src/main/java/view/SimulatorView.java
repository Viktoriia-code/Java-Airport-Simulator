package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SimulatorView extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulator_interface.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Airport Simulator");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
}
