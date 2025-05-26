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

        // Estimate target mean from the initial window
//        double sum = 0;
//        int skippedRuns = job.getRuns().size() * 2;
//        for (int i = skippedRuns; i < job.getData().size(); i++) {
//            sum += data.get(i).getSpeed();
//        }
//        double targetMean = sum / (job.getData().size() - skippedRuns);
        //double targetMean = 20000;
//        double targetMean = job.getAverageSpeed();
        double firstSpeed = job.getData().getFirst().getSpeed();
        double targetMean = (firstSpeed - this.job.getAverageSpeed()) / (this.job.getStandardDeviation());


        cusumPosData.add(new XYChart.Data<>(0.0, 0.0));
        cusumNegData.add(new XYChart.Data<>(0.0, 0.0));
        cusumData.add(new XYChart.Data<>(0.0,0.0));


        double lastCPos = 0;
        double lastCNeg = 0;
        double lastCSum = 0;
        int l = 1;
        for (int i = 1; i < data.size(); i += 1) {
            double speed = data.get(i).getSpeed();
            double time = data.get(i).getTime();
            double cPos = Math.max(0.0, lastCPos + (speed - targetMean - k));
            double cNeg = Math.min(0.0, lastCNeg + (speed - targetMean + k));
            double cSum = lastCSum + (speed - targetMean - k);

            // Check if last `windowSize` CUSUM values are within threshold
//            if (i >= windowSize * l) {
//                sum = 0;
//                int j = windowSize * l;
//                int nextWindow = windowSize * (l + 1);
//                for (; j < nextWindow; j++) {
//                    if (j < data.size() - windowSize) {
//                        sum += data.get(j).getSpeed();
//                    }
//                }
//            }
//            if (sum != 0) {
//                targetMean = sum / windowSize;
//                time = data.get(i).getTime();
//            }

            lastCPos = cPos;
            lastCNeg = cNeg;
            lastCSum = cSum;
            targetMean = (speed - this.job.getAverageSpeed()) / (this.job.getStandardDeviation());
            cusumPosData.add(new XYChart.Data<>(time, cPos));
            cusumNegData.add(new XYChart.Data<>(time, cNeg));
            cusumData.add(new XYChart.Data<>(time, cSum));

            l++;
        }
    }

    public void calculateWindowed(){
        List<DataPoint> data = this.job.getData();
        List<Double> windowList = new ArrayList<>();
        int windowSize = 100;
        double k = 0;           // Slack value
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
