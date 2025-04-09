package com.mycompany.atool;


import com.mycompany.atool.Analysis.Charter;
import com.mycompany.atool.Analysis.ConInt;
import java.io.IOException;
import java.util.logging.Logger;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.math3.distribution.NormalDistribution;



public class PrimaryController implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( PrimaryController.class.getName() );

    Charter tester;
    InputModule inputModule;
    
    @FXML public MenuItem menuItem_open;
    @FXML public MenuItem menuItem_ANOVA;
    @FXML public MenuItem menuItem_Info;
    
    @FXML public Button button_refreshTable;
    
    @FXML public Label labelLoadInfo;
    
    @FXML public TableView<Job> table;
    @FXML public TableColumn<Job,String> IDColumn;
    @FXML public TableColumn<Job,String> fileNameColumn;
    @FXML public TableColumn<Job,Integer> runsCounterColumn;
    @FXML public TableColumn<Job,String> speedColumn;
    @FXML public TableColumn<Job,String> timeColumn;
    @FXML public TableColumn<Job,String> lastModifiedColumn;
    @FXML public TableColumn<Job,String> fileCreatedColumn;
    @FXML public TableColumn<Job,Double> epsilonColumn;
    @FXML public TableColumn<Job,Double> alphaColumn;

    private void setColumnTextField() {
        runsCounterColumn.setCellFactory(tc -> new TextFieldTableCell<>(new IntegerStringConverter(){
            @Override
            public Integer fromString(String value){
            Pattern pattern = Pattern.compile("^[0-9]*$");
            Matcher matcher = pattern.matcher(value);
            boolean matchFound = matcher.find();
            if(matchFound) {
                return Integer.valueOf(value);
            } else {
                String msg = String.format("Input was not a natural number!", STATUS.WRONG_USER_INPUT);
                LOGGER.log(Level.WARNING, msg);
            }
            return Job.DEFAULT_RUN_COUNT;
            }
        }) {
            @Override
            public void commitEdit(Integer newValue) {
                if(newValue > Job.MAX_RUN_COUNT){
                    String msg = String.format("Run count is too large %d < %d. App state: %s", newValue, Job.MAX_RUN_COUNT, STATUS.WRONG_USER_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_RUN_COUNT);
                } else if (newValue < Job.MIN_RUN_COUNT) {
                    String msg = String.format("Run count is too small %d > %d. App state: %s", newValue, Job.MIN_RUN_COUNT, STATUS.WRONG_USER_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_RUN_COUNT);
                } else {
                    super.commitEdit(newValue);
                }
            }
        });

        alphaColumn.setCellFactory(tc -> new TextFieldTableCell<>(new DoubleStringConverter(){
            @Override
            public Double fromString(String value){
            Pattern pattern = Pattern.compile("^[0-9]*$");
            Matcher matcher = pattern.matcher(value);
            boolean matchFound = matcher.find();
            if(matchFound) {
                return Double.valueOf(value);
            } else {
                String msg = String.format("Input was not a number!", STATUS.WRONG_USER_INPUT);
                LOGGER.log(Level.WARNING, msg);
            }
            return Job.DEFAULT_ALPHA;
            }
        }) {
            @Override
            public void commitEdit(Double newValue) {
                if(newValue > Job.MAX_ALPHA){
                    String msg = String.format("Alpha is too large %f < %f. App state: %s", newValue, Job.MAX_ALPHA, STATUS.WRONG_USER_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_ALPHA);
                } else if (newValue < Job.MIN_ALPHA) {
                    String msg = String.format("Alpha is too small %f < %f. App state: %s", newValue, Job.MIN_ALPHA, STATUS.WRONG_USER_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_ALPHA);
                } else {
                    super.commitEdit(newValue);
                }
            }
        });

        epsilonColumn.setCellFactory(tc -> new TextFieldTableCell<>(new DoubleStringConverter(){
            @Override
            public Double fromString(String value){
            Pattern pattern = Pattern.compile("^[0-9]*$");
            Matcher matcher = pattern.matcher(value);
            boolean matchFound = matcher.find();
            if(matchFound) {
                return Double.valueOf(value);
            } else {
                String msg = String.format("Input was not a number!", STATUS.WRONG_USER_INPUT);
                LOGGER.log(Level.WARNING, msg);
            }
            return Job.DEFAULT_EPSILON;
            }
        }) {
            @Override
            public void commitEdit(Double newValue) {
                if(newValue > Job.MAX_EPSILON){
                    String msg = String.format("Run count is too large %f < %f. App state: %s", newValue, Job.MAX_EPSILON, STATUS.WRONG_USER_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_EPSILON);
                } else if (newValue < Job.MIN_EPSILON) {
                    String msg = String.format("Run count is too small %f > %f. App state: %s", newValue, Job.MIN_EPSILON, STATUS.WRONG_USER_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_EPSILON);
                } else {
                    super.commitEdit(newValue);
                }
            }
        });        
}
    
    enum STATUS{
        SUCCESS,
        WRONG_USER_INPUT
    }
    
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new CustomFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputModule = new InputModule();
        tester = new Charter();
        
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("File"));
        runsCounterColumn.setCellValueFactory(new PropertyValueFactory<>("RunsCounter"));
 

        speedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("TimeInSec"));
        lastModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("FileLastModifiedDate"));
        fileCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("FileCreationDate"));
        epsilonColumn.setCellValueFactory(new PropertyValueFactory<>("Epsilon"));
        alphaColumn.setCellValueFactory(new PropertyValueFactory<>("Alpha"));
        setColumnTextField();
        prepareTable();

        

    }

    private void prepareTable(){
        runsCounterColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Integer> t) -> {
            t.getRowValue().setRunsCounter(t.getNewValue());
            t.getRowValue().setRuns();
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
                Job removedJob = table.getItems().remove(pos);
                LOGGER.log(Level.INFO, String.format("Removed Job -> %s", removedJob.toString()));
            }
        });
        
        table.setRowFactory((TableView<Job> tableView) -> {
            final TableRow<Job> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            MenuItem applyTestItem = new MenuItem("Draw job speed");
            applyTestItem.setOnAction((ActionEvent event) -> {
                tester.drawJob(row.getItem());
            });   
                        
            MenuItem drawFrequencyItem = new MenuItem("Draw job frequency");
            drawFrequencyItem.setOnAction((ActionEvent event) -> {
                tester.drawJobFreqeuncy(row.getItem());
            });   
            
            MenuItem removeItem = new MenuItem("Delete");
            removeItem.setOnAction((ActionEvent event) -> {
                table.getItems().remove(row.getItem());
                 LOGGER.log(Level.INFO, String.format("Removed Job -> %s", row.getItem().toString()));
            });

            MenuItem calculateConInt = new MenuItem("Calculate Confidence Interval");
            calculateConInt.setOnAction((ActionEvent event) -> {
                    ConInt conInt = new ConInt(row.getItem());
                    conInt.openWindow();
                    conInt.printSelectedJob();
                });
            
            

            rowMenu.getItems().addAll(applyTestItem, drawFrequencyItem, calculateConInt, removeItem);

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
        // Logic Block
        labelLoadInfo.setText("trying to load files...");
        InputModule.STATUS state = inputModule.loadFile();

        switch (state) {
            case NO_DIR_SET:
                labelLoadInfo.setText("No directory set!");
                break;
            case NO_FILES_FOUND:
                labelLoadInfo.setText("No files found!");
                break;
            case SUCCESS:
                labelLoadInfo.setText("All files loaded!");
                table.setItems(inputModule.getJobs());
                break;
        }
        

        if(state != InputModule.STATUS.SUCCESS){
            LOGGER.log(Level.WARNING, String.format("Couldnt not load Files. App state: %s", state));
        } else {
            for (Job job : inputModule.getJobs()) {
                LOGGER.log(Level.INFO, String.format("Jobs loaded: %s", job.toString()));
            }
        }
    }
    
    @FXML
    private void onActionRefreshTable(){
        labelLoadInfo.setText("Refresh Table...");
        InputModule.STATUS status = inputModule.readFiles();

        if(status != InputModule.STATUS.SUCCESS){
            LOGGER.log(Level.WARNING, String.format("Coudn't refresh table! App state: %s", status));
            labelLoadInfo.setText("Couldn't load Files!");
        } else {
            labelLoadInfo.setText("All Files Loaded!");
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
        NormalDistribution norm = new NormalDistribution();
        
        System.err.println(norm.inverseCumulativeProbability(0.95)); // z_a/2 = 1 - alpha/2
        
            try {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("secondary.fxml"));


        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("New Window");
        stage.setScene(scene);
        stage.show();
    } catch (IOException e) {
        LOGGER.log(Level.SEVERE, (Supplier<String>) e);
        }
    }   
}



