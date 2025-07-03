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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.TDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author meni1999
 */
public class TTest extends GenericTest implements Initializable {
    @FXML public Label zCritLabel;
    @FXML public Label steadyStateLabel;

    @FXML public Button drawTTest;

    @FXML public TableView<Run> TTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> TColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;

    private double tCrit;
    private final List<XYChart.Data<Number, Number>> tData;

    public TTest(Job job, Settings settings) {
        super(job, settings.getTTestSkipRunsCounter(), settings.isTTestUseAdjacentRun(), 2, job.getAlpha() ,settings.isBonferroniTTestSelected(), settings.getRequiredRunsForSteadyState());
        this.tData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        TColumn.setCellValueFactory(new PropertyValueFactory<>("T"));
        TColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTTest.setOnAction(e -> drawTGraph(this.job));
        TTable.setItems(getResultRuns());
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
    protected void calculateTest(List<List<Run>> groups, List<Run> resultRuns) {
        TDistribution t = new TDistribution(job.getData().size() * 2 - 2);
        this.tCrit = t.inverseCumulativeProbability(1 - this.alpha / 2.0);


        for (int i = 0; i < job.getRuns().size() - 1; i++) {
            Run run = job.getRuns().get(i);
            Run run2 = job.getRuns().get(i + 1);

            double sse = calculateSSE(run, run2);
            double runVariance1 = calculateVariance(run, sse);
            double runVariance2 = calculateVariance(run2, sse);
            double runSize = this.job.getData().size();

            double nominator = (run.getAverageSpeed() - run2.getAverageSpeed());
            double denominator = Math.sqrt((runVariance1 / runSize) + (runVariance2 / runSize));
            double tVal = Math.abs(nominator / denominator);
            run.setT(tVal);

            tData.add(new XYChart.Data<>(run.getID(), tVal));
            resultRuns.add(run);
        }
    }

    @Override
    protected double extractValue(Run run) {
        return run.getT();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < this.tCrit;
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("T-Test", "Run", "T-Value", "critical T", tCrit, new Charter.ChartData("calculated T", tData));
    }

    @Override
    public double getCriticalValue() {
        return this.tCrit;
    }

    private double calculateVariance(Run run, double sse) {
        return (1.0 / (run.getData().size() - 1.0)) * sse;
    }

    private double calculateSSE(Run run1, Run run2) {
        double sse = 0;
        double averageSpeed = (run1.getAverageSpeed() + run2.getAverageSpeed()) / 2.0;

        for (DataPoint dp : run1.getData()) {
            sse += (Math.pow((dp.getData() - averageSpeed), 2));
        }

        for (DataPoint dp : run2.getData()) {
            sse += (Math.pow((dp.getData() - averageSpeed), 2));
        }

        return sse;
    }

    @Override
    public String getTestName() {
        return "T-Test";
    }

    private void drawTGraph(Job job) {
        charter.drawGraph("T-Test", "Run", "T-Value", "critical T", tCrit, new Charter.ChartData("calculated T", tData));
        charter.openWindow();
    }

    @Override
    public TableView<Run> getTable() {
        return TTable;
    }
}
