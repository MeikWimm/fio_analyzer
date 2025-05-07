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
    private final List<DataPoint> rawData;
    private final List<DataPoint> data = new ArrayList<>();
    private final List<Run> runToCompare = new ArrayList<>();
    private final int runID;
    private double intervalFrom = 0;
    private double intervalTo = 0;
    private double averageSpeed;
    private double standardDeviation = 0;
    private int averageSpeedPerMillisec = 10;
    public static final int SPEED_PER_SEC = 10;
    private double ssa;
    private double sse;
    private boolean isNullhypothesisAccepted = false;
    private double F;
    private double zVal = 0;
    private double qVal = 0;
    private boolean calcualteDataFlag = true;
    public float rank = 0;
    private InputModule.CONVERT convert = InputModule.CONVERT.DEFAULT;
    private final int DEFAULT_SPEED_PER_MILLI = 1;
    private final int MAX_SPEED_PER_MIILI = 2000;
    private final int MIN_SPEED_PER_MIILI = 1;
    
    
    

    
    public Run(final int runNumber, ArrayList<DataPoint> runData){
        this.runID = runNumber;
        this.rawData = runData;
        calculateRun();
    }

    private void calculateRun() {
        double ioSpeed = 0;
        for (DataPoint p : rawData) {
            ioSpeed += p.getSpeed();
        }
        this.averageSpeed = ioSpeed / rawData.size();
        
        double nominator = 0;
        for (DataPoint p : rawData) {
            nominator += Math.pow(p.getSpeed() - averageSpeed, 2);
        }
        
        this.standardDeviation = Math.floor(Math.sqrt((nominator / rawData.size()))* Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }
    
    public void setConversion(InputModule.CONVERT convert){
        this.convert = convert;
    }
    
    /**
     * Set the average speed per millisecond.
     * If averageSpeedPerMillisec is over 2000 or under 1, set to default: 1
     * @param averageSpeedPerMillisec 
     */
    public void setSpeedPerMillisec(int averageSpeedPerMillisec){
        if(averageSpeedPerMillisec < MIN_SPEED_PER_MIILI || averageSpeedPerMillisec > MAX_SPEED_PER_MIILI){
            averageSpeedPerMillisec = DEFAULT_SPEED_PER_MILLI;
        }
        calcualteDataFlag = true; // everytime new average speed per millic sec is set, getData() re-calculates the data from raw data.
        this.averageSpeedPerMillisec = averageSpeedPerMillisec;
    }
    
    /**
     * Returns the data of this Run.
     * If averageSpeedPerMillisec is set to 1, return the raw data.
     * Raw Data means for every millisecond there is one datapoint.
     * So, if averageSpeedPerMillisec is set to 1000 get the average of 1000 datapoint.
     * Therefore 1 datapoint for every 1000 millisecond (or every second).
     * @return 
     */
    public List<DataPoint> getData(){
        if(this.averageSpeedPerMillisec == 1){
            return rawData;
        }
        
        if(calcualteDataFlag){
            double speed = 0;
            int j;
            boolean flag = false;
            int counter = 0;
            for (j = 0; j < rawData.size(); j++) {
                if(j % averageSpeedPerMillisec == 0 && flag){
                    double average_speed = speed / averageSpeedPerMillisec;
                    data.add(new DataPoint(average_speed, j));
                    speed = 0;
                    counter = 0;
                } else {
                    flag = true;
                    speed += rawData.get(j).getSpeed();
                    counter++;
                }
            }
            if (j != 0){
                //converted_data.add(new Point2D(j, speed / counter));
                //System.out.println("Last data for converted list ignored.");
            }

            speed = 0;
            double convertVal = InputModule.CONVERT.getConvertValue(this.convert);
            for (DataPoint point2D : data) {
                speed += point2D.getSpeed() / convertVal;
            }
            calcualteDataFlag = false;
        }

                
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

    public List<DataPoint> getRawData(){
        return this.rawData;
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
