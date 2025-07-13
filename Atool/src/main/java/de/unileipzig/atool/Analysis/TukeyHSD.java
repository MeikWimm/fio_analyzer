/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;

import java.net.URL;
import java.util.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD extends PostHocTest implements Initializable {
    @FXML private Label labelHeader;
    @FXML private Label unitLabel;
    @FXML private Label qCritLabel;
    @FXML private Label anovaSteadyStateLabel;
    @FXML private Label tukeySteadyStateLabel;
    @FXML private Label evalLabel;

    @FXML private Button drawTukey;
    
    @FXML private TableView<Run> TukeyTable;
    @FXML private TableColumn<Run, Integer> runIDColumn;
    @FXML private TableColumn<Run, Integer> compareToRunColumn;
    @FXML private TableColumn<Run, Double> QColumn;
    @FXML private TableColumn<Run, Boolean> hypothesisColumn;
    private double qHSD;
    private final List<XYChart.Data<Number, Number>> meanData;

    public TukeyHSD(){
        super();
        this.meanData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        unitLabel.setText(Settings.getConversion());

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        //compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());

        Utils.CustomRunTableRowFactory menuItems = new Utils.CustomRunTableRowFactory();
        menuItems.addMenuItem("Show Run calculation", this::showTukeySections);
        TukeyTable.setRowFactory(menuItems);

        TukeyTable.setItems(this.getTest().getJob().getRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
        labelHeader.setText(getTest().getJob().toString());
    }

    public void showTukeySections(TableRow<Run> row, TableView<Run> table) {
        SectionWindow sectionWindow = new SectionWindow(row.getItem());
        sectionWindow.setShowQColumn(true);
        sectionWindow.openWindow();
    }

    public void draw(){
        charter.drawGraph("U-Test","Group","Mean/Difference","Q-HSD", qHSD,new Charter.ChartData("Run mean", meanData));
        charter.openWindow();
    }

    @Override
    public String getTestName() {
        return "Tukey-HSD";
    }

    @Override
    protected void initPostHocTest(Run run, GenericTest test, List<List<Section>> postHocGroups) {
        int totalObservations = run.getData().size();
        double n = run.getData().size();
        int numberOfGroups = postHocGroups.size();
        int dfError = totalObservations - numberOfGroups;
        Tukey tukey = new Tukey(postHocGroups.getFirst().size(), numberOfGroups, dfError);
        double standardError = Math.sqrt(run.getMSE() / n);
        double qCritical = tukey.inverse_survival(test.getAlpha(), false);
        qHSD = qCritical * standardError;
    }

    @Override
    protected double extractValue(Section section) {
        return 0;
    }

    @Override
    protected void calculateTest(List<Section> firstGroup, List<Section> secondGroup, List<Section> resultSections){
        Section section = firstGroup.getFirst();
        double overallMean = getOverallMean(firstGroup, secondGroup);

        section.setQ(overallMean);
        resultSections.add(section);

        //meanData.add(new XYChart.Data<>(run.getGroupID(), overallMean));
    }

    private double getOverallMean(List<Section> group1, List<Section> group2) {
        double speedSumGroup1 = 0;
        double speedSumGroup2 = 0;

        for (Section s : group1) {
            speedSumGroup1 += s.getAverageSpeed();
        }

        for (Section s : group2) {
            speedSumGroup2 += s.getAverageSpeed();
        }

        double averageSpeedGroup1 = speedSumGroup1 / group1.size();
        double averageSpeedGroup2 = speedSumGroup2 / group2.size();

        return Math.abs(averageSpeedGroup1 - averageSpeedGroup2);
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < qHSD;
    }

    protected void setLabeling() {
        if(steadyStateRun == null && steadyStateSection == null){
            evalLabel.setText("No steady state run found.");
            return;
        }

        anovaSteadyStateLabel.setText("Run " + steadyStateSection.getID());

        if(steadyStateRun == null && steadyStateSection != null){
            evalLabel.setText("Run " + steadyStateSection.getID() + " most likely in transient state.");
        }

        if(steadyStateRun != null){
            if(steadyStateRun.getID() != steadyStateSection.getID()){
                tukeySteadyStateLabel.setText("Run " + steadyStateRun.getID());
                evalLabel.setText("Run " + steadyStateRun.getID() + " more likely in steady state than Run " + steadyStateSection.getID() + ".");
            } else {
                tukeySteadyStateLabel.setText("Run " + steadyStateRun.getID());
                evalLabel.setText("Run " + steadyStateRun.getID() + " is in steady state.");
            }
        }
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/TukeyHSD.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated Tukey HSD";
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("U-Test","Group","Mean/Difference","Q-HSD", qHSD,new Charter.ChartData("Run mean", meanData));
    }

    @Override
    public double getCriticalValue() {
        return this.qHSD;
    }

    @Override
    public TableView<Run> getTable() {
        return TukeyTable;
    }

}