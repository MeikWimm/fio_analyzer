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
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

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
    private final double pCrit = 0.05;

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
        //compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        ZColumn.setCellValueFactory(new PropertyValueFactory<>("AcceptedSectionsRate"));
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
        SectionWindow sectionWindow = new SectionWindow(row.getItem());
        sectionWindow.setShowZColumn(true);
        sectionWindow.openWindow();
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

    private void calculatePair(Run run, Section section1, Section section2, List<Section> resultSections) {
        NormalDistribution n = new NormalDistribution();

        double[] data1 =  new double[section1.getData().size()];
        double[] data2 =  new double[section2.getData().size()];

        for(int i = 0; i < section1.getData().size(); i++){
            data1[i] = section1.getData().get(i).data;
            data2[i] = section2.getData().get(i).data;
        }

        MannWhitneyUTest uTest = new MannWhitneyUTest();
        double p = uTest.mannWhitneyUTest(data1, data2);


        double calcZ = n.inverseCumulativeProbability(p);

        section1.setZ(p);
        section1.setP(p);
        resultSections.add(section1);
        uTestData.add(new XYChart.Data<>(section1.getID(), p));
    }

    @Override
    public double getCriticalValue() {
        return this.zCrit;
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
    protected boolean isWithinThreshold(double value) {
        return value < this.pCrit;
    }

    @Override
    protected double extractValue(Section section) {
        return section.getP();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("U-Test", "Run", "calculated p-Value","p-critical", 0.05, new Charter.ChartData("calculated P", uTestData));
    }

    @Override
    public TableView<Run> getTable() {
        return uTestTable;
    }
}