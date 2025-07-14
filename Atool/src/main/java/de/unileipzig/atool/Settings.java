/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.MathUtils;
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
import java.util.logging.Level;

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

    private int requiredRunsForSteadyState = DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE;
    private int groupSize = 2;

    private static final int DIGIT = 3;
    public static final String DIGIT_FORMAT = "%,." + DIGIT + "f";
    public static final int FRACTION_DIGITS = DIGIT;

    public static double CONVERSION_VALUE = MathUtils.CONVERT.getConvertValue(MathUtils.CONVERT.DEFAULT);
    public static MathUtils.CONVERT CONVERSION = MathUtils.CONVERT.DEFAULT;

    private int anovaSkipRunsCounter = 0;
    private int covSkipRunsCounter = 0;
    private int conIntSkipRunsCounter = 0;
    private int tTestSkipRunsCounter = 0;
    private int uTestSkipRunsCounter = 0;

    private boolean isBonferroniANOVASelected = false;
    private boolean isBonferroniConIntSelected = false;
    private boolean isBonferroniTTestSelected = false;
    private boolean isBonferroniUTestSelected = false;

    private boolean hasChanged = false;


    @FXML public CheckBox bonferroniANOVAcheckbox;
    @FXML public CheckBox bonferroniConIntcheckbox;
    @FXML public CheckBox bonferroniTTestcheckbox;
    @FXML public CheckBox bonferroniUTestcheckbox;

    @FXML public Spinner<Integer> skipRunAnovaSpinner;
    @FXML public Spinner<Integer> skipRunCoVSpinner;
    @FXML public Spinner<Integer> skipRunConIntSpinner;
    @FXML public Spinner<Integer> skipRunTTestSpinner;
    @FXML public Spinner<Integer> skipRunUTestSpinner;
    @FXML public Spinner<Integer> requiredRunsForSteadyStateSpinner;

    @FXML public Slider runCompareCounterSlider;
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
            case MathUtils.CONVERT.MEBI_BYTE -> "(MebiByte)";
            case MathUtils.CONVERT.MEGA_BYTE -> "(MegaByte)";
            case MathUtils.CONVERT.KILO_BYTE -> "(KiloByte)";
            default -> "(KibiByte)";
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        radioButtonKibiByte.setUserData(MathUtils.CONVERT.DEFAULT);
        radioButtonKiloByte.setUserData(MathUtils.CONVERT.KILO_BYTE);
        radioButtonMebibyte.setUserData(MathUtils.CONVERT.MEBI_BYTE);

        radioButtonKibiByte.setToggleGroup(toggleGorup);
        radioButtonKiloByte.setToggleGroup(toggleGorup);
        radioButtonMebibyte.setToggleGroup(toggleGorup);

        skipRunAnovaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunCoVSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunConIntSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunTTestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));
        skipRunUTestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_COUNT, MAX_SKIP_COUNT, DEFAULT_SKIP_COUNT));

        requiredRunsForSteadyStateSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_REQUIRED_RUNS_FOR_STEADY_STATE, MAX_REQUIRED_RUNS_FOR_STEADY_STATE, DEFAULT_REQUIRED_RUNS_FOR_STEADY_STATE));

        buttonSaveSettings.setOnAction(this::onActionSaveSettings);

        initSettings();
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

        runCompareCounterSlider.setValue(groupSize);


        skipRunAnovaSpinner.getValueFactory().setValue(anovaSkipRunsCounter);
        skipRunCoVSpinner.getValueFactory().setValue(covSkipRunsCounter);
        skipRunConIntSpinner.getValueFactory().setValue(conIntSkipRunsCounter);
        skipRunTTestSpinner.getValueFactory().setValue(tTestSkipRunsCounter);
        skipRunUTestSpinner.getValueFactory().setValue(uTestSkipRunsCounter);

        requiredRunsForSteadyStateSpinner.getValueFactory().setValue(requiredRunsForSteadyState);
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

    private void onActionSaveSettings(ActionEvent actionEvent) {
        CONVERSION = (MathUtils.CONVERT) toggleGorup.getSelectedToggle().getUserData();
        CONVERSION_VALUE = MathUtils.CONVERT.getConvertValue(CONVERSION);
        groupSize = (int) runCompareCounterSlider.getValue();

        anovaSkipRunsCounter = skipRunAnovaSpinner.getValue();
        covSkipRunsCounter = skipRunCoVSpinner.getValue();
        conIntSkipRunsCounter = skipRunConIntSpinner.getValue();
        tTestSkipRunsCounter = skipRunTTestSpinner.getValue();
        uTestSkipRunsCounter = skipRunUTestSpinner.getValue();

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

    public void updatedSettings() {
        hasChanged = false;
    }

    public void setAnovaSkipRunsCounter(int anovaSkipRunsCounter) {
        this.anovaSkipRunsCounter = anovaSkipRunsCounter;
    }

    public void setTTestSkipRunsCounter(int tTestSkipRunsCounter) {
        this.tTestSkipRunsCounter = tTestSkipRunsCounter;
    }

    public void setUTestSkipRunsCounter(int uTestSkipRunsCounter) {
        this.uTestSkipRunsCounter = uTestSkipRunsCounter;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }
}
