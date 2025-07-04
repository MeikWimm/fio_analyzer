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
    public static final int MAX_SKIP_COUNT = 5;
    public static final int MIN_SKIP_COUNT = 0;
    public static final int DEFAULT_SKIP_COUNT = 0;

    public static final int MAX_REQUIRED_RUNS_FOR_STEADY_STATE = 10;
    public static final int MIN_REQUIRED_RUNS_FOR_STEADY_STATE = 2;
    public static final int DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE = 5;

    private double covThreshold = Job.DEFAULT_CV_THRESHOLD;
    private int requiredRunsForSteadyState = DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE;
    private int groupSize = 2;
    private boolean isSpeedPerMilliSelected;

    private static final int DIGIT = 3;
    public static final String DIGIT_FORMAT = "%,." + DIGIT + "f";
    public static final int FRACTION_DIGITS = DIGIT;

    public static double CONVERSION_VALUE = CONVERT.getConvertValue(CONVERT.DEFAULT);
    public static CONVERT CONVERSION = CONVERT.DEFAULT;

    private int anovaSkipRunsCounter = 0;
    private int covSkipRunsCounter = 0;
    private int conIntSkipRunsCounter = 0;
    private int tTestSkipRunsCounter = 0;
    private int uTestSkipRunsCounter = 0;
    private int cusumSkipRunsCounter = 0;

    private boolean anovaUseAdjacentRun = false;
    private boolean covUseAdjacentRun = false;
    private boolean conIntUseAdjacentRun = false;
    private boolean tTestUseAdjacentRun = false;
    private boolean uTestUseAdjacentRun = false;
    private boolean cusumUseAdjacentRun = false;

    private boolean isBonferroniANOVASelected = false;
    private boolean isBonferroniConIntSelected = false;
    private boolean isBonferroniTTestSelected = false;
    private boolean isBonferroniUTestSelected = false;

    private boolean hasChanged = false;

    @FXML public CheckBox checkboxSpeedPerSec;
    @FXML public CheckBox adjacentRunANOVAcheckbox;
    @FXML public CheckBox adjacentRunCoVcheckbox;
    @FXML public CheckBox adjacentRunConIntcheckbox;
    @FXML public CheckBox adjacentRunTTestcheckbox;
    @FXML public CheckBox adjacentRunUTestcheckbox;
    @FXML public CheckBox adjacentRunCUSUMcheckbox;

    @FXML public CheckBox bonferroniANOVAcheckbox;
    @FXML public CheckBox bonferroniConIntcheckbox;
    @FXML public CheckBox bonferroniTTestcheckbox;
    @FXML public CheckBox bonferroniUTestcheckbox;

    @FXML public Spinner<Integer> skipRunAnovaSpinner;
    @FXML public Spinner<Integer> skipRunCoVSpinner;
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

    public static String getConversion() {
        return switch (CONVERSION) {
            case CONVERT.MEBI_BYTE -> "(MebiByte)";
            case CONVERT.MEGA_BYTE -> "(MegaByte)";
            case CONVERT.KILO_BYTE -> "(KiloByte)";
            default -> "(KibiByte)";
        };
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
        skipRunCoVSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunConIntSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunTTestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunUTestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunCUSUMSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));

        requiredRunsForSteadyStateSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_REQUIRED_RUNS_FOR_STEADY_STATE, MAX_REQUIRED_RUNS_FOR_STEADY_STATE, DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE));

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
            if (toggle.getUserData().equals((CONVERSION))) {
                toggle.setSelected(true);
            }
        }

        bonferroniANOVAcheckbox.setSelected(isBonferroniANOVASelected);
        bonferroniConIntcheckbox.setSelected(isBonferroniConIntSelected);
        bonferroniTTestcheckbox.setSelected(isBonferroniTTestSelected);
        bonferroniUTestcheckbox.setSelected(isBonferroniUTestSelected);

        checkboxSpeedPerSec.setSelected(isSpeedPerMilliSelected);

        runCompareCounterSlider.setValue(groupSize);


        skipRunAnovaSpinner.getValueFactory().setValue(anovaSkipRunsCounter);
        skipRunCoVSpinner.getValueFactory().setValue(covSkipRunsCounter);
        skipRunConIntSpinner.getValueFactory().setValue(conIntSkipRunsCounter);
        skipRunTTestSpinner.getValueFactory().setValue(tTestSkipRunsCounter);
        skipRunUTestSpinner.getValueFactory().setValue(uTestSkipRunsCounter);
        skipRunCUSUMSpinner.getValueFactory().setValue(cusumSkipRunsCounter);

        requiredRunsForSteadyStateSpinner.getValueFactory().setValue(requiredRunsForSteadyState);

        adjacentRunANOVAcheckbox.setSelected(anovaUseAdjacentRun);
        adjacentRunCoVcheckbox.setSelected(covUseAdjacentRun);
        adjacentRunConIntcheckbox.setSelected(conIntUseAdjacentRun);
        adjacentRunTTestcheckbox.setSelected(tTestUseAdjacentRun);
        adjacentRunUTestcheckbox.setSelected(uTestUseAdjacentRun);
        adjacentRunCUSUMcheckbox.setSelected(cusumUseAdjacentRun);
    }


    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/Settings.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            Logging.log(Level.SEVERE, "Settings", "Coudn't open Settings Window! App state");
        }
    }

    @FXML
    public void onActionUseSpeedPerSec(ActionEvent event) {
        isSpeedPerMilliSelected = checkboxSpeedPerSec.isSelected();
        Logging.log(Level.INFO, "Settings",String.format("use Average Speed per Sec set to %b", isSpeedPerMilliSelected));
        avSpeedSlider.setDisable(!isSpeedPerMilliSelected);
    }


    private void onActionSaveSettings(ActionEvent actionEvent) {
        CONVERSION = (CONVERT) toggleGorup.getSelectedToggle().getUserData();
        CONVERSION_VALUE = CONVERT.getConvertValue(CONVERSION);
        groupSize = (int) runCompareCounterSlider.getValue();
        anovaUseAdjacentRun = adjacentRunANOVAcheckbox.isSelected();
        covUseAdjacentRun = adjacentRunCoVcheckbox.isSelected();
        conIntUseAdjacentRun = adjacentRunConIntcheckbox.isSelected();
        tTestUseAdjacentRun = adjacentRunTTestcheckbox.isSelected();
        uTestUseAdjacentRun = adjacentRunUTestcheckbox.isSelected();
        cusumUseAdjacentRun = adjacentRunCUSUMcheckbox.isSelected();

        anovaSkipRunsCounter = skipRunAnovaSpinner.getValue();
        covSkipRunsCounter = skipRunCoVSpinner.getValue();
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
            Logging.log(Level.SEVERE, "Settings", "No PrimaryController set!");
        }
        Logging.log(Level.INFO, "Settings", "Settings saved");
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

    public int getGroupSize() {
        return groupSize;
    }

    public boolean isAnovaUseAdjacentRun() {
        return anovaUseAdjacentRun;
    }

    public boolean isCovUseAdjacentRun() {
        return covUseAdjacentRun;
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

    public int getAnovaSkipRunsCounter() {
        return anovaSkipRunsCounter;
    }

    public int getCovSkipRunsCounter() {
        return covSkipRunsCounter;
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
