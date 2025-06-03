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
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.TDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author meni1999
 */
public class TTest extends GenericTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(TTest.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("TTest"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    @FXML public Label zCritLabel;
    @FXML public Label steadyStateLabel;

    @FXML public Button drawTTest;

    @FXML public TableView<Run> TTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> TColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;

    private double tCrit;
    private final Charter charter;
    private final List<XYChart.Data<Number, Number>> tData;

    public TTest(Job job, Settings settings, double alpha) {
        super(job, settings.getTTestSkipRunsCounter(), settings.isTTestUseAdjacentRun(), 2, alpha ,settings.isBonferroniTTestSelected());
    this.charter = new Charter();
        this.tData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        TColumn.setCellValueFactory(new PropertyValueFactory<>("T"));
        TColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTTest.setOnAction(e -> drawTGraph(this.job));
        TTable.setItems(this.job.getFilteredRuns());
    }

    private void setLabeling() {
        zCritLabel.setText(String.format(Locale.ENGLISH, "%,.5f", this.tCrit));
        if(this.getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + this.getSteadyStateRun().getID());
        }
    }

    @Override
    public void calculate() {
        if (groups.size() <= 1) return;
        TDistribution t = new TDistribution(job.getData().size() * 2 - 2);
        this.tCrit = t.inverseCumulativeProbability(1 - this.alpha / 2.0);


        for (int i = 0; i < job.getRuns().size() - 1; i++) {
            Run run = job.getRuns().get(i);
            Run run2 = job.getRuns().get(i + 1);

            double sse = calculateSSE(run, run2);
            double runVariance1 = calculateVariance(run, sse);
            double runVariance2 = calculateVariance(run2, sse);
            double runSize = this.job.getData().size();

            double nominator = (run.getAverageSpeed() - run2.getAverageSpeed());
            double denominator = Math.sqrt((runVariance1 / runSize) + (runVariance2 / runSize));
            double tVal = Math.abs(nominator / denominator);
            run.setT(tVal);

            tData.add(new XYChart.Data<>(run.getID(), tVal));

            if (this.tCrit < tVal) {
                run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            } else {
                run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
            }

            this.resultRuns.add(run);
        }
    }

    private double calculateVariance(Run run, double sse) {
        return (1.0 / (run.getData().size() - 1.0)) * sse;
    }

    private double calculateSSE(Run run1, Run run2) {
        double sse = 0;
        double averageSpeed = (run1.getAverageSpeed() + run2.getAverageSpeed()) / 2.0;

        for (DataPoint dp : run1.getData()) {
            sse += (Math.pow((dp.getSpeed() - averageSpeed), 2));
        }

        for (DataPoint dp : run2.getData()) {
            sse += (Math.pow((dp.getSpeed() - averageSpeed), 2));
        }

        return sse;
    }

    private void drawTGraph(Job job) {
        charter.drawGraph("T-Test", "Run", "T-Value", "critical T", tCrit, new Charter.ChartData("calculated T", tData));
    }

    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/TTest.fxml"));
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
            stage.setTitle("Calculated T-Test");
            stage.setScene(new Scene(root1));
            setLabeling();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            //LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
        }
    }

}
