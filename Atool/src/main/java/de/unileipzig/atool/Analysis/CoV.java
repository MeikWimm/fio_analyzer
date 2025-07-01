package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CoV extends GenericTest implements Initializable {
    private final List<XYChart.Data<Number, Number>> covData;
    private final CoVWindowed covWindowed;
    private final double STEADY_STATE_COV_THRESHOLD;

    @FXML public TableView<Run> covTable;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Double> covColumn;
    @FXML public TableColumn<Run, Double> startTimeColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML private Label steadyStateLabelWindowed;
    @FXML private Label steadyStateLabel;
    @FXML Button showCoVWindowedGraphButton;
    @FXML Button showCoVGraphButton;


    public CoV(Job job, Settings settings) {
        super(job, settings.getCovSkipRunsCounter(), settings.isCovUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), false, settings.getRequiredRunsForSteadyState());
        final int dataSizeWithRuns = job.getRuns().size() * 2;
        this.covData = new ArrayList<>(dataSizeWithRuns);
        this.covWindowed = new CoVWindowed(job, settings);
        this.STEADY_STATE_COV_THRESHOLD = this.job.getCvThreshold();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));
        covColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));

        showCoVGraphButton.setOnAction(e -> covWindowed.drawWindowedCoV());
        showCoVWindowedGraphButton.setOnAction(e -> drawCoV());

        covTable.setItems(this.job.getFilteredRuns());

    }

    @Override
    protected void setLabeling() {
        if(getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state CV found.");
        } else {
            steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
        }

        if(covWindowed.getSteadyStateRun() == null){
            steadyStateLabelWindowed.setText("No steady state CV found.");
        } else {
            steadyStateLabelWindowed.setText("at run " + covWindowed.getSteadyStateRun().getID() + " | time: " + covWindowed.getSteadyStateRun().getStartTime());
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
    public void calculateTest() {
        calculateCoV();
        covWindowed.calculate();
    }

    @Override
    public String getTestName() {
        return "Coefficient of Variation";
    }

    private void calculateCoV(){
        for (List<Run> group : this.groups) {
            Run run = group.getFirst();
            double cov = calculateCoVGroup(group);
            run.setCoV(cov);
            covData.add(new XYChart.Data<>(run.getRunID(), cov));
            this.resultRuns.add(run);
        }
    }

    private double calculateCoVGroup(List<Run> group) {
        if (group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group cannot be null or empty");
        }

        double average = MathUtils.average(group);
        if (average == 0) {
            throw new IllegalArgumentException("Cannot calculate CoV when mean is zero");
        }

        double n = 0;
        double sum = 0;

        for (Run run : group) {
            if (run == null || run.getData() == null) {
                throw new IllegalArgumentException("Invalid run data");
            }
            for (DataPoint dp : run.getData()) {
                sum += Math.pow(dp.getData() - average, 2);
                n++;
            }
        }

        if (n <= 1) {
            throw new IllegalArgumentException("Need at least two data points to calculate CoV");
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
        return value < STEADY_STATE_COV_THRESHOLD;
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("Run CoV", "Per run", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("CV over Job", covData));
    }

    public void drawCoV() {
        charter.drawGraph("Run CoV", "Per run", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("CV over Job", covData));
        charter.openWindow();
    }
}