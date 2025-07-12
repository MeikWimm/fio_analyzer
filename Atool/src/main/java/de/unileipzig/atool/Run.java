/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool;

import de.unileipzig.atool.Analysis.GenericTest;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author meni1999
 */
public class Run /*Section*/ {
    public static Double UNDEFINED_DOUBLE_VALUE = Double.MIN_VALUE;
    public static Integer UNDEFINED_INTEGER = Integer.MIN_VALUE;
    private List<DataPoint> data = new ArrayList<>();
    private final List<Section> sections = new ArrayList<>();
    private final int runID;
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
    private int groupID = UNDEFINED_INTEGER;
    private String group = "UNDEFINED";
    private final int sectionCount = 10;
    private List<List<Section>> groups = new ArrayList<>();


    public Run(final int runNumber, List<DataPoint> runData){
        this.runID = runNumber;
        this.data = runData;
        calculateRun(this.data);
        prepareSections(this.data);
    }

    // Copy constructor
    public Run(Run other) {
        this.runID = other.getRunID();
        this.data = new ArrayList<>();
        for (DataPoint dataPoint: other.getData()){
            this.data.add(new DataPoint(dataPoint));
        }
        prepareSections(this.data);
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
        this.isOverlapping = other.getOverlap();
        this.groups = other.groups;
    }

    public static List<List<Section>> setupGroups(Run run, boolean skipGroups, int groupSize) {
        if (groupSize < 2) {
            return new ArrayList<>();
        }

        List<List<Section>> groups = new ArrayList<>();
        List<Section> sections = run.getSections();
        int runsCounter = sections.size();

        if (skipGroups) {
            // create Groups like Run 1 - Run 2, Run 3 - Run 4, Run 5 - Run 6, ...
            int groupCount = runsCounter / groupSize;
            int runIndex = 0;

            for (int i = 0; i < groupCount; i++) {
                List<Section> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    group.add(sections.get(runIndex));
                    runIndex++;
                }

                groups.add(group);
            }
        } else {
            // create Groups like Run 1 - Run 2, Run 2 - Run 3, Run 3 - Run 4, ...
            int groupCount = runsCounter / 2;
            groupCount = groupCount + (groupCount - 1);
            int runIndex = 0;

            for (int i = 0; i < groupCount; i++) {
                List<Section> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    if (runIndex + j < sections.size()) {
                        group.add(sections.get(runIndex + j));
                    }
                }
                if (group.size() == groupSize) {
                    groups.add(group);
                }
                runIndex++;
            }
        }
        return groups;
    }

    private void calculateRun(List<DataPoint> data) {
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


        this.startTime = data.getFirst().time;
        this.endTime = data.getLast().time;
        this.duration = this.endTime - this.startTime;
    }

    private void prepareSections(List<DataPoint> data){
        int sectionCount = 10;
        int sectionSize = data.size() / sectionCount;
        for(int i = 0; i < data.size(); i++){
            for(int j = 0; j < sectionCount; j++){
                if(i == sectionSize * j){
                    Section section = new Section(data.subList(i, i + sectionSize), j);
                    sections.add(section);
                    break;
                }
            }
        }
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
        return runID;
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

    public List<Section> getSections() {
        return sections;
    }

    public List<List<Section>> getGroups(){
        return this.groups;
    }

    public void setGroups(List<List<Section>> groups) {
        this.groups = groups;
    }
}
