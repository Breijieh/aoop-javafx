package com.assignment2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.*;

public class App extends Application {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) throws IOException {
        configureLogging();

        Parent root = loadFXML("primary"); // Ensure "Primary.fxml" exists and is correctly named

        Scene scene = new Scene(root, 800, 600);
        stage.setMaximized(true);
        try {
            scene.getStylesheets().add(getClass().getResource("/com/assignment2/styles/styles.css").toExternalForm());
        } catch (NullPointerException e) {
            logger.warning("Stylesheet not found. Proceeding without CSS.");
        }
        stage.setScene(scene);
        stage.setTitle("Analytics Project");
        stage.show();
    }

    /**
     * Configures the logging settings.
     */
    private void configureLogging() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(Level.ALL);
            }
        }
        logger.setLevel(Level.ALL);
    }

    /**
     * Loads an FXML file.
     *
     * @param fxml The name of the FXML file (without extension).
     * @return The loaded Parent node.
     * @throws IOException If the FXML file is not found or cannot be loaded.
     */
    static Parent loadFXML(String fxml) throws IOException {
        String fxmlPath = "/com/assignment2/" + fxml + ".fxml";
        logger.info("Attempting to load FXML: " + fxmlPath);
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlPath));
        if (fxmlLoader.getLocation() == null) {
            logger.severe("FXML file not found at path: " + fxmlPath);
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
