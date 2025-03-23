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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *  A Class for loading a reading the log file of fio Jobs
 * @author meni1999
 */
public class InputModule {
    DirectoryChooser directoryChooser;
    public File selectedDirectory;
    ObservableList<Job> jobs = FXCollections.observableArrayList();

    public InputModule(){
        directoryChooser = new DirectoryChooser();
    }

    /**
     * DirectoryChooser gets all log files from a choosen directory.
     */
    public void loadFile(){
        this.selectedDirectory = directoryChooser.showDialog(new Stage());
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
    }

    /**
     * Reads all files listed in directoryChooser with the extension type ".log".
     * If a specific file is already loaded, it'll be ignored.
     * @param files 
     */
    public void readFiles(File[] files) {
        ArrayList<Job> temp = new ArrayList<>();
        for (Job job : jobs) {
            temp.add(job);
        }

        for (File file : files) {
            boolean is_file_already_added = false;
            if(!temp.isEmpty()){
               for (Job j : temp) {
                if(file.toString().equals(j.getFile().toString())){
                    is_file_already_added = true;
               }
            }    
                if(!is_file_already_added){
                    System.out.println(file.toString());
                    Job job = new Job();
                    job.setFile(file);
                    jobs.add(job);
                    readData(job);
                }
          } else {
                    Job job = new Job();
                    job.setFile(file);
                    jobs.add(job);
                    readData(job);
            }
        }
    }

    /**
     * Reads the data of a .log file and saves the data in a job instance.
     * @param job 
     */
    private void readData(Job job) {
        List<Point2D> data = new ArrayList<>();
        Map<Integer, Integer> freq = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(job.getFile()))) {
            String line = br.readLine();
            if(line == null){
                System.out.println("com.mycompany.atool.InputModule.readData() Couldn't read no data from file: " + job.getFile().toString());
                return;
            }
            long current_speed_sum = 0;
            double average_speed_per_milli;
            double sum_speed = 0;
            int counter = 1;
            String[] s = line.split(", ");
            int old_time = Integer.parseInt(s[0]);
            int speed = Integer.parseInt(s[1]);
            current_speed_sum += Long.parseLong(s[1]);
            freq.put((int) speed, 1);
            
            
            while ((line = br.readLine()) != null) {
                s = line.split(", ");
                int new_time = Integer.parseInt(s[0]);
                speed = Integer.parseInt(s[1]);
                if(freq.containsKey((int) speed)){
                        freq.put(speed, freq.get(speed) + 1);
                    } else {
                        freq.put((int) speed, 1);
                    }
                if(old_time != new_time){
                    average_speed_per_milli = (double) current_speed_sum/counter;
                    data.add(new Point2D(new_time, average_speed_per_milli));
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
            data.add(new Point2D(time, average_speed_per_milli));
            job.setFrequency(freq);

        job.setData(data);
        } catch (IOException ex) {
            ex.toString();
        }
    }

    public ObservableList<Job> getJobs(){
        return jobs;
    }

    public File getSelectedDir() {
        return this.selectedDirectory;
    }
    
}
