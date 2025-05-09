/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author meni1999
 */
public class Run {
    private List<DataPoint> data = new ArrayList<>();
    private final List<Run> runToCompare = new ArrayList<>();
    private final int runID;
    private double intervalFrom = 0;
    private double intervalTo = 0;
    private double averageSpeed;
    private double standardDeviation = 0;
    public static final int SPEED_PER_SEC = 10;
    private double ssa;
    private double sse;
    private boolean isNullhypothesisAccepted = false;
    private double F;
    private double zVal = 0;
    private double qVal = 0;
    public float rank = 0;

    
    public Run(final int runNumber, List<DataPoint> runData){
        this.runID = runNumber;
        this.data = runData;
        calculateRun();
    }

    private void calculateRun() {
        double ioSpeed = 0;
        for (DataPoint p : data) {
            ioSpeed += p.getSpeed();
        }
        this.averageSpeed = ioSpeed / data.size();
        
        double nominator = 0;
        for (DataPoint p : data) {
            nominator += Math.pow(p.getSpeed() - averageSpeed, 2);
        }
        
        this.standardDeviation = Math.floor(Math.sqrt((nominator / data.size()))* Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }
    
    public void setData(List<DataPoint> runData){
        this.data = runData;
        calculateRun();
    }

    public List<DataPoint> getData(){      
        return data;
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
    
    public double getPlusMinusValue(){
        return Math.abs(this.intervalTo - this.intervalFrom);
    }
    
    public int getOverlapping(){
        return 0;
    } 

    public void setIntervalFrom(double d) {
        this.intervalFrom = d;
    }
    
    public void addRunToCompareTo(Run run){
        runToCompare.add(run);
    }
    
    public List<Run> getRunToCompareTo(){
        return runToCompare;
    }
    
    public double getAverageSpeedOfRunsToCompareTo(){
        double speed = 0.0;
        for (Run run : this.runToCompare) {
            speed += run.getAverageSpeed();
        }
        speed = speed / this.runToCompare.size();
        return speed;
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
    
    public void setZ(double zVal){
        this.zVal = zVal;
    }

    public double getZ(){
        return this.zVal;
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
    
    public void setNullypothesis(boolean isNullhypothesisAccepted){
        this.isNullhypothesisAccepted = isNullhypothesisAccepted;
    }
    
    public boolean getNullhypothesis(){
        return this.isNullhypothesisAccepted;
    }

    public void setQ(double qVal) {
        this.qVal = qVal;
    }
}
