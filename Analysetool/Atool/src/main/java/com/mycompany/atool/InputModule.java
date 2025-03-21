/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
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

    public void loadFile(){
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        File[] files;
                
        if (selectedDirectory != null) {
            files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
            
            if(files == null || files.length == 0){
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("Keine Logs gefunden!");
                alert.show();
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
        Map<Integer, Double> data = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if(line == null){
                System.out.println("com.mycompany.atool.InputModule.readData() Couldn't read no data from file: " + file.toString());
                return;
            }
            long current_speed_sum = 0;
            double average_speed_per_milli;
            double sum_speed = 0;
            int counter = 1;
            String[] s = line.split(", ");
            int old_time = Integer.parseInt(s[0]);
            current_speed_sum += Long.parseLong(s[1]);
 
            
            
            while ((line = br.readLine()) != null) {
                s = line.split(", ");
                int new_time = Integer.parseInt(s[0]);
                
                if(old_time != new_time){
                    average_speed_per_milli = (double) current_speed_sum/counter;
                    data.put(new_time, average_speed_per_milli);
                    sum_speed += average_speed_per_milli;
                    old_time = new_time;
                    current_speed_sum = Long.parseLong(s[1]);
                    counter = 1;
                } else {
                    current_speed_sum += Long.parseLong(s[1]);
                    counter++;
                }
            }
            
            int time = Integer.parseInt(s[0]);
            job.setTime(time);
            average_speed_per_milli = (double) current_speed_sum/counter;
            sum_speed += average_speed_per_milli;
            double average_speed = (double) sum_speed / data.size();
            job.setAverageSpeed(average_speed);
            data.put(time, average_speed_per_milli);

        job.setData(data);
        } catch (IOException ex) {
            ex.toString();
        }
    }
}
