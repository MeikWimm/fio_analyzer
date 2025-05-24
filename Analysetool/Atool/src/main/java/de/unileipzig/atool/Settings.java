/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.beans.value.ChangeListener;
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
    public final static int DEFAULT_SPEED_PER_MILLI = 1;
    public final static int MAX_SPEED_PER_MIILI = 2000;
    public final static int MIN_SPEED_PER_MIILI = 1;
    public static final boolean SKIP_GROUPS = false;
    private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());
    private static final int DIGIT = 3;
    public static final String DIGIT_FORMAT = "%,." + DIGIT + "f";
    public static final int FRACTION_DIGITS = DIGIT;
    public static boolean HAS_CHANGED = false;
    public static double CONVERSION_VALUE = CONVERT.getConvertValue(CONVERT.DEFAULT);
    public static int AVERAGE_SPEED_PER_MILLISEC = DEFAULT_SPEED_PER_MILLI;
    public static int RUN_TO_COMPARE_TO_SIZE = 2;
    public static int NUMBER_AFTER_COMMA = 10000;
    public static double CONVERT_SPEED_UNIT = CONVERT.getConvertValue(CONVERT.DEFAULT);
    private static CONVERT conversion = CONVERT.DEFAULT;

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    @FXML public CheckBox checkboxSpeedPerSec;
    @FXML public Slider avSpeedSlider;
    @FXML public Slider runCompareCounterSlider;
    @FXML public Label labelSliderVal;
    @FXML public Button buttonSaveSettings;
    @FXML public RadioButton radioButtonMebibyte;
    @FXML public RadioButton radioButtonKibiByte;
    @FXML public RadioButton radioButtonKiloByte;
    private final ToggleGroup toggleGorup = new ToggleGroup();
    private boolean isFileAttr;
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

        radioButtonKibiByte.setSelected(true);

        for (Toggle toggle : toggleGorup.getToggles()) {
            if (toggle.getUserData().equals((conversion))) {
                toggle.setSelected(true);
            }
        }

        final ChangeListener<Number> numberChangeListener = (obs, old, val) -> {
            double roundedValue = Math.floor(val.doubleValue() / 50.0) * 50.0;
            if (roundedValue <= 1.0) roundedValue = 1;
            avSpeedSlider.valueProperty().set(roundedValue);
            labelSliderVal.setText(Integer.toString((int) roundedValue));
        };

        avSpeedSlider.valueProperty().addListener(numberChangeListener);
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
    public void onActionSaveSettings() {
        //LOGGER.log(Level.INFO, String.format("is FileAttr set to %b", this.isFileAttr));
        conversion = (CONVERT) toggleGorup.getSelectedToggle().getUserData();
        CONVERSION_VALUE = CONVERT.getConvertValue(conversion);
        AVERAGE_SPEED_PER_MILLISEC = (int) avSpeedSlider.getValue();
        RUN_TO_COMPARE_TO_SIZE = (int) runCompareCounterSlider.getValue();
        Settings.HAS_CHANGED = true;

        // get a handle to the stage
        Stage stage = (Stage) buttonSaveSettings.getScene().getWindow();
        // do what you have to do
        stage.close();
        primaryController.update();

    }

    @FXML
    public void onActionUseSpeedPerSec() {
        boolean isSpeedPerSecSelected = checkboxSpeedPerSec.isSelected();
        LOGGER.log(Level.INFO, String.format("use Average Speed per Sec set to %b", isSpeedPerSecSelected));
        avSpeedSlider.setDisable(!isSpeedPerSecSelected);
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
