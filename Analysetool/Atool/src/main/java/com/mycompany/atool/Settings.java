/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
public class Settings implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( Settings.class.getName() );
    private boolean isFileAttr;
    private boolean isSpeedPerSecSelected;
    
    ToggleGroup toggleGorup = new ToggleGroup();

    
    @FXML public CheckBox checkboxFileAtt;
    @FXML public CheckBox checkboxSpeedPerSec;
    @FXML public Slider avSpeedSlider;
    @FXML public Label labelSliderVal;
    @FXML public Button buttonSaveSettings;
    @FXML public RadioButton radioButtonMebibyte;
    @FXML public RadioButton radioButtonKibiByte;
    @FXML public RadioButton radioButtonKiloByte;
    
    public final static int DEFAULT_SPEED_PER_MILLI = 1;
    public final static int MAX_SPEED_PER_MIILI = 2000;
    public final static int MIN_SPEED_PER_MIILI = 1;
    public static boolean HAS_CHANGED = false;
    private static CONVERT conversion = CONVERT.DEFAULT;
    
    public static double CONVERSION_VALUE = CONVERT.getConvertValue(CONVERT.DEFAULT);
    public static int AVERAGE_SPEED_PER_MILLISEC = 1;
    
    private PrimaryController primaryController;
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Settings"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    public enum CONVERT{
          DEFAULT, // KIBI_BYTE
          MEGA_BYTE,
          MEBI_BYTE,
          KILO_BYTE;

          public static double getConvertValue(CONVERT hl) {
              switch (hl) {
            case MEGA_BYTE:
                return 976.6;
            case MEBI_BYTE:
                return 1024.0;    
            case KILO_BYTE:
                return 1.0 / 1024.0;    
            default: // KIBI_BYTE
                return 1.0;
            }
        }
    }
    
    public Settings(PrimaryController primaryController){
        this.primaryController = primaryController;
    }
    
    public static int NUMBER_AFTER_COMMA = 10000;
    public static double CONVERT_SPEED_UNIT = CONVERT.getConvertValue(CONVERT.DEFAULT);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(checkboxSpeedPerSec.isSelected()){
            avSpeedSlider.setDisable(false);
        } else {
            avSpeedSlider.setDisable(true);
        }
        radioButtonKibiByte.setUserData(CONVERT.DEFAULT);
        radioButtonKiloByte.setUserData(CONVERT.KILO_BYTE);
        radioButtonMebibyte.setUserData(CONVERT.MEBI_BYTE);
        
        radioButtonKibiByte.setToggleGroup(toggleGorup);
        radioButtonKiloByte.setToggleGroup(toggleGorup);
        radioButtonMebibyte.setToggleGroup(toggleGorup);

        radioButtonKibiByte.setSelected(true);
        
        for (Toggle toggle : toggleGorup.getToggles()) {
            if(((CONVERT)toggle.getUserData()).equals((conversion))){
                toggle.setSelected(true);
            }
        }
        
        final ChangeListener<Number> numberChangeListener = (obs, old, val) -> {
            double roundedValue = Math.floor(val.doubleValue() / 50.0) * 50.0;
            if(roundedValue <= 1.0) roundedValue = 1;
            avSpeedSlider.valueProperty().set(roundedValue);
            labelSliderVal.setText(Integer.toString((int)roundedValue));
        };

        avSpeedSlider.valueProperty().addListener(numberChangeListener);
        
//        avSpeedSlider.valueProperty().addListener(new ChangeListener<Number>() {
//
//                @Override
//                public void changed(
//                   ObservableValue<? extends Number> observableValue, 
//                   Number oldValue, 
//                   Number newValue) { 
//                      labelSliderVal.textProperty().setValue(
//                           String.valueOf(newValue.intValue()));
//                  }
//            });
       
    }
    

    public void openWindow(){
    try {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/Settings.fxml"));
        fxmlLoader.setController(this);
        Parent root1 = (Parent) fxmlLoader.load();
        /* 
         * if "fx:controller" is not set in fxml
         * fxmlLoader.setController(NewWindowController);
         */
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(new Scene(root1));
                stage.show();
    } catch (IOException e) {
        LOGGER.log(Level.SEVERE, String.format("Coudn't open Settings Window! App state: %s", PrimaryController.STATUS.IO_EXCEPTION));
        }
    }
    
    @FXML
    public void onActionFileAttributes(){
        this.isFileAttr = checkboxFileAtt.isSelected();
    }
    
    @FXML
    public void onActionSaveSettings(){
        this.isFileAttr = checkboxFileAtt.isSelected();
        //LOGGER.log(Level.INFO, String.format("is FileAttr set to %b", this.isFileAttr));
        conversion = (CONVERT) toggleGorup.getSelectedToggle().getUserData(); 
        CONVERSION_VALUE = CONVERT.getConvertValue(conversion);
        AVERAGE_SPEED_PER_MILLISEC = (int) avSpeedSlider.getValue();
        Settings.HAS_CHANGED = true;
        
        // get a handle to the stage
        Stage stage = (Stage) buttonSaveSettings.getScene().getWindow();
        // do what you have to do
        stage.close();
        primaryController.update();
        
    }
    
    @FXML
    public void onActionUseSpeedPerSec(){
        this.isSpeedPerSecSelected = checkboxSpeedPerSec.isSelected();
        LOGGER.log(Level.INFO, String.format("use Average Speed per Sec set to %b", this.isSpeedPerSecSelected));
        if(this.isSpeedPerSecSelected){
            avSpeedSlider.setDisable(false);
        } else {
            avSpeedSlider.setDisable(true);
        }
    }
}
