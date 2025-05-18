/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.DataPoint;
import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import com.mycompany.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
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
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author meni1999
 */
public class TTest implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( TTest.class.getName() );
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("TTest"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    @FXML public Label zCritLabel;
    
    @FXML public Button drawTTest;
    
    @FXML public TableView<Run> TTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, String> TColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    
    private Job job;
    private TDistribution t;
    private double tCrit;
    private Charter charter;
    private Map<Integer, Double> tData;
    
    public void tTtest(){
        
        if(job.getRuns().get(0).getRunToCompareTo().size() <= 1) return;

        for (int i = 0; i < job.getRuns().size(); i += 2) {
            Run run1 = job.getRuns().get(i);
            Run run2 = job.getRuns().get(i + 1);
            
            double sse = calculateSSE(run1, run2);
            double runVariance1 = calculateVariance(run1, sse);
            double runVariance2 = calculateVariance(run2, sse);
            
            
            double runSize1 = run1.getData().size();
            double runSize2 = run2.getData().size();
            
            double nominator = (run1.getAverageSpeed() - run2.getAverageSpeed());
            double denominator = Math.sqrt((runVariance1 / runSize1) + (runVariance2 / runSize2));
            double tVal = Math.abs(nominator / denominator);
            run1.setT(tVal);
            
            tData.put(run1.getID(), tVal);
            
            run2.setT(Run.UNDEFINED_VALUE);
            run2.setNullhypothesis(Run.UNDEFIND_NULLHYPOTHESIS);
            if(this.tCrit < tVal){
                run1.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            } else {
                run1.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
            }                   
        }
    }
    
   public TTest(Job job){
       this.job = job;
       this.job.clearRuns();
       charter = new Charter();
       tData = new HashMap<>();
       if(job.getRunDataSize() <= 1) return;

       this.t = new TDistribution(job.getRuns().get(0).getData().size() * 2 - 2);
       this.tCrit = t.inverseCumulativeProbability(1 - job.getAlpha() / 2.0);
   }
   
    private void setLabeling(){
        zCritLabel.setText(String.format(Locale.ENGLISH, "%,.5f", this.tCrit));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("PairwiseRunToCompareToAsString"));
        TColumn.setCellValueFactory(new PropertyValueFactory<>("TAsString"));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTTest.setOnAction(e -> drawTGraph(this.job));
        TTable.setItems(this.job.getRuns());
        setLabeling();
    }
    
    private void drawTGraph(Job job){
        charter.drawGraph(job, "T-Test", "Run", "T-Value", "calculated T", this.tData, tCrit);
    }
    
    private double calculateVariance(Run run, double sse){
        return (1.0 / (run.getData().size() - 1.0)) * sse;
    }
    
    private double calculateSSE(Run run1, Run run2){
        double sse = 0;
        double averageSpeed = (run1.getAverageSpeed() + run2.getAverageSpeed()) / 2.0;
        
        for (DataPoint dp : run1.getData()) {
                    sse += (Math.pow((dp.getSpeed() - averageSpeed), 2));
        }
    
        for (DataPoint dp : run2.getData()) {
                    sse += (Math.pow((dp.getSpeed() - averageSpeed), 2));
        }
        
        return sse;
    }
    
    public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/TTest.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();
            /* 
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setMaxWidth(1200);      
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculated T-Test");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
        e.printStackTrace();
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            //LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }

}
