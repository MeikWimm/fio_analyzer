/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.inference.OneWayAnova;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;

/**
 * @author meni1999
 */
public class Anova extends GenericTest implements Initializable {

    private final List<XYChart.Data<Number, Number>> anovaData;
    @FXML private Label jobLabel;
    @FXML private Label averageSpeedLabel;
    @FXML private Label sseLabel;
    @FXML private Label ssaLabel;
    @FXML private Label sstLabel;
    @FXML private Label ssaSstLabel;
    @FXML private Label sseSstLabel;
    @FXML private Label fCriticalLabel;
    @FXML private Label fCalculatedLabel;
    @FXML private Button showFGraphButton;
    @FXML private Button showCoVGraph;
    @FXML private Button showWinCoVGraph;
    @FXML private Label sigmaJobLabel;
    @FXML private Label steadyStateLabel;

    @FXML private TableView<Run> anovaTable;
    @FXML private TableColumn<Run, Double> averageSpeedColumn;
    @FXML private TableColumn<Run, Integer> runIDColumn;
    @FXML private TableColumn<Run, Double> startTimeColumn;
    @FXML private TableColumn<Run, Double> FColumn;
    @FXML private TableColumn<Run, Boolean> hypothesisColumn;
    private double fCrit;
    private final Job job;

    public Anova(Job job, Settings settings) {
        super(job, settings.getAnovaSkipRunsCounter(), settings.isAnovaUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), settings.isBonferroniANOVASelected() , settings.getRequiredRunsForSteadyState());
        final int dataSize = job.getData().size();
        this.anovaData = new ArrayList<>(dataSize);
        this.job = getJob();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));

        FColumn.setCellValueFactory(new PropertyValueFactory<>("AcceptedSectionsRate"));
        FColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        anovaTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Run run = anovaTable.getSelectionModel().getSelectedItem();
                if (run != null) {
                    updateLabeling(run);
                }
            }
        });

        showFGraphButton.setOnAction(e -> drawANOVAGraph());
        jobLabel.setText(this.job.toString());
        anovaTable.setItems(this.job.getRuns());
        Utils.CustomRunTableRowFactory menuItems = new Utils.CustomRunTableRowFactory();

        menuItems.addMenuItem("Show Run calculation", this::showAnovaSections);

        anovaTable.setRowFactory(menuItems);
    }

    public void showAnovaSections(TableRow<Run> row, TableView<Run> table) {
        SectionWindow sectionWindow = new SectionWindow(row.getItem());
        sectionWindow.setShowFColumn(true);
        sectionWindow.openWindow();
    }

    private void updateLabeling(Run run) {
        String averageSpeedLabelText = String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getAverageSpeed());
//        averageSpeedLabel.setText(String.format(Locale.ENGLISH, "%s %s", averageSpeedLabelText, Settings.getConversion()));
//        sseLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSE()));
//        ssaLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSA()));
//        sstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSST()));
//        ssaSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (run.getSSA() / run.getSST())));
//        sseSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (run.getSSE() / run.getSST())));
//        fCalculatedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getF()));
        if(this.getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + this.getSteadyStateRun().getID());
        }
    }

    @Override
    protected void setLabeling() {
        fCriticalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.fCrit));
        sigmaJobLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.job.getStandardDeviation()));
        if(this.possibleSteadyStateRuns.isEmpty()){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + this.possibleSteadyStateRuns.getFirst().getID());
        }
    }

    @Override
    protected void calculateTest(Run run, List<Section> resultSections) {
        List<List<Section>> groups = run.getGroups();
        OneWayAnova anova = new OneWayAnova();

        for (List<Section> group : groups) {
            List<double[]> dataGroups = new ArrayList<>();
           for (Section section : group) {
               double[] data1 = section.getData().stream().mapToDouble(dp -> dp.data).toArray();
               dataGroups.add(data1);
           }

            double pValue = anova.anovaPValue(dataGroups);
            group.getFirst().setP(pValue);
            anovaData.add(new XYChart.Data<>(group.getFirst().getID(), pValue));
        }
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < this.fCrit;
    }

    @Override
    protected double extractValue(Section section) {
        return section.getF();
    }

    public double getCriticalValue() {
        return fCrit;
    }

    @Override
    public String getTestName() {
        return "ANOVA";
    }

    public TableView<Run> getTable() {
        return anovaTable;
    }

    private void drawANOVAGraph() {
        charter.drawGraph("ANOVA", "Run", "F-Value", "Critical value", getAlpha(), new Charter.ChartData("calculated F", anovaData));
        charter.openWindow();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("ANOVA", "Run", "F-Value", "Critical value", getAlpha(), new Charter.ChartData("calculated F", anovaData));
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/Anova.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated Anova";
    }
}
