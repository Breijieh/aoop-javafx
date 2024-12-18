package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;
import com.assignment2.util.DataUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.ToDoubleFunction;

/**
 * Controller for the Statistics Dialog.
 */
public class StatisticsDialogController {

    @FXML
    private ComboBox<String> columnComboBox;

    @FXML
    private ListView<String> operationsListView;

    private Analytics<DataRow> analytics;

    private Stage stage;

    private static final Logger logger = Logger.getLogger(StatisticsDialogController.class.getName());

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        operationsListView.setItems(FXCollections.observableArrayList("Sum", "Average", "Max", "Min"));
        operationsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Sets the Analytics instance and populates the column ComboBox.
     *
     * @param analytics Analytics instance containing data.
     */
    public void setAnalytics(Analytics<DataRow> analytics) {
        this.analytics = analytics;
        List<String> numericColumns = AnalyticsService.getAvailableNumericColumns(analytics);
        columnComboBox.setItems(FXCollections.observableArrayList(numericColumns));
        if (!numericColumns.isEmpty()) {
            columnComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Sets the stage for this dialog.
     *
     * @param stage The Stage to set.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Handles applying the selected statistical operations.
     */
    @FXML
    private void handleApplyStatistics() {
        String selectedColumn = columnComboBox.getValue();
        ObservableList<String> selectedOperations = operationsListView.getSelectionModel().getSelectedItems();

        if (selectedColumn == null || selectedOperations.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a column and at least one operation.");
            return;
        }

        try {
            StringBuilder resultBuilder = new StringBuilder();
            ToDoubleFunction<DataRow> mapper = row -> DataUtil.parseSafeDouble(row.getField(selectedColumn));

            for (String operation : selectedOperations) {
                double result = AnalyticsService.performStatistic(analytics, operation, mapper);
                resultBuilder.append(operation).append(" of '")
                        .append(DataUtil.toTitleCase(selectedColumn)).append("': ")
                        .append(result).append("\n");
            }

            showAlert(Alert.AlertType.INFORMATION, "Statistics Results", resultBuilder.toString());

            // Log the statistics results
            logger.info("Performed statistics on column '" + selectedColumn + "':\n" + resultBuilder.toString());

            // Close the dialog
            stage.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Statistics Error", "An error occurred: " + e.getMessage());
            logger.log(Level.SEVERE, "Statistics operation error:", e);
        }
    }

    /**
     * Handles canceling the statistics operation.
     */
    @FXML
    private void handleCancel() {
        stage.close();
    }

    /**
     * Displays an alert dialog.
     *
     * @param alertType Type of alert.
     * @param title     Title of the dialog.
     * @param message   Content message.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
