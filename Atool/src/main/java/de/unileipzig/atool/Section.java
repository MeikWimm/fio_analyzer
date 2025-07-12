package de.unileipzig.atool;

import java.util.List;


public class Section{
    private final List<DataPoint> data;
    private final int ID;
    private double averageSpeed;
    private double standardDeviation;
    private double startTime;
    private double endTime;
    private double duration;
    private boolean isNullhypothesis = false;
    private boolean isOverlapping = false;
    private double F = Run.UNDEFINED_DOUBLE_VALUE;
    private double zVal = Run.UNDEFINED_DOUBLE_VALUE;
    private double ssa = Run.UNDEFINED_DOUBLE_VALUE;
    private double sse = Run.UNDEFINED_DOUBLE_VALUE;
    private double qVal = Run.UNDEFINED_DOUBLE_VALUE;
    private double P = Run.UNDEFINED_DOUBLE_VALUE;
    private double tVal = Run.UNDEFINED_DOUBLE_VALUE;
    private double cov = Run.UNDEFINED_DOUBLE_VALUE;
    private double qHSD = Run.UNDEFINED_DOUBLE_VALUE;
    private double p = Run.UNDEFINED_DOUBLE_VALUE;
    private int groupID = Run.UNDEFINED_INTEGER;
    private String group = "UNDEFINED";
    private List<Section> sections;

    public Section(List<DataPoint> data, int ID) {
        this.data = data;
        this.ID = ID;
        calculateSection();
    }

    public Section(Section other){
        this.data = other.data;
        this.ID = other.ID;
        this.averageSpeed = other.averageSpeed;
        this.standardDeviation = other.standardDeviation;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.duration = other.duration;
        this.isNullhypothesis = other.isNullhypothesis;
        this.isOverlapping = other.isOverlapping;
        this.F = other.F;
        this.zVal = other.zVal;
        this.tVal = other.tVal;
        this.cov = other.cov;
        this.qHSD = other.qHSD;
        this.p = other.p;
        this.groupID = other.groupID;
        this.group = other.group;
        this.sections = other.sections;
        this.sse = other.sse;
        this.ssa = other.ssa;
        this.qVal = other.qVal;
        this.P = other.P;
        this.F = other.F;
        this.tVal = other.tVal;
        this.cov = other.cov;
        this.qHSD = other.qHSD;
        this.p = other.p;
        this.groupID = other.groupID;
        this.group = other.group;
    }

    private void calculateSection(){
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

    public List<DataPoint> getData() {
        return data;
    }

    public int getID() {
        return ID;
    }

    public double getStandardDeviation(){
        return this.standardDeviation;
    }

    public double getAverageSpeed(){
        return  this.averageSpeed;
    }

    public double getIntervalFrom(){
        return  this.data.getFirst().time;
    }

    public double getIntervalTo(){
        return  this.data.getLast().time;
    }

    public void setIntervalFrom(double d) {
        this.startTime = d;
    }

    public void setIntervalTo(double d) {
        this.endTime = d;
    }

    public double getPlusMinusValue(){
        return Math.abs(this.endTime - this.startTime);
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

    @Override
    public String toString() {
        return String.format("Section %d: CV: %f, F: %f, Z: %f, T: %f", ID, cov, F, zVal, tVal);
    }

    public double getCoV() {
        return cov;
    }

    public void setCoV(double cov) {
        this.cov = cov;
    }

    public void setNullhypothesis(boolean b) {
        this.isNullhypothesis = b;
    }

    public boolean getNullhypothesis(){
        return this.isNullhypothesis;
    }

    public void setSSE(double sse){
        this.sse = sse;
    }

    public double getSSE(){
        return this.sse;
    }

    public double getSSA(){
        return this.ssa;
    }

    public void setSSA(double ssa){
        this.ssa = ssa;
    }

    public double getSST(){
        return sse + ssa;
    }

    public double getQ(){
        return this.qVal;
    }

    public void setQ(double qVal) {
        this.qVal = qVal;
    }

    public double getP(){
        return this.p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public void setGroupID(int id) {
        this.groupID = id;
    }
}
