package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import de.unileipzig.atool.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class GameHowell extends PostHocTest implements Initializable, PostHocAnalyzer {
    @FXML
    public Label qCritLabel;

    @FXML public Button drawTukey;

    @FXML public TableView<Run> TukeyTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> QColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private double qHSD;
    private Charter charter;
    public GameHowell(GenericTest test) {
        super(test);
        this.charter = new Charter();

    }

    @Override
    public void apply(List<Run> resultWithRuns, List<List<Run>> resultWithGroups) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());

        //TukeyTable.setItems(this.test.getJob().getRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
    }

    public void draw(){
        //charter.drawGraph(this.test.getJob(), "ANOVA", "Run", "F-Value", "calculated F", anovaData, this.fCrit);
    }
}
