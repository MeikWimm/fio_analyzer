/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Section;
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
    
    @FXML private TableView<Section> TukeyTable;
    @FXML private TableColumn<Section, Integer> runIDColumn;
    @FXML private TableColumn<Section, Integer> compareToRunColumn;
    @FXML private TableColumn<Section, Double> overallMeanColumn;
    @FXML private TableColumn<Section, Double> QColumn;
    @FXML private TableColumn<Section, Boolean> hypothesisColumn;
    private double qHSD;
    private final List<XYChart.Data<Number, Number>> meanData;
    private final List<XYChart.Data<Number, Number>> qData;
    private Job job;

    // group size = 3
    private static final double Q_CRITICAL_0_05_ALPHA = 5.081;
    private static final double Q_CRITICAL_0_10_ALPHA = 2.902;
    private static final double Q_CRITICAL_0_01_ALPHA = 4.120;
    public TukeyHSD(){
        super();
        this.meanData = new ArrayList<>();
        this.qData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        unitLabel.setText(Settings.getConversion());

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("GroupID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        overallMeanColumn.setCellValueFactory(new PropertyValueFactory<>("overallMean"));
        overallMeanColumn.setCellFactory(TextFieldTableCell.<Section, Double>forTableColumn(new Utils.CustomStringConverter()));

        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Section, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());
        
        TukeyTable.setItems(getPostHocRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
        labelHeader.setText(getTest().getJob().toString());
    }



    @Override
    public String getTestName() {
        return "Tukey-HSD";
    }

    @Override
    protected void calculateTest(List<List<Section>> postHocGroup, List<Section> resultSections){
        List<Section> group = postHocGroup.getFirst();
        Section section = group.getFirst();
        double qCrit;
        if(getTest().getAlpha() >= 0.10){
            qCrit = Q_CRITICAL_0_05_ALPHA;
        } else if(getTest().getAlpha() >= 0.05){
            qCrit = Q_CRITICAL_0_10_ALPHA;
        } else{
            qCrit = Q_CRITICAL_0_01_ALPHA;
        }


        for (List<Section> g : postHocGroup) {
            Section s1 = g.getFirst();
            Section s2 = g.getLast();
            double standardError = Math.sqrt(s1.getMSE() / s1.getData().size());
            double overallMean = Math.abs(s1.getAverageSpeed() - s2.getAverageSpeed());
            this.qHSD = qCrit * standardError;

            s1.setQ(qHSD);
            s1.setOverallMean(overallMean);
            resultSections.add(s1);
            meanData.add(new XYChart.Data<>(s1.getGroupID(), overallMean));
            qData.add(new XYChart.Data<>(s1.getGroupID(), qHSD));
        }

    }

    @Override
    protected double extractValue(Section section) {
        return section.getOverallMean();
    }

    @Override
    protected boolean isWithinThreshold(double value, Section section) {
        return value < section.getQ();
    }

    protected void setLabeling() {
        GenericTest test = getTest();
        Section genericTestSteadyStateSection = test.getSteadyStateRun();
        if(genericTestSteadyStateSection == null){
            anovaSteadyStateLabel.setText("No steady state test can be calculated. No Anova Result");
            return;
        }
        anovaSteadyStateLabel.setText("Section " + test.getSteadyStateRun().getID());

        if(steadyStateSection == null){
            evalLabel.setText("Section " + genericTestSteadyStateSection.getID() + " most likely in transient state.");
        }

        if(steadyStateSection != null){
            if(steadyStateSection.getID() != genericTestSteadyStateSection.getID()){
                tukeySteadyStateLabel.setText("Section " + steadyStateSection.getID());
                evalLabel.setText("Section " + steadyStateSection.getID() + " more likely in steady state than Section " + genericTestSteadyStateSection.getID() + ".");
            } else {
                tukeySteadyStateLabel.setText("Section " + steadyStateSection.getID());
                evalLabel.setText("Section " + steadyStateSection.getID() + " is in steady state.");
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
        return charter.drawGraph("U-Test","Group","Mean/Difference", new Charter.ChartData("Q-HSD", qData), new Charter.ChartData("Run mean", meanData));
    }

    public void draw(){
        charter.drawGraph("U-Test","Group","Mean/Difference", new Charter.ChartData("Q-HSD", qData), new Charter.ChartData("Run mean", meanData));
        charter.openWindow();
    }

    @Override
    public double getCriticalValue() {
        return this.qHSD;
    }

    @Override
    public TableView<Section> getTable() {
        return TukeyTable;
    }

}