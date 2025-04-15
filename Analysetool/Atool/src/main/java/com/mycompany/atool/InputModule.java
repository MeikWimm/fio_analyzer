/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        private static final Logger LOGGER = Logger.getLogger( InputModule.class.getName() );
        private boolean isDirChooserOpen = false;
    public enum STATUS {
        SUCCESS,
        NO_FILES_FOUND,
        NO_DIR_SET,
        ERROR_WHILE_READING_FILE,
        DIR_CHOOSER_ALREADY_OPEN,
        FAILURE
    }
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new CustomFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);    
    }
    
    DirectoryChooser directoryChooser;
    public File selectedDirectory;
    ObservableList<Job> jobs = FXCollections.observableArrayList();
    private File[] files;

    public InputModule(){
        directoryChooser = new DirectoryChooser();
    }

    /**
     * DirectoryChooser gets all log files from a choosen directory.
     * 
     * @return 
     */
    public STATUS loadFile(){
        if(!isDirChooserOpen){
            isDirChooserOpen = true;
            this.selectedDirectory = directoryChooser.showDialog(new Stage());
            isDirChooserOpen = false;
        } else {
            return STATUS.DIR_CHOOSER_ALREADY_OPEN;
        }
        
        if (selectedDirectory != null) {
            files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
            
            if(files == null || files.length == 0){
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("No logs found!");
                alert.show();
                return STATUS.NO_FILES_FOUND;
            } else {
                readFiles();
            }
        } else {
            return STATUS.NO_DIR_SET;
        }
        return STATUS.SUCCESS;
    }

    /**
     * Reads all files listed in directoryChooser with the extension type ".log".
     * If a specific file is already loaded, it'll be ignored.
     * @return NO_DIR_SET, if directory of this object is not set. BUFER On success it return SUCCESS.
     * 
     */
    public STATUS readFiles(/*File[] files*/) {
        if(selectedDirectory == null){
            return STATUS.NO_DIR_SET;
        }
        
        files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        
        ArrayList<Job> temp = new ArrayList<>();
        for (Job job : jobs) {
            temp.add(job);
        }
        boolean found_new_files = false;
        for (File file : files) {
            boolean is_file_already_added = false;
            if(!temp.isEmpty()){
               for (Job j : temp) {
                if(file.toString().equals(j.getFile().toString())){
                    is_file_already_added = true;
               }
            }    
                if(!is_file_already_added){
                    found_new_files = true;
                    Job job = new Job();
                    job.setFile(file);
                    STATUS status = readData(job);
                    if(status != STATUS.SUCCESS){
                        return status;
                    }
                    LOGGER.log(Level.INFO, String.format("New job added -> %s", job.toString()));
                    jobs.add(job);
                }
          } else {
                    Job job = new Job();
                    job.setFile(file);
                    STATUS status = readData(job);
                    if(status != STATUS.SUCCESS){
                        return status;
                    }
                    jobs.add(job);
            }
        }
        
        if(found_new_files == false && !temp.isEmpty()){
            LOGGER.log(Level.INFO, "Nothing to refresh in Table.");
        }
        
        return STATUS.SUCCESS;
    }

    /**
     * Reads the data of a .log file and saves the data in a job instance.
     * @param job 
     */
    private STATUS readData(Job job) {
        List<Point2D> data = new ArrayList<>(); // Point2D for x = time and y = speed
        Map<Integer, Integer> freq = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(job.getFile()))) {
            BasicFileAttributes attr = Files.readAttributes(job.getFile().toPath(), BasicFileAttributes.class);
            job.setFileAttributes(attr);
            String line = br.readLine();
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
            LOGGER.log(Level.SEVERE, (Supplier<String>) ex);
            LOGGER.log(Level.SEVERE, String.format("Error occured while reading file: %s. App state: %s", job.getFile(), STATUS.ERROR_WHILE_READING_FILE));
            return STATUS.ERROR_WHILE_READING_FILE;
        }
        return STATUS.SUCCESS;
    }

    public ObservableList<Job> getJobs(){
        return jobs;
    }

    public File getSelectedDir() {
        return this.selectedDirectory;
    }
    
}
