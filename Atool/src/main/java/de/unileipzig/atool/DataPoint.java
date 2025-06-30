/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;


/**
 * @author meni1999
 */
public class DataPoint {
    protected double data;
    protected double time;

    public DataPoint(double speed, double time) {
        this.data = speed;
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

    public double getData() {
        return this.data;
    }

    public double getTime() {
        return this.time;
    }

}
