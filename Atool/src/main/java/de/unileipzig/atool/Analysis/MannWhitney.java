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
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author meni1999
 */
public class MannWhitney extends GenericTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MannWhitney.class.getName());
    private static class RankedDataPoint extends DataPoint {
        int flag;
        double rank;

        public RankedDataPoint(DataPoint dp, int rank, int flag) {
            super(dp.getSpeed(), dp.getTime());
            this.rank = rank;
            this.flag = flag;
        }

        public double getRank() {
            return rank;
        }

        public void setRank(double rank) {
            this.rank = rank;
        }

        public int getFlag() {
            return flag;
        }
    }

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Mann-Whitney"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private final List<XYChart.Data<Number, Number>> uTestData;
    private final Charter charter;
    @FXML public TableView<Run> uTestTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> ZColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    @FXML public Button drawUTestButton;
    @FXML public Label zIntervalLabel;
    @FXML public Label steadyStateLabel;

    private double zCrit;

    public MannWhitney(Job job,Settings settings) {
        super(job, settings.getUTestSkipRunsCounter(), settings.isUTestUseAdjacentRun(), 2, job.getAlpha(), settings.isBonferroniUTestSelected(), settings.getRequiredRunsForSteadyState());
        this.charter = new Charter();
        this.uTestData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        ZColumn.setCellValueFactory(new PropertyValueFactory<>("Z"));
        ZColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        uTestTable.setItems(this.job.getFilteredRuns());

        drawUTestButton.setOnAction(e -> draw());
    }

    private void setLabeling() {
        zIntervalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.zCrit));
        if(this.getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
        }
    }

    public void draw() {
        charter.drawGraph("U-Test", "Run", "Z-Value","z-critical", this.zCrit, new Charter.ChartData("calculated Z", uTestData));
    }

    @Override
    public String getTestName() {
        return "Mann-Whitney";
    }

    private void calculatePair(Run run1, Run run2) {
        List<RankedDataPoint> rankedData1 = new ArrayList<>();
        List<RankedDataPoint> rankedData2 = new ArrayList<>();
        NormalDistribution n = new NormalDistribution();

        int size = run1.getData().size();
        for (int i = 0; i < size; i++) {
            rankedData1.add(new RankedDataPoint(run1.getData().get(i), 0, 0));
            rankedData2.add(new RankedDataPoint(run2.getData().get(i), 0, 1));
        }

        List<RankedDataPoint> mergedData = new ArrayList<>(rankedData1);
        mergedData.addAll(rankedData2);


        mergedData.sort(new Utils.SpeedComparator());

        double r = 1;
        int counter = 1;
        double new_speed, next_speed = -1;
        int index = 0;
        int jindex = 0;
        for (RankedDataPoint p : mergedData) {
            new_speed = p.getSpeed();
            if (jindex < mergedData.size() - 1) {
                next_speed = mergedData.get(jindex + 1).getSpeed();
            }

            if (next_speed == new_speed && jindex < mergedData.size() - 1) {
                if (counter == 1) {
                    index = jindex;
                }
                counter++;
            } else if (counter > 1) {
                // For ties, use average of ranks
                double averageRank = r + (counter - 1) / 2.0;
                for (int i = index; i < index + counter; i++) {
                    mergedData.get(i).setRank(averageRank);
                }
                counter = 1;
            } else {
                p.setRank(r);
            }

            if (counter == 1) {
                r++;
            }
            jindex++;
        }

        double run1_ranksum = 0;
        double run2_ranksum = 0;

        for (RankedDataPoint dataPoint : mergedData) {
            if (dataPoint.getFlag() == 0) {
                run1_ranksum += dataPoint.getRank();
            } else {
                run2_ranksum += dataPoint.getRank();
            }
        }

        double m = mergedData.size() / 2.0;
        double U1 = m * m + ((m * (m + 1) / 2)) - run1_ranksum;
        double U2 = m * m + ((m * (m + 1) / 2)) - run2_ranksum;
        double mu_U = m * m * 0.5;
        double sigma_U = Math.sqrt((m * m * (2 * m + 1)) / 12.0);
        double U = Math.min(U1, U2);
        double z = Math.abs((U - mu_U) / sigma_U);
        this.zCrit = n.inverseCumulativeProbability(1 - this.getAlpha() / 2.0);



        double pCalc = n.cumulativeProbability(z);
        double pCrit = 1 - this.getAlpha() / 2.0;
        byte hypothesis;
        if (pCalc > pCrit) {
            hypothesis = Run.REJECTED_NULLHYPOTHESIS;
        } else {
            hypothesis = Run.ACCEPTED_NULLHYPOTHESIS;
        }

        run1.setZ(z);
        run1.setNullhypothesis(hypothesis);
        uTestData.add(new XYChart.Data<>(run1.getRunID(), z));
        this.resultRuns.add(run1);

    }

    @Override
    public void calculateTest() {
        if (this.job.getRuns().size() <= 1) return;
        List<Run> runs = this.job.getRuns();

        for (int i = 0; i < runs.size(); i ++) {
            if (i < runs.size() - 1) {
                Run run1 = runs.get(i);
                Run run2 = runs.get(i + 1);
                calculatePair(run1, run2);
            }
        }
    }

    @Override
    protected double extractValue(Run run) {
        return run.getZ();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < this.zCrit;
    }

    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/MannWithney.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setMaxWidth(1200);
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculated U-Test");
            stage.setScene(new Scene(root1));
            setLabeling();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}