package de.unileipzig.atool;


import de.unileipzig.atool.Analysis.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
    @FXML
    public MenuItem menuItem_open;
    @FXML
    public MenuItem menuItem_ANOVA;
    //private Anova anova = new Anova();
    @FXML
    public MenuItem menuItem_Info;
    @FXML
    public MenuItem menuItem_generalSettings;
    @FXML
    public Button button_refreshTable;
    @FXML
    public Button button_settings;
    @FXML
    public Label labelLoadInfo;
    @FXML
    public TableView<Job> table;
    @FXML
    public TableColumn<Job, String> IDColumn;
    @FXML
    public TableColumn<Job, String> fileNameColumn;
    @FXML
    public TableColumn<Job, Integer> runsCounterColumn;
    @FXML
    public TableColumn<Job, String> speedColumn;
    @FXML
    public TableColumn<Job, String> timeColumn;
    @FXML
    public TableColumn<Job, String> lastModifiedColumn;
    @FXML
    public TableColumn<Job, String> fileCreatedColumn;
    @FXML
    public TableColumn<Job, Double> epsilonColumn;
    @FXML
    public TableColumn<Job, Double> alphaColumn;

    public MenuItem helloItem;

    private InputModule inputModule;
    private Settings settings;


    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        inputModule = new InputModule();
        settings = new Settings(this);

        setupCellValueFactory();
        setupTableMenuItems();
        setupColumnTextField();
        setupTableCellCommit();

    }

    public void update() {

        if (Settings.HAS_CHANGED) {
            for (Job job : table.getItems()) {
                job.updateRunsData();
            }
        }

        table.getColumns().getFirst().setVisible(false);
        table.getColumns().getFirst().setVisible(true);

        Settings.HAS_CHANGED = false;
    }

    private void setupCellValueFactory() {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("File"));
        runsCounterColumn.setCellValueFactory(new PropertyValueFactory<>("RunsCounter"));
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("TimeInSec"));
        lastModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("FileLastModifiedDate"));
        fileCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("FileCreationDate"));
        epsilonColumn.setCellValueFactory(new PropertyValueFactory<>("Epsilon"));
        alphaColumn.setCellValueFactory(new PropertyValueFactory<>("Alpha"));
    }

    // Code block setup for editing on a table row
    private void setupColumnTextField() {
        //Column Edit setup
        runsCounterColumn.setCellFactory(tc -> new Utils.ValidatedIntegerTableCell<>(
                labelLoadInfo, Job.MIN_RUN_COUNT, Job.MAX_RUN_COUNT, Job.DEFAULT_RUN_COUNT,
                String.format("Run count must be a value between %d and %d", Job.MIN_RUN_COUNT, Job.MAX_RUN_COUNT)
        ));

        alphaColumn.setCellFactory(tc -> new Utils.ValidatedDoubleTableCell<>(
                labelLoadInfo, Job.MIN_ALPHA, Job.MAX_ALPHA, Job.DEFAULT_ALPHA,
                String.format("Alpha must be a value between %f and %f", Job.MIN_ALPHA, Job.MAX_ALPHA)
        ));


        epsilonColumn.setCellFactory(tc -> new Utils.ValidatedDoubleTableCell<>(
                labelLoadInfo, Job.MIN_EPSILON, Job.MAX_EPSILON, Job.DEFAULT_RUN_COUNT,
                String.format("Epsilon count must be a value between %f and %f", Job.MIN_EPSILON, Job.MAX_EPSILON)
        ));
    }

    private void setupTableMenuItems() {
        Utils.CustomTableRowFactory menuItems = new Utils.CustomTableRowFactory();
        menuItems.addMenuItem("Draw Job Speed", this::onActionDrawJobSpeed);
        menuItems.addMenuItem("Draw Job Frequency", this::onActionDrawJobFreq);
        menuItems.addMenuItem("Confidence Interval", this::onActionCalcConInt);
        menuItems.addMenuItem("Anova", this::onActionCalcAnova);
        menuItems.addMenuItem("T-Test", this::onActionCalcTTest);
        menuItems.addMenuItem("U-Test", this::onActionCalcMannWhitneyTest);
        menuItems.addMenuItem("Tukey-HSD", this::onActionCalcTukeyHSD);
        menuItems.addMenuItem("Cusum", this::onActionCalcCusum);

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

        epsilonColumn.setOnEditCommit((TableColumn.CellEditEvent<Job, Double> t) -> {
            t.getRowValue().setEpsilon(t.getNewValue());
        });
    }

    private void onActionDrawJobSpeed(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        charter.drawGraph("Job Speed", "Time", "Speed", "Speed", 0, job.getSeries());
    }

    private void onActionCalcConInt(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        ConInt tTest = new ConInt(job);
        tTest.calculateInterval();
        tTest.openWindow();
    }

    private void onActionCalcAnova(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Anova anova = new Anova(job, false, Settings.RUN_TO_COMPARE_TO_SIZE, job.getAlpha());
        anova.calculate();
        anova.openWindow();
    }

    private void onActionDrawJobFreq(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        CUSUM cusum = new CUSUM(job, true, Settings.RUN_TO_COMPARE_TO_SIZE, job.getAlpha());
        cusum.calculateWindowed();
        cusum.draw();
    }

    private void onActionCalcTTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        TTest tTest = new TTest(job, true, Settings.RUN_TO_COMPARE_TO_SIZE, job.getAlpha());
        tTest.calculate();
        tTest.openWindow();
    }

    private void onActionCalcMannWhitneyTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        MannWhitney tTest = new MannWhitney(job, false, Settings.RUN_TO_COMPARE_TO_SIZE, job.getAlpha());
        tTest.calculate();
        tTest.openWindow();
    }

    private void onActionCalcTukeyHSD(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Anova anova = new Anova(job, true, Settings.RUN_TO_COMPARE_TO_SIZE, job.getAlpha());
        TukeyHSD tTest = new TukeyHSD(anova);
        anova.calculate();
        anova.calculatePostHoc(tTest);
        tTest.openWindow();
    }

    private void onActionCalcCusum(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        CUSUM cusum = new CUSUM(job, true, Settings.RUN_TO_COMPARE_TO_SIZE, job.getAlpha());
        cusum.calculate();
        cusum.draw();
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
    private void openGeneralSettings() {
        settings.openWindow();
    }

    // Handling keyboard input
    @FXML
    private void onActionKey(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            int pos = table.getSelectionModel().getSelectedIndex();
            Job removedJob = table.getItems().remove(pos);
            LOGGER.log(Level.INFO, String.format("Removed Job -> %s", removedJob.toString()));
        }
    }

    public void onActionHello(ActionEvent event) {
        System.out.println("ITEMS SE");
    }

}



