/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ConInt (Confidence Interval)
 *
 * @author meni1999
 */
public class ConInt extends GenericTest implements Initializable {
    @FXML private Label labelHeader;
    @FXML private Button drawConIntDiffButton;
    @FXML private Button drawWindowedRCIWButton;
    @FXML private TableView<Run> conIntTable;
    @FXML private TableColumn<Run, Integer> runsColumn;
    @FXML private TableColumn<Run, Double> averageSpeedColumn;
    @FXML private TableColumn<Run, Double> intervalFromColumn;
    @FXML private TableColumn<Run, Double> intervalToColumn;
    @FXML private TableColumn<Run, Double> plusMinusValueColumn;
    @FXML private TableColumn<Run, Double> standardDeviationColumn;
    @FXML private TableColumn<Run, Boolean> hypothesisColumn;
    @FXML private Label steadyStateLabel;
    private final Job job;


    public ConInt(Job job,Settings settings) {
        super(job, settings.getConIntSkipRunsCounter(), settings.isConIntUseAdjacentRun(), 2, job.getAlpha(), settings.isBonferroniConIntSelected(), settings.getRequiredRunsForSteadyState());
        this.job = getJob();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        averageSpeedColumn.setText("Average Speed" + Settings.getConversion());
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        //intervalFromColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalFrom"));
        //intervalFromColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        //intervalToColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalTo"));
        //intervalToColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        //plusMinusValueColumn.setCellValueFactory(new PropertyValueFactory<>("PlusMinusValue"));
        //plusMinusValueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
        standardDeviationColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        Utils.CustomRunTableRowFactory menuItems = new Utils.CustomRunTableRowFactory();
        menuItems.addMenuItem("Show Run calculation", this::showConIntSections);
        conIntTable.setRowFactory(menuItems);

        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getRuns());
    }

    public void showConIntSections(TableRow<Run> row, TableView<Run> table) {
        SectionWindow sectionWindow = new SectionWindow(row.getItem());
        sectionWindow.setShowPlusMinusValueColumn(true);
        sectionWindow.setShowIntervalToColumn(true);
        sectionWindow.setShowIntervalFromColumn(true);
        sectionWindow.openWindow();
    }

    @Override
    protected void calculateTest(Run run, List<Section> resultSection) {
        NormalDistribution normDis = new NormalDistribution();
        double dataSize = run.getSections().getFirst().getData().size();

        /*
        Calculate first the intervals
         */
        for (Section section : run.getSections()) {
            double averageSpeed = section.getAverageSpeed();
            double alpha = getAlpha();
            double std = section.getStandardDeviation();

            double c1 = averageSpeed - (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            section.setIntervalFrom(c1);

            double c2 = averageSpeed + (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            section.setIntervalTo(c2);
        }

        /*
        Look for overlapping intervals
         */
        for (List<Section> sections : run.getGroups()) {
            Section section1 = sections.get(0);
            Section section2 = sections.get(1);
            double a1 = section1.getIntervalFrom();
            double a2 = section2.getIntervalFrom();
            double b1 = section1.getIntervalTo();
            double b2 = section2.getIntervalTo();
            section1.setOverlap(doConfidenceIntervalsOverlap(a1, b1, a2, b2));

            double averageSpeed1 = section1.getAverageSpeed();
            double averageSpeed2 = section2.getAverageSpeed();
            double alpha = getAlpha();
            double std1 = section1.getStandardDeviation();
            double dataSize1 = section1.getData().size();
            double std2 = section2.getStandardDeviation();
            double dataSize2 = section1.getData().size();
            /*
            Calculate new interval
             */
            double s_x = Math.sqrt((Math.pow(std1, 2) / dataSize1) + (Math.pow(std2, 2)  / dataSize2));
            double x = averageSpeed1 - averageSpeed2;
            double y = s_x * normDis.inverseCumulativeProbability(1.0 - alpha / 2.0);
            double c1 = x - y;
            double c2 = x + y;

            section1.setNullhypothesis(doesIntervalContainZero(c1, c2));
            resultSection.add(section1);
        }
    }

    private boolean doConfidenceIntervalsOverlap(double a1, double b1, double a2, double b2) {
        return Math.max(a1, a2) <= Math.min(b1, b2);
    }

    private boolean doesIntervalContainZero(double lowerBound, double upperBound) {
        return lowerBound <= 0 && upperBound >= 0;
    }

    @Override
    protected void calculateSectionHypothesis(Run run, List<Section> resultSections) {
        //NO-OP
    }

    @Override
    protected double extractValue(Section section) {
        //NO-OP
        return 0;
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        //NO-OP
        return false;
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
        //Run run = this.getSteadyStateRun();
//        if(run == null){
//            steadyStateLabel.setText("No steady state found");
//            return;
//        }

        //steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
    }

    @Override
    public double getCriticalValue() {
        return Run.UNDEFINED_DOUBLE_VALUE;
    }

    @Override
    public TableView<Run> getTable() {
        return conIntTable;
    }
}
