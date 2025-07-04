package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Utils;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {

    public static double average(Run run) {
        double sum = 0;
        double n = run.getData().size();
        for (DataPoint dp : run.getData()) {
            sum += dp.data;
        }
        return sum / n;
    }

    public static double average(List<Run> group) {
        double sum = 0;
        double n = 0;
        for (Run run : group) {
            for (DataPoint dp : run.getData()) {
                sum += dp.data;
                n++;
            }
        }
        return sum / n;
    }

    // Calculate the median of a sorted list
    public static double median(List<DataPoint> data) {
        ArrayList<DataPoint> sorted = new ArrayList<DataPoint>(data);
        sorted.sort(new Utils.SpeedComparator());
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2).data;
        } else {
            return (sorted.get(n / 2 - 1).data + sorted.get(n / 2).data) / 2.0;
        }
    }

    // Calculate MAD
    public static double mad(Run run, double median) {
        List<DataPoint> deviations = new ArrayList<>();
        for (DataPoint dp : run.getData()) {
            deviations.add(new DataPoint(Math.abs(dp.data - median), dp.time));
        }
        deviations.sort(new Utils.SpeedComparator());
        return median(deviations);
    }

    public static double variance(Run run) {
        double sum = 0;
        double average = average(run);
        double n = run.getData().size();
        for (DataPoint dp : run.getData()) {
            sum += Math.pow(dp.data - average, 2);
        }
        return sum / (n - 1);
    }

    public static double variance(List<Double> list, double average) {
        double sum = 0;
        double n = list.size();
        for (Double d : list) {
            sum += Math.pow(d - average, 2);
        }
        return sum / (n - 1);
    }

    public static double variance(double[] list, double average) {
        double sum = 0;
        double n = list.length;
        for (double d : list) {
            sum += Math.pow(d - average, 2);
        }
        return sum / (n - 1);
    }

    public static double average(double[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array must not be null or empty.");
        }

        double sum = 0.0;
        for (double d : data) {
            sum += d;
        }

        return sum / data.length;
    }

    public static double standardDeviation(double[] data) {
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("Data array must contain at least two elements.");
        }

        double mean = 0.0;
        for (double d : data) {
            mean += d;
        }
        mean /= data.length;

        double sumSquaredDiffs = 0.0;
        for (double d : data) {
            double diff = d - mean;
            sumSquaredDiffs += diff * diff;
        }

        return Math.sqrt(sumSquaredDiffs / (data.length - 1));  // Sample standard deviation
    }

    public static double calculateDeviation(List<DataPoint> data, double average_speed) {
        double sum = 0.0;
        for (DataPoint dataPoint : data) {
            sum += Math.pow(dataPoint.data - average_speed, 2);
        }
        return Math.sqrt(sum / data.size());
    }
}
