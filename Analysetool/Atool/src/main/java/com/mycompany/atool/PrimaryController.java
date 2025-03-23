package com.mycompany.atool;


import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;



public class PrimaryController implements Initializable{
    Tester tester;
    InputModule inputModule;
    
    @FXML public MenuItem menuItem_open;
    @FXML public MenuItem menuItem_ANOVA;
    @FXML public MenuItem menuItem_Info;
    
    @FXML public Button button_refreshTable;
    
    @FXML public TableView<Job> table;
    @FXML public TableColumn<Job,String> fileNameColumn;
    @FXML public TableColumn<Job,Integer> runsColumn;
    @FXML public TableColumn<Job,String> speedColumn;
    @FXML public TableColumn<Job,String> timeColumn;
    @FXML public TableColumn<Job,String> lastModifiedColumn;
    @FXML public TableColumn<Job,String> fileCreatedColumn;
    @FXML public TableColumn<Job,Double> epsilonColumn;
    @FXML public TableColumn<Job,Double> alphaColumn;
    
    
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputModule = new InputModule();
        tester = new Tester();
        
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("File"));
        lastModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("FileLastModifiedDate"));
        fileCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("FileCreationDate"));
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("Runs"));
        runsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("TimeInSec"));
        epsilonColumn.setCellValueFactory(new PropertyValueFactory<>("Epsilon"));
        epsilonColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        alphaColumn.setCellValueFactory(new PropertyValueFactory<>("Alpha"));
        alphaColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        
        prepareTable();

        

    }

    private void prepareTable(){
        runsColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Integer> t) -> {
            t.getRowValue().setRuns(t.getNewValue());
        });
        
        alphaColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setAlpha(t.getNewValue());
        });
        
        epsilonColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setEpsilon(t.getNewValue());
        });

        table.setOnKeyReleased((KeyEvent t)-> {
            KeyCode key=t.getCode();
            if (key==KeyCode.DELETE){
                int pos=table.getSelectionModel().getSelectedIndex();
                Job remove = table.getItems().remove(pos);
                System.out.println("Removed: " + remove.toString());
            }
            for (Job item : table.getItems()) {
                System.out.println("Jobs left: " + item.toString());
            }
        });
        
        table.setRowFactory((TableView<Job> tableView) -> {
            final TableRow<Job> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            MenuItem applyTestItem = new MenuItem("Draw job speed");
            applyTestItem.setOnAction((ActionEvent event) -> {
                tester.drawJob(row.getItem());
            });            
            
            MenuItem removeItem = new MenuItem("Delete");
            removeItem.setOnAction((ActionEvent event) -> {
                table.getItems().remove(row.getItem());
            });
            
            MenuItem drawFrequencyItem = new MenuItem("Draw job frequency");
            drawFrequencyItem.setOnAction((ActionEvent event) -> {
                tester.drawJobFreqeuncy(row.getItem());
            });   
            
            rowMenu.getItems().addAll(applyTestItem, drawFrequencyItem, removeItem);

            // only display context menu for non-null items:
            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu)null));
            return row;
        });
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
            //System.out.println("File loaded:");
            //System.out.println(job.toString());
        }
            //System.out.println("----------------------------------");
    }
    
    @FXML
    private void onActionRefreshTable(){
        if(inputModule.getSelectedDir() != null){
            inputModule.readFiles(inputModule.selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log")));
        } else {
            System.out.println("com.mycompany.atool.PrimaryController.onActionRefreshTable() | directoryChooser is not set!");
        }
    }
    
    @FXML
    private void executeANOVA(){
        if(table.getSelectionModel().getSelectedItem() != null){
            tester.executeANOVA(table.getSelectionModel().getSelectedItem());
        } else {
            System.out.println("com.mycompany.atool.PrimaryController.executeANOVA() No item choosen for ANOVA!");  
        }
    }

    @FXML
    private void openInfoWindow(){
            try {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("secondary.fxml"));
        /* 
         * if "fx:controller" is not set in fxml
         * fxmlLoader.setController(NewWindowController);
         */
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("New Window");
        stage.setScene(scene);
        stage.show();
    } catch (IOException e) {
        
        }
    }   
}

