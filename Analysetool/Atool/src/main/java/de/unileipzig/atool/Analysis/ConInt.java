/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.InputModule;
import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConInt (Confidence Interval)
 *
 * @author meni1999
 */
public class ConInt implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(InputModule.class.getName());
    private static final int jobRunCounter = 0;
    private static final double jobAlpha = -1.0;

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private final Job job;
    @FXML public Label labelHeader;
    @FXML public Button drawConIntDiffButton;
    @FXML public TableView<Run> conIntTable;
    @FXML public TableColumn<Run, Integer> runsColumn;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Double> intervalFromColumn;
    @FXML public TableColumn<Run, Double> intervalToColumn;
    @FXML public TableColumn<Run, Double> plusMinusValueColumn;
    @FXML public TableColumn<Run, Double> standardDeviationColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, String> overlappingColumn;
    private final Charter charter;
    private final Map<Integer, Double> conIntData;

    public ConInt(Job job) {
        charter = new Charter();
        conIntData = new HashMap<>();
        this.job = job;
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

        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));

        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("OverlappingDifferenceAsString"));

        drawConIntDiffButton.setOnAction(e -> drawOverlappingDiffernce(this.job));

        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getRunsCompacted());
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
            stage.setTitle("Calculate Confidence Interval");
            stage.setScene(new Scene(root1));
            stage.show();

        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ConInt! App state: %s", STATUS.IO_EXCEPTION));
        }
    }

    private void drawOverlappingDiffernce(Job job) {
        charter.drawGraph(job, "Overlapping Differnce of confidence intervals", "Run", "Overlapping difference (%)", "Overlapping Difference", conIntData, Run.UNDEFINED_DOUBLE_VALUE);
    }

    private double calculateOverlapp(Run run1, Run run2) {
        double overlap = Math.max(0, Math.min(run1.getIntervalTo(), run2.getIntervalTo()) - Math.max(run1.getIntervalFrom(), run2.getIntervalFrom()));
        double length = run1.getIntervalTo() - run1.getIntervalFrom() + run2.getIntervalTo() - run2.getIntervalFrom();

        return (1.0 - 2 * overlap / length) * 100;
    }
    /*
    private void checkOverlappingInterval(Job job){
        List<Run> runs = job.getRuns();
        
        for (Run run : runs) {
            int overlap = 0;
            for (Run comparedRun : runs) {
                if(!run.equals(comparedRun)){
                    if(Math.max (0, Math.min(run.getIntervalTo(), comparedRun.getIntervalTo()) - Math.max(run.getIntervalFrom(), run.getIntervalTo()) + 1) > 0){
                        overlap++;
                        run.setOverlapping(overlap);
                    }
                }
            }
        }
    }
        */

    public void calculateInterval() {
        /*
        if(jobRunCounter == this.job.getRunsCounter() && jobAlpha == this.job.getAlpha()) {
            return;
        } else {
            System.err.println("Job Change detected!");
        }
        */
        NormalDistribution normDis = new NormalDistribution();


        for (Run run : this.job.getRuns()) {
            double c1 = run.getAverageSpeed() - (normDis.inverseCumulativeProbability(1.0 - this.job.getAlpha() / 2.0) * (run.getStandardDeviation() / Math.sqrt(run.getData().size())));
            run.setIntervalFrom(c1);

            double c2 = run.getAverageSpeed() + (normDis.inverseCumulativeProbability(1.0 - this.job.getAlpha() / 2.0) * (run.getStandardDeviation() / Math.sqrt(run.getData().size())));
            run.setIntervalTo(c2);
        }
        List<Run> runs = this.job.getRuns();
        for (int i = 0; i < this.job.getRuns().size() - 1; i += Job.DEFAULT_SKIP_COUNT) {
            double overlappingDiff = calculateOverlapp(runs.get(i), runs.get(i + 1));
            runs.get(i).setOverlappingDifference(overlappingDiff);
            runs.get(i + 1).setOverlappingDifference(Run.UNDEFINED_DOUBLE_VALUE);
            conIntData.put(runs.get(i).getID(), overlappingDiff);
            //System.err.println(overlappingDiff);
        }


    }

    public enum STATUS {
        SUCCESS,
        IO_EXCEPTION
    }
}
