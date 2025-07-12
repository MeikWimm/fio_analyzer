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
        this.updateRunsData();
    }

    /**
     * Updates the runs data by dividing the raw input data into a specified number of runs.
     * Each run contains a subset of the data points, and the data points are converted
     * based on a predefined conversion factor. The conversion updates the data value by
     * dividing it with the Settings.CONVERSION_VALUE.
     * <>
     * If the runs counter is not within the permissible range, it is reset to a default value.
     * The raw input data is then equally split into the required number of runs, with each run
     * containing a proportional size of the total data. During this process, the converted
     * data points are also stored into a separate list for further processing.
     *
     * The method ensures that partial data points are grouped into runs, and complete run objects
     * are stored in the runs list for accessibility.
     *
     * Important class invariants:
     * - If runsCounter is less than or equal to 0 or greater than 1000, the default runs count
     *   (DEFAULT_RUN_COUNT) is used.
     * - The size of each run (runDataSize) is calculated by dividing the total data size by
     *   the runs counter.
     *
     * Method constraints and operations:
     * - Iterates through the raw data points, converts them using the predefined conversion factor,
     *   and groups them into multiple runs.
     * - Each run is instantiated with a unique identifier and the corresponding list of data points.
     *
     * Preconditions:
     * - rawData should not be null and must contain valid DataPoint objects.
     *
     * Postconditions:
     * - The runs list is populated with run objects containing grouped and converted data points.
     * - The convertedData list contains all data points after conversion.
     *
     * Note:
     * This method is primarily used to preprocess and structure data for further analysis or
     * computation tasks associated with the job.
     */
    public void updateRunsData() {
        List<DataPoint> data = rawData;
        runs = new ArrayList<>();
        convertedData = new ArrayList<>();
        if (runsCounter <= 0 || runsCounter > 1000) {
            runsCounter = DEFAULT_RUN_COUNT;
        }

        this.conversion = Settings.CONVERSION_VALUE;
        this.runDataSize = (data.size() / runsCounter);

        /*
        Split job into runs depending on run_size
        */
        ArrayList<DataPoint> run_data;
        int i = 0;
        for (int j = 1; j <= runsCounter; j++) {
            run_data = new ArrayList<>();
            for (; i < this.runDataSize * j; i++) {
                DataPoint dp = new DataPoint(data.get(i).data / Settings.CONVERSION_VALUE, rawData.get(i).time);
                run_data.add(dp);
                convertedData.add(dp);
            }
            Run run = new Run(j, run_data);
            runs.add(run);
        }
    }

    /**
     * Prepares and processes skipped data for a job by removing a specified number of runs
     * and their corresponding data points from the beginning of the job. If the number of
     * runs to be skipped is invalid or exceeds the available data size, the operation is
     * logged and aborted.
     *
     * @param skipRuns the number of runs to skip. Must be a positive integer less than or
     *                 equal to the number of available runs in the job. If this value
     *                 exceeds the data size, a warning is logged, and no data is skipped.
     */
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

    /**
     * Retrieves the series of XYChart.Data points representing the processed job data.
     * If the series is empty, it is populated using the underlying data points by mapping
     * their time and data values into XYChart.Data elements. This allows the same series
     * to be reused without redundant recalculation.
     *
     * @return a list of {@code XYChart.Data<Number, Number>} objects representing the job's processed speed data
     */
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

    /**
     * Retrieves the name of the file associated with this job.
     * If no file is set, an empty string is returned.
     *
     * @return the file name as a String, or an empty string if the file is null.
     */
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

    /**
     * Sets the runs counter for the job. The value must be between MIN_RUN_COUNT
     * and MAX_RUN_COUNT. If the provided value is outside this range, the runs
     * counter is set to the default value (DEFAULT_RUN_COUNT), and a warning
     * message is logged.
     *
     * @param runsCounter the desired runs counter value. Must be between
     *                    MIN_RUN_COUNT and MAX_RUN_COUNT. If invalid, the value
     *                    will default to DEFAULT_RUN_COUNT.
     */
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

    /**
     * Sets the alpha value for the job. The alpha value must be between
     * MIN_ALPHA and MAX_ALPHA. If the provided value is outside this range,
     * a default value (DEFAULT_ALPHA) will be used, and a warning message will be logged.
     *
     * @param alpha the desired alpha value. Must be between MIN_ALPHA and MAX_ALPHA.
     *              If invalid, the value will default to DEFAULT_ALPHA.
     */
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

    /**
     * Sets the coefficient of variation (CV) threshold value for the job.
     * If the provided value is outside the acceptable range, the threshold
     * is set to a default value, and a warning message is logged.
     *
     * @param cvThreshold the desired CV threshold value. Must be between
     *                     MIN_CV_THRESHOLD and MAX_CV_THRESHOLD. If invalid,
     *                     the value will default to DEFAULT_CV_THRESHOLD.
     */
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

    public void setFrequencyMap(Map<Integer, Integer> freq) {
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
