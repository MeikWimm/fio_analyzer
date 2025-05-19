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
public class MannWhitney implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MannWhitney.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Mann-Whitney"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    @FXML public TableView<Run> uTestTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, String> ZColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    @FXML public Button drawUTestButton;
    @FXML public Label zIntervalLabel;
    NormalDistribution nDis = new NormalDistribution();
    private double zCrit_rightside = -1;
    private final Map<Integer, Double> uTestData;
    private final Charter charter;

    private final Job job;

    public MannWhitney(Job job) {
        nDis = new NormalDistribution();
        zCrit_rightside = nDis.inverseCumulativeProbability(1 - job.getAlpha() / 2.0);
        this.job = job;
        this.job.clearRuns();
        this.charter = new Charter();
        this.uTestData = new HashMap<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("PairwiseRunToCompareToAsString"));
        ZColumn.setCellValueFactory(new PropertyValueFactory<>("ZAsString"));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        uTestTable.setItems(this.job.getRuns());

        drawUTestButton.setOnAction(e -> drawUTest(this.job));
        setLabeling();
    }

    private void setLabeling() {
        zIntervalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.zCrit_rightside));
    }

    private void drawUTest(Job job) {
        charter.drawGraph(job, "U-Test", "Run", "Z-Value", "calculated Z-Value", uTestData, zCrit_rightside);
    }

    private void calculateMannWhitney(Run run1, Run run2) {
        List<DataPoint> runData1 = run1.getData();
        List<DataPoint> runData2 = run2.getData();
        for (int i = 0; i < runData1.size(); i++) {
            runData1.get(i).setFlag(0);
            runData2.get(i).setFlag(1);
        }

        List<DataPoint> mergedData = new ArrayList<>(runData1);
        mergedData.addAll(runData2);


        mergedData.sort(new Utils.SpeedComparator());

        double r = 1;
        int counter = 1;
        double new_speed, next_speed = -1;
        int index = 0;
        int jindex = 0;
        for (DataPoint p : mergedData) {
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
                for (int i = index; i < index + counter; i++) {
                    double splitted_rank = Math.floor(((1.0 / (double) counter)) * 100.0) / 100.0;
                    mergedData.get(i).setRank(r + splitted_rank);
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

        for (DataPoint dataPoint : mergedData) {
            if (dataPoint.getFlag() == 0) {
                run1_ranksum += dataPoint.getRank();
            } else {
                run2_ranksum += dataPoint.getRank();
            }
        }

        double m = mergedData.size() / 2.0;
        //.err.println("Rank Sum 1: " + run1_ranksum + " m: " + m);
        //double z_1 = (run1_ranksum - 0.5 * m * (2.0 * m + 1.0)) / (Math.sqrt((1.0/12.0) * Math.pow(m, 2) * (2.0 * m + 1.0)));
        //double z_2 = (run2_ranksum - 0.5 * m * (2.0 * m + 1.0)) / (Math.sqrt((1.0/12.0) * Math.pow(m, 2) * (2.0 * m + 1.0)));

        // System.err.println("Z_1: " + z_1 + " | Z_2: " + z_2);

        double U1 = m * m + ((m * (m + 1) / 2)) - run1_ranksum;
        double U2 = m * m + ((m * (m + 1) / 2)) - run2_ranksum;
        double mu_U = m * m * 0.5;
        double sigma_U = Math.sqrt((m * m * (2 * m + 1)) / 12.0);
        double U = Math.min(U1, U2);
        double z = Math.abs((U - mu_U) / sigma_U);


        run1.setZ(z);
        run2.setZ(Run.UNDEFINED_VALUE);
        run2.setNullhypothesis(Run.UNDEFIND_NULLHYPOTHESIS);
        NormalDistribution n = new NormalDistribution();

        uTestData.put(run1.getID(), z);

        double pCalc = n.cumulativeProbability(z);

        if (pCalc > 1 - this.job.getAlpha() / 2.0) {
            run1.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
        } else {
            run1.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
        }
    }


    public void calculateMannWhitneyTest() {

        if (this.job.getRuns().size() <= 1) return;
        /*
        if(jobRunCounter == this.job.getRunsCounter() && jobAlpha == this.job.getAlpha()) {
            return;
        } else {
            System.err.println("Job Change detected!");
        } 
        */
        List<Run> runs = this.job.getRuns();

        for (int i = 0; i < runs.size(); i += 2) {
            if (i < runs.size() - 1) {
                Run run1 = runs.get(i);
                Run run2 = runs.get(i + 1);
                calculateMannWhitney(run1, run2);
            }
        }
    }

    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/MannWithney.fxml"));
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
            stage.setTitle("Calculated U-Test");
            stage.setScene(new Scene(root1));
            stage.show();

        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            //LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
        }
    }
}
