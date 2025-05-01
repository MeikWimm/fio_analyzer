/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.DataPoint;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.commons.math3.distribution.FDistribution;

/**
 *
 * @author meni1999
 */
public class Anova implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( Anova.class.getName() );
    
    private Job job;
    
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
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Integer> FColumn;
    @FXML public TableColumn<Run, Boolean> hypothesisColumn;
    
    private static int jobRunCounter = 0;
    private boolean isWindowReady = false;
    private Stage stage;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        FColumn.setCellValueFactory(new PropertyValueFactory<>("F"));
        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(col -> {
            TableCell<Run, Boolean> cell = new TableCell<Run, Boolean>() {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (item == null) {
                        setText("");
                        setStyle("");
                    } else if(item == false) {
                        setStyle("-fx-background-color: tomato;");
                        setText("Rejected");
                    } else {
                        setStyle("-fx-background-color: green;");
                        setText("Accepted");
                    }
                }
            };
            return cell;
        });

        anovaTable.setItems(this.job.getRuns());
        setLabeling();
    }
    
    public Anova(){
        this.job = new Job();
    }
    
    public Anova(Job job){
        this.job = job;
    }

    public void calculateANOVA(){
        if(jobRunCounter == job.getRunsCounter()) return; // If run counter didn't change, than there is no need to calculate again.
        
        //calulcate ssa and sse for the whole job
        double sse = 0.0;
        double ssa = 0.0;
        //int num = job.getRuns().get(0).getMinimizedData(Run.SPEED_PER_SEC).size();
        //int denom = job.getRuns().get(0).getRunToCompareTo().size();
        //System.err.println(num + " denom: " + denom);
        FDistribution F = new FDistribution(1, 2);

//      calculate F value between runs
//      SSA 
        for (Run run : job.getRuns()) {      
            for (Run runToCompare : run.getRunToCompareTo()) {
                double averageSpeedOfRunMinimizedData = Run.calculateAverageSpeedOfData(runToCompare.getMinimizedData(Run.SPEED_PER_SEC));
                double averageSpeedOfAllComparedRuns = Run.calculateAverageSpeedOfRuns(run.getRunToCompareTo());
                //System.err.println("converted ave speed: " + averageSpeedOfRunMinimizedData + "    average speed of all runs: " + averageSpeedOfAllComparedRuns);
                ssa += Math.pow(averageSpeedOfRunMinimizedData - averageSpeedOfAllComparedRuns,2);
            }
            ssa *= run.getMinimizedData(Run.SPEED_PER_SEC).size();
            run.setSSA(ssa);
            ssa = 0;
        }
        
        //SSE
        for (Run run : job.getRuns()) {
            for (Run runToCompare : run.getRunToCompareTo()) {
                for (DataPoint dp : runToCompare.getMinimizedData(Run.SPEED_PER_SEC)) {
                    sse += (Math.pow((dp.getSpeed() - Run.calculateAverageSpeedOfData(runToCompare.getMinimizedData(Run.SPEED_PER_SEC))), 2));
                }
            }
            run.setSSE(sse);
            sse = 0;
//            for (Run run1 : job.getRuns()) {
//                System.err.println(String.format("----------------------------------------------- ID: %d", run1.getID()));
//                for (DataPoint dp : run1.getMinimizedData(Run.SPEED_PER_SEC)) {
//                    System.err.println(String.format("%f", dp.getSpeed()));
//                }
//            }
        }
        
        double fValue = 1;
        for (Run run : job.getRuns()) {
            double s_2_a = run.getSSA() / (run.getRunToCompareTo().size() - 1); 
            double s_2_e = run.getSSE() / (run.getRunToCompareTo().size()  * (run.getMinimizedData(Run.SPEED_PER_SEC).size() - 1));
            fValue = s_2_a / s_2_e;
            run.setF(s_2_a / s_2_e);
            //System.err.println(String.format("SSA: %f, SSE: %f", run.getSSA(), run.getSSE()));
            //System.err.println(F.inverseCumulativeProbability(0.95));
         
            if(F.inverseCumulativeProbability(0.95) > fValue){
                System.err.println("Run: " + run.getID() + " accepted");
                //run.setNullypothesis(true);
            } else {
                run.setNullypothesis(false);
                //System.err.println("Run: " + run.getID() + " rejected");
            }
        }
        jobRunCounter = job.getRunsCounter(); // remember counter if changed, to avoid multiple calculations with the same values.
    }

    private void setLabeling(){
        averageSpeedLabel.setText(String.format("%f", this.job.getAverageSpeed()));
        sseLabel.setText(String.format("%f", this.job.getSSE()));
        ssaLabel.setText(String.format("%f", this.job.getSSA()));
        sstLabel.setText(String.format("%f", this.job.getSST()));
        ssaSstLabel.setText(String.format("%f",(this.job.getSSA() / this.job.getSST())));
        sseSstLabel.setText(String.format("%f",(this.job.getSSE() / this.job.getSST())));
        fCriticalLabel.setText(String.format("%f", this.job.getF()));
        fCalculatedLabel.setText(String.format("%f", this.job.F));
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
            stage.setTitle("Calculated ANOVA");
            stage.setScene(new Scene(root1));            
    } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for ANOVA! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }
}
