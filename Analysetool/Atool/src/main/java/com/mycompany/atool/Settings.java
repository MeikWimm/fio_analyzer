/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
public class Settings implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( Settings.class.getName() );
    private final Stage stage;
    private boolean isFileAttr;
    private boolean isSpeedPerSecSelected;

    
    @FXML public CheckBox checkboxFileAtt;
    @FXML public CheckBox checkboxSpeedPerSec;
    @FXML public TextField textboxSpeedPerSec;
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new CustomFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }
    
    public static int NUMBER_AFTER_COMMA = 10000;
    public static double CONVERT_SPEED_UNIT = InputModule.CONVERT.getConvertValue(InputModule.CONVERT.DEFAULT);

    public Settings(){
        stage = new Stage();
        init();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(checkboxSpeedPerSec.isSelected()){
            textboxSpeedPerSec.setDisable(false);
        } else {
            textboxSpeedPerSec.setDisable(true);
        }
    }
    
    private void init(){
        try {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/Settings.fxml"));
        fxmlLoader.setController(this);
        Parent root1 = (Parent) fxmlLoader.load();
        /* 
         * if "fx:controller" is not set in fxml
         * fxmlLoader.setController(NewWindowController);
         */
        stage.setTitle("Settings");
        stage.setScene(new Scene(root1));
    } catch (IOException e) {
        LOGGER.log(Level.SEVERE, (Supplier<String>) e);
        LOGGER.log(Level.SEVERE, String.format("Coudn't open Settings Window! App state: %s", PrimaryController.STATUS.IO_EXCEPTION));
        }
    }
    public boolean isWindowOpen(){
        return stage.isShowing();
    }
    
    public void openWindow(){
        stage.show();
    }
    
    @FXML
    public void onActionFileAttributes(){
        this.isFileAttr = checkboxFileAtt.isSelected();
        LOGGER.log(Level.INFO, String.format("is FileAttr set to %b", this.isFileAttr));
    }
    
    @FXML
    public void onActionUseSpeedPerSec(){
        this.isSpeedPerSecSelected = checkboxSpeedPerSec.isSelected();
        LOGGER.log(Level.INFO, String.format("use Average Speed per Sec set to %b", this.isSpeedPerSecSelected));
        if(this.isSpeedPerSecSelected){
            textboxSpeedPerSec.setDisable(false);
        } else {
            textboxSpeedPerSec.setDisable(true);
        }
    }
}
