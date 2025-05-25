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
        int initWindow = 50;
        int windowSize = 50;
        double k = 0.5;           // Slack value
        double h = 5.0;
        if (data.size() < initWindow + windowSize) return;

        // Estimate target mean from the initial window
        double sum = 0;
        for (int i = 0; i < initWindow; i++) {
            sum += data.get(i).getSpeed();
        }
        double targetMean = sum / initWindow;


        cusumPosData.add(new XYChart.Data<>(0.0, 0.0));
        cusumNegData.add(new XYChart.Data<>(0.0, 0.0));
        cusumData.add(new XYChart.Data<>(0.0,0.0));


        double lastCPos = 0;
        double lastCNeg = 0;
        double lastCSum = 0;
        int l = 1;
        for (int i = 0; i < data.size(); i++) {
            double speed = data.get(i).getSpeed();
            double time = data.get(i).getTime();
            double cPos = Math.max(0.0, lastCPos + (speed - targetMean - k));
            double cNeg = Math.min(0.0, lastCNeg + (speed - targetMean + k));
            double cSum = lastCSum + (speed - targetMean - k);

            // Check if last `windowSize` CUSUM values are within threshold
            if (i >= windowSize * l) {
                sum = 0;
                for (int j = 0; j < windowSize; j++) {
                    sum += data.get(i).getSpeed();
                }
                targetMean = sum / windowSize;
                l++;
            }


            lastCPos = cPos;
            lastCNeg = cNeg;
            lastCSum = cSum;
            cusumPosData.add(new XYChart.Data<>(time, cPos));
            cusumNegData.add(new XYChart.Data<>(time, cNeg));
            cusumData.add(new XYChart.Data<>(time, cSum));
        }
    }

    public void draw(){
        charter.drawGraph("CUSUM", "Time", "Cusum", "CUSUM Postive", 10000000.0,this.cusumData, this.cusumPosData, this.cusumNegData);
    }
}
