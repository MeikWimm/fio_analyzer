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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author meni1999
 */
public class Anova extends GenericTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(Anova.class.getName());
    private static final double STEADY_STATE_COV_THRESHOLD = .3;

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private final int WINDOW_SIZE;
    private final List<XYChart.Data<Number, Number>> anovaData;
    private final List<XYChart.Data<Number, Number>> covData;
    private final List<XYChart.Data<Number, Number>> covAveragedData;
    private final Charter charter;
    @FXML
    public Label averageSpeedLabel;
    @FXML
    public Label sseLabel;
    @FXML
    public Label ssaLabel;
    @FXML
    public Label sstLabel;
    @FXML
    public Label ssaSstLabel;
    @FXML public Label sseSstLabel;
    @FXML
    public Label fCriticalLabel;
    @FXML
    public Label fCalculatedLabel;
    @FXML
    public Button showFGraphButton;
    @FXML
    public Button showCoVGraph;
    @FXML public Button showWinCoVGraph;
    @FXML public Label sigmaJobLabel;
    @FXML public Label steadyStateLabel;
    @FXML public Label steadyStateCVLabel;
    @FXML
    public Pane anovaPane;
    @FXML
    public TableView<Run> anovaTable;
    @FXML
    public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Double> covColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, Double> FColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private double fCrit;
    private final List<Run> possibleCVSteadyStateRuns;

    public Anova(Job job, Settings settings, double alpha) {
        super(job, settings.getAnovaSkipRunsCounter(), settings.isAnovaUseAdjacentRun(), settings.getGroupSize(), alpha, settings.isBonferroniANOVASelected());
        this.charter = new Charter();
        final int dataSize = job.getData().size();
        this.anovaData = new ArrayList<>(dataSize);
        this.covData = new ArrayList<>(dataSize);
        this.covAveragedData = new ArrayList<>(dataSize);
        this.possibleCVSteadyStateRuns = new ArrayList<>();
        WINDOW_SIZE = settings.getWindowSize();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));
        covColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
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
        showCoVGraph.setOnAction(e -> drawAveragedCoVGraph());
        showWinCoVGraph.setOnAction(e -> drawCoVGraph());

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

        if(this.possibleCVSteadyStateRuns.isEmpty()){
            steadyStateCVLabel.setText("No steady state CV found.");
        } else {
            steadyStateCVLabel.setText("at run " + this.possibleCVSteadyStateRuns.getFirst().getID());
        }

    }



    @Override
    public void calculate() {
        calculateAnovaAndCoV();
        calculateWindowedCoV();
    }

    private void calculateWindowedCoV() {
        int initWindow = WINDOW_SIZE;
        int windowSize = WINDOW_SIZE;
        double sum = 0;
        //double k = 0.5;           // Slack value

        List<DataPoint> data = this.job.getData();
        List<Double> windowList = new ArrayList<>();
        for (int i = 0; i < initWindow; i++) {
            sum += data.get(i).getSpeed();
            windowList.add(data.get(i).getSpeed());
        }
        double targetMean = sum / initWindow;
        //double time = data.get(i).getTime();
        double cov = Math.sqrt(GenericTest.variance(windowList, targetMean)) / targetMean;
        covAveragedData.add(new XYChart.Data<>(data.getFirst().getTime(), cov));

        for (int i = 1; i < data.size() - windowSize; i++) {
            sum = 0;
            int nextWindow = windowSize + i;
            for (int j = i; j < nextWindow; j++) {
                if (j < data.size()) {
                    windowList.add(data.get(j).getSpeed());
                    sum += data.get(j).getSpeed();
                }
            }
            targetMean = sum / windowSize;
            double time = data.get(i).getTime();
            cov = Math.sqrt(GenericTest.variance(windowList, targetMean)) / targetMean;
            covAveragedData.add(new XYChart.Data<>(time, cov));
            windowList.clear();
        }


        // Using the average cov of the job and compare it to the windowed cov
//        double sumCoV = 0;
//        for (XYChart.Data<Number, Number> covDatum : covData) {
//            sumCoV += (double) covDatum.getYValue();
//        }
//        double averageCoV = sumCoV / covData.size();

//        for (XYChart.Data<Number, Number> covDatum : covData) {
//            double cov = (double) covDatum.getYValue();
//            covAveragedData.add(new XYChart.Data<>(covDatum.getXValue(), cov));
//        }
    }

    private void calculateAnovaAndCoV() {
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
                ssa += Math.pow(averageSpeedOfRun - GenericTest.average(group), 2);
                ssa *= run.getData().size();
                run.setSSA(ssa);
                ssa = 0;
            }
        }


        for (List<Run> group : this.groups) {
            for (Run run : group) {
                for (DataPoint dp : run.getData()) {
                    sse += (Math.pow((dp.getSpeed() - GenericTest.average(group)), 2));
                }
                run.setSSE(sse);
                sse = 0;
            }
        }

        double fValue;
        double cov;
        for (List<Run> group : this.groups) {
            Run run = group.getFirst();
            //cov = GenericTest.calculateCoVOverStd(this.job.getStandardDeviation(), group);
            cov = GenericTest.calculateCoV(group);
            double s_2_a = run.getSSA() / (this.groups.size() - 1);
            double s_2_e = run.getSSE() / (this.groups.size() * (run.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            run.setCoV(cov);
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
            covData.add(new XYChart.Data<>(run.getID(), cov));
        }

        double totalSSE = 0;
        for (Run run : this.resultRuns) {
            calculateSteadyStateInterval(run, run.getCoV());
            totalSSE += run.getSSE();
        }

        this.job.setSSE(totalSSE);
        this.job.setMSE(totalSSE / denom);
    }

    private void calculateSteadyStateInterval(Run run, double value) {
        if (value < this.job.getCvThreshold()) {
            possibleCVSteadyStateRuns.add(run);
        }

        if (possibleCVSteadyStateRuns.isEmpty()) {
            LOGGER.log(Level.WARNING, String.format("No steady state found for value %s", value));
        } else {
            LOGGER.log(Level.INFO, String.format("Found steady state at Run %d for value %s", possibleCVSteadyStateRuns.getFirst().getID(), value));
        }
    }

    private void drawANOVAGraph() {

        charter.drawGraph("ANOVA", "Run", "F-Value", "Critical value", this.fCrit, new Charter.ChartData("calculated F", anovaData));
    }

    private void drawCoVGraph() {
        charter.drawGraph("Run CoV", "Per run", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("CV over Job", covData));
    }

    private void drawAveragedCoVGraph() {
        charter.drawGraph("CoV Windowed", "Job", "F-Value", new Charter.ChartData("Windowed CV over Job", covAveragedData));
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
