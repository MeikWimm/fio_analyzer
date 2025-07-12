/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;


/**
 * @author meni1999
 */
public class DataPoint {
    public final double data;
    public final double time;

    /**
     * Constructs a new DataPoint instance with the specified data and time values.
     *
     * @param data the data value associated with this DataPoint
     * @param time the time value associated with this DataPoint
     */
    public DataPoint(double data, double time) {
        this.data = data;
        this.time = time;
    }

    public DataPoint(DataPoint other){
        this.data = other.data;
        this.time = other.time;
    }

    @Override
    public String toString() {
        return String.format("Speed: %f, Time: %f", data, time);
    }

    public static class RankedDataPoint extends DataPoint {
        public final int flag;
        double rank;

        /**
         * Constructs a new RankedDataPoint object.
         *
         * @param dp   the original DataPoint from which this RankedDataPoint is derived
         * @param rank the rank value assigned to this data point
         * @param flag an additional flag for categorization or identification
         */
        public RankedDataPoint(DataPoint dp, int rank, int flag) {
            super(dp.data, dp.time);
            this.rank = rank;
            this.flag = flag;
        }

        public double getRank() {
            return rank;
        }

        public void setRank(double rank) {
            this.rank = rank;
        }
    }
}
