/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.InputModule;
import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * ConInt (Confidence Interval)
 * @author meni1999
 */
public class ConInt implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( InputModule.class.getName() );
    
    @FXML public Label labelHeader;
    
    @FXML public TableView<Run> conIntTable;
    @FXML public TableColumn<Run,Integer> runsColumn;
    @FXML public TableColumn<Run,Integer> averageSpeedColumn;
    @FXML public TableColumn<Run,Double> intervalFromColumn;
    @FXML public TableColumn<Run,Double> intervalToColumn;
    @FXML public TableColumn<Run,Double> plusMinusValueColumn;
    @FXML public TableColumn<Run,Double> standardDeviationColumn;
    @FXML public TableColumn<Run,Integer> overlappingColumn;

    public enum STATUS {
        SUCCESS,
        IO_EXCEPTION
    }
    
    private Job job;
    public ConInt(){
        job = null;
    }
    
    public ConInt(Job job){
        this.job = job;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        intervalFromColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalFrom"));
        intervalToColumn.setCellValueFactory(new PropertyValueFactory<>("IntervalTo"));
        plusMinusValueColumn.setCellValueFactory(new PropertyValueFactory<>("PlusMinusValue"));
        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
        overlappingColumn.setCellValueFactory(new PropertyValueFactory<>("Overlapping"));

        labelHeader.setText(this.job.toString());
        conIntTable.setItems(this.job.getRuns());
    }
    
    /**
     * 
     * @param job 
     */
    public void setJob(Job job){
        this.job = job;
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
            Stage stage = new Stage();
            stage.setTitle("Calculate Confidence Interval");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ConInt! App state: %s", STATUS.IO_EXCEPTION));
            return STATUS.IO_EXCEPTION;
        }
        return STATUS.SUCCESS;
    }
    
    public static void calculateInterval(Job job){
        NormalDistribution normDis = new NormalDistribution();
        
        for (Run run : job.getRuns()) {
            System.err.println("-----------------------------------------------------");
            System.err.println(run.toString());
            System.err.println(run.getAverageSpeed());
            System.err.println(normDis.inverseCumulativeProbability(job.getAlpha()));
            System.err.println(run.getStandardDeviation());
            System.err.println((run.getData().size()));
                        System.err.println("-----------------------------------------------------");
            double c1 = run.getAverageSpeed() - (normDis.inverseCumulativeProbability(job.getAlpha()) * (run.getStandardDeviation() / Math.sqrt(run.getData().size())));
            run.setIntervalFrom(c1);

            double c2 = run.getAverageSpeed() - (normDis.inverseCumulativeProbability(job.getAlpha()) * (run.getStandardDeviation() / run.getData().size()));
            run.setIntervalTo(c2);   
        }
    }
}
