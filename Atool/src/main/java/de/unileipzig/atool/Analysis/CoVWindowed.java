package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CoVWindowed extends GenericTest {
    private final List<XYChart.Data<Number, Number>> covWindowedData;
    private final List<DataPoint> windowedPossibleCoVSteadyState;
    private final int WINDOW_SIZE;
    private final double STEADY_STATE_COV_THRESHOLD;


    public CoVWindowed(Job job, Settings settings) {
        super(job, settings.getCovSkipRunsCounter(), settings.isCovUseAdjacentRun(), settings.getGroupSize(), job.getAlpha(), false, settings.getRequiredRunsForSteadyState());
        final int dataSize = job.getData().size();
        this.WINDOW_SIZE = settings.getWindowSize();
        this.covWindowedData = new ArrayList<>(dataSize);
        this.windowedPossibleCoVSteadyState = new ArrayList<>(1000);
        this.STEADY_STATE_COV_THRESHOLD = this.job.getCvThreshold();
    }
    @Override
    public void calculateTest() {
        calculateWindowedCoV();
    }

    @Override
    public void calculateSteadyState(){
        double windowedTime = windowedPossibleCoVSteadyState.getFirst().getTime();
        for (Run run : this.job.getRuns()){
            if(run.getStartTime() <= windowedTime && run.getEndTime() >= windowedTime){
                possibleSteadyStateRuns.add(run);
            }
        }
    }

    @Override
    public Scene getCharterScene() {
        return charter.drawGraph("CoV Windowed", "Job", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("Windowed CV over Job", covWindowedData));
    }

    @Override
    protected URL getFXMLPath() {
        return null;
    }

    @Override
    protected String getWindowTitle() {
        return "";
    }

    @Override
    public String getTestName() {
        return "Windowed - CV";
    }

    private void calculateWindowedCoV() {
        int windowSize = WINDOW_SIZE;
        double sum = 0;

        List<DataPoint> data = this.job.getData();
        double[] windowList = new double[WINDOW_SIZE];

        for (int i = 0; i < windowSize; i++) {
            double speed = data.get(i).getData();
            sum += speed;
            windowList[i] = speed;
        }
        double targetMean = sum / windowSize;
        double cov = Math.sqrt(MathUtils.variance(windowList, targetMean)) / targetMean;
        covWindowedData.add(new XYChart.Data<>(data.getFirst().getTime(), cov));

        for (int i = 1; i < data.size(); i++) {
            sum = 0;
            boolean done = false;
            for (int j = 0; j < windowSize; j++) {
                if(i + j < data.size()) {
                    double speed = data.get(i + j).getData();
                    sum += speed;
                    windowList[j] = speed;
                } else {
                    done = true;
                    break;
                }
            }
            targetMean = sum / windowSize;
            double time = data.get(i).getTime();
            cov = Math.sqrt(MathUtils.variance(windowList, targetMean)) / targetMean;
            covWindowedData.add(new XYChart.Data<>(time, cov));

            if(cov < STEADY_STATE_COV_THRESHOLD){
                windowedPossibleCoVSteadyState.add(new DataPoint(cov, time));
            }

            if (done) break;
        }
    }

    //function calculateSteadyState() is overwritten, so no need for this function
    @Override
    protected double extractValue(Run run) {
        return 0;
    }

    //function calculateSteadyState() is overwritten, so no need for this function
    @Override
    protected boolean isWithinThreshold(double value) {
        return false;
    }

    public void drawWindowedCoV() {
        charter.drawGraph("CoV Windowed", "Job", "F-Value", "Threshold", this.job.getCvThreshold(), new Charter.ChartData("Windowed CV over Job", covWindowedData));
        charter.openWindow();
    }

    @Override
    public TableView<Run> getTable() {
        return null;
    }
}