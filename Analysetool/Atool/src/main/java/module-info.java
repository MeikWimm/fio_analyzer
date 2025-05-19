module de.unileipzig.atool {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.math3;
    requires java.logging;
    requires jdistlib;

    opens de.unileipzig.atool to javafx.fxml;
    opens de.unileipzig.atool.Analysis to javafx.fxml;
    exports de.unileipzig.atool;
}
