module de.unileipzig.atool {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires commons.math3;
    requires java.logging;
    requires jdistlib;
    requires java.sql;

    opens de.unileipzig.atool to javafx.fxml;
    opens de.unileipzig.atool.Analysis to javafx.fxml;
    exports de.unileipzig.atool;
    exports de.unileipzig.atool.Analysis;
}
