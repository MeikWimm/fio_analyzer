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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    public AtoolTTest(Job job, Settings settings) {
        super(job, settings.getTTestSkipRunsCounter(), settings.isTTestUseAdjacentRun(), 2, job.getAlpha() ,settings.isBonferroniTTestSelected(), settings.getRequiredRunsForSteadyState());
        this.tData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setText("Average Speed " + Settings.getConversion());
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        TColumn.setCellValueFactory(new PropertyValueFactory<>("T"));
        TColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        labelHeader.setText(this.job.toString());

        drawTTest.setOnAction(e -> drawTGraph());
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
        TTest tTest = new TTest();

        for (List<Run> group : groups) {
            Run run1 = group.getFirst();
            Run run2 = group.get(1);
            double[] data1 = run1.getData().stream().mapToDouble(dp -> dp.data).toArray();
            double[] data2 = run2.getData().stream().mapToDouble(dp -> dp.data).toArray();


            double pValue = tTest.tTest(data1, data2);
            group.getFirst().setP(pValue);
            tData.add(new XYChart.Data<>(group.getFirst().getID(), pValue));
            resultRuns.add(group.getFirst());
        }
    }

    @Override
    protected double extractValue(Run run) {
        return run.getP();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < getAlpha();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("T-Test", "Run", "T-Value", "critical T", getAlpha(), new Charter.ChartData("calculated T", tData));
    }

    @Override
    public double getCriticalValue() {
        return this.tCrit;
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
