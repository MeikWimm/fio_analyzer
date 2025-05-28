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
    private final int WINDOW_SIZE = Settings.WINDOW_SIZE;

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
    @FXML
    public Label labelHeader;
    @FXML
    public Button drawConIntDiffButton;
    @FXML
    public Button drawWindowedRCIWButton;
    @FXML
    public TableView<Run> conIntTable;
    @FXML
    public TableColumn<Run, Integer> runsColumn;
    @FXML
    public TableColumn<Run, Double> averageSpeedColumn;
    @FXML
    public TableColumn<Run, Double> intervalFromColumn;
    @FXML
    public TableColumn<Run, Double> intervalToColumn;
    @FXML
    public TableColumn<Run, Double> plusMinusValueColumn;
    @FXML
    public TableColumn<Run, Double> standardDeviationColumn;
    @FXML
    public TableColumn<Run, String> compareToRunColumn;
    @FXML
    public TableColumn<Run, Double> overlappingColumn;

    public ConInt(Job job, double alpha) {
        super(job, Settings.CON_INT_SKIP_RUNS_COUNTER, Settings.CON_INT_USE_ADJACENT_RUN, 2, alpha);
        int dataSize = this.job.getData().size();
        charter = new Charter();
        conIntData = new ArrayList<>(dataSize);
        windowedRCIWData = new ArrayList<>(dataSize);
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
        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("RCIW"));
        overlappingColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

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
    public void calculate() {
        NormalDistribution normDis = new NormalDistribution();
        double dataSize = this.job.getRuns().getFirst().getData().size();

        for (Run run : this.job.getRuns()) {
            double averageSpeed = run.getAverageSpeed();
            double alpha = job.getAlpha();
            double std = run.getStandardDeviation();

            double c1 = averageSpeed - (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            run.setIntervalFrom(c1);

            double c2 = averageSpeed + (normDis.inverseCumulativeProbability(1.0 - alpha / 2.0) * (std / Math.sqrt(dataSize)));
            run.setIntervalTo(c2);
        }

        for (List<Run> runs : this.groups) {
            for (int i = 0; i < runs.size() - 1; i++) {
                Run run = runs.get(i);
                double RCIW = Math.abs(run.getIntervalFrom() - run.getIntervalTo()) / run.getAverageSpeed();
                runs.get(i).setRCIW(RCIW);
                conIntData.add(new XYChart.Data<>(runs.get(i).getRunID(), RCIW));
            }
        }
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
        double averageSpeed = average(windowData);
        double std = standardDeviation(windowData);
        double z = normDis.inverseCumulativeProbability(1.0 - alpha / 2.0);

        double c1 = averageSpeed - (z * (std / Math.sqrt(windowSize)));
        double c2 = averageSpeed + (z * (std / Math.sqrt(windowSize)));
        double RCIW = Math.abs(c1 - c2) / averageSpeed;
        windowedRCIWData.add(new XYChart.Data<>(data.getFirst().getTime(), RCIW));


        for (int i = 0; i < data.size() - windowSize; i++) {
            for (int j = 0; j < windowSize; j++) {
                windowData[j] = data.get(i + j).getSpeed();
            }

            averageSpeed = average(windowData);
            std = standardDeviation(windowData);
            c1 = averageSpeed - (z * (std / Math.sqrt(windowSize)));
            c2 = averageSpeed + (z * (std / Math.sqrt(windowSize)));
            RCIW = Math.abs(c1 - c2) / averageSpeed;

            windowedRCIWData.add(new XYChart.Data<>(data.get(i).getTime(), RCIW));
        }
    }
//
//    public void calculateWindowedRCIWEfficient() {
//        NormalDistribution normDis = new NormalDistribution();
//        int windowSize = WINDOW_SIZE;
//        List<DataPoint> data = this.job.getRuns().getFirst().getData();
//        double alpha = job.getAlpha();
//        double z = normDis.inverseCumulativeProbability(1.0 - alpha / 2.0);
//
//        if (data.size() < windowSize) return;
//
//        double sum = 0.0;
//        double sumSq = 0.0;
//
//        for (int i = 0; i < windowSize; i++) {
//            double speed = data.get(i).getSpeed();
//            sum += speed;
//            sumSq += speed * speed;
//        }
//
//        for (int i = 0; i <= data.size() - windowSize; i++) {
//            double avg = sum / windowSize;
//            double variance = (sumSq - (sum * sum) / windowSize) / (windowSize - 1);
//            double std = Math.sqrt(variance);
//
//            double c1 = avg - (z * (std / Math.sqrt(windowSize)));
//            double c2 = avg + (z * (std / Math.sqrt(windowSize)));
//            double RCIW = Math.abs(c1 - c2) / avg;
//
//            windowedRCIWData.add(new XYChart.Data<>(data.get(i).getTime(), RCIW));
//
//            if (i + windowSize < data.size()) {
//                double out = data.get(i).getSpeed();
//                double in = data.get(i + windowSize).getSpeed();
//
//                sum -= out;
//                sumSq -= out * out;
//
//                sum += in;
//                sumSq += in * in;
//            }
//        }
//    }


    private static double average(double[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array must not be null or empty.");
        }

        double sum = 0.0;
        for (double d : data) {
            sum += d;
        }

        return sum / data.length;
    }

    private static double standardDeviation(double[] data) {
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("Data array must contain at least two elements.");
        }

        double mean = 0.0;
        for (double d : data) {
            mean += d;
        }
        mean /= data.length;

        double sumSquaredDiffs = 0.0;
        for (double d : data) {
            double diff = d - mean;
            sumSquaredDiffs += diff * diff;
        }

        return Math.sqrt(sumSquaredDiffs / (data.length - 1));  // Sample standard deviation
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
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
