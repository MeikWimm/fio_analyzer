/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.collections.FXCollections;
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
public class Anova implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(Anova.class.getName());

    public record SigRunData(List<Run> comparedRuns, double sse){}

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }


    private final Job job;
    private final Map<Integer, Double> anovaData;
    private final Map<Integer, Double> covData;
    private final Charter charter;
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
    @FXML public TableColumn<Run, String> covColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, String> FColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    //private static String jobCode = "";
    private double fCrit;
    private final List<SigRunData> significantRuns;

    public Anova(Job job) {
        this.job = new Job(job);

        this.charter = new Charter();
        this.anovaData = new HashMap<>();
        this.covData = new HashMap<>();
        this.significantRuns = new ArrayList<>();
        FDistribution fDistribution;
        if (job.getRuns().getFirst().getRunToCompareTo().size() <= 1) {
            fDistribution = new FDistribution(1, 1);
        } else {
            int num = job.getRuns().getFirst().getRunToCompareTo().size() - 1;
            //System.err.println("run data size: " + job.getRunDataSize());
            int denom = (num + 1) * (job.getRunDataSize() - 1);
            //if(denom == 0 ||num == 0) return;
            LOGGER.log(Level.INFO, String.format("Calculated Numerator %d and Denominator %d", num, denom));
            fDistribution = new FDistribution(num, denom);
            fCrit = fDistribution.inverseCumulativeProbability(1.0 - job.getAlpha());
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));

        covColumn.setCellValueFactory(new PropertyValueFactory<>("CoVAsString"));

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        FColumn.setCellValueFactory(new PropertyValueFactory<>("FAsString"));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());



        anovaTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                setLabeling(anovaTable.getSelectionModel().getSelectedItem());
            }
        });

        showCoVGraph.setOnAction(e -> drawCoVGraph(this.job));
        showFGraphButton.setOnAction(e -> drawANOVAGraph(this.job));
        anovaTable.setItems(FXCollections.observableList(this.job.getRuns()));
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

    public List<SigRunData> calculateANOVA() {
        if (job.getRuns().size() <= 1) return null;
        /*
        if(job.getCode().equals(jobCode)) {
            return;
        } else {
            LOGGER.log(Level.INFO,String.format("Change detected for %s", this.job));
        }
        */
        double sse = 0.0;
        double ssa = 0.0;
        int num = job.getRuns().getFirst().getRunToCompareTo().size() - 1;
        int denom = (num + 1) * (job.getRunDataSize() - 1);
        LOGGER.log(Level.INFO, String.format("Calculated Numerator %d and Denominator %d", num, denom));

//      calculate F value between runs
//      SSA 
        for (Run run : this.job.getRuns()) {
            for (Run runToCompare : run.getRunToCompareTo()) {
                double averageSpeedOfRun = runToCompare.getAverageSpeed();
                double averageSpeedOfAllComparedRuns = run.getAverageSpeedOfRunsToCompareTo();
                ssa += Math.pow(averageSpeedOfRun - averageSpeedOfAllComparedRuns, 2);
            }
            ssa *= run.getData().size();
            run.setSSA(ssa);
            ssa = 0;
        }

        //SSE
        for (Run run : this.job.getRuns()) {
            for (Run runToCompare : run.getRunToCompareTo()) {
                for (DataPoint dp : runToCompare.getData()) {
                    sse += (Math.pow((dp.getSpeed() - run.getAverageSpeedOfRunsToCompareTo()), 2));
                }
            }

            run.setSSE(sse);
            sse = 0;
        }

        double fValue;
        for (Run run : this.job.getRuns()) {
            double s_2_a = run.getSSA() / (run.getRunToCompareTo().size() - 1);
            double s_2_e = run.getSSE() / (run.getRunToCompareTo().size() * (run.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            run.setF(fValue);
            if (!run.getRunToCompareTo().isEmpty()) {
                // critical p-value < alpha value of job
                double cov = calcualteCoV(run);
                if (this.fCrit < fValue) {
                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
                } else {
                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                    significantRuns.add(new SigRunData(run.getRunToCompareTo(), run.getSSE()));
                }
                anovaData.put(run.getID(), fValue);
                covData.put(run.getID(), cov);
            } else {
                run.setNullhypothesis(Run.UNDEFIND_NULLHYPOTHESIS);
                run.setCoV(Run.UNDEFINED_DOUBLE_VALUE);
            }
        }
    return significantRuns;
        //calculateSteadyState();

        // remember run counter and alpha to avoid multiple calculations with the same values.
        //  jobCode = job.getCode();        
    }

    private double calcualteCoV(Run r) {
        double jobStandardDeviation = this.job.getStandardDeviation();
        double sum = 0;
        for (Run run : r.getRunToCompareTo()) {
            sum += run.getAverageSpeed();
        }
        double average = sum / r.getRunToCompareTo().size();
        return jobStandardDeviation / average;
    }

    public void drawANOVAGraph(Job job) {
        charter.drawGraph(job, "ANOVA", "Run", "F-Value", "calculated F", anovaData, this.fCrit);
    }

    public void drawCoVGraph(Job job) {
        charter.drawGraph(job, "Coefficent of Variation", "Run", "CoV", "calculated CoV (%)", covData, Run.UNDEFINED_DOUBLE_VALUE);
    }

    private void initStage() {
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
            stage.show();
        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ANOVA! App state: %s", ConInt.STATUS.IO_EXCEPTION));
        }
    }

    public void openWindow() {
        initStage();
    }
}
