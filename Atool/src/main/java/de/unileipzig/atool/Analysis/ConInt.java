/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConInt (Confidence Interval)
 *
 * @author meni1999
 */
public class ConInt extends GenericTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ConInt.class.getName());
    private static final double STEADY_STATE_RCIW_THRESHOLD = .02;
    private final int WINDOW_SIZE;

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    @FXML public Label labelHeader;
    @FXML public Button drawConIntDiffButton;
    @FXML public Button drawWindowedRCIWButton;
    @FXML public TableView<Run> conIntTable;
    @FXML public TableColumn<Run, Integer> runsColumn;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Double> intervalFromColumn;
    @FXML public TableColumn<Run, Double> intervalToColumn;
    @FXML public TableColumn<Run, Double> plusMinusValueColumn;
    @FXML public TableColumn<Run, Double> standardDeviationColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, Byte> overlappingColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    @FXML public Label steadyStateLabel;


    public ConInt(Job job,Settings settings) {
        super(job, settings.getConIntSkipRunsCounter(), settings.isConIntUseAdjacentRun(), 2, job.getAlpha(), settings.isBonferroniConIntSelected(), settings.getRequiredRunsForSteadyState());
        int dataSize = this.job.getData().size();
        WINDOW_SIZE = settings.getWindowSize();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        intervalFromColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalFrom"));
        intervalFromColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        intervalToColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalTo"));
        intervalToColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        plusMinusValueColumn.setCellValueFactory(new PropertyValueFactory<>("PlusMinusValue"));
        plusMinusValueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
        standardDeviationColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        compareToRunColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGroup()));
        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("Overlap"));
        overlappingColumn.setCellFactory(Utils.getBooleanCellFactory()); //TODO currently workaround and bad name
        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getFilteredRuns());
    }

    @Override
    public void calculateTest() {
        NormalDistribution normDis = new NormalDistribution();
        double dataSize = this.job.getRunDataSize();

        /*
        Calculate first the intervals
         */
        for (Run run : this.job.getRuns()) {
            double averageSpeed = run.getAverageSpeed();
            double alpha = job.getAlpha();
            double std = run.getStandardDeviation();

            double c1 = averageSpeed - (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            run.setIntervalFrom(c1);

            double c2 = averageSpeed + (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            run.setIntervalTo(c2);
        }

        /*
        Look for overlapping intervals
         */
        for (List<Run> runs : this.groups) {
            Run run1 = runs.get(0);
            Run run2 = runs.get(1);
            double a1 = run1.getIntervalFrom();
            double a2 = run2.getIntervalFrom();
            double b1 = run1.getIntervalTo();
            double b2 = run2.getIntervalTo();
            run1.setOverlap(doConfidenceIntervalsOverlap(a1, b1, a2, b2));

            double averageSpeed1 = run1.getAverageSpeed();
            double averageSpeed2 = run2.getAverageSpeed();
            double alpha = job.getAlpha();
            double std1 = run1.getStandardDeviation();
            double dataSize1 = run1.getData().size();
            double std2 = run2.getStandardDeviation();
            double dataSize2 = run1.getData().size();
            /*
            Calculate new interval
             */
            double s_x = Math.sqrt((Math.pow(std1, 2) / dataSize1) + (Math.pow(std2, 2)  / dataSize2));
            double x = averageSpeed1 - averageSpeed2;
            double y = s_x * normDis.inverseCumulativeProbability(1.0 - alpha / 2.0);
            double c1 = x - y;
            double c2 = x + y;

            run1.setNullhypothesis(doesIntervalContainZero(c1, c2));
            this.resultRuns.add(run1);
        }
    }

    /**
     * Replaced with doConfidenceIntervalsOverlap() function
     */
    @Override
    protected void checkForHypothesis() {
        //NO-OP
    }

    private byte doConfidenceIntervalsOverlap(double a1, double b1, double a2, double b2) {
        //TODO add extra byte type
        if(Math.max(a1, a2) <= Math.min(b1, b2)){
            return Run.ACCEPTED_NULLHYPOTHESIS;
        } else {
            return Run.REJECTED_NULLHYPOTHESIS;
        }
    }

    private byte doesIntervalContainZero(double lowerBound, double upperBound) {
        //TODO add extra byte type
        if(lowerBound <= 0 && upperBound >= 0){
            return Run.ACCEPTED_NULLHYPOTHESIS;
        } else {
            return Run.REJECTED_NULLHYPOTHESIS;
        }
    }

    @Override
    protected double extractValue(Run run) {
        return run.getNullhypothesis();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value == Run.ACCEPTED_NULLHYPOTHESIS;
    }

    @Override
    public Scene getCharterScene() {
        return null;
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/ConInt.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated Confidence Interval";
    }

    @Override
    public String getTestName() {
        return "Confidence Interval";
    }

    @Override
    protected void setLabeling() {
        Run run = this.getSteadyStateRun();
        if(run == null){
            steadyStateLabel.setText("No steady state found");
            return;
        }

        steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
    }

    @Override
    public TableView<Run> getTable() {
        return conIntTable;
    }
}
