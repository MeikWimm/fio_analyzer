/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Log files of fio are here represented as Jobs.
 *
 * @author meni1999
 */
public class Job {
    public final static Integer MAX_RUN_COUNT = 100;
    public final static Integer MIN_RUN_COUNT = 4;
    public final static Integer DEFAULT_RUN_COUNT = 4;

    public final static Double DEFAULT_ALPHA = 0.05;
    public final static Double MAX_ALPHA = 0.99999;
    public final static Double MIN_ALPHA = 0.00001;

    public final static Double DEFAULT_CV_THRESHOLD = .2;
    public final static Double MAX_CV_THRESHOLD = .6;
    public final static Double MIN_CV_THRESHOLD = .05;

    private final static int WINDOW_SIZE = 60000;
    private final static int WINDOW_STEP_SIZE = 1000;

    private static int COUNTER = 1;
    private final int ID = COUNTER; // so that each Job has a unique ID
    private final List<XYChart.Data<Number, Number>> speedSeries;
    private Map<Integer, Integer> freq;
    private List<XYChart.Data<Number, Number>> chartData;
    private final List<DataPoint> rawData;
    private List<DataPoint> data;
    private List<DataPoint> convertedData;
    private List<Run> runs;
    private File file;
    private BasicFileAttributes attr;
    private int runsCounter = DEFAULT_RUN_COUNT;
    private int time;
    private int runDataSize;
    private double conversion;
    private double averageSpeed;
    private double alpha = DEFAULT_ALPHA;
    private double cvThreshold = DEFAULT_CV_THRESHOLD;
    private double calculatedF;
    private double standardDeviation;
    private double SSE;
    private double MSE;
    private int skipSize;


    public Job(List<DataPoint> data) {
        this.freq = new TreeMap<>();
        this.speedSeries = new ArrayList<>();
        this.data = new ArrayList<>(data);
        this.rawData = new ArrayList<>(data);
        this.convertedData = new ArrayList<>();
        this.chartData = new ArrayList<>();
        updateRunsData();
        COUNTER++;
    }

    public Job(Job other) {
        this.file = other.file;
        this.runs = new ArrayList<>();
        for (Run run : other.runs) {
            this.runs.add(new Run(run));
        }
        this.data = other.data;
        this.rawData = other.rawData;
        this.speedSeries = other.speedSeries;
        this.file = other.file;
        this.convertedData = new ArrayList<>(other.convertedData);
        this.freq = new HashMap<>(other.freq);
        this.runsCounter = other.runsCounter;
        this.conversion = other.conversion;
        this.time = other.time;
        this.averageSpeed = other.averageSpeed;
        this.attr = other.attr;
        this.alpha = other.alpha;
        this.calculatedF = other.calculatedF;
        this.standardDeviation = other.standardDeviation;
        this.MSE = other.MSE;
        this.SSE = other.SSE;
        this.cvThreshold = other.cvThreshold;
        this.chartData = other.chartData;
        this.skipSize = other.skipSize;
        this.runDataSize = other.runDataSize;
    }

    public static List<List<Run>> setupGroups(Job job, boolean skipGroups, int groupSize) {
        if (groupSize < 2) {
            return new ArrayList<>();
        }

        List<List<Run>> groups = new ArrayList<>();
        List<Run> runs = job.getRuns();
        int runsCounter = runs.size();

//        if (skipGroups) {
//            // create Groups like Run 1 - Run 2, Run 3 - Run 4, Run 5 - Run 6,...
//            int groupCount = runsCounter / groupSize;
//            int runIndex = 0;
//
//            for (int i = 0; i < groupCount; i++) {
//                List<Run> group = new ArrayList<>();
//                for (int j = 0; j < groupSize; j++) {
//                    group.add(runs.get(runIndex));
//                    runIndex++;
//                }
//                group.getFirst().setGroup(String.format("Run %d - Run %d", group.getFirst().getRunID(), group.getLast().getRunID()));
//
//                groups.add(group);
//            }
//        } else {
//
//        }


        int groupCount = runsCounter / 2;
        groupCount = groupCount + (groupCount - 1);
        int runIndex = 0;

        for (int i = 0; i < groupCount; i++) {
            List<Run> group = new ArrayList<>();
            for (int j = 0; j < groupSize; j++) {
                if (runIndex + j < runs.size()) {
                    group.add(runs.get(runIndex + j));
                }
            }
            if (group.size() == groupSize) {
                group.getFirst().setGroup(String.format("Run %d - Run %d", group.getFirst().getRunID(), group.getLast().getRunID()));

                groups.add(group);
            }
            runIndex++;
        }

        return groups;
    }

    public void updateRunsData() {
        List<DataPoint> data = rawData;
        runs = new ArrayList<>();
        convertedData = new ArrayList<>();
        if (runsCounter <= 0 || runsCounter > 1000) {
            runsCounter = DEFAULT_RUN_COUNT;
        }

        int windowSize = WINDOW_SIZE;
        int stepSize = WINDOW_STEP_SIZE; // Sliding Step

        this.conversion = Settings.CONVERSION_VALUE;
        if(data.size() < windowSize){
            Logging.log(Level.WARNING, "Job", String.format("Data size %d is less than window size %d", data.size(), WINDOW_SIZE));
            return;
        }
        this.runDataSize = (data.size() / WINDOW_SIZE);


        int runId = 1;

        for (int i = 0; i + windowSize <= data.size(); i += stepSize) {
            List<DataPoint> run_data = new ArrayList<>();

            for (int j = i; j < i + windowSize; j++) {
                DataPoint dp = new DataPoint(data.get(j).data / Settings.CONVERSION_VALUE, rawData.get(j).time);
                run_data.add(dp);
                convertedData.add(dp);
            }

            Run run = new Run(runId++, run_data);
            runs.add(run);
        }
    }

    public void prepareSkippedData(int skipRuns) {
        this.skipSize = skipRuns;
        if (skipRuns < 1) {
            return;
        }
        int skipSize = this.runDataSize * skipRuns;

        if(skipSize > this.data.size()){
            Logging.log(Level.WARNING, "Job", "Skipped data size " + skipSize + " exceeds job data size " + this.data.size());
            return;
        }

        this.data.subList(0, skipSize).clear();
        this.runs.subList(0, skipRuns).clear();
        runsCounter = runsCounter - skipRuns;
    }

    public List<XYChart.Data<Number, Number>> getFrequencySeries() {
        return chartData;
    }

    public List<XYChart.Data<Number, Number>> getSeries() {
        if (this.speedSeries.isEmpty()) {
            for (DataPoint dp : data) {
                speedSeries.add(new XYChart.Data<>(dp.time, dp.data));
            }
        }

        return this.speedSeries;
    }

    public void setFileAttributes(BasicFileAttributes attr) {
        this.attr = attr;
    }

    public List<DataPoint> getData() {
        return this.data;
    }

    public void setData(List<DataPoint> data) {
        this.data = data;
    }

    public File getFile() {
        return this.file;
    }

    public String getFileName() {
        if(this.file == null){
            return "";
        }
        return this.file.getName();
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
        if(runsCounter < MIN_RUN_COUNT || runsCounter > MAX_RUN_COUNT){
            Logging.log(Level.WARNING, "Job", String.format("In Job: %s", getFileName()));
            Logging.log(Level.WARNING, "Job", String.format("Runs counter must be between %d and %d", MIN_RUN_COUNT, MAX_RUN_COUNT));
            Logging.log(Level.WARNING, "Job", String.format("Runs counter set to default value %d", DEFAULT_RUN_COUNT));
            this.runsCounter = DEFAULT_RUN_COUNT;
            return;
        }

        this.runsCounter = runsCounter;
    }

    public double getAlpha() {
        return this.alpha;
    }

    void setAlpha(Double alpha) {
        if(alpha < MIN_ALPHA || alpha > MAX_ALPHA){
            Logging.log(Level.WARNING, "Job", String.format("Alpha must be between %f and %f", MIN_ALPHA, MAX_ALPHA));
            Logging.log(Level.WARNING, "Job", String.format("Alpha set to default value %f", DEFAULT_ALPHA));
            this.alpha = DEFAULT_ALPHA;
            return;
        }
        this.alpha = alpha;
    }

    public double getCvThreshold() {
        return this.cvThreshold;
    }

    public void setCvThreshold(Double cvThreshold) {
        if(cvThreshold < MIN_CV_THRESHOLD || cvThreshold > MAX_CV_THRESHOLD){
            Logging.log(Level.WARNING, "Job", String.format("CV threshold must be between %f and %f", MIN_CV_THRESHOLD, MAX_CV_THRESHOLD));
            Logging.log(Level.WARNING, "Job", String.format("CV threshold set to default value %f", DEFAULT_CV_THRESHOLD));
            this.cvThreshold = DEFAULT_CV_THRESHOLD;
            return;
        }
        this.cvThreshold = cvThreshold;
    }

    public int getID() {
        return this.ID;
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
        return ((this.averageSpeed / Settings.CONVERSION_VALUE));
    }

    public void setAverageSpeed(double average_speed) {
        this.averageSpeed = average_speed;
    }

    public ObservableList<Run> getRuns() {
        return FXCollections.observableArrayList(this.runs);
    }

    public double getStandardDeviation() {
        return this.standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public void setSSE(double SSE) {
        this.SSE = SSE;
    }

    public double getSSE() {
        return SSE;
    }

    public double getMSE() {
        return MSE;
    }

    public void setMSE(double MSE) {
        this.MSE = MSE;
    }

    public void setFrequency(Map<Integer, Integer> freq) {
        this.freq = freq;
        this.chartData = freq.entrySet()
                .stream()
                .map(entry -> new XYChart.Data<Number, Number>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public int getRunDataSize() {
        return this.runDataSize;
    }

    public int getSkippedRunsDataSize() {
        return this.skipSize;
    }

    @Override
    public String toString() {
        return String.format("Job ID: %d | Average Speed %s | Runs: %d | Alpha: %f | File: %s", this.ID, new DecimalFormat("#.##").format(this.averageSpeed), this.runsCounter, this.alpha, this.file);
    }
}
