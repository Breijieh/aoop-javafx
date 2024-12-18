package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private void handleAddAggregation(ActionEvent event) {
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
    private void handleApplyGroupBy(ActionEvent event) {
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

            // Convert groupedData to a list of DataRow
            List<DataRow> groupedRows = new ArrayList<>();
            for (Map.Entry<Object, Map<String, Object>> entry : groupedData.entrySet()) {
                DataRow row = new DataRow();
                row.addField(groupByColumn, entry.getKey().toString());
                for (Map.Entry<String, Object> aggEntry : entry.getValue().entrySet()) {
                    row.addField(aggEntry.getKey(), aggEntry.getValue().toString());
                }
                groupedRows.add(row);
            }

            groupedAnalytics = new Analytics<>(groupedRows);

            // Close the dialog
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

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
    private void handleCancel(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to close the dialog.");
            logger.log(Level.SEVERE, "Error closing Group By dialog:", e);
        }
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
