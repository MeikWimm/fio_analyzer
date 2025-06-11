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
    private static final Logger LOGGER = Logger.getLogger(Anova.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private final List<XYChart.Data<Number, Number>> anovaData;
    private final Charter charter;
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
    @FXML public Pane anovaPane;
    @FXML public TableView<Run> anovaTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Double> covColumn;
    @FXML public TableColumn<Run, Double> startTimeColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, Double> FColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private final CoV cov;
    private double fCrit;

    public Anova(Job job, Settings settings, double alpha) {
        super(job, settings.getAnovaSkipRunsCounter(), settings.isAnovaUseAdjacentRun(), settings.getGroupSize(), alpha, settings.isBonferroniANOVASelected() , settings.getRequiredRunsForSteadyState());
        final int dataSize = job.getData().size();
        this.cov = new CoV(job, settings);
        this.charter = new Charter();
        this.anovaData = new ArrayList<>(dataSize);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));
        covColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

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
        showCoVGraph.setOnAction(e -> cov.drawAveragedCoVGraph());
        showWinCoVGraph.setOnAction(e -> cov.drawCoVGraph());

        anovaTable.setItems(this.job.getFilteredRuns());

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

    private void setLabeling() {
        fCriticalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.fCrit));
        sigmaJobLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.job.getStandardDeviation()));
        if(this.possibleSteadyStateRuns.isEmpty()){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + this.possibleSteadyStateRuns.getFirst().getID());
        }

        if(cov.getSteadyStateRun() == null){
            steadyStateCVLabel.setText("No steady state CV found.");
        } else {
            steadyStateCVLabel.setText("at run " + cov.getSteadyStateRun().getID() + " | time: " + cov.getSteadyStateRun().getStartTime());
        }

    }

    @Override
    public void calculate() {
        calculateAnova();
        CoV.calculateCoVGroups(this.groups);
        cov.calculate();
        cov.calculateSteadyState();
    }

    @Override
    protected double extractValue(Run run) {
        return run.getF();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < this.fCrit;
    }

    private void calculateAnova() {
        int num = this.groups.size() - 1;
        int denom = (num + 1) * (this.job.getData().size() - 1);

        if (this.groups.isEmpty()) {
            LOGGER.log(Level.WARNING, "Groups list cannot be empty");
            return;
        }

        if (this.groups.size() < 2) {
            LOGGER.log(Level.WARNING, "Groups list must contain at least two groups");
            return;
        }

        FDistribution fDistribution = new FDistribution(num, denom);
        fCrit = fDistribution.inverseCumulativeProbability(1.0 - alpha);
        double sse = 0.0;
        double ssa = 0.0;


        for (List<Run> group : this.groups) {
            for (Run run : group) {
                double averageSpeedOfRun = run.getAverageSpeed();
                ssa += Math.pow(averageSpeedOfRun - MathUtils.average(group), 2);
                ssa *= run.getData().size();
                run.setSSA(ssa);
                ssa = 0;
            }
        }


        for (List<Run> group : this.groups) {
            for (Run run : group) {
                for (DataPoint dp : run.getData()) {
                    sse += (Math.pow((dp.getSpeed() - MathUtils.average(group)), 2));
                }
                run.setSSE(sse);
                sse = 0;
            }
        }

        double fValue;
        for (List<Run> group : this.groups) {
            Run run = group.getFirst();
            double s_2_a = run.getSSA() / (this.groups.size() - 1);
            double s_2_e = run.getSSE() / (this.groups.size() * (run.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            run.setF(fValue);
            run.setP(1.0 - fDistribution.cumulativeProbability(fValue));

            if (this.fCrit < fValue) {
                run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            } else {
                run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                resultGroups.add(group);
            }

            this.resultRuns.add(run);
            anovaData.add(new XYChart.Data<>(run.getID(), fValue));
        }

        double totalSSE = 0;
        for (Run run : this.resultRuns) {
            totalSSE += run.getSSE();
        }

        this.job.setSSE(totalSSE);
        this.job.setMSE(totalSSE / denom);
    }

    private void drawANOVAGraph() {
        charter.drawGraph("ANOVA", "Run", "F-Value", "Critical value", this.fCrit, new Charter.ChartData("calculated F", anovaData));
    }

    public double getCriticalValue() {
        return fCrit;
    }

    public final void openWindow() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/Anova.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setMaxWidth(1200);
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculated ANOVA");
            stage.setScene(new Scene(root1));
            setLabeling();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
