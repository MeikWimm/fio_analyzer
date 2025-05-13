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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 *
 * @author meni1999
 */
public class MannWhitney implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( MannWhitney.class.getName() );
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Mann-Whitney"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    @FXML public TableView<Run> uTestTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Integer> ZColumn;
    @FXML public TableColumn<Run, Boolean> hypothesisColumn;
   
    @FXML public Label zIntervalLabel;
    @FXML public Label sseLabel;
    @FXML public Label ssaLabel;
    @FXML public Label sstLabel;
    @FXML public Label ssaSstLabel;
    @FXML public Label sseSstLabel;
    @FXML public Label fCriticalLabel;
    @FXML public Label fCalculatedLabel;
    
    NormalDistribution nDis = new NormalDistribution();
    private double zCrit_leftside = -1;
    private double zCrit_rightside = -1;
    private static int jobRunCounter = 0;
    private static double jobAlpha = -1.0;
    

    private void calculateMannWhitney(Run run1, Run run2) {
        List<DataPoint> runData1 = run1.getData();
        List<DataPoint> runData2 = run2.getData();
        for (int i = 0; i < runData1.size(); i++) {
            runData1.get(i).setFlag(0);
            runData2.get(i).setFlag(1);
        }
        
        List<DataPoint> mergedData = new ArrayList<>(runData1);
        mergedData.addAll(runData2);
        
       
        
        Collections.sort(mergedData, new Utils.SpeedComparator());
 
        double r = 1;
        int counter = 1;
        double new_speed, next_speed = -1;
        int index = 0;
        int jindex = 0;
        for (DataPoint p : mergedData) {
            new_speed = p.getSpeed();
            if(jindex < mergedData.size() - 1) {
                next_speed = mergedData.get(jindex+1).getSpeed();
            }
            
            if(next_speed == new_speed && jindex < mergedData.size() - 1){
                if(counter == 1){
                    index = jindex;
                }
                counter++;
            } else if (counter > 1) {
                for (int i = index; i < index + counter; i++) {
                    double splitted_rank = Math.floor(((1.0 / (double) counter)) * 100.0) / 100.0;
                    mergedData.get(i).setRank( r + splitted_rank);
                }
                counter = 1;
            } else {
                p.setRank(r);
            }

            if(counter == 1){
                r++;
            }
            jindex++;
        }
        
        double run1_ranksum = 0;
        double run2_ranksum = 0;
        
        for (DataPoint dataPoint : mergedData) {
            if(dataPoint.getFlag() == 0){
                run1_ranksum += dataPoint.getRank();
            } else {
                run2_ranksum += dataPoint.getRank();
            }
        }
        
        double m = mergedData.size() / 2.0;
        System.err.println("Rank Sum 1: " + run1_ranksum + " m: " + m);
        //double z_1 = (run1_ranksum - 0.5 * m * (2.0 * m + 1.0)) / (Math.sqrt((1.0/12.0) * Math.pow(m, 2) * (2.0 * m + 1.0)));
        //double z_2 = (run2_ranksum - 0.5 * m * (2.0 * m + 1.0)) / (Math.sqrt((1.0/12.0) * Math.pow(m, 2) * (2.0 * m + 1.0)));
        
       // System.err.println("Z_1: " + z_1 + " | Z_2: " + z_2);
        
        double U1 = m * m + ((m * (m + 1) / 2)) - run1_ranksum;
        double U2 = m * m + ((m * (m + 1) / 2)) - run2_ranksum;
        double mu_U = m * m * 0.5;
        double sigma_U = Math.sqrt((m * m * (2*m + 1))/12.0);
        double U = Math.min(U1, U2);
        double z = (U - mu_U) / sigma_U;
        
        System.out.println(z);
        
        
        double zCrit_left = nDis.inverseCumulativeProbability(job.getAlpha() / 2);
        System.err.println("Z Crit Left: " + zCrit_left);
        
        double zCrit_right = nDis.inverseCumulativeProbability(1 - job.getAlpha() / 2);
        System.err.println("Z Crit Right: " + zCrit_right);
        
        run1.setZ(z);
        
        if(z > zCrit_left && z < zCrit_right){
            run1.setNullypothesis(true);
        }
    }
    private Job job;
    public MannWhitney(Job job){
        nDis = new NormalDistribution();
        zCrit_leftside = nDis.inverseCumulativeProbability(job.getAlpha() / 2.0);
        zCrit_rightside = nDis.inverseCumulativeProbability(1 - job.getAlpha() / 2.0);
        this.job = job;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        ZColumn.setCellValueFactory(new PropertyValueFactory<>("Z"));
        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        uTestTable.setItems(this.job.getRuns());
        setLabeling();
    }
    
    public void calculateMannWhitneyTest(){
        if(this.job.getRuns().size() <= 1) return;
        
        if(jobRunCounter == this.job.getRunsCounter() && jobAlpha == this.job.getAlpha()) {
            return;
        } else {
            System.err.println("Job Change detected!");
        } 
        
        List<Run> runs = this.job.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            if(i < runs.size() - 1){
                Run run1 = runs.get(i);
                Run run2 = runs.get(i+1);
                calculateMannWhitney(run1, run2);
            }
        }
        jobRunCounter = this.job.getRunsCounter(); // remember counter if changed, to avoid multiple calculations with the same values.
        jobAlpha = this.job.getAlpha();
    }
    
        public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/MannWithney.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();
            /* 
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setTitle("Calculated U-Test");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            //LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }

    private void setLabeling() {
        zIntervalLabel.setText(String.format("[%f;%f]", zCrit_leftside, zCrit_rightside));
    }
}
