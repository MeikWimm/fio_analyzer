/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.MathUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

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
import java.util.logging.Level;

/**
 * A Class for loading a reading the log file of fio Jobs
 *
 * @author meni1999
 */
public class
InputModule {
    public static final int MIN_POSSIBLE_DATA_SIZE = 60000;


    public static File SELECTED_DIRECTORY;
    DirectoryChooser directoryChooser;
    ObservableList<Job> jobs = FXCollections.observableArrayList();
    private int time;
    private double averageSpeed;
    private double standardDeviation;
    private List<DataPoint> data;
    private Map<Integer, Integer> freq;
    private BasicFileAttributes fileAttribute;
    private final String className = "InputModule";

    public InputModule() {
        directoryChooser = new DirectoryChooser();
    }

    public void openDirectoryChooser(Window ownerWindow) {
        SELECTED_DIRECTORY = directoryChooser.showDialog(ownerWindow);
    }

    /**
     * DirectoryChooser gets all log files from a choosen directory.
     *
     */
    public STATUS loadFile() {
        Logging.log(Level.INFO, className, "Loading input module...");
        STATUS state;
        File[] files;

        if (SELECTED_DIRECTORY != null) {
            boolean isDirReadable = SELECTED_DIRECTORY.canRead();
            Logging.log(Level.INFO, className, "Is directory selected: " + SELECTED_DIRECTORY.isDirectory());
            Logging.log(Level.INFO, className,"Selected directory: " + SELECTED_DIRECTORY.getAbsolutePath());
            Logging.log(Level.INFO, className,"Is directory readable: " + isDirReadable);

            if(!isDirReadable){
                Alert alert = new Alert(AlertType.WARNING);
                alert.setContentText("Directory is not readable!");
                alert.show();
                return STATUS.DIR_NOT_READABLE;
            }

            files = SELECTED_DIRECTORY.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));


            if (files == null || files.length == 0) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("No logs found!");
                alert.show();


                if(files == null){
                    Logging.log(Level.WARNING, className, "files is Null!");
                } else {
                    Logging.log(Level.WARNING, className,"files array length is 0!");
                }

                return STATUS.NO_FILES_FOUND;
            } else {
                state = readFiles(files);
            }
        } else {
            Logging.log(Level.WARNING, className,"Dir chooser is null!");
            return STATUS.NO_DIR_SET;
        }
        return state;
    }

    /**
     * Reads all files listed in directoryChooser with the extension type ".log".
     * If a specific file is already loaded, it'll be ignored.
     *
     * @return NO_DIR_SET, if directory of this object is not set. BUFFER On success it return SUCCESS.
     */
    public STATUS readFiles(File[] files) {
        boolean foundNewFile = false;
        for (File file : files) {
            boolean exists = false;
            for (Job job: jobs) {
                if (file.equals(job.getFile())) {
                    exists = true;
                    break;
                }
            }

            if(!exists){
                foundNewFile =  true;
                if(readData(file) != STATUS.SUCCESS){
                    Logging.log(Level.WARNING, className, String.format("Error while reading file -> %s", file.getAbsolutePath()));
                } else {
                    if(this.data.size() < MIN_POSSIBLE_DATA_SIZE){
                        Logging.log(Level.WARNING, className,String.format("file -> %s is to small!", file.getAbsolutePath()));
                        Logging.log(Level.WARNING, className,String.format("Log should be at least be a minute ling!", file.getAbsolutePath()));
                    } else {
                        Logging.log(Level.INFO, "Input Module", "Preparing Job Data: " + file);
                        Job job = new Job(this.data);
                        job.setFrequency(this.freq);
                        job.setFile(file);
                        job.setFileAttributes(this.fileAttribute);
                        job.setTime(this.time);
                        job.setAverageSpeed(this.averageSpeed);
                        job.setStandardDeviation(this.standardDeviation);
                        jobs.add(job);
                    }
                }
            }
        }

        if(foundNewFile){
            Logging.log(Level.INFO, className,"New file\\s found.");
        }
        Logging.log(Level.INFO, className,"Finished reading files!");
        return STATUS.SUCCESS;
    }

    public static int[] parseFirstTwoValues(String line) {
        int[] result = new int[2];
        int value = 0, idx = 0;

        if(line == null){
            return null;
        }

        for (int i = 0; i < line.length() && idx < 2; i++) {
            char ch = line.charAt(i);
            if (ch == ',') {
                result[idx++] = value;
                value = 0;
                // Skip space if present
                if (i + 1 < line.length() && line.charAt(i + 1) == ' ') i++;
            } else if (ch >= '0' && ch <= '9') {
                value = value * 10 + (ch - '0');
            }
        }
        return result;
    }
    
    private STATUS readData(File file) {
        List<DataPoint> data = new ArrayList<>(); // Point2D for x = time and y = speed
        Map<Integer, Integer> freq = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            this.fileAttribute = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            String line = br.readLine();
            int[] values = parseFirstTwoValues(line);
            if (values == null) {
                return STATUS.ERROR_WHILE_READING_FILE;
            }

            int old_time = values[0];
            int speed = values[1];
            long current_speed_sum = 0;
            double average_speed_per_milli;
            double sum_speed = 0;
            int counter = 1;
            current_speed_sum += speed;
            freq.put(speed, 1);


            while ((line = br.readLine()) != null) {
                int[] s = parseFirstTwoValues(line);
                if (s == null) {
                    return STATUS.ERROR_WHILE_READING_FILE;
                }
                int new_time = s[0];
                speed = s[1];

                freq.merge(speed, 1, Integer::sum);

                if (old_time != new_time) {
                    average_speed_per_milli = (double) current_speed_sum / counter;
                    data.add(new DataPoint(average_speed_per_milli, new_time));
                    sum_speed += average_speed_per_milli;
                    old_time = new_time;
                    current_speed_sum = s[1];
                    counter = 1;
                } else {
                    current_speed_sum += s[1];
                    counter++;
                }
            }
            average_speed_per_milli = current_speed_sum / (double) counter;
            sum_speed += average_speed_per_milli;


            this.time = old_time;
            this.averageSpeed = sum_speed / data.size(); // milli sec to sec
            this.standardDeviation = MathUtils.calculateDeviation(data, this.averageSpeed);
            this.freq = freq;
            this.data = data;
        } catch (IOException ex) {
            Logging.log(Level.SEVERE, className,String.format("Error occured while reading file: %s. App state: %s", file, STATUS.ERROR_WHILE_READING_FILE));
        }
        return STATUS.SUCCESS;
    }

    public ObservableList<Job> getJobs() {
        return jobs;
    }


    public String getInfo(InputModule.STATUS state) {
        return switch (state) {
            case NO_DIR_SET -> "No directory set!";
            case NO_FILES_FOUND -> "No files found!";
            case DIR_NOT_READABLE -> "Directory is not readable!";
            case SUCCESS -> "All files loaded!";
            default -> "Unknown state!";
        };
    }

    public enum STATUS {
        SUCCESS,
        NO_FILES_FOUND,
        NO_DIR_SET,
        DIR_NOT_READABLE,
        ERROR_WHILE_READING_FILE,
        FAILURE
    }

}
