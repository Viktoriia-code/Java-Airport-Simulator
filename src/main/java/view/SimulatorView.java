package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SimulatorView extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulator_interface.fxml"));
        Parent root = loader.load();

        // Set the title
        primaryStage.setTitle("Airport Simulator");
        // Set the icon
        Image applicationIcon = new Image(getClass().getResourceAsStream("/icon.png"));
        primaryStage.getIcons().add(applicationIcon);

        primaryStage.setScene(new Scene(root, 1100, 800));
        primaryStage.show();
    }
}
