package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Filter Dialog.
 */
public class FilterDialogController {

    @FXML
    private ComboBox<String> columnComboBox;

    @FXML
    private ComboBox<String> operatorComboBox;

    @FXML
    private TextField valueField;

    @FXML
    private Button applyFilterButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ListView<String> conditionsListView;

    private Analytics<DataRow> analytics;
    private Analytics<DataRow> filteredAnalytics;

    private static final Logger logger = Logger.getLogger(FilterDialogController.class.getName());

    @FXML
    private void initialize() {
        // Initialization logic if needed
    }

    /**
     * Sets the Analytics instance and populates the column ComboBox.
     *
     * @param analytics Analytics instance containing data.
     */
    public void setAnalytics(Analytics<DataRow> analytics) {
        this.analytics = analytics;
        populateColumns();
    }

    private void populateColumns() {
        if (analytics.getData().isEmpty()) {
            return;
        }
        DataRow firstRow = analytics.getData().get(0);
        columnComboBox.setItems(FXCollections.observableArrayList(firstRow.getFields().keySet()));
        if (!firstRow.getFields().keySet().isEmpty()) {
            columnComboBox.getSelectionModel().selectFirst();
        }

        // Initialize operators based on the first column's type
        updateFilterOperations();

        // Add listener to update operators when column selection changes
        columnComboBox.setOnAction(event -> updateFilterOperations());
    }

    private void updateFilterOperations() {
        String selectedColumn = columnComboBox.getValue();
        if (selectedColumn == null || selectedColumn.isEmpty()) {
            operatorComboBox.setItems(FXCollections.observableArrayList());
            return;
        }
        DataRow.DataType type = analytics.getData().get(0).getFieldType(selectedColumn);
        operatorComboBox.getItems().clear();
        if (type == DataRow.DataType.INTEGER || type == DataRow.DataType.DOUBLE) {
            operatorComboBox.setItems(FXCollections.observableArrayList("<", ">", "<=", ">=", "=", "!="));
        } else if (type == DataRow.DataType.STRING) {
            operatorComboBox
                    .setItems(FXCollections.observableArrayList("Contains", "Starts With", "Ends With", "=", "!="));
        } else if (type == DataRow.DataType.BOOLEAN) {
            operatorComboBox.setItems(FXCollections.observableArrayList("=", "!="));
        }
        if (!operatorComboBox.getItems().isEmpty()) {
            operatorComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleAddCondition() {
        String column = columnComboBox.getValue();
        String operator = operatorComboBox.getValue();
        String value = valueField.getText().trim();

        if (column == null || column.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a column.");
            return;
        }

        if (operator == null || operator.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select an operator.");
            return;
        }

        if (value.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a value.");
            return;
        }

        String condition = column + " " + operator + " " + value;
        conditionsListView.getItems().add(condition);

        // Optionally, clear the value field after adding
        valueField.clear();
    }

    @FXML
    private void handleClearConditions() {
        conditionsListView.getItems().clear();
    }

    @FXML
    private void handleApplyFilters() {
        ObservableList<String> conditions = conditionsListView.getItems();

        if (conditions.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No Conditions", "Please add at least one filter condition.");
            return;
        }

        try {
            // Combine all predicates using logical AND
            Predicate<DataRow> combinedPredicate = conditions.stream()
                    .map(this::parseCondition)
                    .reduce(dataRow -> true, Predicate::and);

            filteredAnalytics = AnalyticsService.filter(analytics, combinedPredicate);

            // Close the dialog
            Stage stage = (Stage) applyFilterButton.getScene().getWindow();
            stage.close();

            logger.info("Filters applied successfully.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Filter Error", "An error occurred while applying filters.");
            logger.log(Level.SEVERE, "Error applying filters:", e);
        }
    }

    private Predicate<DataRow> parseCondition(String condition) {
        String[] parts = condition.split(" ", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }

        String column = parts[0];
        String operator = parts[1];
        String value = parts[2];

        DataRow.DataType type = analytics.getData().get(0).getFieldType(column);

        switch (type) {
            case INTEGER:
                int intValue = Integer.parseInt(value);
                switch (operator) {
                    case "<":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Integer) field) < intValue;
                        };
                    case ">":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Integer) field) > intValue;
                        };
                    case "<=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Integer) field) <= intValue;
                        };
                    case ">=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Integer) field) >= intValue;
                        };
                    case "=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Integer) field) == intValue;
                        };
                    case "!=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Integer) field) != intValue;
                        };
                    default:
                        throw new IllegalArgumentException("Unsupported operator for INTEGER: " + operator);
                }
            case DOUBLE:
                double doubleValue = Double.parseDouble(value);
                switch (operator) {
                    case "<":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Double) field) < doubleValue;
                        };
                    case ">":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Double) field) > doubleValue;
                        };
                    case "<=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Double) field) <= doubleValue;
                        };
                    case ">=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Double) field) >= doubleValue;
                        };
                    case "=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Double) field) == doubleValue;
                        };
                    case "!=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Double) field) != doubleValue;
                        };
                    default:
                        throw new IllegalArgumentException("Unsupported operator for DOUBLE: " + operator);
                }
            case BOOLEAN:
                boolean boolValue = Boolean.parseBoolean(value);
                switch (operator) {
                    case "=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Boolean) field) == boolValue;
                        };
                    case "!=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((Boolean) field) != boolValue;
                        };
                    default:
                        throw new IllegalArgumentException("Unsupported operator for BOOLEAN: " + operator);
                }
            case STRING:
                switch (operator) {
                    case "Contains":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).contains(value);
                        };
                    case "Starts With":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).startsWith(value);
                        };
                    case "Ends With":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).endsWith(value);
                        };
                    case "=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).equals(value);
                        };
                    case "!=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && !((String) field).equals(value);
                        };
                    default:
                        throw new IllegalArgumentException("Unsupported operator for STRING: " + operator);
                }
            default:
                throw new IllegalArgumentException("Unsupported data type for filtering: " + type);
        }
    }

    /**
     * Retrieves the filtered Analytics instance.
     *
     * @return The filtered Analytics instance.
     */
    public Analytics<DataRow> getFilteredAnalytics() {
        return filteredAnalytics;
    }

    /**
     * Handles the Cancel button action.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
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
