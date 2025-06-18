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
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConInt (Confidence Interval)
 *
 * @author meni1999
 */
public class ConInt extends GenericTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ConInt.class.getName());
    private static final double STEADY_STATE_RCIW_THRESHOLD = .02;
    private final int WINDOW_SIZE;

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private final Charter charter;
    private final List<XYChart.Data<Number, Number>> conIntData;
    private final List<XYChart.Data<Number, Number>> windowedRCIWData;
    @FXML public Label labelHeader;
    @FXML public Button drawConIntDiffButton;
    @FXML public Button drawWindowedRCIWButton;
    @FXML public TableView<Run> conIntTable;
    @FXML public TableColumn<Run, Integer> runsColumn;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Double> intervalFromColumn;
    @FXML public TableColumn<Run, Double> intervalToColumn;
    @FXML public TableColumn<Run, Double> plusMinusValueColumn;
    @FXML public TableColumn<Run, Double> standardDeviationColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, Byte> overlappingColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    @FXML public Label steadyStateLabel;


    public ConInt(Job job,Settings settings) {
        super(job, settings.getConIntSkipRunsCounter(), settings.isConIntUseAdjacentRun(), 2, job.getAlpha(), settings.isBonferroniConIntSelected(), settings.getRequiredRunsForSteadyState());
        int dataSize = this.job.getData().size();
        charter = new Charter();
        conIntData = new ArrayList<>(dataSize);
        windowedRCIWData = new ArrayList<>(dataSize);
        WINDOW_SIZE = settings.getWindowSize();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        intervalFromColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalFrom"));
        intervalFromColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        intervalToColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalTo"));
        intervalToColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        plusMinusValueColumn.setCellValueFactory(new PropertyValueFactory<>("PlusMinusValue"));
        plusMinusValueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
        standardDeviationColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        compareToRunColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGroup()));
        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("Overlap"));
        overlappingColumn.setCellFactory(Utils.getBooleanCellFactory()); //TODO currently workaround and bad name
        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawConIntDiffButton.setOnAction(e -> draw());
        drawWindowedRCIWButton.setOnAction(e -> drawWindowedRCIW());

        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getFilteredRuns());
    }

    public void draw() {
        charter.drawGraph(
                "Per-Run Relative Confidence Interval Width (RCIW) over Job Average",
                "Run Index",
                "RCIW / Job Average Speed",
                "Threshold",
                this.job.getRciwThreshold(),
                new Charter.ChartData("RCIW per Run", this.conIntData)
        );
    }

    private void drawWindowedRCIW() {
        calculateSWindowedRCIW();
        charter.drawGraph(
                "Windowed Relative Confidence Interval Width ( = " + this.WINDOW_SIZE + " time steps)",
                "Last time point in window",
                "RCIW (Width / Mean)",
                new Charter.ChartData("RCIW Value", this.windowedRCIWData)
        );
    }

    @Override
    public void calculateTest() {
        NormalDistribution normDis = new NormalDistribution();
        double dataSize = this.job.getRunDataSize();

        /*
        Calculate first the intervals
         */
        for (Run run : this.job.getRuns()) {
            double averageSpeed = run.getAverageSpeed();
            double alpha = job.getAlpha();
            double std = run.getStandardDeviation();

            double c1 = averageSpeed - (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            run.setIntervalFrom(c1);

            double c2 = averageSpeed + (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            run.setIntervalTo(c2);

           // double RCIW = Math.abs(run.getIntervalFrom() - run.getIntervalTo()) / this.job.getAverageSpeed();
           // run.setRCIW(RCIW);
        }

        /*
        Look for overlapping intervals
         */
        for (List<Run> runs : this.groups) {
            Run run1 = runs.get(0);
            Run run2 = runs.get(1);
            double a1 = run1.getIntervalFrom();
            double a2 = run2.getIntervalFrom();
            double b1 = run1.getIntervalTo();
            double b2 = run2.getIntervalTo();
            run1.setOverlap(doConfidenceIntervalsOverlap(a1, b1, a2, b2));

            double averageSpeed1 = run1.getAverageSpeed();
            double averageSpeed2 = run2.getAverageSpeed();
            double alpha = job.getAlpha();
            double std1 = run1.getStandardDeviation();
            double dataSize1 = run1.getData().size();
            double std2 = run2.getStandardDeviation();
            double dataSize2 = run1.getData().size();
            /*
            Calculate new interval
             */
            double s_x = Math.sqrt((Math.pow(std1, 2) / dataSize1) + (Math.pow(std2, 2)  / dataSize2));
            double x = averageSpeed1 - averageSpeed2;
            double y = s_x * normDis.inverseCumulativeProbability(1.0 - alpha / 2.0);
            double c1 = x - y;
            double c2 = x + y;

            run1.setNullhypothesis(doesIntervalContainZero(c1, c2));
            this.resultRuns.add(run1);
        }
    }

    private byte doConfidenceIntervalsOverlap(double a1, double b1, double a2, double b2) {
        //TODO add extra byte type
        if(Math.max(a1, a2) <= Math.min(b1, b2)){
            return Run.ACCEPTED_NULLHYPOTHESIS;
        } else {
            return Run.REJECTED_NULLHYPOTHESIS;
        }
    }

    private byte doesIntervalContainZero(double lowerBound, double upperBound) {
        //TODO add extra byte type
        if(lowerBound <= 0 && upperBound >= 0){
            return Run.ACCEPTED_NULLHYPOTHESIS;
        } else {
            return Run.REJECTED_NULLHYPOTHESIS;
        }
    }

    @Override
    protected double extractValue(Run run) {
        return run.getNullhypothesis();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value == Run.ACCEPTED_NULLHYPOTHESIS;
    }

    public void calculateSWindowedRCIW() {
        NormalDistribution normDis = new NormalDistribution();
        int windowSize = WINDOW_SIZE;
        double[] windowData = new double[windowSize];
        List<DataPoint> data = this.job.getData();

        if (data.size() < windowSize) return;
        for (int i = 0; i < windowSize; i++) {
            windowData[i] = data.get(i).getSpeed();
        }

        double alpha = job.getAlpha();
        double averageSpeed = MathUtils.average(windowData);
        double std = MathUtils.standardDeviation(windowData);
        double z = normDis.inverseCumulativeProbability(1.0 - alpha / 2.0);

        double c1 = averageSpeed - (z * (std / Math.sqrt(windowSize)));
        double c2 = averageSpeed + (z * (std / Math.sqrt(windowSize)));
        double RCIW = Math.abs(c1 - c2) / averageSpeed;
        int windowCount = 1 + this.job.getRunDataSize() * job.getSkippedRuns();
        int windowStartPoint = 0;
        int windowEndPoint = 0;
        windowedRCIWData.add(new XYChart.Data<>(data.getFirst().getTime(), RCIW));
        boolean isSteadyStateFound = false;

        for (int i = 0; i < data.size() - windowSize; i++) {
            for (int j = 0; j < windowSize; j++) {
                windowData[j] = data.get(i + j).getSpeed();
            }

            averageSpeed = MathUtils.average(windowData);
            std = MathUtils.standardDeviation(windowData);
            c1 = averageSpeed - (z * (std / Math.sqrt(windowSize)));
            c2 = averageSpeed + (z * (std / Math.sqrt(windowSize)));
            RCIW = Math.abs(c1 - c2) / averageSpeed;

            if(RCIW < 0.03 && !isSteadyStateFound){
                isSteadyStateFound = true;
                windowStartPoint = windowCount;
                windowEndPoint = windowSize + windowCount;
            }
            windowedRCIWData.add(new XYChart.Data<>(data.get(i).getTime(), RCIW));
            windowCount++;
        }

        if(isSteadyStateFound){
            for(Run run: this.job.getRuns()){
                int startPoint = run.getData().size() * (run.getID() - 1);
                int endPoint = startPoint + run.getData().size();

                if(windowStartPoint < endPoint && windowEndPoint > startPoint){
                    System.out.println("Run should be " + run.getID());
                }
            }
        }
    }

    @Override
    public String getTestName() {
        return "Confidence Interval";
    }

    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/ConInt.fxml"));
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
            stage.setTitle("Calculated Confidence Interval");
            stage.setScene(new Scene(root1));
            setLabeling();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLabeling() {
        Run run = this.getSteadyStateRun();
        if(run == null){
            steadyStateLabel.setText("No steady state found");
            return;
        }

        steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
    }
}
