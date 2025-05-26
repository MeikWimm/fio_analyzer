package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.List;

public class CUSUM extends GenericTest{
    private final Charter charter;
    private final List<XYChart.Data<Number, Number>> cusumData;
    private final List<XYChart.Data<Number, Number>> cusumPosData;
    private final List<XYChart.Data<Number, Number>> cusumNegData;

    public CUSUM(Job job, boolean skipGroup, int groupSize, double alpha) {
        super(job, skipGroup, groupSize, alpha);
        this.charter = new Charter();
        this.cusumPosData = new ArrayList<>();
        this.cusumNegData = new ArrayList<>();
        this.cusumData = new ArrayList<>();
    }

    @Override
    public void calculate() {
        List<DataPoint> data = this.job.getData();
        int initWindow = 1000;
        int windowSize = 1000;
        double k = 100;           // Slack value
        double h = 5.0;
        if (data.size() < initWindow + windowSize) return;

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
        List<Double> windowList = new ArrayList<>();
        int windowSize = 100;
        double k = 0.0;           // Slack value
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
        double std = Math.sqrt(GenericTest.variance(windowList, targetMean));


        cusumPosData.add(new XYChart.Data<>(0.0, 0.0));
        cusumNegData.add(new XYChart.Data<>(0.0, 0.0));
        cusumData.add(new XYChart.Data<>(0.0,0.0));


        double lastCPos = 0;
        double lastCNeg = 0;
        double lastCSum = 0;
        //double speed = data.get(i).getSpeed();
        //double time = data.get(i).getTime();
        int l = 1;
        for (int i = 0; i < data.size(); i += 1) {
            double speed = data.get(i).getSpeed();
            double z = ((speed - targetMean) / (std));
            double cPos = Math.max(0.0, lastCPos + (speed - targetMean - k));
            double cNeg = Math.min(0.0, lastCNeg + (speed - targetMean + k));
            double cSum = lastCSum + (speed - targetMean - k);

            // Check if last `windowSize` CUSUM values are within threshold
            int counter = windowSize;
            if (i >= windowSize * l) {
                sum = 0;
                int j = windowSize * l;
                int nextWindow = windowSize * (l + 1);
                for (; j < nextWindow; j++) {
                    if (j < data.size() - windowSize) {
                        speed = data.get(j).getSpeed();
                        sum += speed;
                        windowList.add(speed);
                        counter --;
                    }
                }
                if (counter != 0) {
                    break;
                }

                targetMean = sum / windowSize;
                std = Math.sqrt(GenericTest.variance(windowList, targetMean));
                l++;
            }

            lastCPos = cPos;
            lastCNeg = cNeg;
            lastCSum = cSum;


            cusumPosData.add(new XYChart.Data<>(i, cPos));
            cusumNegData.add(new XYChart.Data<>(i, cNeg));
            cusumData.add(new XYChart.Data<>(i, cSum));
            windowList.clear();
        }
    }

    public void draw(){
        charter.drawGraph("CUSUM", "Time", "Cusum", "CUSUM Postive", 0,this.cusumData, this.cusumPosData, this.cusumNegData);
    }

}
