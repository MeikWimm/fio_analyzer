/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
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
    public static final int MIN_TIME_SKIP = 30;
    public final static int MAX_TIME_SKIP = 60;
    public final static int DEFAULT_TIME_SKIP = 30;

    private static int COUNTER = 1;
    private final int ID = COUNTER; // so that each Job has a unique ID
    private final List<XYChart.Data<Number, Number>> speedSeries;
    private Map<Integer, Integer> freq;
    private List<XYChart.Data<Number, Number>> chartData;
    private final List<DataPoint> rawData;
    private List<DataPoint> data;
    private List<DataPoint> convertedData;
    private List<Section> sections;
    private File file;
    private BasicFileAttributes attr;
    private int runsCounter = DEFAULT_RUN_COUNT;
    private int time;
    private int runDataSize;
    private double conversion;
    private double averageSpeed;
    private double cvThreshold = DEFAULT_CV_THRESHOLD;
    private double calculatedF;
    private double standardDeviation;
    private double SSE;
    private double MSE;
    private int skipSeconds;
    private int skipSize;

    private final DoubleProperty alphaProperty = new SimpleDoubleProperty();

    public Job(List<DataPoint> data) {
        this.freq = new TreeMap<>();
        this.speedSeries = new ArrayList<>();
        this.data = new ArrayList<>(data);
        this.rawData = new ArrayList<>(data);
        this.convertedData = new ArrayList<>();
        this.chartData = new ArrayList<>();
        this.alphaProperty.set(DEFAULT_ALPHA);
        updateRunsData();
        COUNTER++;
    }

    public Job(Job other) {
        this.file = other.file;
        this.sections = new ArrayList<>();
        for (Section section : other.sections) {
            this.sections.add(new Section(section));
        }
        this.alphaProperty.set(other.alphaProperty.get());
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
        this.calculatedF = other.calculatedF;
        this.standardDeviation = other.standardDeviation;
        this.MSE = other.MSE;
        this.SSE = other.SSE;
        this.cvThreshold = other.cvThreshold;
        this.chartData = other.chartData;
        this.skipSize = other.skipSize;
        this.runDataSize = other.runDataSize;
    }

    public static List<List<Section>> setupGroups(Job job, boolean skipGroups, int groupSize) {
        if (groupSize < 2) {
            return new ArrayList<>();
        }

        List<List<Section>> groups = new ArrayList<>();
        List<Section> sections = job.getRuns();
        int runsCounter = sections.size();
        int groupCount = runsCounter / 2;
        groupCount = groupCount + (groupCount - 1);
        int runIndex = 0;

        for (int i = 0; i < groupCount; i++) {
            List<Section> group = new ArrayList<>();
            for (int j = 0; j < groupSize; j++) {
                if (runIndex + j < sections.size()) {
                    group.add(sections.get(runIndex + j));
                }
            }
            if (group.size() == groupSize) {
                group.getFirst().setGroup(String.format("Section %d - Section %d", group.getFirst().getID(), group.getLast().getID()));

                groups.add(group);
            }
            runIndex++;
        }

        return groups;
    }

    public void updateRunsData() {
        List<DataPoint> data = rawData;
        sections = new ArrayList<>();
        convertedData = new ArrayList<>();
        if (runsCounter <= 0 || runsCounter > 1000) {
            runsCounter = DEFAULT_RUN_COUNT;
        }

        int windowSize = Settings.WINDOW_SIZE;
        int stepSize = Settings.WINDOW_STEP_SIZE; // Sliding Step

        this.conversion = Settings.CONVERSION_VALUE;
        if(data.size() < windowSize){
            Logging.log(Level.WARNING, "Job", String.format("Data size %d is less than window size %d", data.size(), windowSize));
            return;
        }
        this.runDataSize = (data.size() / windowSize);


        int runId = 1;

        for (int i = 0; i + windowSize <= data.size(); i += stepSize) {
            List<DataPoint> run_data = new ArrayList<>();

            for (int j = i; j < i + windowSize; j++) {
                DataPoint dp = new DataPoint(data.get(j).data / Settings.CONVERSION_VALUE, rawData.get(j).time);
                run_data.add(dp);
                convertedData.add(dp);
            }

            Section section = new Section(runId++, run_data);
            sections.add(section);
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
        this.sections.subList(0, skipRuns).clear();
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
//
//    public int getRunsCounter() {
//        return this.runsCounter;
//    }
//
//    public void setRunsCounter(int runsCounter) {
//        if(runsCounter < MIN_RUN_COUNT || runsCounter > MAX_RUN_COUNT){
//            Logging.log(Level.WARNING, "Job", String.format("In Job: %s", getFileName()));
//            Logging.log(Level.WARNING, "Job", String.format("Runs counter must be between %d and %d", MIN_RUN_COUNT, MAX_RUN_COUNT));
//            Logging.log(Level.WARNING, "Job", String.format("Runs counter set to default value %d", DEFAULT_RUN_COUNT));
//            this.runsCounter = DEFAULT_RUN_COUNT;
//            return;
//        }
//
//        this.runsCounter = runsCounter;
//    }

    public double getAlpha() {
        return this.alphaProperty.get();
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

    public ObservableList<Section> getRuns() {
        return FXCollections.observableArrayList(this.sections);
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

    public int getSkipSeconds() {
        return skipSeconds;
    }

    public void setSkipSeconds(int skipSeconds) {
        this.skipSeconds = skipSeconds;
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
        return String.format("Job ID: %d | Average Speed %s | Time in sec.: %f | Alpha: %f | File: %s", this.ID, new DecimalFormat("#.##").format(this.averageSpeed), this.getTimeInSec(), getAlpha(), this.file);
    }
    public DoubleProperty alphaProperty() { return alphaProperty; }
}
