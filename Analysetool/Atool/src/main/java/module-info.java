module com.mycompany.atool {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.math3;
    requires java.logging;

    opens com.mycompany.atool to javafx.fxml;
    opens com.mycompany.atool.Analysis to javafx.fxml;
    exports com.mycompany.atool;
}
