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
    private final List<Point2D> data;
    private final List<Double> new_data = new ArrayList<>();
    private final List<Run> runToCompare = new ArrayList<>();
    private final int runID;
    private double intervalFrom = 0;
    private double intervalTo = 0;
    private double averageSpeed;
    private double convetedAverageSpeed = 0;
    private double standardDeviation = 0;
    public static final int SPEED_PER_SEC = 1000;
    private boolean flasg = true;
    private boolean runToCompareItself = false;
    private double ssa;
    private double sse;
    public double F;

    
    public Run(final int runNumber, ArrayList<Point2D> run_data){
        this.runID = runNumber;
        this.data = run_data;
        calculateRun();
    }

    private void calculateRun() {
        double ioSpeed = 0;
        for (Point2D p : data) {
            ioSpeed += p.getY();
        }
        this.averageSpeed = ioSpeed / data.size();
        
        double zaehler = 0;
        for (Point2D p : data) {
            zaehler += Math.pow(p.getY() - averageSpeed, 2);
        }
        
        this.standardDeviation = Math.floor(Math.sqrt((double)(zaehler / data.size()))* Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }
    
    public List<Point2D> getMinimizedData(int average_speed_per_millisec){
        List<Point2D> converted_data = new ArrayList<>();
        double speed = 0;
        int j;
        boolean flag = false;
        int counter = 0;
        for (j = 0; j < data.size(); j++) {
            if(j % average_speed_per_millisec == 0 && flag){
                double average_speed = speed / average_speed_per_millisec;
                converted_data.add(new Point2D(j, average_speed));
                speed = 0;
                counter = 0;
            } else {
                flag = true;
                speed += data.get(j).getY();
                counter++;
            }
        }
        if (j != 0){
            //converted_data.add(new Point2D(j, speed / counter));
            //System.out.println("Last data for converted list ignored.");
        }
        
        speed = 0;
        for (Point2D point2D : converted_data) {
            speed += point2D.getY();
        }
        
        convetedAverageSpeed = speed / converted_data.size();
        
        return converted_data;
    }
    
    
    public int getRunID(){
        return runID;
    }
    
    public double getStandardDeviation(){
        return this.standardDeviation;
    }
    
    public double getAverageSpeed(){
        return  this.averageSpeed;
    }
    
    public double getIntervalFrom(){
        return  this.intervalFrom;
    }
    
    public double getIntervalTo(){
        return  this.intervalTo;
    }

     public void getIntervalFrom(double intervalFrom){
        this.intervalFrom = intervalFrom;
    }
    
    public void getIntervalTo(double intervalTo){
        this.intervalTo = intervalTo;
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
    
    public void addRunToCompareTo(Run run){
        if(!runToCompareItself) {
            runToCompare.add(this);
            runToCompareItself = true;
        }
        runToCompare.add(run);
    }
    
    public List<Run> getRunToCompareTo(){
        return runToCompare;
    }
    
    public String getRunToCompareToAsString(){
        StringBuilder sb = new StringBuilder();
        for (Run run : runToCompare) {
            sb.append(String.format("Run %d", run.getID()));
        }
        
        return sb.toString();
    }

    public void setIntervalTo(double d) {
        this.intervalTo = d;
    }

    public void setF(double F){
        this.F = F;
    }

    public double getF(){
        return this.F;
    }
    
    public void setSSE(double sse) {
        this.sse = sse;
    }
    
    public double getSSE() {
        return sse;
    }
    
    public double getSSA() {
        return ssa;
    }
    
    public void setSSA(double ssa) {
        this.ssa = ssa;
    }

    public double getSST() {
        return sse + ssa;
    }
    
    public int getID(){
        return runID;
    }
    
    public static double calculateAverageSpeedOfData(List<Point2D> data){
        double average = 0;
        for (Point2D point2D : data) {
            average += point2D.getY();
        }
        
        return average / data.size();
    }
    
    public static double calculateAverageSpeedOfRuns(List<Run> runs){
        double average = 0;
        for (Run run : runs) {
            average += calculateAverageSpeedOfData(run.getMinimizedData(SPEED_PER_SEC));
        }
        return average / runs.size();
    }
}
