module de.unileipzig.atool {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.math3;
    requires java.logging;
    requires jdistlib;

    opens de.unileipzig.atool to javafx.fxml;
    opens com.mycompany.atool.Analysis to javafx.fxml;
    exports de.unileipzig.atool;
}
