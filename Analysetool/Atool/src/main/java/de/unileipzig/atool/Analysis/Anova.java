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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.FDistribution;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author meni1999
 */
public class Anova extends GenericTest implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(Anova.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    private final Map<Integer, Double> anovaData;
    private final Map<Integer, Double> covData;
    private final Charter charter;
    //private List<List<Run>> significantRuns;
    @FXML public Label averageSpeedLabel;
    @FXML public Label sseLabel;
    @FXML public Label ssaLabel;
    @FXML public Label sstLabel;
    @FXML public Label ssaSstLabel;
    @FXML public Label sseSstLabel;
    @FXML public Label fCriticalLabel;
    @FXML public Label fCalculatedLabel;
    @FXML public Button showFGraphButton;
    @FXML public Button showCoVGraph;
    @FXML public Pane anovaPane;
    @FXML public TableView<Run> anovaTable;
    @FXML public TableColumn<Run, Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Double> covColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, Double> FColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private double fCrit;

    public Anova(Job job, boolean skip, int groupSize, double alpha) {
        super(job, skip, groupSize, alpha);
        this.charter = new Charter();
        this.anovaData = new HashMap<>();
        this.covData = new HashMap<>();


    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("CoV"));
        covColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));

        FColumn.setCellValueFactory(new PropertyValueFactory<>("F"));
        FColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        anovaTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Run run = anovaTable.getSelectionModel().getSelectedItem();
                if(run != null){
                    setLabeling(run);
                }
            }
        });

        showCoVGraph.setOnAction(e -> drawCoVGraph(this.job));
        showFGraphButton.setOnAction(e -> drawANOVAGraph(this.job));
        anovaTable.setItems(this.job.getRuns());

    }

    private void setLabeling(Run run) {
        averageSpeedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getAverageSpeed()));
        sseLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSE()));
        ssaLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSA()));
        sstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSST()));
        ssaSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (run.getSSA() / run.getSST())));
        sseSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, (run.getSSE() / run.getSST())));
        fCriticalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.fCrit));
        fCalculatedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getF()));
    }

    @Override
    public void calculate() {

        int num = this.groups.size() - 1;
        int denom = (num + 1) * (this.job.getRunDataSize() - 1);
        FDistribution fDistribution = new FDistribution(num, denom);
        fCrit = fDistribution.inverseCumulativeProbability(1.0 - alpha);
        double sse = 0.0;
        double ssa = 0.0;
        //int num = job.getGroupSize() - 1;
        //int denom = (num + 1) * (job.getRunDataSize() - 1);

//      calculate F value between runs
//      SSA
//        for (Run run : this.job.getRuns()) {
//            for (Run runToCompare : run.getRunToCompareTo()) {
//                double averageSpeedOfRun = runToCompare.getAverageSpeed();
//                double averageSpeedOfAllComparedRuns = run.getAverageSpeedOfRunsToCompareTo();
//                ssa += Math.pow(averageSpeedOfRun - averageSpeedOfAllComparedRuns, 2);
//            }
//            ssa *= run.getData().size();
//            run.setSSA(ssa);
//            ssa = 0;
//        }


        for (List<Run> group : this.groups) {
            for (Run run : group) {
                double averageSpeedOfRun = run.getAverageSpeed();
                ssa += Math.pow(averageSpeedOfRun - this.getGroupAverageSpeed(group), 2);
                ssa *= run.getData().size();
                run.setSSA(ssa);
                ssa = 0;
            }
        }

        //SSE
//        for (Run run : this.job.getRuns()) {
//            for (Run runToCompare : run.getRunToCompareTo()) {
//                for (DataPoint dp : runToCompare.getData()) {
//                    sse += (Math.pow((dp.getSpeed() - run.getAverageSpeedOfRunsToCompareTo()), 2));
//                }
//            }
//
//            run.setSSE(sse);
//            sse = 0;
//        }

        for (List<Run> group : this.groups) {
            for (Run run : group) {
                for (DataPoint dp : run.getData()) {
                    sse += (Math.pow((dp.getSpeed() - this.getGroupAverageSpeed(group)), 2));
                }
                run.setSSE(sse);
                sse = 0;
            }
        }

        double fValue;
        double cov;
        for (List<Run> group : this.groups) {
            Run run = group.getFirst();
            cov = calcualteCoV(group);
            double s_2_a = run.getSSA() / (this.groups.size() - 1);
            double s_2_e = run.getSSE() / (this.groups.size() * (run.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            run.setCoV(cov);
            run.setF(fValue);
            run.setP(1.0 - fDistribution.cumulativeProbability(fValue));

            anovaData.put(run.getID(), fValue);
            covData.put(run.getID(), cov);
            if (this.fCrit < fValue) {
                run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            } else {
                run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                resultGroups.add(group);
            }
        }

        //Post-Hoc
//        for (List<Run> group : this.job.getGroups()) {
//            Run firstRun = new Run(group.getFirst());
//            if(firstRun.getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
//                significantRuns.add(group);
//            }
//        }

//        for (Run run : this.job.getRuns()) {
//            double s_2_a = run.getSSA() / (this.job.getGroupSize() - 1);
//            double s_2_e = run.getSSE() / (this.job.getGroupSize() * (run.getData().size() - 1));
//            fValue = s_2_a / s_2_e;
//            if (!job.getGroups().isEmpty()) {
//                // critical p-value < alpha value of job
//                double cov = calcualteCoV(run);
//                if (this.fCrit < fValue) {
//                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
//                } else {
//                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
//                    significantRuns.add(new SigRunData(run.getRunToCompareTo(), run.getSSE()));
//                }
//                run.setCoV(cov);
//                run.setF(fValue);
//                anovaData.put(run.getID(), fValue);
//                covData.put(run.getID(), cov);
//            } else {
//                run.setNullhypothesis(Run.UNDEFIND_NULLHYPOTHESIS);
//                run.setCoV(Run.UNDEFINED_DOUBLE_VALUE);
//            }
//        }
//        LOGGER.log(Level.INFO, "SSA, SSE, and F-values before calculation:");
//        for (List<Run> group : this.groups) {
//            for (Run run : group) {
//                LOGGER.log(Level.INFO, String.format("Run %d | Avg: %.2f | SSE: %.2f | SSA: %.2f",
//                        run.getID(), run.getAverageSpeed(), run.getSSE(), run.getSSA()));
//            }
//        }
        //return significantRuns;
        LOGGER.log(Level.INFO, String.format("Calculated Numerator %d and Denominator %d", num, denom));
    }

    private double getGroupAverageSpeed(List<Run> group) {
        double sum = 0.0;
        for (Run run : group) {
            sum += run.getAverageSpeed();
        }
        return sum / group.size();
    }

    private double calcualteCoV(List<Run> group) {
        double jobStandardDeviation = this.job.getStandardDeviation();
        double sum = 0;
        for (Run run : group) {
            sum += run.getAverageSpeed();
        }
        double average = sum / group.size();
        return jobStandardDeviation / average;
    }

    public void drawANOVAGraph(Job job) {
        charter.drawGraph(job, "ANOVA", "Run", "F-Value", "calculated F", anovaData, this.fCrit);
    }

    public void drawCoVGraph(Job job) {
        charter.drawGraph(job, "Coefficent of Variation", "Run", "CoV", "calculated CoV (%)", covData, Run.UNDEFINED_DOUBLE_VALUE);
    }

    public final void openWindow() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/Anova.fxml"));
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
            stage.setTitle("Calculated ANOVA");
            stage.setScene(new Scene(root1));
            System.out.println(this.job);
            stage.show();
        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ANOVA! App state: %s", ConInt.STATUS.IO_EXCEPTION));
        }
    }

    public boolean shouldResetHypotheses() {
        if(App.DEBUG_MODE && this.getClass() != Anova.class){
                LOGGER.log(Level.WARNING, String.format("Subclass %s should overwrite shouldResetHypotheses()", this.getClass().getSimpleName()));
        }
        return false;
    }
}
