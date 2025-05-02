/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD implements Initializable{
    double df = 0;
    private static int jobRunCounter = 0;
    private static double jobAlpha = -1.0;

    private final Job job;
    public TukeyHSD(Job job){
        this.job = job;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }
    
    public void calculateTukeyHSD(){
        if(jobRunCounter == this.job.getRunsCounter() && jobAlpha == this.job.getAlpha()) {
            return;
        } else {
            System.err.println("Job Change detected!");
        } 
        
        
        new Anova(job).calculateANOVA();
        Tukey tukey = new Tukey(1, 2, 2 * (job.getRuns().get(0).getData().size() - 1));
        for (Run run : job.getRuns()) {
            double qVal = 0;
            List<Run> runs = run.getRunToCompareTo();
            if(run.getRunToCompareTo().size() > 1){
                qVal = (Math.abs(runs.get(0).getAverageSpeed() - runs.get(1).getAverageSpeed())) / (Math.sqrt(run.getSSE() / run.getData().size()));
            }
            System.err.println("Run: " + run.getID());
            System.err.println("Q (calc): " + qVal);
            System.err.println("-----------------------------------------------");
            run.setQ(qVal);
        }
        System.err.println("Q (crit): " + tukey.inverse_survival(0.05, false));
        jobRunCounter = this.job.getRunsCounter(); // remember counter if changed, to avoid multiple calculations with the same values.
        jobAlpha = this.job.getAlpha();
    }
    

    public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/TukeyHSD.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();
            /* 
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setTitle("Calculated Tukey HSD");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            //LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }
}