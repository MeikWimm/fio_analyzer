package de.unileipzig.atool;


import de.unileipzig.atool.Analysis.*;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PrimaryController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PrimaryController.class.getName());

    // Setting up Logger
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Primary Controller"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    // FXML Items
    @FXML public MenuItem menuItem_open;
    @FXML public MenuItem menuItem_ANOVA;
    //private Anova anova = new Anova();
    @FXML public MenuItem menuItem_Info;
    @FXML public MenuItem menuItem_generalSettings;
    @FXML public Button button_refreshTable;
    @FXML public Button button_settings;
    @FXML public Label labelLoadInfo;
    @FXML public TableView<Job> table;
    @FXML public TableColumn<Job, String> IDColumn;
    @FXML public TableColumn<Job, String> fileNameColumn;
    @FXML public TableColumn<Job, Integer> runsCounterColumn;
    @FXML public TableColumn<Job, String> speedColumn;
    @FXML public TableColumn<Job, String> timeColumn;
    @FXML public TableColumn<Job, String> lastModifiedColumn;
    @FXML public TableColumn<Job, String> fileCreatedColumn;
    @FXML public TableColumn<Job, Double> epsilonColumn;
    @FXML public TableColumn<Job, Double> alphaColumn;
    private InputModule inputModule;
    private Settings settings;
    private Anova anova;
    private TukeyHSD tHSD;
    private MannWhitney mw;
    private ConInt conInt;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputModule = new InputModule();
        settings = new Settings(this);

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

    @FXML
    private void openLogfile() {
        labelLoadInfo.setText("trying to open files...");
        InputModule.STATUS state = inputModule.loadFile();

        switch (state) {
            case NO_DIR_SET:
                labelLoadInfo.setText("No directory set!");
                break;
            case NO_FILES_FOUND:
                labelLoadInfo.setText("No files found!");
                break;
            case DIR_CHOOSER_ALREADY_OPEN:
                labelLoadInfo.setText("Directory chooser already open!");
                break;
            case SUCCESS:
                labelLoadInfo.setText("All files loaded!");
                table.setItems(inputModule.getJobs());
                break;
        }


        if (state != InputModule.STATUS.SUCCESS) {
            LOGGER.log(Level.WARNING, String.format("Couldnt not load Files. App state: %s", state));
        } else {
            for (Job job : inputModule.getJobs()) {
                LOGGER.log(Level.INFO, String.format("Jobs loaded: %s", job.toString()));
            }
        }
    }

    // Code Block of callback functions

    @FXML
    private void onActionRefreshTable() {
        labelLoadInfo.setText("Refresh Table...");
        InputModule.STATUS status = inputModule.readFiles();

        if (status != InputModule.STATUS.SUCCESS) {
            LOGGER.log(Level.WARNING, String.format("Coudn't refresh table! App state: %s", status));
            labelLoadInfo.setText("Couldn't load Files!");
        } else {
            labelLoadInfo.setText("All files loaded!");
        }
    }

    @FXML
    private void onActionOpenSettings() {

    }

    @FXML
    private void openGeneralSettings() {
        settings.openWindow();
    }

    @FXML
    private void openInfoWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("secondary.fxml"));


            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setTitle("New Window");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, (Supplier<String>) e);
            LOGGER.log(Level.SEVERE, String.format("Coudn't open Info Window! App state: %s", STATUS.IO_EXCEPTION));
        }
    }

    public void update() {

        if (Settings.HAS_CHANGED) {
            for (Job job : table.getItems()) {
                job.update();
            }
        }

        table.getColumns().getFirst().setVisible(false);
        table.getColumns().getFirst().setVisible(true);

        Settings.HAS_CHANGED = false;
    }

    private void setColumnTextField() {

        //Run Column setup
        runsCounterColumn.setCellFactory(tc -> new TextFieldTableCell<>(new IntegerStringConverter() {
            @Override
            public Integer fromString(String value) {
                Integer val = Job.DEFAULT_RUN_COUNT;
                try {
                    val = Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    String msg = String.format("Input was not a number! App status: %s", STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                }
                return val;
            }
        }) {
            @Override
            public void commitEdit(Integer newValue) {
                if (newValue > Job.MAX_RUN_COUNT) {
                    String msg = String.format("Run count is too large %d < %d. App state: %s", newValue, Job.MAX_RUN_COUNT, STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    labelLoadInfo.setText(String.format("Allowed run count between %d and %d", Job.MIN_RUN_COUNT, Job.MAX_RUN_COUNT));
                    super.commitEdit(Job.DEFAULT_RUN_COUNT);
                } else if (newValue < Job.MIN_RUN_COUNT) {
                    String msg = String.format("Run count is too small %d > %d. App state: %s", newValue, Job.MIN_RUN_COUNT, STATUS.INCORRECT_INPUT);
                    labelLoadInfo.setText(String.format("Allowed run count between %d and %d", Job.MIN_RUN_COUNT, Job.MIN_RUN_COUNT));
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_RUN_COUNT);
                } else {
                    super.commitEdit(newValue);
                }
            }
        });

        alphaColumn.setCellFactory(tc -> new TextFieldTableCell<>(new DoubleStringConverter() {
            @Override
            public Double fromString(String value) {
                Double val = Job.DEFAULT_ALPHA;
                try {
                    val = Double.valueOf(value);
                } catch (NumberFormatException e) {
                    String msg = String.format("Input was not a number! App status: %s", STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                }
                return val;
            }
        }) {
            @Override
            public void commitEdit(Double newValue) {
                if (newValue > Job.MAX_ALPHA) {
                    String msg = String.format("Alpha is too large %f < %f. App state: %s", newValue, Job.MAX_ALPHA, STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    labelLoadInfo.setText(String.format("Allowed alpha between %f and %f", Job.MIN_ALPHA, Job.MAX_ALPHA));
                    super.commitEdit(Job.DEFAULT_ALPHA);
                } else if (newValue < Job.MIN_ALPHA) {
                    String msg = String.format("Alpha is too small %f < %f. App state: %s", newValue, Job.MIN_ALPHA, STATUS.INCORRECT_INPUT);
                    labelLoadInfo.setText(String.format("Allowed alpha between %f and %f", Job.MIN_ALPHA, Job.MAX_ALPHA));
                    LOGGER.log(Level.WARNING, msg);
                    super.commitEdit(Job.DEFAULT_ALPHA);
                } else {
                    super.commitEdit(newValue);
                }
            }
        });

        epsilonColumn.setCellFactory(tc -> new TextFieldTableCell<>(new DoubleStringConverter() {
            @Override
            public Double fromString(String value) {
                Double val = Job.DEFAULT_EPSILON;
                try {
                    val = Double.valueOf(value);
                } catch (NumberFormatException e) {
                    String msg = String.format("Input was not a number! App status: %s", STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                }
                return val;
            }
        }) {
            @Override
            public void commitEdit(Double newValue) {
                if (newValue > Job.MAX_EPSILON) {
                    String msg = String.format("Run count is too large %f < %f. App state: %s", newValue, Job.MAX_EPSILON, STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    labelLoadInfo.setText(String.format("Allowed epsilon between %f and %f", Job.MIN_EPSILON, Job.MAX_EPSILON));
                    super.commitEdit(Job.DEFAULT_EPSILON);
                } else if (newValue < Job.MIN_EPSILON) {
                    String msg = String.format("Run count is too small %f > %f. App state: %s", newValue, Job.MIN_EPSILON, STATUS.INCORRECT_INPUT);
                    LOGGER.log(Level.WARNING, msg);
                    labelLoadInfo.setText(String.format("Allowed epsilon between %f and %f", Job.MIN_EPSILON, Job.MAX_EPSILON));
                    super.commitEdit(Job.DEFAULT_EPSILON);
                } else {
                    super.commitEdit(newValue);
                }
            }
        });
    }


    // Code Block for setting up Table view

    private void prepareTable() {
        runsCounterColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Integer> t) -> {
            t.getRowValue().setRunsCounter(t.getNewValue());
            t.getRowValue().update();
        });

        alphaColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setAlpha(t.getNewValue());
        });

        epsilonColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setEpsilon(t.getNewValue());
        });

        table.setOnKeyReleased((KeyEvent t) -> {
            KeyCode key = t.getCode();
            if (key == KeyCode.DELETE) {
                int pos = table.getSelectionModel().getSelectedIndex();
                Job removedJob = table.getItems().remove(pos);
                LOGGER.log(Level.INFO, String.format("Removed Job -> %s", removedJob.toString()));
            }
        });

        table.setRowFactory((TableView<Job> tableView) -> {
            final TableRow<Job> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            MenuItem drawJob = new MenuItem("Draw job speed");
            drawJob.setOnAction((ActionEvent event) -> {
                new Charter().drawJob(row.getItem());
            });

            MenuItem drawFrequency = new MenuItem("Draw job frequency");
            drawFrequency.setOnAction((ActionEvent event) -> {
                new Charter().drawJobFreqeuncy(row.getItem());
            });

            MenuItem calculateConInt = new MenuItem("Calculate Confidence Interval");
            calculateConInt.setOnAction((ActionEvent event) -> {
                Job job = row.getItem();
                conInt = new ConInt(job);
                conInt.calculateInterval();
                conInt.openWindow();
            });

            MenuItem calculateANOVA = new MenuItem("Calculate ANOVA");
            calculateANOVA.setOnAction((ActionEvent event) -> {
                Job job = row.getItem();
                anova = new Anova(job);
                anova.calculateANOVA();
                anova.openWindow();
            });

            MenuItem calculateUTest = new MenuItem("Calculate U-Test");
            calculateUTest.setOnAction((ActionEvent event) -> {
                Job job = row.getItem();
                mw = new MannWhitney(job);
                mw.calculateMannWhitneyTest();
                mw.openWindow();
            });

            MenuItem calculateTukeyHSD = new MenuItem("Calculate Tukey HSD");
            calculateTukeyHSD.setOnAction((ActionEvent event) -> {
                Job job = row.getItem();
                tHSD = new TukeyHSD(job);
                tHSD.calculateTukeyHSD();
                tHSD.openWindow();
            });

            MenuItem calculateTTtest = new MenuItem("Calculate T-Test");
            calculateTTtest.setOnAction((ActionEvent event) -> {
                Job job = row.getItem();
                TTest tTest = new TTest(job);
                tTest.tTtest();
                tTest.openWindow();
            });

            MenuItem removeItem = new MenuItem("Delete");
            removeItem.setOnAction((ActionEvent event) -> {
                table.getItems().remove(row.getItem());
                LOGGER.log(Level.INFO, String.format("Removed Job -> %s", row.getItem().toString()));
            });

            rowMenu.getItems().addAll(drawJob, drawFrequency, calculateConInt, calculateANOVA, calculateUTest, calculateTukeyHSD, calculateTTtest, removeItem);

            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu) null));
            return row;
        });
    }

    // Controller status enum
    enum STATUS {
        SUCCESS,
        INCORRECT_INPUT,
        IO_EXCEPTION
    }
}



