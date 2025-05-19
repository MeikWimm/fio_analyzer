/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author meni1999
 */
public class Run {
    public static Byte ACCEPTED_NULLHYPOTHESIS = 1;
    public static Byte REJECTED_NULLHYPOTHESIS = 0;
    public static Byte UNDEFIND_NULLHYPOTHESIS = -1;
    public static Double UNDEFINED_VALUE = Double.MIN_VALUE;
    public static Float UNDEFINED_FLOAT_VALUE = Float.MIN_VALUE;
    public static Integer UNDEFINED_INTEGER = Integer.MIN_VALUE;
    
    private List<DataPoint> data = new ArrayList<>();
    private final List<Run> runToCompare = new ArrayList<>();
    private final int runID;
    private double intervalFrom = UNDEFINED_VALUE;
    private double intervalTo  = UNDEFINED_VALUE;
    private double averageSpeed = UNDEFINED_VALUE;
    private double standardDeviation = UNDEFINED_VALUE;
    private double ssa = UNDEFINED_VALUE;
    private double sse = UNDEFINED_VALUE;
    private Byte isNullhypothesis = UNDEFIND_NULLHYPOTHESIS;
    private double F = UNDEFINED_VALUE;
    private double zVal = UNDEFINED_VALUE;
    private double qVal = UNDEFINED_VALUE;
    private double tVal = UNDEFINED_VALUE;
    private double cov = UNDEFINED_VALUE;
    private double OverlappingDifference = UNDEFINED_VALUE;
    public float rank = UNDEFINED_FLOAT_VALUE;

    
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
        this.data = new ArrayList<>();
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
    
    public double getOverlappingDifference(){
        return this.OverlappingDifference;
    } 
    
    public String getOverlappingDifferenceAsString(){
        if(this.OverlappingDifference == UNDEFINED_VALUE){
            return "";
        }
        return String.format(Settings.DIGIT_FORMAT, this.OverlappingDifference);
    } 
    
    public void setOverlappingDifference(double OverlappingDifference){
        this.OverlappingDifference = OverlappingDifference;
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
    
        public double getAverageSpeedOfRunsToCompareToTEST(){
        double speed = 0.0;
        for (Run run : this.runToCompare) {
            speed += run.getAverageSpeed();
        }
        speed = speed / this.runToCompare.size();
        return speed;
    }
    
    
    public String getRunToCompareToAsString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < runToCompare.size(); i++) {
            if(i < runToCompare.size() - 1){
                sb.append(String.format("Run %d, ", runToCompare.get(i).getID()));
            } else {
                sb.append(String.format("Run %d", runToCompare.get(i).getID()));            }
        }
        
        return sb.toString();
    }

    public String getPairwiseRunToCompareToAsString(){
        if(this.getID() % 2 == 0){
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Run %d, ", this.getID()));
        sb.append(String.format("Run %d", this.getID() + 1));
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
    
    public String getFAsString(){
        if(Double.isNaN(this.F)){
            return "";
        }
        return String.format(Settings.DIGIT_FORMAT, this.F);
    }
    
    public void setZ(double zVal){
        this.zVal = zVal;
    }

    public double getZ(){
        return this.zVal;
    }
    
    public String getZAsString(){
        if(this.zVal == UNDEFINED_VALUE){
            return "";
        }
        return String.format(Settings.DIGIT_FORMAT, this.zVal);
    }
    
    public void setT(double tVal){
        this.tVal = tVal;
    }

    public double getT(){
        return this.tVal;
    }

    public String getTAsString(){
        if(this.tVal == UNDEFINED_VALUE){
            return "";
        }
        return String.format(Settings.DIGIT_FORMAT, this.tVal);
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
    
    public void setNullhypothesis(byte isNullhypothesis){
        this.isNullhypothesis = isNullhypothesis;
    }
    
    public byte getNullhypothesis(){
        return this.isNullhypothesis;
    }

    public void setQ(double qVal) {
        this.qVal = qVal;
    }

    public double getQ(){
        return this.qVal;
    }
    
    public String getQAsString(){
        if(this.qVal == UNDEFINED_VALUE){
            return "";
        }
        return String.format(Locale.ENGLISH,Settings.DIGIT_FORMAT, this.qVal);
    }
    
        
    public double getCoV(){
        return this.cov;
    }
    
        
    public String getCoVAsString(){
        if(this.cov == Run.UNDEFINED_VALUE){
            return "";
        }
        return String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.cov * 100);
    }
    
        public void setCoV(double cov){
            this.cov = cov;
    }

}
