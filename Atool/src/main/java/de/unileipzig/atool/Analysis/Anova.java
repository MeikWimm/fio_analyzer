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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.FDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author meni1999
 */
public class Anova extends GenericTest implements Initializable {

    private final List<XYChart.Data<Number, Number>> anovaData;
    @FXML public Label averageSpeedLabel;
    @FXML public Label sseLabel;
    @FXML public Label ssaLabel;
    @FXML public Label sstLabel;
    @FXML public Label ssaSstLabel;
    @FXML public Label sseSstLabel;
    @FXML public Label fCriticalLabel;
    @FXML public Label fCalculatedLabel;
    @FXML public Button showFGraphButton;
    @FXML public Button showCoVGraph;
    @FXML public Button showWinCoVGraph;
    @FXML public Label sigmaJobLabel;
    @FXML public Label steadyStateLabel;
    @FXML public Label steadyStateCVLabel;
    @FXML public Label steadyStateCVWinLabel;
    @FXML public Pane anovaPane;
    @FXML public TableView<Run> anovaTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Double> startTimeColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, Double> FColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private double fCrit;

    public Anova(Job job, Settings settings) {
        super(job, settings.getAnovaSkipRunsCounter(), settings.isAnovaUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), settings.isBonferroniANOVASelected() , settings.getRequiredRunsForSteadyState());
        final int dataSize = job.getData().size();
        this.anovaData = new ArrayList<>(dataSize);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));

        FColumn.setCellValueFactory(new PropertyValueFactory<>("F"));
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

        anovaTable.setItems(getResultRuns());
    }

    private void updateLabeling(Run run) {
        averageSpeedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getAverageSpeed()));
        sseLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSE()));
        ssaLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSA()));
        sstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSST()));
        ssaSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (run.getSSA() / run.getSST())));
        sseSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (run.getSSE() / run.getSST())));
        fCalculatedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getF()));
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
    protected void calculateTest(List<List<Run>> groups, List<Run> resultRuns) {
        int num = groups.size() - 1;
        int denom = (num + 1) * (this.job.getData().size() - 1);

        FDistribution fDistribution = new FDistribution(num, denom);
        fCrit = fDistribution.inverseCumulativeProbability(1.0 - alpha);
        double sse = 0.0;
        double ssa = 0.0;


        for (List<Run> group : groups) {
            for (Run run : group) {
                double averageSpeedOfRun = run.getAverageSpeed();
                ssa += Math.pow(averageSpeedOfRun - MathUtils.average(group), 2);
                ssa *= run.getData().size();
                run.setSSA(ssa);
                ssa = 0;
            }
        }


        for (List<Run> group : groups) {
            for (Run run : group) {
                for (DataPoint dp : run.getData()) {
                    sse += (Math.pow((dp.getData() - MathUtils.average(group)), 2));
                }
                run.setSSE(sse);
                sse = 0;
            }
        }

        double fValue;
        for (List<Run> group : groups) {
            Run run = group.getFirst();
            double s_2_a = run.getSSA() / (groups.size() - 1);
            double s_2_e = run.getSSE() / (groups.size() * (run.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            run.setF(fValue);
            run.setP(1.0 - fDistribution.cumulativeProbability(fValue));
            resultRuns.add(run);
            anovaData.add(new XYChart.Data<>(run.getID(), fValue));
        }

        double totalSSE = 0;
        for (Run run : resultRuns) {
            totalSSE += run.getSSE();
        }

        this.job.setSSE(totalSSE);
        this.job.setMSE(totalSSE / denom);
    }

    @Override
    protected double extractValue(Run run) {
        return run.getF();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < this.fCrit;
    }

    private void drawANOVAGraph() {
        charter.drawGraph("ANOVA", "Run", "F-Value", "Critical value", this.fCrit, new Charter.ChartData("calculated F", anovaData));
        charter.openWindow();
    }

    public double getCriticalValue() {
        return fCrit;
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("ANOVA", "Run", "F-Value", "Critical value", this.fCrit, new Charter.ChartData("calculated F", anovaData));
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/Anova.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated Anova";
    }

    @Override
    public String getTestName() {
        return "ANOVA";
    }

    public TableView<Run> getTable() {
        return anovaTable;
    }
}
