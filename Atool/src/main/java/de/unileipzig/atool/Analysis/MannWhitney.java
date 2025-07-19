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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import java.net.URL;
import java.util.*;


/**
 * @author meni1999
 */
public class MannWhitney extends GenericTest implements Initializable {
    private static class RankedDataPoint extends DataPoint {
        int flag;
        double rank;

        public RankedDataPoint(DataPoint dp, int rank, int flag) {
            super(dp.data, dp.time);
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

    private final List<XYChart.Data<Number, Number>> uTestData;

    @FXML public Label labelHeader;

    @FXML public TableView<Section> uTestTable;
    @FXML public TableColumn<Section, Double> averageSpeedColumn;
    @FXML public TableColumn<Section, Integer> runIDColumn;
    @FXML public TableColumn<Section, Integer> compareToRunColumn;
    @FXML public TableColumn<Section, Double> ZColumn;
    @FXML public TableColumn<Section, Boolean> hypothesisColumn;
    @FXML public Button drawUTestButton;
    @FXML public Label steadyStateLabel;

    private double zCrit;
    private static final int GROUP_SIZE = 2;

    public MannWhitney(Job job,Settings settings) {
        super(job, job.getSkipSeconds(), false, GROUP_SIZE, job.getAlpha(), settings.getRequiredRunsForSteadyState());
        this.uTestData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setText("Average Speed " + Settings.getConversion());
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        ZColumn.setCellValueFactory(new PropertyValueFactory<>("P"));
        ZColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        uTestTable.setItems(getResultRuns());
        labelHeader.setText(this.job.toString());

        drawUTestButton.setOnAction(e -> draw());
    }

    @Override
    protected void setLabeling() {
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


    @Override
    public String getTestName() {
        return "Mann-Whitney";
    }

    @Override
    public double getCriticalValue() {
        return this.zCrit;
    }

    @Override
    protected void calculateTest(List<List<Section>> groups, List<Section> resultSections) {
        if (this.job.getRuns().size() <= 1) return;
        MannWhitneyUTest uTest = new MannWhitneyUTest();
        for (List<Section> group : groups) {
            Section section1 = group.getFirst();
            Section section2 = group.get(1);
            double[] data1 = section1.getData().stream().mapToDouble(dp -> dp.data).toArray();
            double[] data2 = section2.getData().stream().mapToDouble(dp -> dp.data).toArray();


            double pValue = uTest.mannWhitneyUTest(data1, data2);
            group.getFirst().setP(pValue);
            uTestData.add(new XYChart.Data<>(group.getFirst().getID(), pValue));
            resultSections.add(group.getFirst());
        }
    }

    @Override
    protected double extractValue(Section section) {
        return section.getP();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < getAlpha();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("U-Test", "Section ID", "P-Value","Alpha level", getAlpha(), new Charter.ChartData("calculated P", uTestData));
    }

    public void draw() {
        charter.drawGraph("U-Test", "Section ID", "P-Value","Alpha level", getAlpha(), new Charter.ChartData("calculated P", uTestData));
        charter.openWindow();
    }

    @Override
    public TableView<Section> getTable() {
        return uTestTable;
    }
}