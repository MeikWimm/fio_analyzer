/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
public class InputModule {
    //FileChooser chooser = new FileChooser();
    DirectoryChooser directoryChooser;
    ObservableList<Job> jobs = FXCollections.observableArrayList();
    

    
    
    public InputModule(){
        directoryChooser = new DirectoryChooser();

        
        //chooser = new FileChooser();
        //chooser.setTitle("Open File");

    }
    
    public void openSettings(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("secondary.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            //stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("ABC");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException ex) {
        }
    }

    public void loadFile(){
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        File[] files;
                
        if (selectedDirectory != null) {
            files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
            
            if(files == null || files.length == 0){
                System.err.println("No logs found!");
            } else {
                    for (File file1 : files) {
                        System.out.println(file1);
                }
                readFiles(files);
            }
        }
        //file = chooser.showOpenDialog(new Stage()); 
    }

    private void readFiles(File[] files) {
        openSettings();
        for (File file : files) {
            Job job = new Job();
            job.setFile(file);
            jobs.add(job);
            readData(file, job);
        }

    }
    
    public ObservableList<Job> getJobs(){
        return jobs;
    }

    private void readData(File file, Job job) {
        List<Double> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            long sum_speed = 0;
            double average_speed_by_100;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if(counter == 100){
                    average_speed_by_100 = sum_speed / 100;
                    data.add(average_speed_by_100);
                    counter = 0;
                    sum_speed = 0;
                }
                
                String[] s = line.split(", ");
                sum_speed += Long.valueOf(s[1]);

                counter++;
            }
        job.setData(data);
        } catch (IOException ex) {
            ex.toString();
        }
    }
}
