/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import com.mycompany.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
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
    
    @FXML public TableView<Run> TTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> TColumn;
    @FXML public TableColumn<Run, Boolean> hypothesisColumn;
    
    private Job job;
    private TDistribution t;
    private double tCrit;
    
    public void tTtest(){
        new Anova(job).calculateANOVA();
        for (Run r : job.getRuns()) {
            if(r.getRunToCompareTo().size() <= 1) return;
            Run run1 = r.getRunToCompareTo().get(0);
            Run run2 = r.getRunToCompareTo().get(1);
                        
            double runVariance1 = calculateVariance(run1);
            double runVariance2 = calculateVariance(run2);
            
            
            double runSize1 = run1.getData().size();
            double runSize2 = run2.getData().size();
            
            double nominator = (run1.getAverageSpeed() - run2.getAverageSpeed());
            double denominator = Math.sqrt((runVariance1 / runSize1) + (runVariance2 / runSize2));
            double tVal = nominator / denominator;
            r.setT(Math.abs(tVal));
            
            if(t.cumulativeProbability(tVal) < this.job.getAlpha()){
                r.setNullypothesis(false);
            } else {
                r.setNullypothesis(true);
            }
        }
    }
    
   public TTest(Job job){
       this.job = job;
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
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        TColumn.setCellValueFactory(new PropertyValueFactory<>("T"));
        TColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());


        TTable.setItems(this.job.getRuns());
        setLabeling();
    }
    
    private double calculateVariance(Run run){
        return (1.0 / (run.getData().size() - 1.0)) * run.getSSE();
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
