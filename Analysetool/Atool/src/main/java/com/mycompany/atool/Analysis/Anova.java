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
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
public class Anova implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( Anova.class.getName() );
    
    private Job job;
    
    @FXML public TableView<Run> anovaTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        TableColumn<Run, Double> runColumn = new TableColumn<>();
        runColumn.setText(String.format("General Average Speed"));
        runColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
         anovaTable.getColumns().add(runColumn);
        
//        for (Run run : this.job.getRuns()) {
//            int counter = 1;
//            TableColumn<Run, Double> runColumn = new TableColumn<>();
//            runColumn.setText(String.format("Run %d", counter));
//            runColumn.setCellValueFactory(new PropertyValueFactory<>("StandardDeviation"));
//            anovaTable.getColumns().add(runColumn);
//            counter++;
//        }
            anovaTable.setItems(this.job.getRuns());


    }
    
    public Anova(Job job){
        this.job = job;
    }
    
    public void calculateANOVA(Job job){
        
        for (Run run : job.getRuns()) {
            System.err.println("ANOVA: " + run.toString());
        }
    }

    public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/Anova.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();
            /* 
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setTitle("Calculated ANOVA");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }
}
