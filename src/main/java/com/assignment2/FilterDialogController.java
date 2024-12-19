package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilterDialogController {

    @FXML
    private ComboBox<String> columnComboBox;

    @FXML
    private ComboBox<String> operatorComboBox;

    @FXML
    private TextField valueField;

    @FXML
    private ListView<String> conditionsListView;

    @FXML
    private Button applyFilterButton;

    @FXML
    private Button cancelButton;

    private Analytics<DataRow> analytics;
    private Analytics<DataRow> filteredAnalytics;

    private static final Logger logger = Logger.getLogger(FilterDialogController.class.getName());

    /**
     * Initializes the controller. This method is automatically called after the
     * FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize operators based on selected column's data type
        columnComboBox.setOnAction(event -> updateOperators());
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

    /**
     * Populates the column ComboBox with available columns from the data.
     */
    private void populateColumns() {
        if (analytics.getData().isEmpty()) {
            logger.warning("Analytics data is empty. Columns cannot be populated.");
            showAlert(Alert.AlertType.WARNING, "No Data", "No data available to filter.");
            return;
        }
        DataRow firstRow = analytics.getData().get(0);
        ObservableList<String> columns = FXCollections.observableArrayList(firstRow.getFields().keySet());
        columnComboBox.setItems(columns);
        if (!columns.isEmpty()) {
            columnComboBox.getSelectionModel().selectFirst();
            updateOperators();
        }
        logger.info("Populated columns: " + columns);
    }

    /**
     * Updates the operator ComboBox based on the selected column's data type.
     */
    private void updateOperators() {
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
        logger.info("Updated operators for column '" + selectedColumn + "' (Type: " + type + "): "
                + operatorComboBox.getItems());
    }

    /**
     * Handles adding a filter condition to the list.
     *
     * @param event The action event triggered by clicking the "Add Condition"
     *              button.
     */
    @FXML
    private void handleAddCondition(ActionEvent event) {
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

        // Validate the value based on column data type
        DataRow.DataType type = analytics.getData().get(0).getFieldType(column);
        if (!validateValue(type, value)) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid value for the selected column type.");
            return;
        }

        // Use '||' as a unique delimiter to handle operators with spaces
        String condition = column + "||" + operator + "||" + value;
        conditionsListView.getItems().add(condition);
        logger.info("Added condition: " + condition);

        // Optionally, clear the value field after adding
        valueField.clear();
    }

    /**
     * Validates the input value based on the column's data type.
     *
     * @param type  The data type of the column.
     * @param value The input value to validate.
     * @return True if valid, else false.
     */
    private boolean validateValue(DataRow.DataType type, String value) {
        try {
            switch (type) {
                case INTEGER:
                    Integer.parseInt(value);
                    break;
                case DOUBLE:
                    Double.parseDouble(value);
                    break;
                case BOOLEAN:
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        return false;
                    }
                    break;
                case STRING:
                    // No validation needed for strings
                    break;
                default:
                    return false;
            }
            return true;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Value validation failed for type " + type + " with value: " + value, e);
            return false;
        }
    }

    /**
     * Handles clearing all filter conditions from the list.
     *
     * @param event The action event triggered by clicking the "Clear Conditions"
     *              button.
     */
    @FXML
    private void handleClearConditions(ActionEvent event) {
        conditionsListView.getItems().clear();
        logger.info("Cleared all filter conditions.");
    }

    /**
     * Handles applying the filter conditions and updating the analytics data.
     *
     * @param event The action event triggered by clicking the "Apply Filters"
     *              button.
     */
    @FXML
    private void handleApplyFilters(ActionEvent event) {
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
            logger.info("Filters applied successfully. Filtered data size: "
                    + (filteredAnalytics != null ? filteredAnalytics.getData().size() : "null"));

            // Close the dialog
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Filter Error", "An error occurred while applying filters.");
            logger.log(Level.SEVERE, "Error applying filters:", e);
        }
    }

    /**
     * Parses a condition string into a Predicate for filtering.
     *
     * @param condition The condition string (e.g., "Category||Starts With||E").
     * @return A Predicate representing the condition.
     */
    private Predicate<DataRow> parseCondition(String condition) {
        String[] parts = condition.split("\\|\\|", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }

        String column = parts[0];
        String operator = parts[1];
        String value = parts[2].trim();

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
                String trimmedValue = value.trim();
                switch (operator) {
                    case "Contains":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).toLowerCase().contains(trimmedValue.toLowerCase());
                        };
                    case "Starts With":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null
                                    && ((String) field).toLowerCase().startsWith(trimmedValue.toLowerCase());
                        };
                    case "Ends With":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).toLowerCase().endsWith(trimmedValue.toLowerCase());
                        };
                    case "=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && ((String) field).equalsIgnoreCase(trimmedValue);
                        };
                    case "!=":
                        return row -> {
                            Object field = row.getField(column);
                            return field != null && !((String) field).equalsIgnoreCase(trimmedValue);
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
     * Handles canceling the filter operation and closing the dialog.
     *
     * @param event The action event triggered by clicking the "Cancel" button.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            logger.info("Filter dialog closed without applying filters.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to close the dialog.");
            logger.log(Level.SEVERE, "Error closing Filter dialog:", e);
        }
    }

    /**
     * Displays an alert dialog to the user.
     *
     * @param alertType The type of alert.
     * @param title     The title of the alert dialog.
     * @param message   The content message of the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
