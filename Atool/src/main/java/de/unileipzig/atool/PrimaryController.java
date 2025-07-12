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


/**
 * The PrimaryController class serves as the main controller for the application's graphical user interface.
 * It manages the interaction between the user and the data model, handling events, updating views, and coordinating
 * the application's core operations.
 *
 * This controller is primarily responsible for initializing the GUI components, setting up tables for displaying
 * job-related data, handling events triggered by the user (e.g., menu actions, button clicks, keyboard inputs),
 * and performing operations like loading files and performing statistical calculations.
 *
 * The class implements the Initializable interface to guarantee the initialization of FX components after
 * the FXML elements are loaded.
 *
 * Core Functionality:
 * - Setting up table columns and defining data cell properties for displaying job data.
 * - Providing menu and button-based actions for features like loading files, refreshing the data table,
 *   and performing statistical evaluations.
 * - Handling user interactions such as editing cells in the table and responding to key inputs.
 * - Managing various statistical and visualization operations for the loaded jobs, such as calculating confidence
 *   intervals, conducting T-tests, and drawing graphs.
 *
 * Key Methods:
 * - initialize: Initializes the controller after the FXML elements are loaded and sets up the data table.
 * - setupCellValueFactory: Configures the columns of the table to define how data objects are displayed.
 * - setupTableMenuItems: Adds context menu items to table rows for performing job-specific operations.
 * - setupColumnTextField: Configures editable table columns with validation for numerical inputs (e.g., run counters, alpha values).
 * - update: Updates table data and reflects changes in settings.
 * - onActionRefreshTable: Refreshes the data table by reloading the file.
 * - openLogfile: Facilitates loading a log file containing job data into the table.
 * - onActionCalcualteSteadyState: Handles steady-state evaluation for a selected job, if any.
 * - openGeneralSettings: Opens the general settings window for the application.
 * - onActionKey: Handles keyboard input for operations like deleting a selected job.
 *
 * Dependencies:
 * This class relies on several external classes and modules such as Settings, InputModule, Job,
 * and various statistical and utility classes for its operation (e.g., ConInt, Anova, Charter, Utils).
 *
 * Usage Notes:
 * The controller is intended to be used as part of a JavaFX application. It expects the corresponding
 * FXML file to define the layout and wire up FX elements like MenuItems, Labels, Buttons, and TableColumns.
 */
public class PrimaryController implements Initializable {
    @FXML private MenuItem menuItem_generalSettings;
    @FXML private MenuItem menuItem_open;
    @FXML private MenuItem refreshTableMenuItem;
    @FXML private MenuBar menuBar;
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

    /**
     * Initializes the primary controller by setting up various modules and configurations.
     * This method is called automatically when the corresponding FXML file is loaded.
     *
     * @param arg0 the location of the FXML file or null if the location is not known.
     * @param arg1 the resource bundle to use for localization, or null if no resource bundle is available.
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        settings = new Settings(this);
        inputModule = new InputModule();


        setupCellValueFactory();
        setupTableMenuItems();
        setupColumnTextField();
        setupTableCellCommit();
    }

    /**
     * Configures the job items table by loading data from user-selected files.
     *
     * This method initiates the process of loading job data by opening a directory chooser
     * dialog for the user to select a folder. If the file loading operation is successful,
     * the loaded job items are set to populate the table view. Additionally, it updates a label
     * with corresponding status information regarding the file loading process.
     *
     * Operational Details:
     * - Opens a directory chooser for the user to select the input source.
     * - Calls the input module to load the selected file.
     * - Updates the table view with job data upon successful file loading.
     * - Displays feedback regarding the loading status in a label.
     */
    private void setupJobItems() {
        inputModule.openDirectoryChooser(getOwner());
        InputModule.STATUS state = inputModule.loadFile();

        if(state == InputModule.STATUS.SUCCESS){
            table.setItems(inputModule.getJobs());
        }

        labelLoadInfo.setText(inputModule.getInfo(state));
    }

    /**
     * Updates the table and corresponding data by checking if the settings have changed.
     * If changes are detected, it iterates through the items in the table and updates their run data.
     * Additionally, it updates the label of the speed column with the appropriate unit conversion
     * and performs a visibility toggle on the first column to refresh its state.
     * Finally, marks the settings as having been updated.
     */
    public void update() {
        if (settings.hasChanged()) {
            for (Job job : table.getItems()) {
                job.updateRunsData();
            }
        }

        speedColumn.setText("Average Speed " + Settings.getConversion()); // TODO labeling with unit
        settings.updatedSettings();

        table.getColumns().getFirst().setVisible(false);
        table.getColumns().getFirst().setVisible(true);
    }

    /**
     * Configures the cell value factories for a set of table columns in a JavaFX TableView.
     * This method maps specific property names of the data model to corresponding table columns
     * and applies custom cell formatting where necessary.
     *
     * The method performs the following configurations:
     * - Assigns property names (e.g., "ID", "File", etc.) to specific table columns using
     *   PropertyValueFactory to display corresponding data from the model.
     * - Applies a custom cell factory to the "AverageSpeed" column using
     *   TextFieldTableCell with a custom converter for appropriate formatting.
     * - Sets the header text for the "AverageSpeed" column dynamically by appending a unit of measurement
     *   retrieved from the Settings configuration.
     */
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

    /**
     * Configures the table columns to use text fields for editing and applies validation to ensure that
     * input values fall within specified bounds. Also sets up a mouse click handler to select a table row.
     *
     * This method initializes the cell factories for specific columns in a table with custom validations:
     * - For the `runsCounterColumn`, the input is validated as an integer within a defined range.
     * - For the `alphaColumn`, the input is validated as a double within a defined range.
     * - For the `cvColumn`, the input is validated as a double within a defined range.
     *
     * Validation is performed using utility classes, and the corresponding error messages are configured
     * to display when the input values fall outside the acceptable ranges.
     *
     * Additionally, a mouse click event handler is registered to handle row selection. When a row is clicked
     * with the primary mouse button, the selected `Job` object from the table is assigned to the local field.
     */
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

    /**
     * Configures the context menu items for the table rows. This method
     * utilizes a custom table row factory to add specific menu options
     * that perform statistical or visualization actions on the table data.
     * Each menu item is associated with a corresponding action handler.
     *
     * Menu items added:
     * - Draw Job Speed: Triggers the handler for drawing job speed visualization.
     * - Draw Job Frequency: Triggers the handler for drawing job frequency visualization.
     * - Confidence Interval: Triggers the calculation of a confidence interval.
     * - Anova: Triggers the calculation for the Analysis of Variance (ANOVA).
     * - CV: Triggers the calculation of the coefficient of variation (CV).
     * - T-Test: Triggers the calculation for a T-Test.
     * - U-Test: Triggers the calculation for a Mann-Whitney U-Test.
     * - Tukey-HSD: Triggers the calculation for Tukey's Honest Significant Difference (HSD) test.
     *
     * Sets the custom row factory with the configured menu items to the table.
     */
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

    /**
     * Configures the commit behavior for editable cells in a table.
     *
     * This method is used to define and handle the behavior when a user commits changes
     * to certain columns in a table. Specifically, it sets commit handlers for the
     * `runsCounterColumn`, `alphaColumn`, and `cvColumn` to update corresponding values
     * in the associated row objects upon editing.
     *
     * The following columns are configured:
     * - `runsCounterColumn`: Updates the `runsCounter` property of the Job entity with the new value.
     * - `alphaColumn`: Updates the `alpha` property of the Job entity with the new value.
     * - `cvColumn`: Updates the `cvThreshold` property of the Job entity with the new value.
     *
     * This ensures the table model remains synchronized with user edits.
     */
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

    /**
     * Handles the action for drawing the job speed graph from the provided table row.
     *
     * @param row the table row containing the job data for which the speed graph is to be drawn
     * @param table the table view that contains the list of jobs, including the specified row
     */
    private void onActionDrawJobSpeed(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        String yAxisLabel = "Speed " + Settings.getConversion();
        charter.drawGraph("Job Speed", "Time in (ms)", yAxisLabel, new Charter.ChartData("Job speed",job.getSeries()));
        charter.openWindow();
    }

    /**
     * Handles the action to draw a speed frequency graph for a given job.
     * This method retrieves a {@code Job} instance from the specified table row,
     * uses its frequency data, and generates a frequency graph displayed in a new window.
     *
     * @param row The table row containing the {@code Job} instance from which frequency data will be extracted.
     * @param table The table view containing the rows of {@code Job} instances.
     */
    private void onActionDrawJobFreq(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Charter charter = new Charter();
        charter.drawGraph("Speed Frequency", "Speed", "Frequency", new Charter.ChartData("Speed frequency",job.getFrequencySeries()));
        charter.openWindow();
    }

    /**
     * Handles the action to calculate confidence intervals for a specific job
     * selected in the provided table row. This method retrieves the corresponding job
     * information, performs the confidence interval calculation, and opens a new window
     * to display the results.
     *
     * @param row   the table row containing the job for which confidence intervals will be calculated
     * @param table the table view holding the list of jobs
     */
    private void onActionCalcConInt(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        ConInt conInt = new ConInt(job, settings);
        conInt.calculate();
        conInt.openWindow();
    }

    /**
     * Handles the action of performing an ANOVA calculation for the selected job in the table.
     * Retrieves the selected job, initializes the Anova object with the necessary settings,
     * performs the ANOVA calculation, and opens a window to display the results.
     *
     * @param row   the row in the table containing the selected job
     * @param table the table containing all job entries
     */
    private void onActionCalcAnova(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Anova anova = new Anova(job, settings);
        anova.calculate();
        anova.openWindow();
    }

    /**
     * Handles the calculation of the coefficient of variation (CV) for a given job.
     * This method retrieves the job data from the row, performs CV calculation,
     * and opens a window displaying the results.
     *
     * @param row   the table row containing the job for which the CV is calculated
     * @param table the table view used to retrieve and manage the job data
     */
    private void onActionCalcCV(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        CoV cov = new CoV(job, settings);
        cov.calculate();
        cov.openWindow();
    }

    /**
     * Handles the calculation of the T-Test for a specific job in the table and opens a window to display the results.
     *
     * @param row   the table row containing the job for which the T-Test is to be calculated
     * @param table the table view containing the job data
     */
    private void onActionCalcTTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        TTest tTest = new TTest(job, settings);
        tTest.calculate();
        tTest.openWindow();
    }

    /**
     * Handles the calculation of the Mann-Whitney U test for a specific job.
     * This method retrieves the selected job from the specified table row, performs
     * the Mann-Whitney U test calculation using the associated settings, and opens
     * a window displaying the results.
     *
     * @param row   The table row containing the job for which the Mann-Whitney U test
     *              will be calculated.
     * @param table The table view that holds and displays the list of jobs.
     */
    private void onActionCalcMannWhitneyTest(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        MannWhitney uTest = new MannWhitney(job, settings);
        uTest.calculate();
        uTest.openWindow();
    }

    /**
     * Handles the Tukey HSD (Honestly Significant Difference) post-hoc test calculation for a selected job.
     * This method retrieves the job from the specified table row, initializes an ANOVA object with
     * the associated settings, assigns a Tukey HSD test as the post-hoc analysis, performs
     * the calculations, and opens a new window to display the results.
     *
     * @param row   the table row containing the job for which the Tukey HSD test will be calculated
     * @param table the table view holding the list of jobs
     */
    private void onActionCalcTukeyHSD(TableRow<Job> row, TableView<Job> table) {
        Job job = row.getItem();
        Anova anova = new Anova(job, settings);
        TukeyHSD tukey = new TukeyHSD();
        anova.setPostHocTest(tukey);
        anova.calculate();
        tukey.openWindow();
    }

    /**
     * Handles the action to open log files for processing.
     *
     * This method updates the UI label with feedback about the current operation and
     * initiates the loading of job items by invoking the {@code setupJobItems} method.
     *
     * Functional Details:
     * - Displays a message in the {@code labelLoadInfo} to inform the user that the system
     *   is attempting to open log files.
     * - Delegates the process of setting up job items to the {@code setupJobItems} method.
     *
     * Intended Behavior:
     * - Provides a visual indication of the file loading process.
     * - Efficiently triggers file selection and data loading by calling another method.
     */
    @FXML
    private void openLogfile() {
        labelLoadInfo.setText("trying to open files...");
        setupJobItems();
    }

    /**
     * Handles the action event to refresh the table view with updated data.
     * This method is triggered by an associated UI element (e.g., a button or menu item).
     *
     * Functional Details:
     * - Invokes the {@code inputModule.loadFile()} method to load data from the selected input source.
     * - Retrieves the status of the file loading process and displays relevant information in {@code labelLoadInfo}.
     *
     * Intended Behavior:
     * - Successfully loads data into the application through the {@code inputModule}.
     * - Updates the UI label to provide immediate feedback on the success or failure of the load operation.
     */
    @FXML
    private void onActionRefreshTable() {
        InputModule.STATUS state = inputModule.loadFile();
        labelLoadInfo.setText(inputModule.getInfo(state));
    }

    /**
     * Handles the action event for calculating the steady state.
     * This method is triggered when the corresponding UI button is pressed.
     * It performs the following actions:
     * - Checks if the required job and settings objects are not null.
     * - If both objects are valid, it creates a new instance of {@code SteadyStateEval}
     *   and associates it with the current job and settings.
     * - The evaluation window is opened for further processing.
     * - If either the job or settings objects are null, an error message is displayed
     *   to the user via the {@code labelLoadInfo} label, indicating that a job must be selected.
     * - Logs a warning if there is an issue in invoking the steady state evaluation window.
     */
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

    /**
     * Opens the General Settings window.
     *
     * This method is invoked when the user interacts with the associated UI element,
     * such as a menu item or button, to access the application's general settings.
     * It delegates the task of loading and displaying the settings window
     * to the {@code settings.openWindow()} method, which manages the initialization
     * and configuration of the settings UI.
     *
     * Intended Behavior:
     * - Opens a new modal window displaying the general settings.
     * - Ensures that the settings window is non-resizable for consistent UI presentation.
     *
     * Error Handling:
     * - If an issue occurs while opening the settings window, the error is logged
     *   using the application's logging system.
     */
    @FXML
    private void openGeneralSettings() {
        settings.openWindow();
    }

    /**
     * Handles keyboard actions within the application, specifically for key events.
     * This method is triggered when a key action occurs in the associated UI component.
     * It listens for specific key inputs and performs appropriate actions.
     * For example, if the DELETE key is pressed, it removes the selected job from the table.
     *
     * @param e the {@code KeyEvent} instance representing the keyboard action. It contains
     *          the details of the key event, such as the key code, modifiers, and other relevant
     *          information.
     */
    // Handling keyboard input
    @FXML
    private void onActionKey(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            int pos = table.getSelectionModel().getSelectedIndex();
            Job removedJob = table.getItems().remove(pos);
            Logging.log(Level.INFO, "Primary Controller",String.format("Removed Job -> %s", removedJob.toString()));
        }
    }

    /**
     * Retrieves the owner window of the current scene associated with the steadyStateEvalButton.
     *
     * @return the owner Window of the scene linked to the steadyStateEvalButton
     */
    public Window getOwner(){
        return steadyStateEvalButton.getScene().getWindow();
    }
}



