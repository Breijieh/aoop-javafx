module com.assignment2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.assignment2 to javafx.fxml;

    exports com.assignment2;
    exports com.assignment2.model to javafx.fxml;
    exports com.assignment2.analytics;
    exports com.assignment2.parser;
    exports com.assignment2.util;
}
