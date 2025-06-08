/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author meni1999
 */
public class Run /*Section*/ {
    public static final byte ACCEPTED_NULLHYPOTHESIS = 1;
    public static final byte REJECTED_NULLHYPOTHESIS = 0;
    public static final byte UNDEFIND_NULLHYPOTHESIS = -1;

    public static Double UNDEFINED_DOUBLE_VALUE = Double.MIN_VALUE;
    public static Float UNDEFINED_FLOAT_VALUE = Float.MIN_VALUE;
    public static Integer UNDEFINED_INTEGER = Integer.MIN_VALUE;
    public static final String UNDEFINED = "UNDEFINED";
    private List<DataPoint> data = new ArrayList<>();
    private final int runID;
    private double intervalFrom = UNDEFINED_DOUBLE_VALUE;
    private double intervalTo  = UNDEFINED_DOUBLE_VALUE;
    private double averageSpeed = UNDEFINED_DOUBLE_VALUE;
    private double standardDeviation = UNDEFINED_DOUBLE_VALUE;
    private double ssa = UNDEFINED_DOUBLE_VALUE;
    private double sse = UNDEFINED_DOUBLE_VALUE;
    private Byte isNullhypothesis = UNDEFIND_NULLHYPOTHESIS;
    private double F = UNDEFINED_DOUBLE_VALUE;
    private double zVal = UNDEFINED_DOUBLE_VALUE;
    private double qVal = UNDEFINED_DOUBLE_VALUE;
    private double tVal = UNDEFINED_DOUBLE_VALUE;
    private double cov = UNDEFINED_DOUBLE_VALUE;
    private double rciw = UNDEFINED_DOUBLE_VALUE;
    private double startTime = UNDEFINED_DOUBLE_VALUE;
    private double endTime = UNDEFINED_DOUBLE_VALUE;
    private double duration = UNDEFINED_DOUBLE_VALUE;
    private int groupID = UNDEFINED_INTEGER;
    private String group = "";
    private double p;


    public Run(final int runNumber, List<DataPoint> runData){
        this.runID = runNumber;
        this.data = runData;
        calculateRun();
    }

    // Copy constructor
    public Run(Run other) {
        this.runID = other.getRunID();
        this.data = new ArrayList<>();
        for (DataPoint dataPoint: other.getData()){
            this.data.add(new DataPoint(dataPoint));
        }
        this.startTime = other.getStartTime();
        this.endTime = other.getEndTime();
        this.duration = other.getDuration();
        this.groupID = other.getGroupID();
        this.intervalFrom = other.getIntervalFrom();
        this.intervalTo = other.getIntervalTo();
        this.averageSpeed = other.getAverageSpeed();
        this.standardDeviation = other.getStandardDeviation();
        this.ssa = other.getSSA();
        this.sse = other.getSSE();
        this.isNullhypothesis = other.getNullhypothesis();
        this.F = other.getF();
        this.zVal = other.getZ();
        this.qVal = other.getQ();
        this.tVal = other.getT();
        this.cov = other.getCoV();
        this.p = other.getP();
        this.group = other.group;
        this.rciw = other.getRCIW();
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
        
        this.standardDeviation = (Math.sqrt((nominator / data.size())));


        this.startTime = this.data.getFirst().time;
        this.endTime = this.data.getLast().time;
        this.duration = this.endTime - this.startTime;
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
    
    public double getRCIW(){
        return this.rciw;
    }
    
    public void setRCIW(double rciw){
        this.rciw = rciw;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public double getDuration() {
        return duration;
    }

    public void setIntervalFrom(double d) {
        this.intervalFrom = d;
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
    
    public void setT(double tVal){
        this.tVal = tVal;
    }

    public double getT(){
        return this.tVal;
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

    public double getCoV(){
        return this.cov;
    }
    
    public void setCoV(double cov){
            this.cov = cov;
    }

    public void reset() {
        this.isNullhypothesis = UNDEFIND_NULLHYPOTHESIS;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public double getP() {
        return this.p;
    }

    public void setP(double p) {
        this.p = p;
    }
}
