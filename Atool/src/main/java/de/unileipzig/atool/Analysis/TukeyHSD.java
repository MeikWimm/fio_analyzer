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
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD extends PostHocTest implements Initializable {

    @FXML public Label qCritLabel;
    @FXML public Label anovaSteadyStateLabel;
    @FXML public Label tukeySteadyStateLabel;
    @FXML public Label evalLabel;

    @FXML public Button drawTukey;
    
    @FXML public TableView<Run> TukeyTable;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> QColumn;
    @FXML public TableColumn<Run, Boolean> hypothesisColumn;
    private double qHSD;
    private final List<XYChart.Data<Number, Number>> meanData;
    private final List<XYChart.Data<Number, Number>> qHSDData;
    private final Anova anova;
    public TukeyHSD(Anova anova){
        super(anova);
        this.anova = anova;
        this.meanData = new ArrayList<>();
        this.qHSDData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("GroupID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());
        
        TukeyTable.setItems(getPostHocRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
    }

    public void draw(){
        charter.drawGraph("U-Test","Group","Mean/Difference",new Charter.ChartData("Run mean", meanData), new Charter.ChartData("QHSD", qHSDData));
        charter.openWindow();
    }

    @Override
    public String getTestName() {
        return "Tukey-HSD";
    }

    @Override
    public void calculateTest(List<List<Run>> postHocGroups, List<Run> postHocRuns) {
        Job job = anova.getJob();
        int totalObservations = job.getData().size();
        double n = job.getData().size();
        int numberOfGroups = postHocGroups.size();
        int dfError = totalObservations - numberOfGroups;
        Tukey tukey = new Tukey(postHocGroups.getFirst().size(), numberOfGroups, dfError);
        double standardError = Math.sqrt(job.getMSE() / n);
        double qCritical = tukey.inverse_survival(anova.getAlpha(), false);
        qHSD = qCritical * standardError;

        for (int i = 0; i <= postHocGroups.size() - 2; i += 2) {
            List<Run> group1 = postHocGroups.get(i);
            List<Run> group2 = postHocGroups.get(i + 1);

            Run run = group1.getFirst();
            double overallMean = getOverallMean(group1, group2);

            run.setQ(overallMean);
            postHocRuns.add(run);

            meanData.add(new XYChart.Data<>(run.getGroupID(), overallMean));
            qHSDData.add(new XYChart.Data<>(run.getGroupID(), qHSD));

            this.firstGroup.add(group1);
            this.secondGroup.add(group2);
        }
    }

    private double getOverallMean(List<Run> group1, List<Run> group2) {
        double speedSumGroup1 = 0;
        double speedSumGroup2 = 0;

        for (Run r : group1) {
            speedSumGroup1 += r.getAverageSpeed();
        }

        for (Run r : group2) {
            speedSumGroup2 += r.getAverageSpeed();
        }

        double averageSpeedGroup1 = speedSumGroup1 / group1.size();
        double averageSpeedGroup2 = speedSumGroup2 / group2.size();

        return Math.abs(averageSpeedGroup1 - averageSpeedGroup2);
    }

    @Override
    protected double extractValue(Run run) {
        return run.getQ();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < qHSD;
    }

    @Override
    protected void setLabeling() {
        if(steadyStateRun == null && anovaSteadyStateRun == null){
            evalLabel.setText("No steady state run found.");
            return;
        }

        anovaSteadyStateLabel.setText("Run " + anovaSteadyStateRun.getID());

        if(steadyStateRun == null && anovaSteadyStateRun != null){
            evalLabel.setText("Run " + anovaSteadyStateRun.getID() + " most likely in transient state.");
        }

        if(steadyStateRun != null){
            if(steadyStateRun.getID() != anovaSteadyStateRun.getID()){
                tukeySteadyStateLabel.setText("Run " + steadyStateRun.getID());
                evalLabel.setText("Run " + steadyStateRun.getID() + " more likely in steady state than Run " + anovaSteadyStateRun.getID() + ".");
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
        return charter.drawGraph("Tukey-HSD-Test","Group","Mean/Difference",new Charter.ChartData("Run mean", meanData), new Charter.ChartData("QHSD", qHSDData));
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