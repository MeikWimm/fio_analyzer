package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class CoV extends GenericTest implements Initializable {
    private final List<XYChart.Data<Number, Number>> covData;
    private final double STEADY_STATE_COV_THRESHOLD;

    @FXML private Label labelHeader;

    @FXML private TableView<Run> covTable;
    @FXML private TableColumn<Run, Integer> runIDColumn;
    @FXML private TableColumn<Run, Double> averageSpeedColumn;
    @FXML private TableColumn<Run, Boolean> covColumn;
    @FXML private TableColumn<Run, Double> startTimeColumn;
    @FXML private TableColumn<Run, Double> minimialCVColumn;
    @FXML private Label steadyStateLabel;
    private final Job job;


    public CoV(Job job, Settings settings) {
        super(job, settings.getCovSkipRunsCounter(), settings.isCovUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), false, settings.getRequiredRunsForSteadyState());
        final int dataSizeWithRuns = job.getRuns().size() * 2;
        this.covData = new ArrayList<>(dataSizeWithRuns);
        this.job = getJob();
        this.STEADY_STATE_COV_THRESHOLD = this.job.getCvThreshold();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        covColumn.setCellFactory(Utils.getHypothesisCellFactory());

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));
        minimialCVColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));

        labelHeader.setText(this.job.toString());
        covTable.setItems(this.job.getRuns());

        Utils.CustomRunTableRowFactory menuItems = new Utils.CustomRunTableRowFactory();
        menuItems.addMenuItem("Show Run calculation", this::showCoVSections);

        covTable.setRowFactory(menuItems);
    }

    public void showCoVSections(TableRow<Run> row, TableView<Run> table) {
        Logging.log(Level.INFO, "CoV", "Showing sections for run " + row.getItem().getRunID());
        for (Section section : row.getItem().getSections()) {
            Logging.log(Level.INFO, "CoV", section.toString());
        }
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
    protected void calculateTest(Run run, List<Section> resultSections) {
        calculateCoV(run, resultSections);

    }

    @Override
    protected void calculateTest(List<List<Run>> groups, List<Run> resultRuns) {
        //calculateCoV(groups, resultRuns);
    }

    @Override
    public String getTestName() {
        return "Coefficient of Variation";
    }

    private void calculateCoV(Run run, List<Section> resultSections){
        double minPossibleCV = Double.MAX_VALUE;

        for (List<Section> group : run.getGroups()) {
            Section section = group.getFirst();
            double cov = calculateCoVGroup(group);
            section.setCoV(cov);
            covData.add(new XYChart.Data<>(section.getID(), cov));
            resultSections.add(section);

            if(cov < minPossibleCV){
                minPossibleCV = cov;
            }
        }

        run.setCoV(minPossibleCV);
    }

    private double calculateCoVGroup(List<Section> sections) {
        double average = MathUtils.average(sections, 0);
        double n = 0;
        double sum = 0;

        for (Section section : sections) {
            for (DataPoint dp : section.getData()) {
                sum += Math.pow(dp.data - average, 2);
                n++;
            }
        }

        double std = Math.sqrt(sum / (n - 1));
        return (std / average);
    }

    @Override
    protected double extractValue(Run run) {
        return 0;
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < STEADY_STATE_COV_THRESHOLD;
    }

    @Override
    protected double extractValue(Section section) {
        return section.getCoV();
    }

    @Override
    public double getCriticalValue() {
        return this.job.getCvThreshold();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("Run CoV", "Per run", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("CV over Job", covData));
    }

    public void drawCoV() {
        charter.drawGraph("Run CoV", "Per run", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("CV over Job", covData));
        charter.openWindow();
    }

    @Override
    public TableView<Run> getTable() {
        return covTable;
    }
}