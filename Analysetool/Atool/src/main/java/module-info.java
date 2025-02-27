module com.mycompany.atool {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.atool to javafx.fxml;
    exports com.mycompany.atool;
}
