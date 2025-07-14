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
public class Section /*Section*/ {
    public static Double UNDEFINED_DOUBLE_VALUE = Double.MIN_VALUE;
    public static Integer UNDEFINED_INTEGER = Integer.MIN_VALUE;
    private List<DataPoint> data = new ArrayList<>();
    private final int sectionID;
    private double intervalFrom = UNDEFINED_DOUBLE_VALUE;
    private double intervalTo  = UNDEFINED_DOUBLE_VALUE;
    private double averageSpeed = UNDEFINED_DOUBLE_VALUE;
    private double standardDeviation = UNDEFINED_DOUBLE_VALUE;
    private double ssa = UNDEFINED_DOUBLE_VALUE;
    private double sse = UNDEFINED_DOUBLE_VALUE;
    private boolean isNullhypothesis = false;
    private boolean isOverlapping = false;
    private double F = UNDEFINED_DOUBLE_VALUE;
    private double zVal = UNDEFINED_DOUBLE_VALUE;
    private double qVal = UNDEFINED_DOUBLE_VALUE;
    private double tVal = UNDEFINED_DOUBLE_VALUE;
    private double cov = UNDEFINED_DOUBLE_VALUE;
    private double qHSD = UNDEFINED_DOUBLE_VALUE;
    private double startTime = UNDEFINED_DOUBLE_VALUE;
    private double endTime = UNDEFINED_DOUBLE_VALUE;
    private double duration = UNDEFINED_DOUBLE_VALUE;
    private double p = UNDEFINED_DOUBLE_VALUE;
    private double mse = UNDEFINED_DOUBLE_VALUE;
    private int groupID = UNDEFINED_INTEGER;
    private double overallMean = UNDEFINED_DOUBLE_VALUE;
    private String group = "UNDEFINED";


    public Section(final int runNumber, List<DataPoint> runData){
        this.sectionID = runNumber;
        this.data = runData;
        calculateRun();
    }

    // Copy constructor
    public Section(Section other) {
        this.sectionID = other.getID();
        this.data = other.getData();
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
        this.qHSD = other.getQHSD();
        this.p = other.getP();
        this.group = other.group;
        this.mse = other.getMSE();
        this.overallMean = other.getOverallMean();
        this.isOverlapping = other.getOverlap();
    }

    private void calculateRun() {
        double ioSpeed = 0;
        for (DataPoint p : data) {
            ioSpeed += p.data;
        }
        this.averageSpeed = ioSpeed / data.size();
        
        double nominator = 0;
        for (DataPoint p : data) {
            nominator += Math.pow(p.data - averageSpeed, 2);
        }
        
        this.standardDeviation = (Math.sqrt((nominator / data.size())));


        this.startTime = this.data.getFirst().time;
        this.endTime = this.data.getLast().time;
        this.duration = this.endTime - this.startTime;
    }

    public List<DataPoint> getData(){      
        return data;
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
    
    public boolean getOverlap(){
        return this.isOverlapping;
    }
    
    public void setOverlap(boolean isOverlapping){
        this.isOverlapping = isOverlapping;
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
        return sectionID;
    }
    
    public void setNullhypothesis(boolean isNullhypothesis){
        this.isNullhypothesis = isNullhypothesis;
    }
    
    public boolean getNullhypothesis(){
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

    public double getQHSD() {
        return qHSD;
    }

    public void setMSE(double mse) {
        this.mse = mse;
    }

    public double getMSE() {
        return mse;
    }

    public void setOverallMean(double overallMean) {
        this.overallMean = overallMean;
    }

    public double getOverallMean() {
        return overallMean;
    }
}
