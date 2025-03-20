package com.mycompany.atool;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class PrimaryController implements Initializable{
    InputModule inputModule;
    OutputModule outputModule;
    
    @FXML public TableView<Job> table;
    @FXML public TableColumn<Job,String> fileNameColumn;
    @FXML public TableColumn<Job,String> runsColumn;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputModule = new InputModule();
        outputModule = new OutputModule();
        
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("File"));
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("Runs"));
        
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void openLogfile() {
        inputModule.loadFile();
        table.setItems(inputModule.getJobs());
        for (Job job : inputModule.getJobs()) {
            System.out.println(job.toString());
        }
    }
    
}
