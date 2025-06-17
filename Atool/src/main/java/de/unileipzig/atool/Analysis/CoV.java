package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.List;

public class CoV extends GenericTest {
    private final List<XYChart.Data<Number, Number>> covAveragedData;
    private final List<XYChart.Data<Number, Number>> covData;
    private final int WINDOW_SIZE;
    private final Charter charter;
    private final double STEADY_STATE_COV_THRESHOLD;


    public CoV(Job job, Settings settings) {
        super(job, settings.getCovSkipRunsCounter(), settings.isCovUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), false, settings.getRequiredRunsForSteadyState());
        final int dataSize = job.getData().size();
        final int dataSizeWithRuns = job.getRuns().size() * 2;
        this.WINDOW_SIZE = settings.getWindowSize();
        this.covAveragedData = new ArrayList<>(dataSize);
        this.covData = new ArrayList<>(dataSizeWithRuns);
        this.charter = new Charter();
        this.STEADY_STATE_COV_THRESHOLD = this.job.getCvThreshold();
    }
    @Override
    public void calculateTest() {
        calculateCoV();
        calculateWindowedCoV();
    }

    private void calculateCoV(){
        for (List<Run> group : this.groups) {
            Run run = group.getFirst();
            double cov = calculateCoVGroup(group);
            run.setCoV(cov);
            covData.add(new XYChart.Data<>(run.getRunID(), cov));
            this.resultRuns.add(run);
        }
    }

    public static void calculateCoVGroups(List<List<Run>> groups){
        for (List<Run> group : groups) {
            Run run = group.getFirst();
            double cov = calculateCoV(group);
            run.setCoV(cov);
        }
    }

    private static double calculateCoV(List<Run> group) {
        if (group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group cannot be null or empty");
        }

        double average = MathUtils.average(group);
        if (average == 0) {
            throw new IllegalArgumentException("Cannot calculate CoV when mean is zero");
        }

        double n = 0;
        double sum = 0;

        for (Run run : group) {
            if (run == null || run.getData() == null) {
                throw new IllegalArgumentException("Invalid run data");
            }
            for (DataPoint dp : run.getData()) {
                sum += Math.pow(dp.getSpeed() - average, 2);
                n++;
            }
        }

        if (n <= 1) {
            throw new IllegalArgumentException("Need at least two data points to calculate CoV");
        }

        double std = Math.sqrt(sum / (n - 1));
        return (std / average);
    }

    @Override
    public String getTestName() {
        return "Coefficient of Variation";
    }

    private double calculateCoVGroup(List<Run> group) {
        if (group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group cannot be null or empty");
        }

        double average = MathUtils.average(group);
        if (average == 0) {
            throw new IllegalArgumentException("Cannot calculate CoV when mean is zero");
        }

        double n = 0;
        double sum = 0;

        for (Run run : group) {
            if (run == null || run.getData() == null) {
                throw new IllegalArgumentException("Invalid run data");
            }
            for (DataPoint dp : run.getData()) {
                sum += Math.pow(dp.getSpeed() - average, 2);
                n++;
            }
        }

        if (n <= 1) {
            throw new IllegalArgumentException("Need at least two data points to calculate CoV");
        }

        double std = Math.sqrt(sum / (n - 1));
        return (std / average);
    }

    private void calculateWindowedCoV() {
        int initWindow = WINDOW_SIZE;
        int windowSize = WINDOW_SIZE;
        double sum = 0;

        List<DataPoint> data = this.job.getData();
        List<Double> windowList = new ArrayList<>();
        for (int i = 0; i < initWindow; i++) {
            sum += data.get(i).getSpeed();
            windowList.add(data.get(i).getSpeed());
        }
        double targetMean = sum / initWindow;
        double cov = Math.sqrt(MathUtils.variance(windowList, targetMean)) / targetMean;
        covAveragedData.add(new XYChart.Data<>(data.getFirst().getTime(), cov));

        for (int i = 1; i < data.size() - windowSize; i++) {
            sum = 0;
            int nextWindow = windowSize + i;
            for (int j = i; j < nextWindow; j++) {
                if (j < data.size()) {
                    windowList.add(data.get(j).getSpeed());
                    sum += data.get(j).getSpeed();
                }
            }
            targetMean = sum / windowSize;
            double time = data.get(i).getTime();
            cov = Math.sqrt(MathUtils.variance(windowList, targetMean)) / targetMean;
            covAveragedData.add(new XYChart.Data<>(time, cov));
            windowList.clear();
        }
    }

    @Override
    protected double extractValue(Run run) {
        return run.getCoV();
    }

    @Override
    protected boolean isWithinThreshold(double value) {
        return value < STEADY_STATE_COV_THRESHOLD;
    }

    public void drawCoVGraph() {
        charter.drawGraph("Run CoV", "Per run", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("CV over Job", covData));
    }

    public void drawAveragedCoVGraph() {
        charter.drawGraph("CoV Windowed", "Job", "F-Value", new Charter.ChartData("Windowed CV over Job", covAveragedData));
    }
}