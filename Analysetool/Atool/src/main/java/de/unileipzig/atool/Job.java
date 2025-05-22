/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Log files of fio are here represented as Jobs.
 *
 * @author meni1999
 */
public class Job {
    public final static Integer MAX_RUN_COUNT = 1000;
    public final static Integer MIN_RUN_COUNT = 1;
    public final static Integer DEFAULT_RUN_COUNT = 1;

    public final static Double DEFAULT_ALPHA = 0.05;
    public final static Double MAX_ALPHA = 0.99999;
    public final static Double MIN_ALPHA = 0.00001;

    public final static Double DEFAULT_EPSILON = 1.0;
    public final static Double MAX_EPSILON = 1000.0;
    public final static Double MIN_EPSILON = 1.0;
    public static final int DEFAULT_GROUP_SIZE = 2;
    private static int COUNTER = 1; // so that each Job has a unique ID
    private final int ID = COUNTER;
    private File file;
    private List<DataPoint> rawData = new ArrayList<>();
    private List<DataPoint> convertedData = new ArrayList<>();
    private List<Run> runs;
    private Map<Integer, Integer> frequency;
    private int runsCounter = 1;
    private double conversion;
    private int time;
    private double averageSpeed;
    private BasicFileAttributes attr;
    private double epsilon = 1;
    private double alpha = 0.05;
    private double calculatedF;
    private double standardDeviation;
    private int groupSize = DEFAULT_GROUP_SIZE;
    private List<List<Run>> groups;
    private List<Run> runss;
    private int runDataSize;
    //private Job job;
    //private double convertedAverageSpeed;

    public Job(List<DataPoint> data) {
        this.frequency = new TreeMap<>();
        setData(data);
        setupGroups();
        COUNTER++;
    }

    public Job(Job other, int groupSize) {
        this.file = other.file;
        this.runs = new ArrayList<>();
        for (Run run : other.runs) {
            this.runs.add(new Run(run));
        }
        this.file = other.file;
        this.rawData = new ArrayList<>(other.rawData);
        this.convertedData = new ArrayList<>(other.convertedData);
        this.frequency = new HashMap<>(other.frequency);
        this.runsCounter = other.runsCounter;
        this.conversion = other.conversion;
        this.time = other.time;
        this.averageSpeed = other.averageSpeed;
        this.attr = other.attr;
        this.epsilon = other.epsilon;
        this.alpha = other.alpha;
        this.calculatedF = other.calculatedF;
        this.standardDeviation = other.standardDeviation;
        this.groupSize = groupSize;
        this.runDataSize = other.runDataSize;
        setupGroups();
    }

    public Job(Job other) {
        this.file = other.file; // Shallow copy — files are immutable in practice
        this.runs = new ArrayList<>();
        for (Run run : other.runs) {
            this.runs.add(new Run(run)); // assumes Run has a copy constructor
        }
        this.rawData = other.rawData;
        this.convertedData = other.convertedData;
        this.frequency = other.frequency;

        this.runsCounter = other.runsCounter;
        this.conversion = other.conversion;
        this.time = other.time;
        this.averageSpeed = other.averageSpeed;
        this.attr = other.attr; // Shallow copy – if you want to copy metadata deeply, use Files.readAttributes again
        this.epsilon = other.epsilon;
        this.alpha = other.alpha;
        this.calculatedF = other.calculatedF;
        this.standardDeviation = other.standardDeviation;
        this.groupSize = DEFAULT_GROUP_SIZE;
        this.runDataSize = other.runDataSize;
        setupGroups();
    }

    public void setFileAttributes(BasicFileAttributes attr) {
        this.attr = attr;
    }

    public List<DataPoint> getData() {
        return this.convertedData;
    }

    private void setData(List<DataPoint> rawData) {
        this.epsilon = rawData.size() / 1000.0;
        if (this.epsilon > MAX_EPSILON) {
            this.epsilon = MAX_EPSILON;
        }
        this.rawData = rawData;
        updateRunsData();
    }

    public List<DataPoint> getRawData() {
        return this.rawData;
    }

    public File getFile() {
        return this.file;
    }


    public void setFile(File file) {
        this.file = file;
    }

    public String getFileCreationDate() {
        return attr.creationTime().toString();
    }

    public String getFileLastModifiedDate() {
        return attr.lastModifiedTime().toString();
    }

    public int getRunsCounter() {
        return this.runsCounter;
    }

    public void setRunsCounter(int runsCounter) {
        this.runsCounter = runsCounter;
        updateRunsData();
    }

    public double getAlpha() {
        return this.alpha;
    }

    void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public int getID() {
        return this.ID;
    }

    @Override
    public String toString() {
        return String.format("Job ID: %d | Average Speed %s | Runs: %d | Alpha: %f", this.ID, new DecimalFormat("#.##").format(this.averageSpeed), this.runsCounter, this.alpha);
    }

    void setTime(int time) {
        this.time = time;
    }

    public int getTimeInMilli() {
        return this.time;
    }

    public double getTimeInSec() {
        return (double) this.time / 1000;
    }

    public double getAverageSpeed() {
        return Math.floor((this.averageSpeed / Settings.CONVERSION_VALUE) * Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }

    public void setAverageSpeed(double average_speed) {
        this.averageSpeed = average_speed;
    }

    public double getConversionVal() {
        return this.conversion;
    }

    public Map<Integer, Integer> getFrequency() {
        return this.frequency;
    }

    public void setFrequency(Map<Integer, Integer> freq) {
        this.frequency = freq;
    }

    public double getEpsilon() {
        return this.epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void resetRuns() {
        for (Run run : this.runs) {
            run.reset();
        }
    }

    public void updateRunsData() {
        runs = new ArrayList<>();
        convertedData = new ArrayList<>();
        if (runsCounter <= 0 || runsCounter > 1000) {
            runsCounter = DEFAULT_RUN_COUNT;
        }

        this.conversion = Settings.CONVERSION_VALUE;
        int AVERAGE_TIME_PER_MILLISEC = Settings.AVERAGE_SPEED_PER_MILLISEC;
        int i = 0;

        final int MIN_RUN_DATA_LENGTH = 8;
        final int MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI = (int) Math.floor(rawData.size() / (double) (MIN_RUN_DATA_LENGTH * runsCounter));

        if (AVERAGE_TIME_PER_MILLISEC > MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI) {
            AVERAGE_TIME_PER_MILLISEC = MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI;
        }

        List<DataPoint> averagedData = new ArrayList<>();
        if (!(AVERAGE_TIME_PER_MILLISEC == 1)) {
            double speed = 0;
            boolean flag = false;
            int counter = 0;

            for (DataPoint dataPoint : rawData) {
                if (i % AVERAGE_TIME_PER_MILLISEC == 0 && flag) {
                    double average_speed = speed / AVERAGE_TIME_PER_MILLISEC;
                    averagedData.add(new DataPoint(average_speed, dataPoint.getTime()));
                    speed = dataPoint.getSpeed();
                    counter = 1;
                } else {
                    flag = true;
                    speed += dataPoint.getSpeed();
                    counter++;
                }
                i++;
            }
        } else {
            averagedData = rawData;
        }

        this.runDataSize = (averagedData.size() / runsCounter);
        /*
        Split job into runs depending on run_size
        */
        i = 0;
        ArrayList<DataPoint> run_data;
        for (int j = 1; j <= runsCounter; j++) {
            run_data = new ArrayList<>();
            for (; i < this.runDataSize * j; i++) {
                DataPoint dp = new DataPoint(averagedData.get(i).getSpeed() / Settings.CONVERSION_VALUE, averagedData.get(i).getTime());
                run_data.add(dp);
                convertedData.add(dp);
            }
            Run run = new Run(j, run_data);
            runs.add(run);
        }
    }

    public void setupGroups(){
        System.out.println("group size" + this.groupSize);
        this.groups = new ArrayList<>();
        this.runss =  new ArrayList<>();
        int groupCount = runsCounter / this.groupSize;
        int runIndex = 0;
        for (int i = 0; i < groupCount; i++) {
            List<Run> group = new ArrayList<>();
            for (int j = 0; j < this.groupSize; j++) {
                group.add(runs.get(runIndex));
                runIndex++;
            }
            group.getFirst().setGroup(String.format("Run %d - Run %d", group.getFirst().getRunID(), group.getLast().getRunID()));
            this.groups.add(group);
            this.runss.add(group.getFirst());
        }
    }

    public List<List<Run>> getGroups(){
        return this.groups;
    }

    public int getGroupSize(){
        return this.groupSize;
    }

    public ObservableList<Run> getRuns() {
        return FXCollections.observableArrayList(this.runs);
    }

    public ObservableList<Run> getRunsCompacted() {
        return FXCollections.observableArrayList(this.runss);
    }

    public int getRunDataSize() {
        return this.runDataSize;
    }

    public double getStandardDeviation() {
        return this.standardDeviation / Settings.CONVERSION_VALUE;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getF() {
        return (calculatedF * Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }

    public void setF(double f) {
        this.calculatedF = f;
    }
}
