/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;


/**
 * @author meni1999
 */
public class DataPoint {
    protected double speed;
    protected double time;

    public DataPoint(double speed, double time) {
        this.speed = speed;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("Speed: %f, Time: %f", speed, time);
    }

    public double getSpeed() {
        return this.speed;
    }

    public double getTime() {
        return this.time;
    }

}
