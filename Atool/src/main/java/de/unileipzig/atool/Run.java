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
    public static Double UNDEFINED_DOUBLE_VALUE = Double.MIN_VALUE;
    public static Integer UNDEFINED_INTEGER = Integer.MIN_VALUE;

    private List<DataPoint> data = new ArrayList<>();
    private final List<Section> sections = new ArrayList<>();
    private final int runID;
    private double averageSpeed = UNDEFINED_DOUBLE_VALUE;
    private double standardDeviation = UNDEFINED_DOUBLE_VALUE;
    private double ssa = UNDEFINED_DOUBLE_VALUE;
    private double sse = UNDEFINED_DOUBLE_VALUE;
    private boolean isNullhypothesis = false;
    private double qVal = UNDEFINED_DOUBLE_VALUE;
    private double cov = UNDEFINED_DOUBLE_VALUE;
    private double startTime = UNDEFINED_DOUBLE_VALUE;
    private double endTime = UNDEFINED_DOUBLE_VALUE;
    private double duration = UNDEFINED_DOUBLE_VALUE;
    private double mse = UNDEFINED_DOUBLE_VALUE;

    private String group = "UNDEFINED";
    private List<List<Section>> groups = new ArrayList<>();
    private List<Section> resultSections;


    public Run(final int runNumber, List<DataPoint> runData){
        this.runID = runNumber;
        this.data = runData;
        calculateRun(this.data);
//        //private final int sectionCount = 30;
//        int sectionCount = (int) (this.data.size() / Settings.WINDOW_SIZE);
//        if(sectionCount == 0){
//            sectionCount = 2;
//        }
        prepareSectionsSliding(this.data);
    }

    // Copy constructor
    public Run(Run other) {
        this.runID = other.getRunID();
        this.data = new ArrayList<>();
        for (DataPoint dataPoint: other.getData()){
            this.data.add(new DataPoint(dataPoint));
        }
        prepareSectionsSliding(this.data);
        this.startTime = other.getStartTime();
//        this.endTime = other.getEndTime();
//        this.duration = other.getDuration();
//        this.groupID = other.getGroupID();
//        this.intervalFrom = other.getIntervalFrom();
//        this.intervalTo = other.getIntervalTo();
//        this.ssa = other.getSSA();
//        this.sse = other.getSSE();
//        this.F = other.getF();
//        this.zVal = other.getZ();
        this.qVal = other.getQ();
//        this.tVal = other.getT();
//        this.cov = other.getCoV();
//        this.qHSD = other.getQHSD();
//        this.p = other.getP();
//        this.isOverlapping = other.getOverlap();
        this.group = other.group;
        this.isNullhypothesis = other.getNullhypothesis();
        this.standardDeviation = other.getStandardDeviation();
        this.averageSpeed = other.getAverageSpeed();
        this.groups = other.groups;
    }

    public static List<List<Section>> setupGroups(Run run, boolean doSequential, int groupSize) {
        if (groupSize < 2) {
            return new ArrayList<>();
        }

        List<List<Section>> groups = new ArrayList<>();
        List<Section> sections = run.getSections();
        int sectionSize = sections.size();

        if (doSequential) {
            // create Groups like Run 1 - Run 2, Run 3 - Run 4, Run 5 - Run 6, ...
            int groupCount = sectionSize / groupSize;
            int runIndex = 0;

            for (int i = 0; i < groupCount; i++) {
                List<Section> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    group.add(sections.get(runIndex));
                    runIndex++;
                }

                Section first = group.getFirst();
                Section last = group.getLast();
                group.getFirst().setGroup("Section " + first.getID() + " - " + "Section " + last.getID());
                groups.add(group);
            }
        } else { // else adjacent
            // create Groups like Run 1 - Run 2, Run 2 - Run 3, Run 3 - Run 4, ...
            int groupCount = (int) Math.ceil(sectionSize / 2.0);
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
                    //.getFirst().setGroup("Section " + group.getFirst().getID() + " - " + "Section " + group.getLast().getID());
                }
                Section first = group.getFirst();
                Section last = group.getLast();
                group.getFirst().setGroup("Section " + first.getID() + " - " + "Section " + last.getID());

                runIndex++;
            }
        }
        return groups;
    }

    public double getAcceptedSectionsRate(){
        double acceptedSectionsRate = 0;
        for(List<Section> sections: groups){
            if(sections.getFirst().getNullhypothesis()){
                acceptedSectionsRate++;
            }
        }
        return acceptedSectionsRate / sections.size();
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

    private void prepareSectionsSliding(List<DataPoint> data) {
        int count = 1;
        int WINDOW_SIZE = Settings.WINDOW_SIZE;
        int WINDOW_STEP_SIZE = Settings.WINDOW_STEP_SIZE;

        for (int i = 0; i <= data.size() - WINDOW_SIZE; i += WINDOW_STEP_SIZE) {
            double timeSection = (WINDOW_STEP_SIZE / 1000.0);
            Section section = new Section(data.subList(i, i + WINDOW_SIZE), count * timeSection, count);
            sections.add(section);
            count++;
        }
    }

//    private void prepareSections(List<DataPoint> data){
//        int sectionSize = data.size() / sectionCount;
//        int count = 1;
//        for(int i = 0; i < data.size(); i++){
//            for(int j = 0; j < sectionCount; j++){
//                if(i == sectionSize * j){
//                    Section section = new Section(data.subList(i, i + sectionSize), count);
//                    sections.add(section);
//                    count++;
//                    break;
//                }
//            }
//        }
//    }

    public int getDataGroupSize(){
        return this.getSections().getFirst().getData().size() * 2; // TODO ANOVA
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

    public double getStartTime() {
        return startTime;
    }

    public void setSSE(double sse) {
        this.sse = sse;
    }

    public double getSSE() {
        return sse;
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

    public List<Section> getSections() {
        return sections;
    }

    public List<List<Section>> getGroups(){
        return this.groups;
    }

    public void setGroups(List<List<Section>> groups) {
        this.groups = groups;
    }

    public void setMSE(double mse) {
        this.mse = mse;
    }

    public double getMSE() {
        return mse;
    }

    public void setResultSections(List<Section> resultSections) {
        this.resultSections = resultSections;
    }

    public List<Section> getResultSections() {
        return resultSections;
    }
}
