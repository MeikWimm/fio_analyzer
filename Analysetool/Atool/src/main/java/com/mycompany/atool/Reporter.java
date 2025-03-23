/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import javafx.scene.control.Alert;

/**
 *
 * @author meni1999
 */
public class Reporter {
    public static void showInfoForGraph(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This could take a while because of more then 10000 data points from this job.");
        alert.showAndWait();
    }
}
