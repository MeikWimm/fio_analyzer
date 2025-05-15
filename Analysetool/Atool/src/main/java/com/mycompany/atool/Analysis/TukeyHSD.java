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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
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

    @FXML public Label averageSpeedLabel;
    @FXML public Label sseLabel;
    @FXML public Label ssaLabel;
    @FXML public Label sstLabel;
    @FXML public Label ssaSstLabel;
    @FXML public Label sseSstLabel;
    @FXML public Label qCritLabel;
    @FXML public Label fCalculatedLabel;
    
    @FXML public TableView<Run> TukeyTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, Double> QColumn;
    @FXML public TableColumn<Run, Boolean> hypothesisColumn;
    
    private final Job job;
    public TukeyHSD(Job job){
        this.job = job;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("RunToCompareToAsString"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        TukeyTable.setItems(this.job.getRuns());   
    }
    
    public void calculateTukeyHSD(){
        
        new Anova(job).calculateANOVA();
        Tukey tukey = new Tukey(1, 2, 2 * (job.getRunDataSize() - 1));
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

    }
    

    public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/TukeyHSD.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();

            Stage stage = new Stage();
            stage.setMaxWidth(1200);      
            stage.setMaxHeight(800);
            stage.setMinHeight(600);
            stage.setMinWidth(600);
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
}