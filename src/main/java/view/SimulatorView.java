package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * This class is responsible for initializing and displaying the primary stage
 * of the JavaFX application. It loads the FXML layout for the simulator interface
 * and sets the application icon and window title.
 */
public class SimulatorView extends Application {
    /**
     * Starts the JavaFX application by setting up the primary stage.
     *
     * @param primaryStage the main stage of the application where the UI is displayed.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulator_interface.fxml"));
        Parent root = loader.load();

        // Set the title
        primaryStage.setTitle("Airport Simulator");
        // Set the icon
        Image applicationIcon = new Image(getClass().getResourceAsStream("/icon.png"));
        primaryStage.getIcons().add(applicationIcon);

        primaryStage.setScene(new Scene(root, 1105, 805));
        primaryStage.show();
    }
}
