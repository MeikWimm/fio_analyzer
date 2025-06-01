package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.List;

public class CUSUM extends GenericTest{
    private final Charter charter;
    private final List<XYChart.Data<Number, Number>> cusumData;
    private final List<XYChart.Data<Number, Number>> cusumPosData;
    private final List<XYChart.Data<Number, Number>> cusumNegData;
    private String title = "";
    private String label = "";
    private final int WINDOW_SIZE;

    public CUSUM(Job job, Settings settings, double alpha) {
        super(job, settings.getCusumSkipRunsCounter(), settings.isCusumUseAdjacentRun(), 2, alpha, true);
        this.charter = new Charter();
        int dataSize = this.job.getData().size();
        this.cusumPosData = new ArrayList<>(dataSize);
        this.cusumNegData = new ArrayList<>(dataSize);
        this.cusumData = new ArrayList<>(dataSize);
        this.WINDOW_SIZE = settings.getWindowSize();
    }

    @Override
    public void calculate() {
        List<DataPoint> data = this.job.getData();
       // int initWindow = 1000;
       // int windowSize = 1000;
        double k = 100;           // Slack value
        double h = 5.0;
       // if (data.size() < initWindow + windowSize) return;

        cusumPosData.add(new XYChart.Data<>(0.0, 0.0));
        cusumNegData.add(new XYChart.Data<>(0.0, 0.0));
        cusumData.add(new XYChart.Data<>(0.0,0.0));


        double lastCPos = 0;
        double lastCNeg = 0;
        double lastCSum = 0;
        List<Run> runs = this.job.getRuns();
        double averageJob = this.job.getAverageSpeed();
        for (Run run: runs) {
            double speed = run.getAverageSpeed();
            double ID = run.getID();
            double cPos = Math.max(0.0, lastCPos + (run.getAverageSpeed() - averageJob - k));
            double cNeg = Math.min(0.0, lastCNeg + (speed - averageJob + k));
            double cSum = lastCSum + (speed - averageJob - k);

            lastCPos = cPos;
            lastCNeg = cNeg;
            lastCSum = cSum;

            cusumPosData.add(new XYChart.Data<>(ID, cPos));
            cusumNegData.add(new XYChart.Data<>(ID, cNeg));
            cusumData.add(new XYChart.Data<>(ID, cSum));
        }

        this.title = "The CUSUM method involved comparing the average of each run to the overall job average";
        this.label = "Run";
    }

    public void calculateWindowedRuns() {

        List<Run> runs = this.job.getRuns();
        int windowSize = 2;
        if (runs.size() < windowSize + windowSize) return;

        double k = 0.25;     // 142202       // Slack value
        double h = 5.0;
        double sum = 0;

        cusumPosData.add(new XYChart.Data<>(0.0, 0.0));
        cusumNegData.add(new XYChart.Data<>(0.0, 0.0));
        cusumData.add(new XYChart.Data<>(0.0,0.0));

        for (int i = 0; i < windowSize; i++){
            sum += runs.get(i).getAverageSpeed();
        }
        double targetMean = sum / windowSize;

        double lastCPos = 0;
        double lastCNeg = 0;
        double lastCSum = 0;
        int l = 1;
        int i = 0;
        for (Run run: runs) {
            double speed = run.getAverageSpeed();
            double ID = run.getID();
            double cPos = Math.max(0.0, lastCPos + (run.getAverageSpeed() - targetMean - k));
            double cNeg = Math.min(0.0, lastCNeg + (speed - targetMean + k));
            double cSum = lastCSum + (speed - targetMean - k);

            // Check if last `windowSize` CUSUM values are within threshold
            int counter = windowSize;
            if (i >= windowSize * l) {
                sum = 0;
                int j = windowSize * l;
                int nextWindow = windowSize * (l + 1);
                for (; j < nextWindow; j++) {
                    if (j < runs.size() - windowSize) {
                        speed = runs.get(j).getAverageSpeed();
                        sum += speed;
                        counter --;
                    }
                }
                if (counter != 0) {
                    break;
                }

                targetMean = sum / windowSize;
                l++;
            }

            lastCPos = cPos;
            lastCNeg = cNeg;
            lastCSum = cSum;

            cusumPosData.add(new XYChart.Data<>(ID, cPos));
            cusumNegData.add(new XYChart.Data<>(ID, cNeg));
            cusumData.add(new XYChart.Data<>(ID, cSum));
            i++;
        }
    }

    public void calculateWindowed(){
        List<DataPoint> data = this.job.getData();
        List<Double> windowList = new ArrayList<>(); // TODO make an array not list
        int windowSize = WINDOW_SIZE;
        double k = 1.0;           // Slack value
        double h = 5.0;
        if (data.size() < windowSize + windowSize) return;

        // Estimate target mean from the initial window
        double sum = 0;
        for (int i = 0; i < windowSize; i++) {
            double speed = data.get(i).getSpeed();
            sum += speed;
            windowList.add(speed);
        }
        double targetMean = sum / windowSize;


        cusumPosData.add(new XYChart.Data<>(0.0, 0.0));
        cusumNegData.add(new XYChart.Data<>(0.0, 0.0));
        cusumData.add(new XYChart.Data<>(0.0,0.0));


        double lastCPos = 0;
        double lastCNeg = 0;
        double lastCSum = 0;
        //double speed = data.get(i).getSpeed();
        //double time = data.get(i).getTime();
        for (int i = 1; i < data.size() - windowSize; i++) {
            double dataSpeed = data.get(i).getSpeed();

            //double z = ((speed - targetMean) / (std));
            double cPos = Math.max(0.0, lastCPos + (targetMean - dataSpeed - k));
            double cNeg = Math.min(0.0, lastCNeg + (targetMean - dataSpeed - k));
            double cSum = lastCSum + (targetMean - dataSpeed - k);

            sum = 0;
            windowList.clear();
            
            // Check if last `windowSize` CUSUM values are within threshold
            int endWindow = Math.min(i + windowSize, data.size());
                for (int j = i; j < endWindow; j++) {
                    double tempSpeed = data.get(j).getSpeed();
                    sum += tempSpeed;
                    windowList.add(tempSpeed);
                //std = Math.sqrt(GenericTest.variance(windowList, targetMean));
            }

                if(windowList.size() == windowSize){
                targetMean = sum / windowSize;
                lastCPos = cPos;
                lastCNeg = cNeg;
                lastCSum = cSum;
                //System.out.println(lastCPos + " " + lastCNeg + " " + lastCSum);
                cusumPosData.add(new XYChart.Data<>(i, cPos));
                cusumNegData.add(new XYChart.Data<>(i, cNeg));
                cusumData.add(new XYChart.Data<>(i, cSum));
            }

        }

        this.title = String.format("Step-wise CUSUM compared each data point to the mean of its corresponding %d-point segment", windowSize);
        this.label = "Run";
    }


    public void draw(){
        Charter.ChartData cusumChartData = new Charter.ChartData("Cusum", this.cusumData);
        Charter.ChartData cusumPosChartData = new Charter.ChartData("Cusum (+)", this.cusumPosData);
        Charter.ChartData cusumNegChartData = new Charter.ChartData("Cusum (-)", this.cusumNegData);
        charter.drawGraph(this.title, this.label, "Cusum", cusumChartData, cusumPosChartData, cusumNegChartData);
    }

}