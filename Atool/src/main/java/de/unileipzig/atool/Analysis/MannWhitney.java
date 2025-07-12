/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;


/**
 * @author meni1999
 */
public class MannWhitney extends GenericTest implements Initializable {
    private final List<XYChart.Data<Number, Number>> uTestData;

    @FXML public Label labelHeader;

    @FXML public TableView<Run> uTestTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> ZColumn;
    @FXML public TableColumn<Run, Boolean> hypothesisColumn;
    @FXML public Button drawUTestButton;
    @FXML public Label zIntervalLabel;
    @FXML public Label steadyStateLabel;

    private double zCrit;
    private final Job job;

    public MannWhitney(Job job,Settings settings) {
        super(job, settings.getUTestSkipRunsCounter(), settings.isUTestUseAdjacentRun(), 2, job.getAlpha(), settings.isBonferroniUTestSelected(), settings.getRequiredRunsForSteadyState());
        this.uTestData = new ArrayList<>();
        this.job = getJob();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setText("Average Speed " + Settings.getConversion());
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        ZColumn.setCellValueFactory(new PropertyValueFactory<>("Z"));
        ZColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        uTestTable.setItems(this.job.getRuns());
        labelHeader.setText(this.job.toString());
        Utils.CustomRunTableRowFactory menuItems = new Utils.CustomRunTableRowFactory();

        menuItems.addMenuItem("Show Run calculation", this::showMannWhitneySections);

        uTestTable.setRowFactory(menuItems);
        drawUTestButton.setOnAction(e -> draw());
    }

    public void showMannWhitneySections(TableRow<Run> row, TableView<Run> table) {
        Logging.log(Level.INFO, "U-Test", "Showing sections for run " + row.getItem().getRunID());
        for (Section section : row.getItem().getSections()) {
            Logging.log(Level.INFO, "U-Test", section.toString());
        }
    }

    @Override
    protected void setLabeling() {
        zIntervalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.zCrit));
        if(this.getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + getSteadyStateRun().getID() + " | time: " + getSteadyStateRun().getStartTime());
        }
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/MannWithney.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated U-Test";
    }

    public void draw() {
        charter.drawGraph("U-Test", "Run", "Z-Value","z-critical", this.zCrit, new Charter.ChartData("calculated Z", uTestData));
        charter.openWindow();
    }

    @Override
    public String getTestName() {
        return "Mann-Whitney";
    }

    private void calculatePair(Run run, Section run1, Section run2, List<Section> resultSections) {
        List<DataPoint.RankedDataPoint> rankedData1 = new ArrayList<>();
        List<DataPoint.RankedDataPoint> rankedData2 = new ArrayList<>();
        NormalDistribution n = new NormalDistribution();

        int size = run1.getData().size();
        for (int i = 0; i < size; i++) {
            rankedData1.add(new DataPoint.RankedDataPoint(run1.getData().get(i), 0, 0));
            rankedData2.add(new DataPoint.RankedDataPoint(run2.getData().get(i), 0, 1));
        }

        List<DataPoint.RankedDataPoint> mergedData = new ArrayList<>(rankedData1);
        mergedData.addAll(rankedData2);


        mergedData.sort(new Utils.SpeedComparator());

        double r = 1;
        int counter = 1;
        double new_speed, next_speed = -1;
        int index = 0;
        int jindex = 0;
        for (DataPoint.RankedDataPoint p : mergedData) {
            new_speed = p.data;
            if (jindex < mergedData.size() - 1) {
                next_speed = mergedData.get(jindex + 1).data;
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

        for (DataPoint.RankedDataPoint dataPoint : mergedData) {
            if (dataPoint.flag == 0) {
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

        run1.setP(pCalc);
        run1.setZ(z);
        //uTestData.add(new XYChart.Data<>(run1.getRunID(), z));
        resultSections.add(run1);
    }

    @Override
    public double getCriticalValue() {
        return this.zCrit;
    }

    @Override
    protected void calculateTest(List<List<Run>> groups, List<Run> resultRuns) {
        if (this.job.getRuns().size() <= 1) return;
        List<Run> runs = this.job.getRuns();

        for (int i = 0; i < runs.size(); i ++) {
            if (i < runs.size() - 1) {
                Run run1 = runs.get(i);
                Run run2 = runs.get(i + 1);
                //calculatePair(resultRuns, run1, run2, resultSections);
            }
        }
    }

    @Override
    protected void calculateTest(Run run, List<Section> resultSections) {
        if (run.getSections().size() <= 1) return;
        List<Section> sections = run.getSections();

        for (int i = 0; i < sections.size(); i ++) {
            if (i < sections.size() - 1) {
                Section run1 = sections.get(i);
                Section run2 = sections.get(i + 1);
                calculatePair(run, run1, run2, resultSections);
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

    @Override
    protected double extractValue(Section section) {
        return section.getZ();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("U-Test", "Run", "Z-Value","z-critical", this.zCrit, new Charter.ChartData("calculated Z", uTestData));
    }

    @Override
    public TableView<Run> getTable() {
        return uTestTable;
    }
}