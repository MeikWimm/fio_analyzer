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
            sum += dp.getSpeed();
        }
        return sum / n;
    }

    public static double average(List<Run> group) {
        double sum = 0;
        double n = 0;
        for (Run run : group) {
            for (DataPoint dp : run.getData()) {
                sum += dp.getSpeed();
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
            return sorted.get(n / 2).getSpeed();
        } else {
            return (sorted.get(n / 2 - 1).getSpeed() + sorted.get(n / 2).getSpeed()) / 2.0;
        }
    }

    // Calculate MAD
    public static double mad(Run run, double median) {
        List<DataPoint> deviations = new ArrayList<>();
        for (DataPoint dp : run.getData()) {
            deviations.add(new DataPoint(Math.abs(dp.getSpeed() - median), dp.getTime()));
        }
        deviations.sort(new Utils.SpeedComparator());
        return median(deviations);
    }

    public static double variance(Run run) {
        double sum = 0;
        double average = average(run);
        double n = run.getData().size();
        for (DataPoint dp : run.getData()) {
            sum += Math.pow(dp.getSpeed() - average, 2);
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

    public static double standardError(Run run1, Run run2) {
        double std = 0;
        std = Math.sqrt(0.5 * (variance(run1) / run1.getData().size() + variance(run2) / run2.getData().size()));
        return std;
    }

    public static double calcualteCoVOverStd(double std, List<Run> group) {
        return std / average(group);
    }
}
