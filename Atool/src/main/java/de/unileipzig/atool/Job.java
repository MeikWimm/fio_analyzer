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

    public final static Double DEFAULT_EPSILON = 1.0;
    public final static Double MAX_EPSILON = 1000.0;
    public final static Double MIN_EPSILON = 1.0;

    public final static Double DEFAULT_CV_THRESHOLD = .2;
    public final static Double MAX_CV_THRESHOLD = .6;
    public final static Double MIN_CV_THRESHOLD = .05;

    public final static Double DEFAULT_RCIW_THRESHOLD = .02;
    public final static Double MAX_RCIW_THRESHOLD = .08;
    public final static Double MIN_RCIW_THRESHOLD = .01;

    private static int COUNTER = 1; // so that each Job has a unique ID
    private Map<Integer, Integer> freq;
    private List<XYChart.Data<Number, Number>> chartData;
    private final int ID = COUNTER;
    private final List<DataPoint> rawData;
    private List<DataPoint> data;
    private final List<XYChart.Data<Number, Number>> speedSeries;
    private List<DataPoint> convertedData;
    private List<Run> runs;
    private File file;
    private BasicFileAttributes attr;
    private int runsCounter = DEFAULT_RUN_COUNT;
    private int time;
    private int runDataSize;
    private int averageTimePerMilliSec;
    private double conversion;
    private double averageSpeed;
    private double epsilon = DEFAULT_EPSILON;
    private double alpha = DEFAULT_ALPHA;
    private double cvThreshold = DEFAULT_CV_THRESHOLD;
    private double rciwThreshold = DEFAULT_RCIW_THRESHOLD;
    private double calculatedF;
    private double standardDeviation;
    private double SSE;
    private double MSE;
    private int skipSize;


    public Job(List<DataPoint> data, int averageTimePerMilliSec) {
        this.freq = new TreeMap<>();
        this.speedSeries = new ArrayList<>();
        this.data = new ArrayList<>(data);
        this.rawData = new ArrayList<>(data);
        this.averageTimePerMilliSec = averageTimePerMilliSec;
        this.convertedData = new ArrayList<>();
        this.chartData = new ArrayList<>();
        prepareData();
        COUNTER++;
    }

    public Job(Job other) {
        other.updateRunsData();
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
        this.averageTimePerMilliSec = other.averageTimePerMilliSec;
        this.attr = other.attr;
        this.epsilon = other.epsilon;
        this.alpha = other.alpha;
        this.calculatedF = other.calculatedF;
        this.standardDeviation = other.standardDeviation;
        this.MSE = other.MSE;
        this.SSE = other.SSE;
        this.cvThreshold = other.cvThreshold;
        this.rciwThreshold = other.rciwThreshold;
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

        if (skipGroups) {
            // create Groups like Run 1 - Run 2, Run 3 - Run 4, Run 5 - Run 6,...
            int groupCount = runsCounter / groupSize;
            int runIndex = 0;

            for (int i = 0; i < groupCount; i++) {
                List<Run> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    group.add(runs.get(runIndex));
                    runIndex++;
                }
                group.getFirst().setGroup(String.format("Run %d - Run %d", group.getFirst().getRunID(), group.getLast().getRunID()));

                groups.add(group);
            }
        } else {

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
        }
        return groups;
    }

    public void prepareSkippedData(int skipRuns) {
        this.skipSize = skipRuns;
        if (skipRuns < 1) {
            return;
        }
        int skipSize = this.runDataSize * skipRuns;
        this.data.subList(0, skipSize).clear();
        this.runs.subList(0, skipRuns).clear();
        runsCounter = runsCounter - skipRuns;
    }

    private List<DataPoint> getRawData() {
        return this.rawData;
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

    private void prepareData() {
        //this.epsilon = rawData.size() / 1000.0;
        if (this.epsilon > MAX_EPSILON) {
            this.epsilon = MAX_EPSILON;
        }

        updateRunsData();
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
    }

    public double getAlpha() {
        return this.alpha;
    }

    void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public double getCvThreshold() {
        return this.cvThreshold;
    }

    public void setCvThreshold(Double cvThreshold) {
        this.cvThreshold = cvThreshold;
    }

    public double getRciwThreshold() {
        return this.rciwThreshold;
    }

    public void setRciwThreshold(Double rciwThreshold) {
        this.rciwThreshold = rciwThreshold;
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
        return ((this.averageSpeed / Settings.CONVERSION_VALUE));
    }

    public void setAverageSpeed(double average_speed) {
        this.averageSpeed = average_speed;
    }

//    public Map<Integer, Integer> getFrequency() {
//        return this.frequency;
//    }
//
//    public void setFrequency(Map<Integer, Integer> freq) {
//        this.frequency = freq;
//    }

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
        //List<DataPoint> data = rawData;
        runs = new ArrayList<>();
        convertedData = new ArrayList<>();
        if (runsCounter <= 0 || runsCounter > 1000) {
            runsCounter = DEFAULT_RUN_COUNT;
        }

        this.conversion = Settings.CONVERSION_VALUE;
        int i = 0;

        final int MIN_RUN_DATA_LENGTH = 8;
        //final int MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI = (int) Math.floor(rawData.size() / (double) (MIN_RUN_DATA_LENGTH * runsCounter));
        final int MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI = (int) Math.floor(rawData.size() / (double) (MIN_RUN_DATA_LENGTH * runsCounter));

        if (averageTimePerMilliSec > MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI) {
            averageTimePerMilliSec = MAX_POSSIBLE_AVERAGE_TIME_PER_MILLI;
        }

        if (averageTimePerMilliSec <= 1) {
            averageTimePerMilliSec = 1;
        }

        List<DataPoint> averagedData = new ArrayList<>();
        if (!(averageTimePerMilliSec == 1)) {
            double speed = 0;
            boolean flag = false;

            for (DataPoint dataPoint : rawData /*rawData*/) {
                if (i % averageTimePerMilliSec == 0 && flag) {
                    double average_speed = speed / averageTimePerMilliSec;
                    averagedData.add(new DataPoint(average_speed, dataPoint.getTime()));
                    speed = dataPoint.getSpeed();
                } else {
                    flag = true;
                    speed += dataPoint.getSpeed();
                }
                i++;
            }
        } else {
            averagedData = new ArrayList<>(rawData);
        }

        this.data = averagedData;
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

    public void setAverageTimePerMilliSec(int averageTimePerMilliSec) {
        this.averageTimePerMilliSec = averageTimePerMilliSec;
    }

    public List<DataPoint> getDataNormalized() {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data list must contain at least two elements.");
        }

        int n = data.size();
        double sum = 0.0;

        for (DataPoint value : data) {
            sum += value.getSpeed();
        }
        double mean = sum / n;

        double squaredDiffSum = 0.0;
        for (DataPoint value : data) {
            double diff = value.getSpeed() - mean;
            squaredDiffSum += diff * diff;
        }
        double stdDev = Math.sqrt(squaredDiffSum / (n - 1)); // Sample standard deviation

        List<DataPoint> normalized = new ArrayList<>();
        for (DataPoint value : data) {
            double z = (value.getSpeed() - mean) / stdDev;
            normalized.add(new DataPoint(z, value.getTime()));
        }

        return normalized;
    }

    // Normalize data using median and MAD
    public List<DataPoint> getMADNormalized() {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data list must contain at least two elements.");
        }

        // Step 1: Compute the median
        List<DataPoint> sorted = new ArrayList<>(data);
        sorted.sort(new Utils.SpeedComparator());
        double median = computeMedian(sorted);

        // Step 2: Compute the absolute deviations from the median
        List<DataPoint> absDeviations = new ArrayList<>();
        for (DataPoint value : data) {
            absDeviations.add(new DataPoint(Math.abs(value.getSpeed() - median), value.time));
        }

        // Step 3: Compute MAD (Median Absolute Deviation)
        sorted.sort(new Utils.SpeedComparator());
        double mad = computeMedian(absDeviations);

        if (mad == 0) {
            throw new ArithmeticException("MAD is zero — normalization cannot proceed.");
        }

        // Step 4: Normalize using robust z-score
        // z_i = (x_i - median) / (MAD * 1.4826)
        double madScale = mad * 1.4826;  // Consistency constant for normal distribution
        List<DataPoint> normalized = new ArrayList<>();
        for (DataPoint value : data) {
            double z = (value.getSpeed() - median) / madScale;
            normalized.add(new DataPoint(z, value.time));
        }

        return normalized;
    }

    // Helper to compute the median
    private double computeMedian(List<DataPoint> sorted) {
        int n = sorted.size();
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1).getSpeed() + sorted.get(n / 2).getSpeed()) / 2.0;
        } else {
            return sorted.get(n / 2).getSpeed();
        }
    }

    public ObservableList<Run> getRuns() {
        return FXCollections.observableArrayList(this.runs);
    }

    public ObservableList<Run> getFilteredRuns() {
        ArrayList<Run> clearedRuns = new ArrayList<>();
        for (Run run : this.runs) {
            if (!(Objects.equals(run.getGroup(), ""))) {
                clearedRuns.add(run);
            }
        }
        return FXCollections.observableArrayList(clearedRuns);
    }

    public double getStandardDeviation() {
        return this.standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public List<XYChart.Data<Number, Number>> getFrequencySeries() {
        if(this.chartData.isEmpty()) {
            this.chartData = freq.entrySet()
                    .stream()
                    .map(entry -> new XYChart.Data<Number, Number>(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
        return chartData;
    }

    public List<XYChart.Data<Number, Number>> getSeries() {
        if (this.speedSeries.isEmpty()) {
            for (DataPoint dp : getData()) {
                speedSeries.add(new XYChart.Data<>(dp.getTime(), dp.getSpeed()));
            }
        }

        return this.speedSeries;
    }

    public int getDataStartPoint(Run run){
        return run.getData().size();
    }

    public int getDataEndPoint(Run run){
        return this.data.size() - run.getData().size();
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
    }

    public int getRunDataSize() {
        return this.runDataSize;
    }

    public int getSkippedRuns() {
        return this.skipSize;
    }
}
