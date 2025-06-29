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
import javafx.stage.Stage;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Class for loading a reading the log file of fio Jobs
 *
 * @author meni1999
 */
public class
InputModule {
    private static final Logger LOGGER = Logger.getLogger(InputModule.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Input Module"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    public File selectedDirectory;
    DirectoryChooser directoryChooser;
    ObservableList<Job> jobs = FXCollections.observableArrayList();
    private boolean isDirChooserOpen = false;
    private File[] files;
    private BufferedReader br;
    private int time;
    private double averageSpeed;
    private double standardDeviation;
    private List<DataPoint> data;
    private Map<Integer, Integer> freq;
    private BasicFileAttributes fileAttribute;
    private final Settings settings;

    public InputModule(Settings settings) {
        this.settings = settings;
        directoryChooser = new DirectoryChooser();
    }

    /**
     * DirectoryChooser gets all log files from a choosen directory.
     *
     */
    public STATUS loadFile(Window ownerWindow) {
        LOGGER.log(Level.INFO, "Loading input module...");

        STATUS state;
        if (!isDirChooserOpen) {
            isDirChooserOpen = true;
            try {
                //this.selectedDirectory = directoryChooser.showDialog(ownerWindow);
                this.selectedDirectory = directoryChooser.showDialog(new Stage());
            } finally {
                isDirChooserOpen = false;
            }
        } else {
            return STATUS.DIR_CHOOSER_ALREADY_OPEN;
        }

        if (selectedDirectory != null) {
            boolean isDirReadable = selectedDirectory.canRead();
            LOGGER.log(Level.INFO, "Is directory selected: " + selectedDirectory.isDirectory());
            LOGGER.log(Level.INFO, "Selected directory: " + selectedDirectory.getAbsolutePath());
            LOGGER.log(Level.INFO, "Is directory readable: " + isDirReadable);

            if(!isDirReadable){
                Alert alert = new Alert(AlertType.WARNING);
                alert.setContentText("Directory is not readable!");
                alert.show();
                return STATUS.DIR_NOT_READABLE;
            }

            files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));


            if (files == null || files.length == 0) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("No logs found!");
                alert.show();


                if(files == null){
                    LOGGER.log(Level.WARNING, "files is Null!");
                } else {
                    LOGGER.log(Level.WARNING, "files array length is 0!");
                }

                return STATUS.NO_FILES_FOUND;
            } else {
                state = readFiles(files);
            }
        } else {
            LOGGER.log(Level.WARNING, "Dir chooser is null!");
            return STATUS.NO_DIR_SET;
        }
        return state;
    }

    public STATUS checkForNewLogs(){
        if (selectedDirectory == null) {
            return STATUS.NO_DIR_SET;
        }

        File[] newFiles = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));

        if (newFiles == null || newFiles.length == 0) {
            LOGGER.log(Level.WARNING, "No new files found!");
            return STATUS.NO_FILES_FOUND;
        }

        if(files.length == 0){
            LOGGER.log(Level.WARNING, "No files found!");
            return STATUS.NO_FILES_FOUND;
        }



        ArrayList<Job> temp = new ArrayList<>();

        boolean found_new_files = false;
        for (File newFile : newFiles) {
            boolean exists = true;

            for (File file : files) {
                exists = file.toPath().equals(newFile.toPath());
                if(exists){
                    break;
                }
            }

            if(!exists){
                found_new_files = true;
                readData(newFile);

                Job job = new Job(this.data, settings.averageSpeedPerMillisec);
                job.setFrequency(this.freq);
                job.setFile(newFile);
                job.setFileAttributes(this.fileAttribute);
                job.setTime(this.time);
                job.setAverageSpeed(this.averageSpeed);
                job.setStandardDeviation(this.standardDeviation);
                jobs.add(job);
                temp.add(job);
            }
        }


        if (!found_new_files) {
            LOGGER.log(Level.INFO, "Nothing to refresh in Table.");
        } else {
            for (Job job : temp) {
                LOGGER.log(Level.INFO, String.format("Added new found job -> %s", job));
                this.files = newFiles;

            }
        }

        return STATUS.SUCCESS;
    }

    /**
     * Reads all files listed in directoryChooser with the extension type ".log".
     * If a specific file is already loaded, it'll be ignored.
     *
     * @return NO_DIR_SET, if directory of this object is not set. BUFFER On success it return SUCCESS.
     */
    public STATUS readFiles(File[] files) {

        if (selectedDirectory == null) {
            return STATUS.NO_DIR_SET;
        }

        if(files.length == 0){
            LOGGER.log(Level.WARNING, "No files found!");
            return STATUS.NO_FILES_FOUND;
        }

        for (File file : files) {
            readData(file);

            Job job = new Job(this.data, settings.averageSpeedPerMillisec);
            job.setFrequency(this.freq);
            job.setFile(file);
            job.setFileAttributes(this.fileAttribute);
            job.setTime(this.time);
            job.setAverageSpeed(this.averageSpeed);
            job.setStandardDeviation(this.standardDeviation);
            jobs.add(job);
        }
        return STATUS.SUCCESS;
    }

    public static int[] parseFirstTwoValues(String line) {
        int[] result = new int[2];
        int value = 0, idx = 0;

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
    
    private void readData(File file) {
        List<DataPoint> data = new ArrayList<>(); // Point2D for x = time and y = speed
        Map<Integer, Integer> freq = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            this.fileAttribute = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            String line = br.readLine();
            int[] values = parseFirstTwoValues(line);

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
            this.averageSpeed = sum_speed / data.size();
            this.standardDeviation = MathUtils.calculateDeviation(data, this.averageSpeed);
            this.freq = freq;
            this.data = data;
        } catch (IOException ex) {
            ex.printStackTrace();
            LOGGER.log(Level.SEVERE, String.format("Error occured while reading file: %s. App state: %s", file, STATUS.ERROR_WHILE_READING_FILE));
        }
    }

    public ObservableList<Job> getJobs() {
        return jobs;
    }

    public enum STATUS {
        SUCCESS,
        NO_FILES_FOUND,
        NO_DIR_SET,
        DIR_NOT_READABLE,
        ERROR_WHILE_READING_FILE,
        DIR_CHOOSER_ALREADY_OPEN,
        FAILURE
    }

}
