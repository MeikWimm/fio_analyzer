/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import de.unileipzig.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD extends PostHocTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger( TukeyHSD.class.getName() );

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("TukeyHSD"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }

    @FXML public Label qCritLabel;
    @FXML public Label anovaSteadyStateLabel;
    @FXML public Label tukeySteadyStateLabel;
    @FXML public Label evalLabel;

    @FXML public Button drawTukey;
    
    @FXML public TableView<Run> TukeyTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> QColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private double qHSD;
    private final List<XYChart.Data<Number, Number>> meanData;
    private final List<XYChart.Data<Number, Number>> qHSDData;

    public TukeyHSD(Anova anova){
        super(anova);
        this.meanData = new ArrayList<>();
        this.qHSDData = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("GroupID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());
        
        TukeyTable.setItems(this.test.getPostHocRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
    }

    public void draw(){
        charter.drawGraph("U-Test","Group","Mean/Difference",new Charter.ChartData("Run mean", meanData), new Charter.ChartData("QHSD", qHSDData));
        charter.openWindow();
    }

    @Override
    public String getTestName() {
        return "Tukey-HSD";
    }

    @Override
    public void apply(List<Run> postHocRuns, List<List<Run>> postHocGroups) {
        if (postHocRuns == null || postHocGroups == null) {
            throw new IllegalArgumentException("Input lists cannot be null");
        }

        int totalObservations = test.getJob().getData().size();
        for (int i = 0; i <= postHocGroups.size() - 2; i += 2) {
                Job job = test.getJob();
                double n = job.getData().size();
                int numberOfGroups = postHocGroups.size();
                int dfError = totalObservations - numberOfGroups;
                Tukey tukey = new Tukey(postHocGroups.getFirst().size(), numberOfGroups, dfError);
                List<Run> group1 = postHocGroups.get(i);
                List<Run> group2 = postHocGroups.get(i + 1);

            if (group1.isEmpty() || group2.isEmpty()) {
                throw new IllegalArgumentException("Groups cannot be empty");
            }

            Run run = group1.getFirst();
                double speedSumGroup1 = 0;
                double speedSumGroup2 = 0;

                for (Run r : group1) {
                    speedSumGroup1 += r.getAverageSpeed();
                }

                for (Run r : group2) {
                    speedSumGroup2 += r.getAverageSpeed();
                }

                double averageSpeedGroup1 = speedSumGroup1 / group1.size();
                double averageSpeedGroup2 = speedSumGroup2 / group2.size();

                double standardError = Math.sqrt(job.getMSE() / n);
                double qCritical = tukey.inverse_survival(test.getAlpha(), false);
                double overallMean = Math.abs(averageSpeedGroup1 - averageSpeedGroup2);
                qHSD = qCritical * standardError;
                run.setQ(overallMean);

                meanData.add(new XYChart.Data<>(run.getGroupID(), overallMean));
                qHSDData.add(new XYChart.Data<>(run.getGroupID(), qHSD));
                if(qHSD < overallMean){
                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
                } else {
                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                }

                checkSteadyStateRun(run, group1, group2);
        }
    }

    @Override
    protected void setLabeling() {
        if(steadyStateRun == null && anovaSteadyStateRun == null){
            evalLabel.setText("No steady state run found.");
            return;
        }

        anovaSteadyStateLabel.setText("Run " + anovaSteadyStateRun.getID());

        if(steadyStateRun == null && anovaSteadyStateRun != null){
            evalLabel.setText("Run " + anovaSteadyStateRun.getID() + " most likely in transient state.");
        }

        if(steadyStateRun != null){
            if(steadyStateRun.getID() != anovaSteadyStateRun.getID()){
                tukeySteadyStateLabel.setText("Run " + steadyStateRun.getID());
                evalLabel.setText("Run " + steadyStateRun.getID() + " more likely in steady state than Run " + anovaSteadyStateRun.getID() + ".");
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
        return charter.drawGraph("U-Test","Group","Mean/Difference",new Charter.ChartData("Run mean", meanData), new Charter.ChartData("QHSD", qHSDData));
    }
}