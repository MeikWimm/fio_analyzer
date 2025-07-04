package de.unileipzig.atool;


import de.unileipzig.atool.Analysis.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;


public class PrimaryController implements Initializable {
    @FXML private Button steadyStateEvalButton;
    @FXML private Label labelLoadInfo;
    @FXML private TableView<Job> table;
    @FXML private TableColumn<Job, String> IDColumn;
    @FXML private TableColumn<Job, String> fileNameColumn;
    @FXML private TableColumn<Job, Integer> runsCounterColumn;
    @FXML private TableColumn<Job, Double> speedColumn;
    @FXML private TableColumn<Job, String> timeColumn;
    @FXML private TableColumn<Job, String> lastModifiedColumn;
    @FXML private TableColumn<Job, String> fileCreatedColumn;
    @FXML private TableColumn<Job, Double> alphaColumn;
    @FXML private TableColumn<Job, Double> cvColumn;

    private InputModule inputModule;
    private Settings settings;
    private Job job;


    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        settings = new Settings(this);
        inputModule = new InputModule(settings);

        setupCellValueFactory();
        setupTableMenuItems();
        setupColumnTextField();
        setupTableCellCommit();
    }

    private void setupJobItems() {
        Window ownerWindow = steadyStateEvalButton.getScene().getWindow();
        inputModule.openDirectoryChooser(ownerWindow);
        InputModule.STATUS state = inputModule.loadFile();

        if(state == InputModule.STATUS.SUCCESS){
            table.setItems(inputModule.getJobs());
        }

        labelLoadInfo.setText(inputModule.getInfo(state));
    }

    public void update() {
        if (settings.hasChanged()) {
            for (Job job : table.getItems()) {
                job.updateRunsData();
            }
        }

        speedColumn.setText("Average Speed " + Settings.getConversion()); // TODO labeling with unit
        table.getColumns().getFirst().setVisible(false);
        table.getColumns().getFirst().setVisible(true);

        settings.updatedSettings();
    }

    private void setupCellValueFactory() {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("File"));
        runsCounterColumn.setCellValueFactory(new PropertyValueFactory<>("RunsCounter"));
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        speedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new Utils.CustomStringConverter()));
        speedColumn.setText("Average Speed " + Settings.getConversion());

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("TimeInSec"));
        lastModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("FileLastModifiedDate"));
        fileCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("FileCreationDate"));
        alphaColumn.setCellValueFactory(new PropertyValueFactory<>("Alpha"));
        cvColumn.setCellValueFactory(new PropertyValueFactory<>("cvThreshold"));
    }

    // Code block setup for editing on a table row
    private void setupColumnTextField() {
        runsCounterColumn.setCellFactory(tc -> new Utils.ValidatedIntegerTableCell<>(
                labelLoadInfo, Job.MIN_RUN_COUNT, Job.MAX_RUN_COUNT, Job.DEFAULT_RUN_COUNT,
                String.format("Run count must be a value between %d and %d", Job.MIN_RUN_COUNT, Job.MAX_RUN_COUNT)
        ));

        alphaColumn.setCellFactory(tc -> new Utils.ValidatedDoubleTableCell<>(
                labelLoadInfo, Job.MIN_ALPHA, Job.MAX_ALPHA, Job.DEFAULT_ALPHA,
                String.format("Alpha must be a value between %f and %f", Job.MIN_ALPHA, Job.MAX_ALPHA)
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
        //menuItems.addMenuItem("CUSUM Runs", this::onActionCalcCusum);
        //menuItems.addMenuItem("CUSUM Job", this::onActionCalcCusumJob);

        table.setRowFactory(menuItems);
    }

    // Code block setup for commiting on a table row
    private void setupTableCellCommit() {
        runsCounterColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Integer> t) -> {
            t.getRowValue().setRunsCounter(t.getNewValue());
        });

        alphaColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setAlpha(t.getNewValue());
        });

        cvColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setCvThreshold(t.getNewValue());
        });
    }

    // Code block callback functions for table menu items
    private void onActionDrawJobSpeed(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        charter.drawGraph("Job Speed", "Time in (ms)", "Speed in KiBi", new Charter.ChartData("Job speed",job.getSeries()));
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

    private void onActionDrawJobFreq(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        charter.drawGraph("Speed Frequency", "Speed", "Frequency", new Charter.ChartData("Speed frequency",job.getFrequencySeries()));
    }

    private void onActionCalcTTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        TTest tTest = new TTest(job, settings);
        tTest.calculate();
        tTest.openWindow();
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
        TukeyHSD tukey = new TukeyHSD(anova);
        anova.setPostHocTest(tukey);
        anova.calculate();
        tukey.openWindow();
    }

    private void onActionCalcCusum(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        CUSUM cusum = new CUSUM(job, settings, job.getAlpha());
        cusum.calculate();
        cusum.draw();
    }

    private void onActionCalcCusumJob(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        CUSUM cusum = new CUSUM(job, settings, job.getAlpha());
        cusum.calculateWindowed();
        cusum.draw();
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
    private void onActionCalcualteSteadyState() {
        if(this.job != null && this.settings != null) {
            SteadyStateEval eval = new SteadyStateEval(this.job, this.settings);
            eval.setOwner(this.steadyStateEvalButton.getScene().getWindow());
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

    // Handling keyboard input
    @FXML
    private void onActionKey(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            int pos = table.getSelectionModel().getSelectedIndex();
            Job removedJob = table.getItems().remove(pos);
            Logging.log(Level.INFO, "Primary Controller",String.format("Removed Job -> %s", removedJob.toString()));
        }
    }
}



