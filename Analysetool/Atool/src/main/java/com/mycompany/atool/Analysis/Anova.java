/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.DataPoint;
import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import com.mycompany.atool.Settings;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.FDistribution;

/**
 *
 * @author meni1999
 */
public class Anova implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( Anova.class.getName() );
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("ANOVA"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    private final Job job;
    
    @FXML public Label averageSpeedLabel;
    @FXML public Label sseLabel;
    @FXML public Label ssaLabel;
    @FXML public Label sstLabel;
    @FXML public Label ssaSstLabel;
    @FXML public Label sseSstLabel;
    @FXML public Label fCriticalLabel;
    @FXML public Label fCalculatedLabel;
    
    @FXML public TableView<Run> anovaTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, String> compareToRunColumn;
    @FXML public TableColumn<Run, String> FColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    
    private Stage stage;
    private static String jobCode = "";
    private double fVal = 0.0;
    private double fCrit;
    private int steadyStateRunID = -1;
    private FDistribution fDistribution;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  
      
        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        FColumn.setCellValueFactory(new PropertyValueFactory<>("FAsString"));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());
        

        
        anovaTable.setOnMouseClicked((MouseEvent event) -> {
                if(event.getButton().equals(MouseButton.PRIMARY)){
                    setLabeling(anovaTable.getSelectionModel().getSelectedItem());
                }
        });        
        
        anovaTable.setItems(this.job.getRuns());
    }
    
    public Anova(){
        this.job = new Job();
    }
    
    public Anova(Job job){
        this.job = job;
        if(job.getRuns().get(0).getRunToCompareTo().size() <= 1){
            fDistribution = new FDistribution(1,1);
        } else {
            int num = job.getRuns().get(0).getRunToCompareTo().size() - 1;
            int denom = (num + 1) * (job.getRunDataSize() - 1);
            LOGGER.log(Level.INFO,String.format("Calculated Numerator %d and Denominator %d", num, denom));
            fDistribution = new FDistribution(num, denom);
            fCrit = fDistribution.inverseCumulativeProbability(1.0-job.getAlpha());
        }
        

    }

    public void calculateANOVA(){
        if(job.getRuns().size() <= 1) return;
       
        /*
        if(job.getCode().equals(jobCode)) {
            return;
        } else {
            LOGGER.log(Level.INFO,String.format("Change detected for %s", this.job));
        }
        */
        double sse = 0.0;
        double ssa = 0.0;
        int num = job.getRuns().get(0).getRunToCompareTo().size() - 1;
        int denom = (num + 1) * (job.getRunDataSize() - 1);
        LOGGER.log(Level.INFO,String.format("Calculated Numerator %d and Denominator %d", num, denom));

//      calculate F value between runs
//      SSA 
        for (Run run : this.job.getRuns()) {      
            for (Run runToCompare : run.getRunToCompareTo()) {
                double averageSpeedOfRun = runToCompare.getAverageSpeed();
                double averageSpeedOfAllComparedRuns = run.getAverageSpeedOfRunsToCompareTo();
                ssa += Math.pow(averageSpeedOfRun - averageSpeedOfAllComparedRuns,2);
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
            double s_2_e = run.getSSE() / (run.getRunToCompareTo().size()  * (run.getData().size() - 1));
            fValue = s_2_a / s_2_e;
            run.setF(s_2_a / s_2_e);
            if(!run.getRunToCompareTo().isEmpty()){
                // critical p-value < alpha value of job
                if(fDistribution.inverseCumulativeProbability(1.0-job.getAlpha()) < fValue){
                    run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
                } else {
                    run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
                }
            }

        }
        
        //calculateSteadyState();
        
        // remember run counter and alpha to avoid multiple calculations with the same values.
          jobCode = job.getCode();        
    }
    
    private void calculateSteadyState(){
        int k = 10;
        int check_runs_counter = this.job.getRunsCounter() - k;
        int largestCountOfHypothesis = -1;
        int runIDWithSmallesCount = -1;
        if(check_runs_counter >= 1){
            for (int i = 0; i < check_runs_counter; i++) {
                int countOfTrueHypothesis = 0;
                
                for (int j = i; j < k+i; j++) {
                    if(this.job.getRuns().get(j).getNullhypothesis() == Run.ACCEPTED_NULLHYPOTHESIS){
                        countOfTrueHypothesis++;
                    }
                }
                
                if(countOfTrueHypothesis > largestCountOfHypothesis){
                    runIDWithSmallesCount = i;
                    largestCountOfHypothesis = countOfTrueHypothesis;
                }
                

            }
        } 
        System.err.println("Run ID: " + runIDWithSmallesCount + " Hypothesis Count: " + largestCountOfHypothesis);
    }

    private void setLabeling(Run run){
        averageSpeedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getAverageSpeed()));
        sseLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSE()));
        ssaLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSSA()));
        sstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getSST()));
        ssaSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT,(run.getSSA() / run.getSST())));
        sseSstLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT,(run.getSSE() / run.getSST())));
        fCriticalLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.fCrit));
        fCalculatedLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, run.getF()));
    }
    
    public void openWindow(){
        initStage();
        stage.show();
    }
    
    public ConInt.STATUS initStage(){
        try {
            
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/Anova.fxml"));
            FXMLLoader.load(getClass().getResource("/com/mycompany/atool/Anova.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();
            /* 
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            stage = new Stage();
            stage.setMaxWidth(1200);      
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculated ANOVA");
            stage.setScene(new Scene(root1));            
    } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ANOVA! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }
}
