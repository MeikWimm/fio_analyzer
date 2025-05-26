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
    private final Charter charter;
    private final List<XYChart.Data<Number, Number>> conIntData;
    private final boolean skip;
    @FXML
    public Label labelHeader;
    @FXML
    public Button drawConIntDiffButton;
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

    public ConInt(Job job, boolean skip, double alpha) {
        super(job, skip, 2, alpha);
        charter = new Charter();
        conIntData = new ArrayList<>();
        this.job = job;
        this.skip = skip;
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

        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("OverlappingDifference"));
        overlappingColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        drawConIntDiffButton.setOnAction(e -> draw());

        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getRuns());
    }

    public void draw() {
        charter.drawGraph("Overlapping Differnce of confidence intervals", "Run", "Overlapping difference (%)", "Overlapping Difference", 0, this.conIntData);
    }

    private double calculateOverlapp(Run run1, Run run2) {
        double overlap = Math.max(0, Math.min(run1.getIntervalTo(), run2.getIntervalTo()) - Math.max(run1.getIntervalFrom(), run2.getIntervalFrom()));
        double length = run1.getIntervalTo() - run1.getIntervalFrom() + run2.getIntervalTo() - run2.getIntervalFrom();

        return (1.0 - 2 * overlap / length) * 100;
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
        List<Run> runs = this.job.getRuns();

        int skipCount = 1;
        int ignoreLast = 1;
        if (skip) {
            skipCount = 2;
            ignoreLast = 2;
        }

            for (int i = 0; i < runs.size() - ignoreLast; i += skipCount) {
                double overlappingDiff = calculateOverlapp(runs.get(i), runs.get(i + 1));
                runs.get(i).setOverlappingDifference(overlappingDiff);
                runs.get(i + 1).setOverlappingDifference(Run.UNDEFINED_DOUBLE_VALUE);
                conIntData.add(new XYChart.Data<>(runs.get(i).getRunID(), overlappingDiff));

        }
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
            e.printStackTrace();
        }
    }
}
