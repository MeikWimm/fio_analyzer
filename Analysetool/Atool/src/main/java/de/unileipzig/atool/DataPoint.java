/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;


/**
 * @author meni1999
 */
public class DataPoint {
    private double speed;
    private double time;
    private int flag;
    private double rank;
    private int runID = Run.UNDEFINED_INTEGER;

    public DataPoint(double speed, double time) {
        this.speed = speed;
        this.time = time;
        this.rank = Run.UNDEFINED_VALUE;
        this.flag = Run.UNDEFINED_INTEGER;
    }

    @Override
    public String toString() {
        return String.format("Speed: %f, Time: %f", speed, time);
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getTime() {
        return this.time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public double getRank() {
        return this.rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public int getRunID() {
        return this.runID;
    }

    public void setRunID(int runID) {
        this.runID = runID;
    }
}
