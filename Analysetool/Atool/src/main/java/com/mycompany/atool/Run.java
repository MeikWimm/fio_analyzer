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
        return this.averageSpeed;
    }
    
    public double getIntervalFrom(){
        return intervalFrom;
    }
    
    public double getIntervalTo(){
        return intervalTo;
    }
    
    public int getOverlapping(){
        return 0;
    }

    
    
}
