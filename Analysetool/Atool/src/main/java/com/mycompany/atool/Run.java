/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

/**
 *
 * @author meni1999
 */
public class Run {
    private List<Point2D> data;
    private List<Double> new_data = new ArrayList<>();
    private final int runID;
    private double intervalFrom = 0;
    private double intervalTo = 0;
    private double averageSpeed;
    private double standardDeviation = 0;
    
    public Run(final int runNumber, ArrayList<Point2D> run_data){
        this.runID = runNumber;
        this.data = run_data; 
        calculateRun();
    }

    private void calculateRun() {
        double speed = 0;
        for (Point2D p : data) {
            speed += p.getY();
        }
        this.averageSpeed = speed / data.size();
        
        double zaehler = 0;
        for (Point2D p : data) {
            zaehler += Math.pow(p.getY() - averageSpeed, 2);
        }
        
        this.standardDeviation = Math.floor(Math.sqrt((double)(zaehler / data.size()))*100) / 100;
    }
    
    
    public int getRunID(){
        return runID;
    }
    
    public double getStandardDeviation(){
        return this.standardDeviation;
    }
    
    public double getAverageSpeed(){
        return  Math.floor(this.averageSpeed * 100) / 100;
    }
    
    public double getIntervalFrom(){
        return  Math.floor(this.intervalFrom * 100) / 100;
    }
    
    public double getIntervalTo(){
        return  Math.floor(this.intervalTo * 100) / 100;
    }

     public void getIntervalFrom(double intervalFrom){
        this.intervalFrom = intervalFrom;
    }
    
    public void getIntervalTo(double intervalTo){
        this.intervalTo = intervalTo;
    }
    
    public double getPlusMinusValue(){
        return Math.floor((this.intervalTo - this.intervalFrom) * 100) / 100;
    }
    
    public List<Point2D> getData(){
        return this.data;
    }
    
    public List<Double> getNewData(){
        for (Point2D point2D : data) {
            new_data.add(point2D.getY());
        }
        return this.new_data;
    }
    
    public int getOverlapping(){
        return 0;
    } 

    public void setIntervalFrom(double d) {
        this.intervalFrom = d;
    }

    public void setIntervalTo(double d) {
        this.intervalTo = d;
    }

    
    
}
