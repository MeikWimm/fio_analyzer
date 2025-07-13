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
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.TTest;

import java.net.URL;
import java.util.*;


/**
 * @author meni1999
 */
public class AtoolTTest extends GenericTest implements Initializable {
    @FXML private Label labelHeader;
    @FXML private Label zCritLabel;
    @FXML private Label steadyStateLabel;

    @FXML private Button drawTTest;

    @FXML private TableView<Run> TTable;
    @FXML private TableColumn<Run, Double> averageSpeedColumn;
    @FXML private TableColumn<Run, Integer> runIDColumn;
    @FXML private TableColumn<Run, Integer> compareToRunColumn;
    @FXML private TableColumn<Run, Double> TColumn;
    @FXML private TableColumn<Run, Boolean> hypothesisColumn;

    private double tCrit;
    private final List<XYChart.Data<Number, Number>> tData;
    private final Job job;

    public AtoolTTest(Job job, Settings settings) {
        super(job, settings.getTTestSkipRunsCounter(), settings.isTTestUseAdjacentRun(), 2, job.getAlpha() ,settings.isBonferroniTTestSelected(), settings.getRequiredRunsForSteadyState());
        this.tData = new ArrayList<>();
        this.job = getJob();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setText("Average Speed " + Settings.getConversion());
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        //compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        TColumn.setCellValueFactory(new PropertyValueFactory<>("AcceptedSectionsRate"));
        TColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        labelHeader.setText(this.job.toString());

        drawTTest.setOnAction(e -> drawTGraph());
        TTable.setItems(this.job.getRuns());

        Utils.CustomRunTableRowFactory menuItems = new Utils.CustomRunTableRowFactory();

        menuItems.addMenuItem("Show Run calculation", this::showTTableSections);

        TTable.setRowFactory(menuItems);
    }

    public void showTTableSections(TableRow<Run> row, TableView<Run> table) {
        SectionWindow sectionWindow = new SectionWindow(row.getItem());
        sectionWindow.setShowTColumn(true);
        sectionWindow.openWindow();
    }

    @Override
    protected void setLabeling() {
        zCritLabel.setText(String.format(Locale.ENGLISH, "%,.5f", this.tCrit));
        if(this.getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
        }
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/TTest.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated T-Test";
    }

    @Override
    protected void calculateTest(Run run, List<Section> resultSections) {
        int dataSize = run.getSections().getFirst().getData().size() * 2;

        TDistribution t = new TDistribution(dataSize * 2 - 2);
        this.tCrit = t.inverseCumulativeProbability(1 - getAlpha() / 2.0);

        for(int i = 0; i < run.getGroups().size(); i++){
            Section section1 = run.getSections().get(i);
            Section section2 = run.getSections().get(i + 1);

            TTest ttest = new TTest();
            double[] data1 = section1.getData().stream().mapToDouble(dp -> dp.data).toArray();
            double[] data2 = section2.getData().stream().mapToDouble(dp -> dp.data).toArray();
            double pVal = ttest.tTest(data1, data2);

            section1.setT(pVal);
            tData.add(new XYChart.Data<>(section1.getID(), pVal));
            resultSections.add(section1);
        }
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < this.tCrit;
    }

    @Override
    protected double extractValue(Section section) {
        return section.getT();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("T-Test", "Run", "T-Value", "critical T", getAlpha(), new Charter.ChartData("calculated T", tData));
    }

    @Override
    public double getCriticalValue() {
        return this.tCrit;
    }

    private double calculateVariance(Section section, double sse) {
        return (1.0 / (section.getData().size() - 1.0)) * sse;
    }

    private double calculateSSE(Section s1, Section s2) {
        double sse = 0;
        double averageSpeed = (s1.getAverageSpeed() + s2.getAverageSpeed()) / 2.0;

        for (DataPoint dp : s1.getData()) {
            sse += (Math.pow((dp.data - averageSpeed), 2));
        }

        for (DataPoint dp : s2.getData()) {
            sse += (Math.pow((dp.data - averageSpeed), 2));
        }

        return sse;
    }

    @Override
    public String getTestName() {
        return "T-Test";
    }

    private void drawTGraph() {
        charter.drawGraph("T-Test", "Run", "T-Value", "critical T", getAlpha(), new Charter.ChartData("calculated T", tData));
        charter.openWindow();
    }

    @Override
    public TableView<Run> getTable() {
        return TTable;
    }
}
