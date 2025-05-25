/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
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
public class InputModule {
    private static final Logger LOGGER = Logger.getLogger(InputModule.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("Input Module"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }
    //private CONVERT convertTo;
    //private double conv = CONVERT.getConvertValue(CONVERT.DEFAULT);

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

    public InputModule() {
        directoryChooser = new DirectoryChooser();
    }

    /**
     * DirectoryChooser gets all log files from a choosen directory.
     *
     * @return
     */
    public STATUS loadFile() {
        if (!isDirChooserOpen) {
            isDirChooserOpen = true;
            this.selectedDirectory = directoryChooser.showDialog(new Stage());
            isDirChooserOpen = false;
        } else {
            return STATUS.DIR_CHOOSER_ALREADY_OPEN;
        }

        if (selectedDirectory != null) {
            files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));

            if (files == null || files.length == 0) {
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
     *
     * @return NO_DIR_SET, if directory of this object is not set. BUFER On success it return SUCCESS.
     */
    public STATUS readFiles(/*File[] files*/) {

        if (selectedDirectory == null) {
            return STATUS.NO_DIR_SET;
        }

        files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));

        ArrayList<Job> temp = new ArrayList<>(jobs);

        boolean found_new_files = false;
        assert files != null;
        for (File file : files) {
            boolean is_file_already_added = false;
            if (!temp.isEmpty()) {
                for (Job j : temp) {
                    if (file.toString().equals(j.getFile().toString())) {
                        is_file_already_added = true;
                    }
                }
                if (!is_file_already_added) {
                    found_new_files = true;
                    readData(file);

                    Job job = new Job(this.data);
                    job.setFile(file);
                    job.setFileAttributes(this.fileAttribute);
                    job.setTime(this.time);
                    job.setAverageSpeed(this.averageSpeed);
                    job.setStandardDeviation(this.standardDeviation);
                    jobs.add(job);
                }
            } else {
                readData(file);

                Job job = new Job(this.data);
                job.setFile(file);
                job.setFileAttributes(this.fileAttribute);
                job.setTime(this.time);
                job.setAverageSpeed(this.averageSpeed);
                job.setStandardDeviation(this.standardDeviation);
                jobs.add(job);
            }
        }

        if (!found_new_files && !temp.isEmpty()) {
            LOGGER.log(Level.INFO, "Nothing to refresh in Table.");
        } else {
        	for (Job job : temp) {
                LOGGER.log(Level.INFO, String.format("Added new found job -> %s", job));
			}
        }

        return STATUS.SUCCESS;
    }


    /**
     * Reads the data of a .log file and saves the data in a job instance.
     *
     */
    private void readData(File file) {
        List<DataPoint> data = new ArrayList<>(); // Point2D for x = time and y = speed
        Map<Integer, Integer> freq = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            this.fileAttribute = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            String line = br.readLine();
            long current_speed_sum = 0;
            double average_speed_per_milli;
            double sum_speed = 0;
            int counter = 1;
            String[] s = line.split(", ");
            int old_time = Integer.parseInt(s[0]);
            int speed = Integer.parseInt(s[1]);
            current_speed_sum += Long.parseLong(s[1]);
            freq.put(speed, 1);


            while ((line = br.readLine()) != null) {
                s = line.split(", ");
                int new_time = Integer.parseInt(s[0]);
                speed = Integer.parseInt(s[1]);

                freq.merge(speed, 1, Integer::sum);

                if (old_time != new_time) {
                    average_speed_per_milli = (double) current_speed_sum / counter;
                    data.add(new DataPoint(average_speed_per_milli, new_time));
                    sum_speed += average_speed_per_milli;
                    old_time = new_time;
                    current_speed_sum = Long.parseLong(s[1]);
                    counter = 1;
                } else {
                    current_speed_sum += Long.parseLong(s[1]);
                    counter++;
                }
            }
            average_speed_per_milli = current_speed_sum / (double) counter;
            sum_speed += average_speed_per_milli;
            //data.add(new DataPoint(average_speed_per_milli, this.time));
            //speedSeries.getData().add(new XYChart.Data<>(average_speed_per_milli, this.time));

            this.time = Integer.parseInt(s[0]);
            this.averageSpeed = sum_speed / data.size();
            this.standardDeviation = calculateDeviation(data, this.averageSpeed);
            this.freq = freq;
            this.data = data;
        } catch (IOException ex) {
            ex.printStackTrace();
            //LOGGER.log(Level.SEVERE, (Supplier<String>) ex);
            LOGGER.log(Level.SEVERE, String.format("Error occured while reading file: %s. App state: %s", "radate", STATUS.ERROR_WHILE_READING_FILE));
        }
    }

    public ObservableList<Job> getJobs() {
        return jobs;
    }

    private double calculateDeviation(List<DataPoint> data, double average_speed) {
        double standardDeviation = 0.0;
        for (DataPoint dataPoint : data) {
            standardDeviation += Math.pow(dataPoint.getSpeed() - average_speed, 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / data.size());
        return standardDeviation;
    }

    public File getSelectedDir() {
        return this.selectedDirectory;
    }

    public enum STATUS {
        SUCCESS,
        NO_FILES_FOUND,
        NO_DIR_SET,
        ERROR_WHILE_READING_FILE,
        DIR_CHOOSER_ALREADY_OPEN,
        FAILURE
    }

}
