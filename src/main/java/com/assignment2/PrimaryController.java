package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;
import com.assignment2.parser.CSVParsingException;
import com.assignment2.parser.CSVParser;
import com.assignment2.util.DataUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the primary application window.
 */
public class PrimaryController {
    @FXML
    private TableView<Map<String, Object>> dataTable;

    @FXML
    private Label statusLabel;

    @FXML
    private Button restoreOriginalDataButton; // New Button

    private Analytics<DataRow> originalAnalytics; // Holds original data
    private Analytics<DataRow> currentAnalytics; // Holds current (filtered/sorted) data

    private static final Logger logger = Logger.getLogger(PrimaryController.class.getName());

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize with empty data
        originalAnalytics = new Analytics<>(new ArrayList<>());
        currentAnalytics = new Analytics<>(new ArrayList<>());

        // Disable Restore button initially since no operations have been applied
        restoreOriginalDataButton.setDisable(true);
    }

    /**
     * Handles the Pie Chart action.
     *
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    private void handlePieChart(ActionEvent event) {
        try {
            PieChart pieChart = createPieChart();
            Stage pieStage = new Stage();
            pieStage.setTitle("Pie Chart");
            Scene scene = new Scene(new VBox(pieChart), 600, 400);
            pieStage.setScene(scene);
            pieStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            pieStage.showAndWait();
            logger.info("Pie Chart displayed successfully.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Pie Chart Error", "Failed to display Pie Chart.");
            logger.log(Level.SEVERE, "Error displaying Pie Chart:", e);
        }
    }

    /**
     * Creates a PieChart based on the currentAnalytics data.
     *
     * @return A PieChart object.
     */
    private PieChart createPieChart() {
        PieChart pieChart = new PieChart();

        // Replace "Category" and "Value" with actual column names from your data
        String categoryColumn = "Category"; // Example column name
        String valueColumn = "Value"; // Example column name

        // Ensure these columns exist in your data
        if (!originalAnalytics.getData().isEmpty()) {
            DataRow firstRow = originalAnalytics.getData().get(0);
            if (!firstRow.hasField(categoryColumn) || !firstRow.hasField(valueColumn)) {
                showAlert(Alert.AlertType.ERROR, "Pie Chart Error",
                        "The specified columns for the Pie Chart do not exist.");
                logger.severe("Specified columns for Pie Chart are missing.");
                return pieChart;
            }
        }

        Map<String, Double> categoryData = AnalyticsService.aggregateForPieChart(currentAnalytics, categoryColumn,
                valueColumn);

        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);
        }

        return pieChart;
    }

    /**
     * Handles the Import CSV action.
     */
    @FXML
    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) dataTable.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Parse CSV with DataRow
                List<DataRow> dataRows = CSVParser.parseCSVSpecific(file.getAbsolutePath(), DataRow.class, ",");
                originalAnalytics = new Analytics<>(dataRows);
                currentAnalytics = originalAnalytics; // Initialize currentAnalytics with original data
                populateTable(currentAnalytics.getData());
                statusLabel.setText("Imported: " + file.getName());
                restoreOriginalDataButton.setDisable(true); // No operations applied yet
                logger.info("Successfully imported CSV: " + file.getName());
            } catch (CSVParsingException e) {
                showAlert(Alert.AlertType.ERROR, "CSV Parsing Error", e.getMessage());
                logger.log(Level.SEVERE, "CSV Parsing Error:", e);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Import Error", "An unexpected error occurred.");
                logger.log(Level.SEVERE, "Import Error:", e);
            }
        }
    }

    /**
     * Populates the TableView with data.
     *
     * @param dataRows List of DataRow objects.
     */
    void populateTable(List<DataRow> dataRows) {
        dataTable.getColumns().clear();

        if (dataRows.isEmpty()) {
            statusLabel.setText("No data to display.");
            return;
        }

        // Dynamically create columns based on Map keys
        DataRow sample = dataRows.get(0);
        Set<String> columns = sample.getFields().keySet();

        for (String key : columns) {
            DataRow.DataType type = sample.getFieldType(key);
            String columnHeader = DataUtil.toTitleCase(key) + " (" + type.toString() + ")";
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnHeader);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(key);
                return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
            });
            column.setPrefWidth(150);
            dataTable.getColumns().add(column);
        }

        // Convert DataRow to Map<String, Object> for dynamic TableView
        List<Map<String, Object>> mapData = new ArrayList<>();
        for (DataRow row : dataRows) {
            mapData.add(row.getFields());
        }

        ObservableList<Map<String, Object>> observableData = FXCollections.observableArrayList(mapData);
        dataTable.setItems(observableData);
    }

    /**
     * Updates the currentAnalytics instance and refreshes the TableView.
     *
     * @param updatedAnalytics The new Analytics instance after operations.
     */
    public void updateAnalytics(Analytics<DataRow> updatedAnalytics) {
        this.currentAnalytics = updatedAnalytics;
        populateTable(this.currentAnalytics.getData());

        // Enable the Restore button since operations have been applied
        restoreOriginalDataButton.setDisable(false);
    }

    /**
     * Handles the Exit action.
     */
    @FXML
    private void handleExit() {
        Stage stage = (Stage) dataTable.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the About action.
     */
    @FXML
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About",
                "Analytics Project\nVersion 1.0\nDeveloped by Your Name");
    }

    /**
     * Handles the Filter button action.
     */
    @FXML
    private void handleFilter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FilterDialog.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the currentAnalytics instance
            FilterDialogController controller = loader.getController();
            controller.setAnalytics(currentAnalytics);

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Filter Data");
            stage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // After dialog is closed, get the updated analytics
            Analytics<DataRow> filteredAnalytics = controller.getFilteredAnalytics();
            if (filteredAnalytics != null) {
                updateAnalytics(filteredAnalytics);
                statusLabel.setText("Data filtered.");
            } else {
                statusLabel.setText("Filter applied but no data matched the criteria.");
                logger.warning("Filtered analytics returned null.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Filter Error", "Failed to open Filter Dialog.");
            logger.log(Level.SEVERE, "Error opening Filter Dialog:", e);
        }
    }

    /**
     * Handles the Group By button action.
     */
    @FXML
    private void handleGroupBy(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GroupByDialog.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the currentAnalytics instance
            GroupByDialogController controller = loader.getController();
            controller.setAnalytics(currentAnalytics);

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Group By");
            stage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // After dialog is closed, get the updated analytics if any
            Analytics<DataRow> groupedAnalytics = controller.getGroupedAnalytics();
            if (groupedAnalytics != null) {
                updateAnalytics(groupedAnalytics);
                statusLabel.setText("Data grouped.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Group By Error", "Failed to open Group By Dialog.");
            logger.log(Level.SEVERE, "Error opening Group By Dialog:", e);
        }
    }

    /**
     * Handles the Statistics button action.
     */
    @FXML
    private void handleStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StatisticsDialog.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the currentAnalytics instance
            StatisticsDialogController controller = loader.getController();
            controller.setAnalytics(currentAnalytics);

            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Statistics");
            stage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Update status or handle any results if necessary
            statusLabel.setText("Statistics applied.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Statistics Error", "Failed to open Statistics Dialog.");
            logger.log(Level.SEVERE, "Error opening Statistics Dialog:", e);
        }
    }

    /**
     * Handles restoring the original data from the CSV.
     */
    @FXML
    private void handleRestoreOriginalData(ActionEvent event) {
        if (originalAnalytics != null) {
            currentAnalytics = originalAnalytics;
            populateTable(currentAnalytics.getData());
            statusLabel.setText("Reverted to original data.");
            restoreOriginalDataButton.setDisable(true); // Disable since we're back to original
            logger.info("Restored original data from CSV.");
        } else {
            showAlert(Alert.AlertType.WARNING, "No Data", "No original data available to restore.");
            logger.warning("Attempted to restore original data, but originalAnalytics is null.");
        }
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
