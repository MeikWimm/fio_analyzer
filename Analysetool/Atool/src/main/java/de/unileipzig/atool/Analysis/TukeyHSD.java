/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

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
public class TukeyHSD extends PostHocTest implements Initializable, PostHocAnalyzer {
    private static final Logger LOGGER = Logger.getLogger( TukeyHSD.class.getName() );

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("TukeyHSD"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }    

    @FXML public Label qCritLabel;

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
    private final Charter charter;

    public TukeyHSD(Anova anova){
        super(anova);
        this.meanData = new ArrayList<>();
        this.qHSDData = new ArrayList<>();
        this.charter = new Charter();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
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
        charter.drawGraph("U-Test","Run","Mean/Difference",new Charter.ChartData("Run mean", meanData), new Charter.ChartData("QHSD", qHSDData));
    }

    @Override
    public void apply(List<Run> postHocRuns, List<List<Run>> postHocGroups) {
        int totalObservations = test.getJob().getData().size();
        for (int i = 0; i < postHocGroups.size() - 1; i++) {
            for (int j = i + 1; j < postHocGroups.size(); j++) {
                double n = test.getJob().getRuns().getFirst().getData().size();
                int numberOfGroups = postHocGroups.size();
                int dfError = totalObservations - numberOfGroups;

                Tukey tukey = new Tukey(postHocGroups.getFirst().size(), numberOfGroups, dfError);
                List<Run> group1 = postHocGroups.get(i);
                List<Run> group2 = postHocGroups.get(j);
                double speedSumGroup1 = 0;
                double speedSumGroup2 = 0;


                for (int k = 0; k < group1.size(); k++) {
                    Run run1 = group1.get(k);
                    Run run2 = group2.get(k);

                    speedSumGroup1 += run1.getAverageSpeed();
                    speedSumGroup2 += run2.getAverageSpeed();
                }

                double averageSpeedGroup1 = speedSumGroup1 / group1.size();
                double averageSpeedGroup2 = speedSumGroup2 / group2.size();

                double sse = postHocGroups.get(i).getFirst().getSSE();
                double MSE = sse / dfError;
                double standardError = Math.sqrt(MSE / n);
                double qCritical = tukey.inverse_survival(test.getAlpha(), false);
                double overallMean = Math.abs(averageSpeedGroup1 - averageSpeedGroup2);
                qHSD = qCritical * standardError;
                Run run = group1.getFirst();
                run.setQ(overallMean);

                meanData.add(new XYChart.Data<>(run.getID(), overallMean));
                qHSDData.add(new XYChart.Data<>(run.getID(), qHSD));
                if(qHSD < overallMean){
                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
                } else {
                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);

                    List<Run> possibleRuns = this.test.getPossibleSteadyStateRuns();

                    for (Run possibleRun : possibleRuns) {
                        int ID = possibleRun.getID();
                        boolean foundInGroup1 = false;
                        boolean foundInGroup2 = false;
                        for (Run run1 : group1) {
                            if (run1.getID() == ID) {
                                foundInGroup1 = true;
                                break;
                            }
                        }

                        for (Run run2 : group2) {
                            if (run2.getID() == ID) {
                                foundInGroup2 = true;
                                break;
                            }
                        }

                        if(foundInGroup1 && foundInGroup2){
                            System.out.println("Possible steady state at run: " + possibleRun.getID());
                        }
                    }
                }
            }
        }
    }

    public void openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/TukeyHSD.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setMaxWidth(1200);      
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculated Tukey HSD");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
}