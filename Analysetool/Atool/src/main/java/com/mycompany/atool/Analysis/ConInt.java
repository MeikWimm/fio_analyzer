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
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
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
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    @FXML public Label labelHeader;
    
    @FXML public TableView<Run> conIntTable;
    @FXML public TableColumn<Run,Integer> runsColumn;
    @FXML public TableColumn<Run,Integer> averageSpeedColumn;
    @FXML public TableColumn<Run,Double> intervalFromColumn;
    @FXML public TableColumn<Run,Double> intervalToColumn;
    @FXML public TableColumn<Run,Double> plusMinusValueColumn;
    @FXML public TableColumn<Run,Double> standardDeviationColumn;
    @FXML public TableColumn<Run,Integer> overlappingColumn;
    
    private Stage stage;
    private static int jobRunCounter = 0;
    private static double jobAlpha = -1.0;

    public enum STATUS {
        SUCCESS,
        IO_EXCEPTION
    }
    
    private final Job job;
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
    
    public void calculateInterval(){
        if(jobRunCounter == this.job.getRunsCounter() && jobAlpha == this.job.getAlpha()) {
            return;
        } else {
            System.err.println("Job Change detected!");
        }        
        
        NormalDistribution normDis = new NormalDistribution();

        
        for (Run run : this.job.getRuns()) {
            double c1 = run.getAverageSpeed() - (normDis.inverseCumulativeProbability(this.job.getAlpha()) * (run.getStandardDeviation() / Math.sqrt(run.getData().size())));
            run.setIntervalFrom(c1);

            double c2 = run.getAverageSpeed() - (normDis.inverseCumulativeProbability(this.job.getAlpha()) * (run.getStandardDeviation() / run.getData().size()));
            run.setIntervalTo(c2);   
        }
        jobRunCounter = this.job.getRunsCounter(); // remember counter if changed, to avoid multiple calculations with the same values.
        jobAlpha = this.job.getAlpha();
    }
}
