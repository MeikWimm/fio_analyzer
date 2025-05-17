/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import com.mycompany.atool.Settings;
import com.mycompany.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( TukeyHSD.class.getName() );
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("TukeyHSD"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }    

    @FXML public Label qCritLabel;

    
    @FXML public TableView<Run> TukeyTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, String> QColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    
    private final Job job;
    private Tukey tukey;
    private double qHSD;
    
    public TukeyHSD(Job job){
        this.job = job;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("QAsString"));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        TukeyTable.setItems(this.job.getRuns());   
        setLabeling();
    }
    
    public void calculateTukeyHSD(){
        new Anova(job).calculateANOVA();
        for (Run run : job.getRuns()) {
            double qVal = 0;
            double overallMean = 0.0;
            List<Run> runs = run.getRunToCompareTo();
            if(run.getRunToCompareTo().size() > 1){
                Run run1 = runs.get(0);
                Run run2 = runs.get(1);
                overallMean = Math.abs(run1.getAverageSpeed() - run2.getAverageSpeed());
                //qVal = (Math.abs(run1.getAverageSpeed() - run2.getAverageSpeed())) / (Math.sqrt(run1.getSSE()) / Math.sqrt(calculateHarmonicMean(run1, run2)));
            }
            
            this.tukey = new Tukey(1, 2, 2 * (job.getRunDataSize() - 1));
            System.err.println("q: " + tukey.inverse_survival(job.getAlpha(), false));
            System.err.println("MSW/n: " + Math.sqrt(run.getSSE() / job.getRunDataSize()));
            
            this.qHSD = tukey.inverse_survival(job.getAlpha(), false) * Math.sqrt((run.getSSE() / (runs.size()* (this.job.getRunDataSize()))) / job.getRunDataSize());
            
            run.setQ(overallMean);
            if(!run.getRunToCompareTo().isEmpty()){
                if(qHSD < overallMean){
                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
                } else {
                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                }
            } else {
                run.setQ(Run.UNDEFINED_VALUE);
            }
        }
    }
    
    private void setLabeling(){
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
    }

    public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/TukeyHSD.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();

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
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }

    private double calculateHarmonicMean(Run run1, Run run2) {
        return 1.0/(1.0/run1.getAverageSpeed() + 1.0/run2.getAverageSpeed());
    }
}