package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Group By Dialog.
 */
public class GroupByDialogController {

    @FXML
    private ComboBox<String> groupByColumnComboBox;

    @FXML
    private ComboBox<String> aggregationFunctionComboBox;

    @FXML
    private ComboBox<String> aggregationColumnComboBox;

    @FXML
    private ListView<String> aggregationListView;

    @FXML
    private Button addAggregationButton;

    @FXML
    private Button applyGroupByButton;

    @FXML
    private Button cancelButton;

    private final List<AggregationFunction> aggregationFunctions = new ArrayList<>();

    private Analytics<DataRow> analytics;
    private Analytics<DataRow> groupedAnalytics;

    private static final Logger logger = Logger.getLogger(GroupByDialogController.class.getName());

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize aggregation functions
        aggregationFunctionComboBox
                .setItems(FXCollections.observableArrayList("Count", "Sum", "Average", "Max", "Min", "List"));
        aggregationFunctionComboBox.getSelectionModel().selectFirst();

        // Initialize aggregation columns ComboBox as disabled initially
        aggregationColumnComboBox.setDisable(true);

        // Enable aggregation column ComboBox only when aggregation function requires a
        // column
        aggregationFunctionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equalsIgnoreCase("Count")) {
                aggregationColumnComboBox.setDisable(true);
            } else {
                aggregationColumnComboBox.setDisable(false);
            }
        });
    }

    /**
     * Sets the Analytics instance and populates the group by column ComboBox.
     *
     * @param analytics Analytics instance containing data.
     */
    public void setAnalytics(Analytics<DataRow> analytics) {
        this.analytics = analytics;
        List<String> columns = new ArrayList<>();
        if (!analytics.getData().isEmpty()) {
            DataRow firstRow = analytics.getData().get(0);
            columns.addAll(firstRow.getFields().keySet());
        }
        groupByColumnComboBox.setItems(FXCollections.observableArrayList(columns));
        if (!columns.isEmpty()) {
            groupByColumnComboBox.getSelectionModel().selectFirst();
        }

        // Populate aggregation columns based on data types
        List<String> numericColumns = AnalyticsService.getAvailableNumericColumns(analytics);
        aggregationColumnComboBox.setItems(FXCollections.observableArrayList(numericColumns));
        if (!numericColumns.isEmpty()) {
            aggregationColumnComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Handles adding an aggregation function.
     */
    @FXML
    private void handleAddAggregation() {
        String function = aggregationFunctionComboBox.getValue();
        String column = aggregationColumnComboBox.getValue();

        if (function == null || function.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select an aggregation function.");
            return;
        }

        // For 'Count', column selection is optional or not applicable
        if (!function.equalsIgnoreCase("Count") && (column == null || column.isEmpty())) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a column for the aggregation.");
            return;
        }

        AggregationFunction aggFunc = new AggregationFunction(function, column);
        aggregationFunctions.add(aggFunc);
        String displayText = aggFunc.toString();
        aggregationListView.getItems().add(displayText);

        // Optionally, reset aggregation column selection if necessary
        if (aggregationColumnComboBox.getItems().isEmpty()) {
            aggregationColumnComboBox.setDisable(true);
        }
    }

    /**
     * Handles applying the group by operations.
     */
    @FXML
    private void handleApplyGroupBy() {
        String groupByColumn = groupByColumnComboBox.getValue();

        if (groupByColumn == null || groupByColumn.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a column to group by.");
            return;
        }

        if (aggregationFunctions.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please add at least one aggregation function.");
            return;
        }

        try {
            // Perform group by with multiple aggregations
            Map<Object, Map<String, Object>> groupedData = AnalyticsService.performGroupByMultipleAggregations(
                    analytics, groupByColumn, aggregationFunctions);

            // Convert groupedData to a new list of DataRow or another appropriate structure
            // For simplicity, we'll skip this step and assume groupedAnalytics can be
            // represented similarly
            // You might need to create a new Analytics<DataRow> instance or a different
            // data structure

            // For demonstration, we'll just log the grouped data
            logger.info("Grouped Data: " + groupedData.toString());

            // TODO: Convert groupedData into Analytics<DataRow> or another suitable
            // structure
            // This part depends on how you want to represent grouped data in your
            // application
            // For now, we'll leave groupedAnalytics as null

            // Close the dialog
            Stage stage = (Stage) applyGroupByButton.getScene().getWindow();
            stage.close();

            // Notify success
            showAlert(Alert.AlertType.INFORMATION, "Group By Success", "Data has been grouped successfully.");
            logger.info("Group By applied successfully.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Group By Error", "An error occurred while applying Group By.");
            logger.log(Level.SEVERE, "Error applying Group By:", e);
        }
    }

    /**
     * Handles canceling the group by operation.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Retrieves the grouped Analytics instance.
     *
     * @return The grouped Analytics instance.
     */
    public Analytics<DataRow> getGroupedAnalytics() {
        return groupedAnalytics;
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
