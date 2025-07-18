package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;


public class PrimaryController implements Initializable {
    @FXML private MenuItem menuItem_generalSettings;
    @FXML private MenuItem menuItem_open;
    @FXML private MenuItem refreshTableMenuItem;
    @FXML private MenuBar menuBar;
    @FXML private Button steadyStateEvalButton;
    @FXML private Button saveAllEvalButton;
    @FXML private Label labelLoadInfo;
    @FXML private TableView<Job> table;
    @FXML private TableColumn<Job, String> IDColumn;
    @FXML private TableColumn<Job, String> fileNameColumn;
    @FXML private TableColumn<Job, Double> speedColumn;
    @FXML private TableColumn<Job, String> timeColumn;
    @FXML private TableColumn<Job, String> lastModifiedColumn;
    @FXML private TableColumn<Job, String> fileCreatedColumn;
    @FXML private TableColumn<Job, Double> alphaColumn;
    @FXML private TableColumn<Job, Double> cvColumn;

    private InputModule inputModule;
    private Settings settings;
    private Job job;
    private File path;



    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        settings = new Settings(this);
        inputModule = new InputModule();

        setupCellValueFactory();
        setupTableMenuItems();
        setupColumnTextField();
        setupTableCellCommit();
    }


    private void setupJobItems() {
        inputModule.openDirectoryChooser(getOwner());
        InputModule.STATUS state = inputModule.loadFile();

        if(state == InputModule.STATUS.SUCCESS){
            table.setItems(inputModule.getJobs());
        }

        labelLoadInfo.setText(inputModule.getInfo(state));
    }


    public void update() {
        for (Job job : table.getItems()) {
            job.setAverageSpeed(job.getAverageSpeed() * Settings.CONVERSION_VALUE);
            job.setSecondsUntilSteadyState(settings.getRequiredRunsForSteadyState());
            job.skipSeconds(settings.getSkipCounter());
            job.updateRunsData();
        }

        speedColumn.setText("Average Speed " + Settings.getConversion());
        
        table.getColumns().getFirst().setVisible(false);
        table.getColumns().getFirst().setVisible(true);
    }

    private void setupCellValueFactory() {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("FileName"));

        speedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        speedColumn.setText("Average Speed " + Settings.getConversion());

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("TimeInSec"));
        lastModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("FileLastModifiedDate"));
        fileCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("FileCreationDate"));
        cvColumn.setCellValueFactory(new PropertyValueFactory<>("cvThreshold"));
        alphaColumn.setCellValueFactory(cell -> cell.getValue().alphaProperty().asObject());
    }

    private void setupColumnTextField() {
        alphaColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                FXCollections.observableArrayList(0.01, 0.05, 0.1)
        ));

        cvColumn.setCellFactory(tc -> new Utils.ValidatedDoubleTableCell<>(
                labelLoadInfo, Job.MIN_CV_THRESHOLD, Job.MAX_CV_THRESHOLD, Job.DEFAULT_CV_THRESHOLD,
                String.format("CV threshold must be a value between %f and %f", Job.MIN_CV_THRESHOLD, Job.MAX_CV_THRESHOLD)
        ));

        table.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Job job = table.getSelectionModel().getSelectedItem();
                if (job != null) {
                    this.job = job;
                }
            }
        });
    }

    private void setupTableMenuItems() {
        Utils.CustomTableRowFactory menuItems = new Utils.CustomTableRowFactory();
        menuItems.addMenuItem("Draw Job Speed", this::onActionDrawJobSpeed);
        menuItems.addMenuItem("Draw Job Frequency", this::onActionDrawJobFreq);
        menuItems.addMenuItem("Confidence Interval", this::onActionCalcConInt);
        menuItems.addMenuItem("Anova", this::onActionCalcAnova);
        menuItems.addMenuItem("CV", this::onActionCalcCV);
        menuItems.addMenuItem("T-Test", this::onActionCalcTTest);
        menuItems.addMenuItem("U-Test", this::onActionCalcMannWhitneyTest);
        menuItems.addMenuItem("Tukey-HSD", this::onActionCalcTukeyHSD);

        table.setRowFactory(menuItems);
    }


    private void setupTableCellCommit() {
        cvColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setCvThreshold(t.getNewValue());
        });
    }

    private void onActionDrawJobSpeed(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        String yAxisLabel = "Speed " + Settings.getConversion();
        charter.drawGraph("Job Speed", "Time in (ms)", yAxisLabel, new Charter.ChartData("Job speed",job.getSeries()));
        charter.openWindow();
    }

    private void onActionDrawJobFreq(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        charter.drawGraph("Speed Frequency", "Speed", "Frequency", new Charter.ChartData("Speed frequency",job.getFrequencySeries()));
        charter.openWindow();
    }

    private void onActionCalcConInt(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        ConInt conInt = new ConInt(job, settings);
        conInt.calculate();
        conInt.openWindow();
    }

    private void onActionCalcAnova(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Anova anova = new Anova(job, settings);
        anova.calculate();
        anova.openWindow();
    }


    private void onActionCalcCV(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        CoV cov = new CoV(job, settings);
        cov.calculate();
        cov.openWindow();
    }


    private void onActionCalcTTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        AtoolTTest atoolTTest = new AtoolTTest(job, settings);
        atoolTTest.calculate();
        atoolTTest.openWindow();
    }

    private void onActionCalcMannWhitneyTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        MannWhitney uTest = new MannWhitney(job, settings);
        uTest.calculate();
        uTest.openWindow();
    }


    private void onActionCalcTukeyHSD(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Anova anova = new Anova(job, settings);
        TukeyHSD tukey = new TukeyHSD();
        anova.setPostHocTest(tukey);
        anova.calculate();
        tukey.openWindow();
    }


    @FXML
    private void openLogfile() {
        labelLoadInfo.setText("trying to open files...");
        setupJobItems();
    }


    @FXML
    private void onActionRefreshTable() {
        InputModule.STATUS state = inputModule.loadFile();
        labelLoadInfo.setText(inputModule.getInfo(state));
    }


    @FXML
    private void onActionSaveAllEval() {
        File path = openDirectoryChooser();

        for (Job job : table.getItems()) {
            SteadyStateEval eval = new SteadyStateEval(job, this.settings);
            eval.setPath(path);
            eval.saveEval();
        }
    }

    @FXML
    private void onActionCalcualteSteadyState() {
        if(this.job != null && this.settings != null) {
            SteadyStateEval eval = new SteadyStateEval(this.job, this.settings);
            eval.setOwner(getOwner());
            eval.openWindow();
        } else {
            labelLoadInfo.setText("Please select a Job!");
            Logging.log(Level.WARNING, "Primary Controller","Error in opening SteadyStateEvalButton");
        }
    }

    @FXML
    private void openGeneralSettings() {
        settings.openWindow();
    }

    @FXML
    private void onActionKey(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            int pos = table.getSelectionModel().getSelectedIndex();
            Job removedJob = table.getItems().remove(pos);
            Logging.log(Level.INFO, "Primary Controller",String.format("Removed Job -> %s", removedJob.toString()));
        }
    }

    public Window getOwner(){
        return steadyStateEvalButton.getScene().getWindow();
    }

    public File openDirectoryChooser(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose a directory");

        if(path == null){
            path = new File(System.getProperty("user.home"));
        } else {
            directoryChooser.setInitialDirectory(path);
        }

        return directoryChooser.showDialog(getOwner());
    }
}



