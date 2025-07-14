/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import de.unileipzig.atool.Utils;

import java.net.URL;
import java.util.*;

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

/**
 *
 * @author meni1999
 */
public class TukeyHSD extends PostHocTest implements Initializable {
    @FXML private Label labelHeader;
    @FXML private Label unitLabel;
    @FXML private Label qCritLabel;
    @FXML private Label anovaSteadyStateLabel;
    @FXML private Label tukeySteadyStateLabel;
    @FXML private Label evalLabel;

    @FXML private Button drawTukey;
    
    @FXML private TableView<Run> TukeyTable;
    @FXML private TableColumn<Run, Integer> runIDColumn;
    @FXML private TableColumn<Run, Integer> compareToRunColumn;
    @FXML private TableColumn<Run, Double> QColumn;
    @FXML private TableColumn<Run, Boolean> hypothesisColumn;
    private double qHSD;
    private final List<XYChart.Data<Number, Number>> meanData;
    private Job job;

    // group size = 3
    private static final double Q_CRITICAL_0_05_ALPHA = 5.081;
    public TukeyHSD(){
        super();
        this.meanData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        unitLabel.setText(Settings.getConversion());

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("GroupID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());
        
        TukeyTable.setItems(getPostHocRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
        labelHeader.setText(getTest().getJob().toString());
    }

    public void draw(){
        charter.drawGraph("U-Test","Group","Mean/Difference","Q-HSD", qHSD,new Charter.ChartData("Run mean", meanData));
        charter.openWindow();
    }

    @Override
    public String getTestName() {
        return "Tukey-HSD";
    }

    @Override
    protected void calculateTest(List<List<Run>> postHocGroup, List<Run> resultRuns){
        List<Run> group = postHocGroup.getFirst();
        Run run = group.getFirst();
        double standardError = Math.sqrt(run.getMSE() / run.getData().size());
        this.qHSD = Q_CRITICAL_0_05_ALPHA * standardError;

        for (List<Run> g : postHocGroup) {
            Run r1 = g.getFirst();
            Run r2 = g.getLast();

            double overallMean = Math.abs(r1.getAverageSpeed() - r2.getAverageSpeed());

            r1.setQ(overallMean);
            resultRuns.add(r1);
            meanData.add(new XYChart.Data<>(r1.getGroupID(), overallMean));
        }

    }

    @Override
    protected double extractValue(Run run) {
        return run.getQ();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < qHSD;
    }

    protected void setLabeling() {
        GenericTest test = getTest();
        Run genericTestSteadyStateRun = test.getSteadyStateRun();
        if(genericTestSteadyStateRun == null){
            anovaSteadyStateLabel.setText("No steady state test can be calculated. No Anova Result");
            return;
        }
        anovaSteadyStateLabel.setText("Run " + test.getSteadyStateRun().getID());

        if(steadyStateRun == null){
            evalLabel.setText("Run " + genericTestSteadyStateRun.getID() + " most likely in transient state.");
        }

        if(steadyStateRun != null){
            if(steadyStateRun.getID() != genericTestSteadyStateRun.getID()){
                tukeySteadyStateLabel.setText("Run " + steadyStateRun.getID());
                evalLabel.setText("Run " + steadyStateRun.getID() + " more likely in steady state than Run " + genericTestSteadyStateRun.getID() + ".");
            } else {
                tukeySteadyStateLabel.setText("Run " + steadyStateRun.getID());
                evalLabel.setText("Run " + steadyStateRun.getID() + " is in steady state.");
            }
        }
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/TukeyHSD.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated Tukey HSD";
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("U-Test","Group","Mean/Difference","Q-HSD", qHSD,new Charter.ChartData("Run mean", meanData));
    }

    @Override
    public double getCriticalValue() {
        return this.qHSD;
    }

    @Override
    public TableView<Run> getTable() {
        return TukeyTable;
    }

}