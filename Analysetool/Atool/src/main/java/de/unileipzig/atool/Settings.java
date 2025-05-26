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

    public static final boolean SKIP_GROUPS_ANOVA = false;
    public static final boolean SKIP_GROUPS_CON_INT = false;
    public static final boolean SKIP_GROUPS_T_TEST = false;
    public static final boolean SKIP_GROUPS_U_TEST = false;
    public static final boolean SKIP_GROUPS_CUSUM = false;
    public static final boolean SKIP_GROUPS_TUKEY_HSD = false;

    public static int GROUP_SIZE = 2;

    private static boolean IS_SPEED_PER_MILLI_SELECTED;

    private static final int DIGIT = 3;
    public static final String DIGIT_FORMAT = "%,." + DIGIT + "f";
    public static final int FRACTION_DIGITS = DIGIT;

    public static double CONVERSION_VALUE = CONVERT.getConvertValue(CONVERT.DEFAULT);
    public static int AVERAGE_SPEED_PER_MILLISEC = DEFAULT_SPEED_PER_MILLI;
    private static CONVERT conversion = CONVERT.DEFAULT;

    public static boolean HAS_CHANGED = false;

    @FXML public CheckBox checkboxSpeedPerSec;
    @FXML public Slider avSpeedSlider;
    @FXML public Slider runCompareCounterSlider;
    @FXML public Label labelSliderVal;
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

        final ChangeListener<Number> numberChangeListener = (obs, old, val) -> {
            double roundedValue = Math.floor(val.doubleValue() / 50.0) * 50.0;
            if (roundedValue <= 1.0) roundedValue = 1;
            avSpeedSlider.valueProperty().set(roundedValue);
            labelSliderVal.setText(Integer.toString((int) roundedValue));
        };

        buttonSaveSettings.setOnAction(this::onActionSaveSettings);
        avSpeedSlider.valueProperty().addListener(numberChangeListener);

        initSettings();
    }

    private void initSettings() {
        radioButtonKibiByte.setSelected(true);

        for (Toggle toggle : toggleGorup.getToggles()) {
            if (toggle.getUserData().equals((conversion))) {
                toggle.setSelected(true);
            }
        }

        checkboxSpeedPerSec.setSelected(IS_SPEED_PER_MILLI_SELECTED);
        avSpeedSlider.setDisable(!IS_SPEED_PER_MILLI_SELECTED);
        avSpeedSlider.setValue(AVERAGE_SPEED_PER_MILLISEC);
        runCompareCounterSlider.setValue(GROUP_SIZE);
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
        IS_SPEED_PER_MILLI_SELECTED = checkboxSpeedPerSec.isSelected();
        LOGGER.log(Level.INFO, String.format("use Average Speed per Sec set to %b", IS_SPEED_PER_MILLI_SELECTED));
        avSpeedSlider.setDisable(!IS_SPEED_PER_MILLI_SELECTED);
    }


    private void onActionSaveSettings(ActionEvent actionEvent) {
        conversion = (CONVERT) toggleGorup.getSelectedToggle().getUserData();
        CONVERSION_VALUE = CONVERT.getConvertValue(conversion);
        AVERAGE_SPEED_PER_MILLISEC = (int) avSpeedSlider.getValue();
        GROUP_SIZE = (int) runCompareCounterSlider.getValue();
        Settings.HAS_CHANGED = true;

        primaryController.update();
        System.out.println("Saved");
        Stage stage = (Stage) buttonSaveSettings.getScene().getWindow();
        stage.close();
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
