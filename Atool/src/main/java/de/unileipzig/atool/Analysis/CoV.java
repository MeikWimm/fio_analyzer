package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CoV extends GenericTest implements Initializable {
    private final List<XYChart.Data<Number, Number>> covData;
    private final double STEADY_STATE_COV_THRESHOLD;

    @FXML private Label labelHeader;

    @FXML private TableView<Run> covTable;
    @FXML private TableColumn<Run, Integer> runIDColumn;
    @FXML private TableColumn<Run, Double> averageSpeedColumn;
    @FXML private TableColumn<Run, Double> covColumn;
    @FXML private TableColumn<Run, Double> startTimeColumn;
    @FXML private TableColumn<Run, String> compareToRunColumn;
    @FXML private Label steadyStateLabelWindowed;
    @FXML private Label steadyStateLabel;
    @FXML private Button showCoVWindowedGraphButton;


    public CoV(Job job, Settings settings) {
        super(job, settings.getCovSkipRunsCounter(), false, settings.getGroupSize(), job.getAlpha(), false, settings.getRequiredRunsForSteadyState());
        final int dataSizeWithRuns = job.getRuns().size() * 2;
        this.covData = new ArrayList<>(dataSizeWithRuns);
        this.STEADY_STATE_COV_THRESHOLD = this.job.getCvThreshold();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));
        covColumn.setCellFactory(Utils.getStatusCellFactory(STEADY_STATE_COV_THRESHOLD));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));

        showCoVWindowedGraphButton.setOnAction(e -> drawCoV());
        labelHeader.setText(this.job.toString());
        covTable.setItems(getResultRuns());

    }

    @Override
    protected void setLabeling() {
        if(getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state CV found.");
        } else {
            steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
        }
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/CoV.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated CoV";
    }

    @Override
    protected void calculateTest(List<List<Run>> groups, List<Run> resultRuns) {
        calculateCoV(groups, resultRuns);
    }

    @Override
    public String getTestName() {
        return "Coefficient of Variation";
    }

    private void calculateCoV(List<List<Run>> groups, List<Run> resultRuns){
        for (List<Run> group : groups) {
            Run run = group.getFirst();
            double cov = calculateCoVGroup(group);
            run.setCoV(cov);
            covData.add(new XYChart.Data<>(run.getRunID(), cov));
            resultRuns.add(run);
        }
    }

    private double calculateCoVGroup(List<Run> group) {
        double average = MathUtils.average(group);
        double n = 0;
        double sum = 0;

        for (Run run : group) {
            for (DataPoint dp : run.getData()) {
                sum += Math.pow(dp.data - average, 2);
                n++;
            }
        }

        double std = Math.sqrt(sum / (n - 1));
        return (std / average);
    }

    @Override
    protected double extractValue(Run run) {
        return run.getCoV();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value > STEADY_STATE_COV_THRESHOLD;
    }

    @Override
    public double getCriticalValue() {
        return this.job.getCvThreshold();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("CoV", "Time in seconds", "CV-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("calculated CV", covData));
    }

    public void drawCoV() {
        charter.drawGraph("CoV", "Time in seconds", "CV-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("calculated CV", covData));
        charter.openWindow();
    }

    @Override
    public TableView<Run> getTable() {
        return covTable;
    }
}