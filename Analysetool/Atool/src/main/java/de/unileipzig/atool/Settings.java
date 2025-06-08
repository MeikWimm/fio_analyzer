/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author meni1999
 */
public class Settings implements Initializable {


    private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    public final static int DEFAULT_SPEED_PER_MILLI = 1;
    public final static int MAX_SPEED_PER_MIILI = 2000;
    public final static int MIN_SPEED_PER_MIILI = 1;

    public static final int MAX_SKIP_COUNT = 3;
    public static final int MIN_SKIP_COUNT = 0;
    public static final int DEFAULT_SKIP_COUNT = 0;

    public static final int MAX_WINDOW_SIZE = 1000;
    public static final int MIN_WINDOW_SIZE = 1;
    public static final int DEFAULT_WINDOW_SIZE = 100;

    public static final int MAX_REQUIRED_RUNS_FOR_STEADY_STATE = 10;
    public static final int MIN_REQUIRED_RUNS_FOR_STEADY_STATE = 2;
    public static final int DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE = 5;

    private double covThreshold = Job.DEFAULT_CV_THRESHOLD;
    private int requiredRunsForSteadyState = DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE;
    private int windowSize = 100; // Default window size 100

    private int groupSize = 2;

    private boolean isSpeedPerMilliSelected;

    private static final int DIGIT = 3;
    public static final String DIGIT_FORMAT = "%,." + DIGIT + "f";
    public static final int FRACTION_DIGITS = DIGIT;

    public static double CONVERSION_VALUE = CONVERT.getConvertValue(CONVERT.DEFAULT);
    public int averageSpeedPerMillisec = DEFAULT_SPEED_PER_MILLI;
    private static CONVERT conversion = CONVERT.DEFAULT;

    private int anovaSkipRunsCounter = 0;
    private int conIntSkipRunsCounter = 0;
    private int tTestSkipRunsCounter = 0;
    private int uTestSkipRunsCounter = 0;
    private int cusumSkipRunsCounter = 0;

    private boolean anovaUseAdjacentRun = false;
    private boolean conIntUseAdjacentRun = false;
    private boolean tTestUseAdjacentRun = false;
    private boolean uTestUseAdjacentRun = false;
    private boolean cusumUseAdjacentRun = false;
    private boolean tukeyUseAdjacentRun = false;

    private boolean isBonferroniANOVASelected = false;
    private boolean isBonferroniConIntSelected = false;
    private boolean isBonferroniTTestSelected = false;
    private boolean isBonferroniUTestSelected = false;

    private boolean hasChanged = false;

    @FXML public CheckBox checkboxSpeedPerSec;
    @FXML public CheckBox adjacentRunANOVAcheckbox;
    @FXML public CheckBox adjacentRunConIntcheckbox;
    @FXML public CheckBox adjacentRunTTestcheckbox;
    @FXML public CheckBox adjacentRunUTestcheckbox;
    @FXML public CheckBox adjacentRunCUSUMcheckbox;
    @FXML public CheckBox adjacentRunTukeycheckbox;

    @FXML public CheckBox skipRunANOVAcheckbox;
    @FXML public CheckBox skipRunConIntcheckbox;
    @FXML public CheckBox skipRunTTestcheckbox;
    @FXML public CheckBox skipRunUTestcheckbox;
    @FXML public CheckBox skipRunCUSUMcheckbox;

    @FXML public CheckBox bonferroniANOVAcheckbox;
    @FXML public CheckBox bonferroniConIntcheckbox;
    @FXML public CheckBox bonferroniTTestcheckbox;
    @FXML public CheckBox bonferroniUTestcheckbox;

    @FXML public Spinner<Integer> skipRunAnovaSpinner;
    @FXML public Spinner<Integer> skipRunConIntSpinner;
    @FXML public Spinner<Integer> skipRunTTestSpinner;
    @FXML public Spinner<Integer> skipRunUTestSpinner;
    @FXML public Spinner<Integer> skipRunCUSUMSpinner;
    @FXML public Spinner<Integer> requiredRunsForSteadyStateSpinner;

    @FXML public Slider avSpeedSlider;
    @FXML public Slider windowSlider;
    @FXML public Slider runCompareCounterSlider;
    @FXML public Label labelSliderVal;
    @FXML public Label windowValueLabel;
    @FXML public Button buttonSaveSettings;
    @FXML public RadioButton radioButtonMebibyte;
    @FXML public RadioButton radioButtonKibiByte;
    @FXML public RadioButton radioButtonKiloByte;
    private final ToggleGroup toggleGorup = new ToggleGroup();
    private final PrimaryController primaryController;

    public Settings(PrimaryController primaryController) {
        this.primaryController = primaryController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        avSpeedSlider.setDisable(!checkboxSpeedPerSec.isSelected());
        radioButtonKibiByte.setUserData(CONVERT.DEFAULT);
        radioButtonKiloByte.setUserData(CONVERT.KILO_BYTE);
        radioButtonMebibyte.setUserData(CONVERT.MEBI_BYTE);

        radioButtonKibiByte.setToggleGroup(toggleGorup);
        radioButtonKiloByte.setToggleGroup(toggleGorup);
        radioButtonMebibyte.setToggleGroup(toggleGorup);
        skipRunAnovaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunConIntSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunTTestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunUTestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunCUSUMSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        requiredRunsForSteadyStateSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_REQUIRED_RUNS_FOR_STEADY_STATE, MAX_REQUIRED_RUNS_FOR_STEADY_STATE, DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE));

//        bonferroniUTestcheckbox.setDisable(true);
//        bonferroniTTestcheckbox.setDisable(true);
//        bonferroniConIntcheckbox.setDisable(true);
//        bonferroniANOVAcheckbox.setDisable(true);

        buttonSaveSettings.setOnAction(this::onActionSaveSettings);
        avSpeedSlider.valueProperty().addListener(setupChangeListener(avSpeedSlider, labelSliderVal));
        windowSlider.valueProperty().addListener(setupChangeListener(windowSlider, windowValueLabel));

        initSettings();
    }

    private ChangeListener<Number> setupChangeListener(Slider slider, Label label) {
        return (obs, old, val) -> {
            double roundedValue = Math.floor(val.doubleValue() / 5.0) * 5.0;
            if (roundedValue <= 1.0) roundedValue = 1;
            slider.valueProperty().set(roundedValue);
            label.setText(Integer.toString((int) roundedValue));
        };
    }

    private void initSettings() {
        radioButtonKibiByte.setSelected(true);

        for (Toggle toggle : toggleGorup.getToggles()) {
            if (toggle.getUserData().equals((conversion))) {
                toggle.setSelected(true);
            }
        }

        bonferroniANOVAcheckbox.setSelected(isBonferroniANOVASelected);
        bonferroniConIntcheckbox.setSelected(isBonferroniConIntSelected);
        bonferroniTTestcheckbox.setSelected(isBonferroniTTestSelected);
        bonferroniUTestcheckbox.setSelected(isBonferroniUTestSelected);

        checkboxSpeedPerSec.setSelected(isSpeedPerMilliSelected);
        avSpeedSlider.setDisable(!isSpeedPerMilliSelected);
        avSpeedSlider.setValue(averageSpeedPerMillisec);
        runCompareCounterSlider.setValue(groupSize);
        windowSlider.setValue(windowSize);
        labelSliderVal.setText(Integer.toString(averageSpeedPerMillisec));
        windowValueLabel.setText(Integer.toString(windowSize));

        skipRunAnovaSpinner.getValueFactory().setValue(anovaSkipRunsCounter);
        skipRunConIntSpinner.getValueFactory().setValue(conIntSkipRunsCounter);
        skipRunTTestSpinner.getValueFactory().setValue(tTestSkipRunsCounter);
        skipRunUTestSpinner.getValueFactory().setValue(uTestSkipRunsCounter);
        skipRunCUSUMSpinner.getValueFactory().setValue(cusumSkipRunsCounter);
        requiredRunsForSteadyStateSpinner.getValueFactory().setValue(requiredRunsForSteadyState);

        adjacentRunANOVAcheckbox.setSelected(anovaUseAdjacentRun);
        adjacentRunConIntcheckbox.setSelected(conIntUseAdjacentRun);
        adjacentRunTTestcheckbox.setSelected(tTestUseAdjacentRun);
        adjacentRunUTestcheckbox.setSelected(uTestUseAdjacentRun);
        adjacentRunCUSUMcheckbox.setSelected(cusumUseAdjacentRun);
        adjacentRunTukeycheckbox.setSelected(tukeyUseAdjacentRun);
    }


    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/Settings.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Coudn't open Settings Window! App state");
        }
    }

    @FXML
    public void onActionUseSpeedPerSec(ActionEvent event) {
        isSpeedPerMilliSelected = checkboxSpeedPerSec.isSelected();
        LOGGER.log(Level.INFO, String.format("use Average Speed per Sec set to %b", isSpeedPerMilliSelected));
        avSpeedSlider.setDisable(!isSpeedPerMilliSelected);
    }


    private void onActionSaveSettings(ActionEvent actionEvent) {
        conversion = (CONVERT) toggleGorup.getSelectedToggle().getUserData();
        CONVERSION_VALUE = CONVERT.getConvertValue(conversion);
        averageSpeedPerMillisec = (int) avSpeedSlider.getValue();
        groupSize = (int) runCompareCounterSlider.getValue();
        windowSize = (int) windowSlider.getValue();
        anovaUseAdjacentRun = adjacentRunANOVAcheckbox.isSelected();
        conIntUseAdjacentRun = adjacentRunConIntcheckbox.isSelected();
        tTestUseAdjacentRun = adjacentRunTTestcheckbox.isSelected();
        uTestUseAdjacentRun = adjacentRunUTestcheckbox.isSelected();
        cusumUseAdjacentRun = adjacentRunCUSUMcheckbox.isSelected();
        tukeyUseAdjacentRun = adjacentRunTukeycheckbox.isSelected();
        anovaSkipRunsCounter = skipRunAnovaSpinner.getValue();
        conIntSkipRunsCounter = skipRunConIntSpinner.getValue();
        tTestSkipRunsCounter = skipRunTTestSpinner.getValue();
        uTestSkipRunsCounter = skipRunUTestSpinner.getValue();
        cusumSkipRunsCounter = skipRunCUSUMSpinner.getValue();
        requiredRunsForSteadyState = requiredRunsForSteadyStateSpinner.getValue();

        isBonferroniANOVASelected = bonferroniANOVAcheckbox.isSelected();
        isBonferroniConIntSelected = bonferroniConIntcheckbox.isSelected();
        isBonferroniTTestSelected = bonferroniTTestcheckbox.isSelected();
        isBonferroniUTestSelected = bonferroniUTestcheckbox.isSelected();

        hasChanged = true;

        if(primaryController != null){
            primaryController.update();
        } else {
            LOGGER.log(Level.SEVERE, "No PrimaryController set!");
        }
        LOGGER.log(Level.INFO, "Settings saved");
        Stage stage = (Stage) buttonSaveSettings.getScene().getWindow();
        stage.close();
    }

    public boolean isBonferroniANOVASelected() {
        return isBonferroniANOVASelected;
    }

    public boolean isBonferroniConIntSelected() {
        return isBonferroniConIntSelected;
    }

    public boolean isBonferroniTTestSelected() {
        return isBonferroniTTestSelected;
    }

    public boolean isBonferroniUTestSelected() {
        return isBonferroniUTestSelected;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public int getAverageSpeedPerMillisec() {
        return averageSpeedPerMillisec;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public boolean isSpeedPerMilliSelected() {
        return isSpeedPerMilliSelected;
    }

    public boolean isAnovaUseAdjacentRun() {
        return anovaUseAdjacentRun;
    }

    public boolean isConIntUseAdjacentRun() {
        return conIntUseAdjacentRun;
    }

    public boolean isTTestUseAdjacentRun() {
        return tTestUseAdjacentRun;
    }

    public boolean isUTestUseAdjacentRun() {
        return uTestUseAdjacentRun;
    }

    public boolean isCusumUseAdjacentRun() {
        return cusumUseAdjacentRun;
    }

    public boolean isTukeyUseAdjacentRun() {
        return tukeyUseAdjacentRun;
    }

    public int getAnovaSkipRunsCounter() {
        return anovaSkipRunsCounter;
    }

    public int getConIntSkipRunsCounter() {
        return conIntSkipRunsCounter;
    }

    public int getTTestSkipRunsCounter() {
        return tTestSkipRunsCounter;
    }

    public int getUTestSkipRunsCounter() {
        return uTestSkipRunsCounter;
    }

    public int getCusumSkipRunsCounter() {
        return cusumSkipRunsCounter;
    }

    public int getRequiredRunsForSteadyState() {
        return requiredRunsForSteadyState;
    }

    public void setRequiredRunsForSteadyState(int requiredRunsForSteadyState) {
        this.requiredRunsForSteadyState = requiredRunsForSteadyState;
    }

    public void updatedSettings() {
        hasChanged = false;
    }

    public void setAnovaSkipRunsCounter(int anovaSkipRunsCounter) {
        this.anovaSkipRunsCounter = anovaSkipRunsCounter;
    }

    public void setAnovaUseAdjacentRun(boolean anovaUseAdjacentRun) {
        this.anovaUseAdjacentRun = anovaUseAdjacentRun;
    }

    public void setConIntSkipRunsCounter(int conIntSkipRunsCounter) {
        this.conIntSkipRunsCounter = conIntSkipRunsCounter;
    }

    public void setConIntUseAdjacentRun(boolean conIntUseAdjacentRun) {
        this.conIntUseAdjacentRun = conIntUseAdjacentRun;
    }

    public void setTTestSkipRunsCounter(int tTestSkipRunsCounter) {
        this.tTestSkipRunsCounter = tTestSkipRunsCounter;
    }

    public void setTTestUseAdjacentRun(boolean tTestUseAdjacentRun) {
        this.tTestUseAdjacentRun = tTestUseAdjacentRun;
    }

    public void setUTestSkipRunsCounter(int uTestSkipRunsCounter) {
        this.uTestSkipRunsCounter = uTestSkipRunsCounter;
    }

    public void setUTestUseAdjacentRun(boolean uTestUseAdjacentRun) {
        this.uTestUseAdjacentRun = uTestUseAdjacentRun;
    }

    public void setCusumSkipRunsCounter(int cusumSkipRunsCounter) {
        this.cusumSkipRunsCounter = cusumSkipRunsCounter;
    }

    public void setCusumUseAdjacentRun(boolean cusumUseAdjacentRun) {
        this.cusumUseAdjacentRun = cusumUseAdjacentRun;
    }

    public void setTukeyUseAdjacentRun(boolean tukeyUseAdjacentRun) {
        this.tukeyUseAdjacentRun = tukeyUseAdjacentRun;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public double getCovThreshold() {
        return covThreshold;
    }

    public void setCovThreshold(double covThreshold) {
        this.covThreshold = covThreshold;
    }

    public enum CONVERT {
        DEFAULT, // KIBI_BYTE
        MEGA_BYTE,
        MEBI_BYTE,
        KILO_BYTE;

        public static double getConvertValue(CONVERT hl) {
            return switch (hl) {
                case MEGA_BYTE -> 976.6;
                case MEBI_BYTE -> 1024.0;
                case KILO_BYTE -> 1.0 / 1024.0;
                default -> // KIBI_BYTE
                        1.0;
            };
        }
    }
}
