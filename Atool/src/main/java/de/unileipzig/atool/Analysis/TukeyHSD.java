/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;

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
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD extends GenericTest implements Initializable {
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

    public TukeyHSD(Job job, Settings settings){
        super(job, settings.getAnovaSkipRunsCounter(), settings.isAnovaUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), settings.isBonferroniANOVASelected() , settings.getRequiredRunsForSteadyState());
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

        //TukeyTable.setItems(getPostHocRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
        //labelHeader.setText(getTest().getJob().toString());
    }

    public void draw(){
        charter.drawGraph("U-Test","Group","Mean/Difference","Q-HSD", qHSD,new Charter.ChartData("Run mean", meanData));
        charter.openWindow();
    }

    @Override
    public String getTestName() {
        return "Tukey-HSD";
    }

    private List<List<Section>> initPostHocTest(List<Section> group) {
        Section section = group.getFirst();
        List<List<Section>> sectionPairs = new ArrayList<>();

        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Section s1 = group.get(i);
                Section s2 = group.get(j);

                List<Section> pair = new ArrayList<>();
                pair.add(s1);
                pair.add(s2);

                sectionPairs.add(pair);
            }
        }

        int totalObservations = section.getData().size() * 3; // TODO - 3 for 3 sections in one group, workaround
        int numberOfGroups = 3;
        int dfError = totalObservations - numberOfGroups;
        Tukey tukey = new Tukey(section.getData().size(), numberOfGroups, dfError);
        double standardError = Math.sqrt(job.getMSE() / totalObservations);
        double qCritical = tukey.inverse_survival(getAlpha(), false);
        qHSD = qCritical * standardError;

        return sectionPairs;
    }

//    @Override
//    protected void calculateTest(List<Run> firstGroup, List<Run> secondGroup, List<Run> postHocRuns){
//        Run run = firstGroup.getFirst();
//        double overallMean = getOverallMean(firstGroup, secondGroup);
//
//        run.setQ(overallMean);
//        postHocRuns.add(run);
//
//        meanData.add(new XYChart.Data<>(run.getGroupID(), overallMean));
//    }

//    private double getOverallMean(List<Section> group1, List<Section> group2) {
//        double speedSumGroup1 = 0;
//        double speedSumGroup2 = 0;
//
//        for (Section s : group1) {
//            speedSumGroup1 += s.getAverageSpeed();
//        }
//
//        for (Section s : group2) {
//            speedSumGroup2 += s.getAverageSpeed();
//        }
//
//        double averageSpeedGroup1 = speedSumGroup1 / group1.size();
//        double averageSpeedGroup2 = speedSumGroup2 / group2.size();
//
//        return Math.abs(averageSpeedGroup1 - averageSpeedGroup2);
//    }

        private double getOverallMean(List<Section> group) {
        Section section = group.getFirst();
        Section section2 = group.getLast();

        return Math.abs(section.getAverageSpeed() - section2.getAverageSpeed());
    }

    @Override
    protected void calculateTest(Run run, List<Section> resultSections) {
        List<List<Section>> groups = run.getGroups();
        for (List<Section> group : groups) {
            List<List<Section>> sectionPairGroups = initPostHocTest(group);
            for (List<Section> sectionPair : sectionPairGroups) {
                Section s1= group.getFirst();
                Section s2 = group.getLast();
                double overallMean = getOverallMean(sectionPair);
                Logging.log(Level.INFO, "Tukey", String.format("Section: %s, Mean; %f | Group 1: %s - %s", group.getFirst(), overallMean, s1.toString(), s2.toString()));
            }

            //run.setQ(overallMean);
            //postHocRuns.add(run);

            //meanData.add(new XYChart.Data<>(run.getGroupID(), overallMean));
        }
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < qHSD;
    }

    @Override
    protected double extractValue(Section section) {
        return 0;
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
    public double getCriticalValue() {
        return this.qHSD;
    }

    @Override
    public Scene getCharterScene() {
        return null;
    }

    @Override
    public TableView<Run> getTable() {
        return TukeyTable;
    }

}