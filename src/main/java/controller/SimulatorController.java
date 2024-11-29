package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SimulatorController {

    @FXML
    private Label checkInLabel;
    @FXML
    private Slider checkInSlider;

    @FXML
    private Label securityCheckLabel;
    @FXML
    private Slider securityCheckSlider;

    @FXML
    private Label borderControlLabel;
    @FXML
    private Slider borderControlSlider;

    @FXML
    private Label onboardingLabel;
    @FXML
    private Slider onboardingSlider;

    @FXML
    private Label speedLabel;
    @FXML
    private Slider speedSlider;

    @FXML
    private Spinner<Integer> timeSpinner;

    @FXML
    private Spinner<Integer> passengerSpinner;

    @FXML
    private Label inputErrorLabel;

    @FXML
    private Button helpButton;

    @FXML
    public void initialize() {
        helpButton.setOnAction(event -> showInstructions());

        // control for the check-in slider
        checkInSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                checkInLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the security check slider
        securityCheckSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                securityCheckLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the border control slider
        borderControlSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                borderControlLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the onboarding slider
        onboardingSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                onboardingLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // control for the speed slider
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                speedLabel.setText(String.format("%.0f", newValue.doubleValue())) // Format to integer display
        );

        // Control for the time spinner
        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30000, 1000, 100);
        timeSpinner.setValueFactory(timeValueFactory);

        // Control for the passenger spinner
        SpinnerValueFactory<Integer> passengerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 100);
        passengerSpinner.setValueFactory(passengerValueFactory);

        // Validate input for the time and passenger spinners
        timeSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> validateInput(timeSpinner, newValue, 1, 30000, "Time"));
        passengerSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> validateInput(passengerSpinner, newValue, 1, 1000, "Passenger count"));

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
}
