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
    public static final int MAX_SKIP_SECOND_COUNT = 60;
    public static final int MIN_SKIP_SECOND_COUNT = 0;
    public static final int DEFAULT_SKIP_SECOND_COUNT = 0;

    public static final int DEFAULT_WINDOW_SIZE = 30000;
    public static final int MIN_WINDOW_SIZE = 10000;
    public static final int MAX_WINDOW_SIZE = 60000;

    public static final int MAX_REQUIRED_SECONDS_FOR_STEADY_STATE = 60;
    public static final int MIN_REQUIRED_SECONDS_FOR_STEADY_STATE = 1;
    public static final int DEFAULT_REQUIRED_SECONDS_FOR_STEADY_STATE = 30;

    private int requiredRunsForSteadyState = DEFAULT_REQUIRED_SECONDS_FOR_STEADY_STATE;

    private static final int DIGIT = 3;
    public static final String DIGIT_FORMAT = "%,." + DIGIT + "f";
    public static final int FRACTION_DIGITS = DIGIT;

    public static double CONVERSION_VALUE = MathUtils.CONVERT.getConvertValue(MathUtils.CONVERT.DEFAULT);
    public static MathUtils.CONVERT CONVERSION = MathUtils.CONVERT.DEFAULT;

    private int skipCounter = 0;

    private boolean isBonferroniSelected = true;

    @FXML public CheckBox bonferroniCheckbox;

    @FXML public Spinner<Integer> skipCountSpinner;
    @FXML public Spinner<Integer> requiredSecondsForSteadyStateSpinner;

    @FXML public Slider windowSizeSlider;
    @FXML public Button buttonSaveSettings;
    @FXML public RadioButton radioButtonMebibyte;
    @FXML public RadioButton radioButtonKibiByte;
    @FXML public RadioButton radioButtonKiloByte;

    private final ToggleGroup toggleGorup = new ToggleGroup();
    private final PrimaryController primaryController;

    public static int WINDOW_SIZE = DEFAULT_WINDOW_SIZE;
    public final static int WINDOW_STEP_SIZE = 1000;

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

        skipCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SKIP_SECOND_COUNT,
                MAX_SKIP_SECOND_COUNT, DEFAULT_SKIP_SECOND_COUNT));

        requiredSecondsForSteadyStateSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_REQUIRED_SECONDS_FOR_STEADY_STATE,
                        MAX_REQUIRED_SECONDS_FOR_STEADY_STATE, DEFAULT_REQUIRED_SECONDS_FOR_STEADY_STATE
                ));

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

        bonferroniCheckbox.setSelected(isBonferroniSelected);

        windowSizeSlider.setValue(WINDOW_SIZE / 1000.0);
        windowSizeSlider.setMin(MIN_WINDOW_SIZE / 1000.0);
        windowSizeSlider.setMax(MAX_WINDOW_SIZE / 1000.0);

        skipCountSpinner.getValueFactory().setValue(skipCounter);
        requiredSecondsForSteadyStateSpinner.getValueFactory().setValue(requiredRunsForSteadyState);
    }



    private void onActionSaveSettings(ActionEvent actionEvent) {
        CONVERSION = (MathUtils.CONVERT) toggleGorup.getSelectedToggle().getUserData();
        CONVERSION_VALUE = MathUtils.CONVERT.getConvertValue(CONVERSION);
        WINDOW_SIZE = (int) windowSizeSlider.getValue() * 1000;

        requiredRunsForSteadyState = requiredSecondsForSteadyStateSpinner.getValue();
        skipCounter = skipCountSpinner.getValue();
        isBonferroniSelected = bonferroniCheckbox.isSelected();

        if(primaryController != null){
            primaryController.update();
        } else {
            Logging.log(Level.SEVERE, "Settings", "No PrimaryController set!");
        }

        Logging.log(Level.INFO, "Settings", "Settings saved");
        Stage stage = (Stage) buttonSaveSettings.getScene().getWindow();
        stage.close();
    }

    public void openWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/Settings.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            Logging.log(Level.SEVERE, "Settings", "Coudn't open Settings Window! App state");
            e.printStackTrace();
        }
    }

    public boolean isBonferroniSelected() {
        return isBonferroniSelected;
    }

    public int getSkipCounter() {
        return skipCounter;
    }

    public int getRequiredRunsForSteadyState() {
        return requiredRunsForSteadyState;
    }

    public void setSkipCounter(int skipCounter) {
        this.skipCounter = skipCounter;
    }

}
