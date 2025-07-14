package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.DataPoint;
import de.unileipzig.atool.Section;

import java.util.List;

public class MathUtils {

    public static double average(Section section) {
        double sum = 0;
        double n = section.getData().size();
        for (DataPoint dp : section.getData()) {
            sum += dp.data;
        }
        return sum / n;
    }

    public static double average(List<Section> group) {
        double sum = 0;
        double n = group.size();
        for (Section section : group) {
            sum += section.getAverageSpeed();
        }
        return sum / n;
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

    public static double calculateDeviation(List<DataPoint> data, double average_speed) {
        double sum = 0.0;
        for (DataPoint dataPoint : data) {
            sum += Math.pow(dataPoint.data - average_speed, 2);
        }
        return Math.sqrt(sum / data.size());
    }


    public enum CONVERT {
        DEFAULT, // KIBI_BYTE
        MEGA_BYTE,
        MEBI_BYTE,
        KILO_BYTE;

        public static double getConvertValue(CONVERT hl) {
            return switch (hl) {
                case MEGA_BYTE -> 976.6;
                case MEBI_BYTE -> 1024.0;
                case KILO_BYTE -> 1.0 / 1024.0;
                default -> // KIBI_BYTE
                        1.0;
            };
        }
    }
}
