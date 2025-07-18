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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.commons.math3.distribution.FDistribution;

import java.net.URL;
import java.util.*;

/**
 * @author meni1999
 */
public class Anova extends GenericTest implements Initializable {

    private final List<XYChart.Data<Number, Number>> anovaData;
    @FXML private Label jobLabel;
    @FXML private Label averageSpeedLabel;
    @FXML private Label sseLabel;
    @FXML private Label ssaLabel;
    @FXML private Label sstLabel;
    @FXML private Label ssaSstLabel;
    @FXML private Label sseSstLabel;
    @FXML private Label fCriticalLabel;
    @FXML private Label fCalculatedLabel;
    @FXML private Button showFGraphButton;
    @FXML private Button showCoVGraph;
    @FXML private Button showWinCoVGraph;
    @FXML private Label sigmaJobLabel;
    @FXML private Label steadyStateLabel;

    @FXML private TableView<Section> anovaTable;
    @FXML private TableColumn<Section, Double> averageSpeedColumn;
    @FXML private TableColumn<Section, Integer> runIDColumn;
    @FXML private TableColumn<Section, Double> startTimeColumn;
    @FXML private TableColumn<Section, String> compareToRunColumn;
    @FXML private TableColumn<Section, Double> FColumn;
    @FXML private TableColumn<Section, Boolean> hypothesisColumn;
    private double fCrit;
    private static final int GROUP_SIZE = 3;


    public Anova(Job job, Settings settings) {
        super(job, job.getSkipSeconds(), false, GROUP_SIZE, job.getAlpha() ,settings.isBonferroniSelected(), settings.getRequiredRunsForSteadyState());
        final int dataSize = job.getData().size();
        this.anovaData = new ArrayList<>(dataSize);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));

        FColumn.setCellValueFactory(new PropertyValueFactory<>("F"));
        FColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        anovaTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Section section = anovaTable.getSelectionModel().getSelectedItem();
                if (section != null) {
                    updateLabeling(section);
                }
            }
        });

        showFGraphButton.setOnAction(e -> drawANOVAGraph());
        jobLabel.setText(this.job.toString());
        anovaTable.setItems(getResultRuns());
    }

    private void updateLabeling(Section section) {
        String averageSpeedLabelText = String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, section.getAverageSpeed());
        averageSpeedLabel.setText(String.format(Locale.ENGLISH, "%s %s", averageSpeedLabelText, Settings.getConversion()));
        sseLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, section.getSSE()));
        ssaLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, section.getSSA()));
        sstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, section.getSST()));
        ssaSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (section.getSSA() / section.getSST())));
        sseSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (section.getSSE() / section.getSST())));
        fCalculatedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, section.getF()));
        if(this.getSteadyStateRun() == null){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + this.getSteadyStateRun().getID());
        }
    }

    @Override
    protected void setLabeling() {
        fCriticalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.fCrit));
        sigmaJobLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.job.getStandardDeviation()));
        if(this.possibleSteadyStateRunsGroup.isEmpty()){
            steadyStateLabel.setText("No steady state run found.");
        } else {
            steadyStateLabel.setText("at run " + this.possibleSteadyStateRunsGroup.getFirst().getFirst().getID());
        }
    }

    @Override
    protected void calculateTest(List<List<Section>> groups, List<Section> resultSections) {
        for (List<Section> group : groups) {
            int num = group.size() - 1;
            int denom = (num + 1) * (group.getFirst().getData().size() - 1);
            FDistribution fDistribution = new FDistribution(num, denom);
            fCrit = fDistribution.inverseCumulativeProbability(1.0 - alpha);
            double sse = 0.0;
            double ssa = 0.0;


            for (Section section : group) {
                    double averageSpeedOfRun = section.getAverageSpeed();
                    ssa += Math.pow(averageSpeedOfRun - MathUtils.average(group), 2);
                    ssa *= section.getData().size();
                    section.setSSA(ssa);
                    ssa = 0;
            }

            for (Section section : group) {
                for (DataPoint dp : section.getData()) {
                    sse += (Math.pow((dp.data - MathUtils.average(group)), 2));
                }
                section.setSSE(sse);
                sse = 0;
            }

            double fValue;

            double totalSSE = 0;
            for (Section section : group) {
                totalSSE += section.getSSE();
            }

            Section section = group.getFirst();
            double s_2_a = section.getSSA() / (groups.size() - 1);
            double s_2_e = section.getSSE() / (groups.size() * (section.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            section.setF(fValue);
            section.setSSE(totalSSE);
            section.setMSE(totalSSE / denom);


            resultSections.add(group.getFirst());
            anovaData.add(new XYChart.Data<>(group.getFirst().getID(), fValue));
        }

    }

    @Override
    protected double extractValue(Section section) {
        return section.getF();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value > this.fCrit;
    }

    public double getCriticalValue() {
        return fCrit;
    }

    @Override
    public String getTestName() {
        return "ANOVA";
    }

    public TableView<Section> getTable() {
        return anovaTable;
    }

    private void drawANOVAGraph() {
        charter.drawGraph("ANOVA", "Section ID", "F-Value", "Critical value", this.fCrit, new Charter.ChartData("calculated F", anovaData));
        charter.openWindow();
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("ANOVA", "Section ID", "F-Value", "Critical value", this.fCrit, new Charter.ChartData("calculated F", anovaData));
    }

    @Override
    protected URL getFXMLPath() {
        return getClass().getResource("/de/unileipzig/atool/Anova.fxml");
    }

    @Override
    protected String getWindowTitle() {
        return "Calculated Anova";
    }
}
