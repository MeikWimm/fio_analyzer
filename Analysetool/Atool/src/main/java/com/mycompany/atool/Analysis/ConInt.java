/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.InputModule;
import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import com.mycompany.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * ConInt (Confidence Interval)
 * @author meni1999
 */
public class ConInt implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( InputModule.class.getName() );
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    @FXML public Label labelHeader;
    
    @FXML public Button drawConIntDiffButton; 
    
    @FXML public TableView<Run> conIntTable;
    @FXML public TableColumn<Run,Integer> runsColumn;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run,Double> intervalFromColumn;
    @FXML public TableColumn<Run,Double> intervalToColumn;
    @FXML public TableColumn<Run,Double> plusMinusValueColumn;
    @FXML public TableColumn<Run,Double> standardDeviationColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run,String> overlappingColumn;
    
    private Stage stage;
    private Charter charter;
    private static int jobRunCounter = 0;
    private static double jobAlpha = -1.0;
    private Map<Integer, Double> conIntData;



    public enum STATUS {
        SUCCESS,
        IO_EXCEPTION
    }
    
    private final Job job;

    
    public ConInt(Job job){
        charter = new Charter();
        conIntData = new HashMap<>();
        this.job = job;
        this.job.clearRuns();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  
        intervalFromColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalFrom"));
        intervalFromColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        intervalToColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalTo"));
        intervalToColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        plusMinusValueColumn.setCellValueFactory(new PropertyValueFactory<>("PlusMinusValue"));
        plusMinusValueColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
        standardDeviationColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  
        
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("PairwiseRunToCompareToAsString"));
        
        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("OverlappingDifferenceAsString"));

        drawConIntDiffButton.setOnAction(e -> drawOverlappingDiffernce(this.job));
        
        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getRuns());
    }

    public STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/ConInt.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();
            /* 
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            stage = new Stage();
            stage.setMaxWidth(1200);      
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculate Confidence Interval");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ConInt! App state: %s", STATUS.IO_EXCEPTION));
            return STATUS.IO_EXCEPTION;
        }
        return STATUS.SUCCESS;
    }
    
    private void drawOverlappingDiffernce(Job job) {
        charter.drawGraph(job, "Overlapping Differnce of confidence intervals", "Run", "Overlapping difference (%)", "Overlapping Difference", conIntData, Run.UNDEFINED_VALUE);
    } 
    /*
    private void checkOverlappingInterval(Job job){
        List<Run> runs = job.getRuns();
        
        for (Run run : runs) {
            int overlap = 0;
            for (Run comparedRun : runs) {
                if(!run.equals(comparedRun)){
                    if(Math.max (0, Math.min(run.getIntervalTo(), comparedRun.getIntervalTo()) - Math.max(run.getIntervalFrom(), run.getIntervalTo()) + 1) > 0){
                        overlap++;
                        run.setOverlapping(overlap);
                    }
                }
            }
        }
    }
        */  

    private double calculateOverlapp(Run run1, Run run2){
        double overlap = Math.max(0, Math.min(run1.getIntervalTo(), run2.getIntervalTo()) - Math.max(run1.getIntervalFrom(), run2.getIntervalFrom()));
        double length = run1.getIntervalTo() - run1.getIntervalFrom() + run2.getIntervalTo() - run2.getIntervalFrom();

        return (1.0 - 2*overlap/length) * 100;
    }
    
    public void calculateInterval(){
        /*
        if(jobRunCounter == this.job.getRunsCounter() && jobAlpha == this.job.getAlpha()) {
            return;
        } else {
            System.err.println("Job Change detected!");
        }        
        */
        NormalDistribution normDis = new NormalDistribution();

        
        for (Run run : this.job.getRuns()) {
            double c1 = run.getAverageSpeed() - (normDis.inverseCumulativeProbability(1.0 - this.job.getAlpha() / 2.0) * (run.getStandardDeviation() / Math.sqrt(run.getData().size())));
            run.setIntervalFrom(c1);

            double c2 = run.getAverageSpeed() + (normDis.inverseCumulativeProbability(1.0 - this.job.getAlpha() / 2.0) * (run.getStandardDeviation() / Math.sqrt(run.getData().size())));
            run.setIntervalTo(c2);   
        }
        List<Run> runs = this.job.getRuns();
        for (int i = 0; i < this.job.getRuns().size() - 1; i += 2) {
            double overlappingDiff = calculateOverlapp(runs.get(i), runs.get(i+1));
            runs.get(i).setOverlappingDifference(overlappingDiff);
            runs.get(i+1).setOverlappingDifference(Run.UNDEFINED_VALUE);
            conIntData.put(runs.get(i).getID(), overlappingDiff);
            //System.err.println(overlappingDiff);
        }
        
        
    }
}
