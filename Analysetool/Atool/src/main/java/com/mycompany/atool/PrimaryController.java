package com.mycompany.atool;


import java.io.IOException;
import javafx.fxml.FXML;


public class PrimaryController {
    InputModule inputModule = new InputModule();

    
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void openLogfile() {
        inputModule.loadFile();
    }
}
