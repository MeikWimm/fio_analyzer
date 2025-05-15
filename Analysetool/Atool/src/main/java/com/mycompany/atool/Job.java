/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import com.mycompany.atool.Analysis.TTest;
import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Log files of fio are here represented as Jobs.
 * @author meni1999
 */
public class Job {
    public final static Integer MAX_RUN_COUNT = 1000;
    public final static Integer MIN_RUN_COUNT = 1;
    public final static Integer DEFAULT_RUN_COUNT = 1;
    
    public final static Double DEFAULT_ALPHA = 0.05;
    public final static Double MAX_ALPHA = 0.99999;
    public final static Double MIN_ALPHA = 0.00001;
    
    public final static Double DEFAULT_EPSILON = 1.0;
    public final static Double MAX_EPSILON = 1000.0;
    public final static Double MIN_EPSILON = 1.0;
    
    private File file;
    private List<DataPoint> rawData = new ArrayList<>();
    private List<DataPoint> convertedData = new ArrayList<>();
    private List<Run> runs;
    private Map<Integer, Integer> frequency;
    private int runsCounter = 1;
    private double conversion;
    private int time;
    private double averageSpeed;
    private BasicFileAttributes attr;
    private double epsilon = 1;
    private double alpha = 0.05;
    private static int COUNTER = 1; // so that each Job has a unique ID
    private final int ID = COUNTER;
    private double calculatedF;
    private double ssa;
    private double sse;
    public double F;
    private StringBuilder stringBuilder;
    //private double convertedAverageSpeed;

    public void setFileAttributes(BasicFileAttributes attr) {
        this.attr = attr;
    }
    
    public Job(){
        //data = new ArrayList<>();
        frequency = new TreeMap<>();
        stringBuilder = new StringBuilder();
        COUNTER++;
    }
    
    public List<DataPoint> getData(){

        return this.convertedData;
    }
    
    public List<DataPoint> getRawData(){
        return this.rawData;
    }
    
    public void setData(List<DataPoint> rawData){
        this.epsilon = rawData.size() / 1000;
        if(this.epsilon > MAX_EPSILON){
            this.epsilon = MAX_EPSILON;
        }
        this.rawData = rawData;
        update();
    }
    
    public File getFile(){
        return this.file;
    }
    
    
    public void setFile(File file){
            this.file = file;
    }
    
    public String getFileCreationDate(){
        return attr.creationTime().toString();   
    }

    public String getFileLastModifiedDate(){
        return attr.lastModifiedTime().toString();   
    }
    
    public int getRunsCounter(){
        return this.runsCounter;
    }

    public void setRunsCounter(int runsCounter){
        this.runsCounter = runsCounter;
    }
    
    public double getAlpha(){
        return this.alpha;
    }
    
    void setAlpha(Double alpha) {
        this.alpha = alpha;
    }
    
    public int getID(){
        return this.ID;
    }
    
    @Override
    public String toString(){ 
        return String.format("Job ID: %d | Average Speed %s | Runs: %d | Alpha: %f", this.ID, new DecimalFormat("#.##").format(this.averageSpeed), this.runsCounter, this.alpha);
    }

    void setTime(int time) {
        this.time = time;
    }
    
    public int getTimeInMilli(){
        return this.time;
    }
    public double getTimeInSec(){
        return (double) this.time / 1000;
    }
    
    public double getAverageSpeed(){
        return  Math.floor((this.averageSpeed / Settings.CONVERSION_VALUE) * Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }
    
    public void setFrequency(Map<Integer, Integer> freq){
        this.frequency = freq;
    }
    
    public double getConversionVal(){
        return this.conversion;
    }
    
    public Map<Integer, Integer> getFrequency(){
        return this.frequency;
    }

    public void setAverageSpeed(double average_speed) {
        this.averageSpeed = average_speed;
    }

    public double getEpsilon() {
        return this.epsilon;
    }

    public void setEpsilon(double epsilon){
        this.epsilon = epsilon;
    }

    public void update() {
        runs = new ArrayList<>();
        convertedData = new ArrayList<>();
        
        if(runsCounter <= 0 || runsCounter > 1000){
            runsCounter = DEFAULT_RUN_COUNT;
        }
        
        this.conversion = Settings.CONVERSION_VALUE;
        
        int run_size = (rawData.size() / runsCounter);
        int i = 0;
        ArrayList<DataPoint> run_data;
        for (int j = 1; j <= runsCounter; j++) {
                run_data = new ArrayList<>();
            for (; i < run_size*j; i++) {
                DataPoint dp = new DataPoint(this.rawData.get(i).getSpeed() / Settings.CONVERSION_VALUE, this.rawData.get(i).getTime());
                run_data.add(dp);
                convertedData.add(dp);
            }
                Run run = new Run(j, run_data);
                run.addRunToCompareTo(run); // add to a list of all runs to compare to for the Tests even itself
                runs.add(run);
        }

        double speed = 0;
        int j;
        boolean flag = false;
        int counter = 0;
        
        //Add runs to compare, i.e compare for ANOVA first run with the second run.
        for (j = 0; j < runs.size()-1; j++) {
            runs.get(j).addRunToCompareTo(runs.get(j+1));
        }
        
        if(runs.size() > 1){
            runs.get(runs.size()-1).addRunToCompareTo(runs.get(runs.size()-2));
        }
        
        
        if(Settings.AVERAGE_SPEED_PER_MILLISEC == 1) return;

        for (Run run : this.getRuns()) {
            List<DataPoint> runData = new ArrayList<>();
            for (j = 0; j < run.getData().size(); j++) {
                if(j % Settings.AVERAGE_SPEED_PER_MILLISEC == 0 && flag){
                    double average_speed = speed / Settings.AVERAGE_SPEED_PER_MILLISEC;
                    runData.add(new DataPoint(average_speed, j));
                    speed = 0;
                    counter = 0;
                } else {
                    flag = true;
                    speed += run.getData().get(j).getSpeed();
                    counter++;
                }
            }
            run.setData(runData);
        }
}

    public ObservableList<Run> getRuns() {
        return FXCollections.observableArrayList(this.runs);
    }
    
    public int getRunDataSize(){
        return this.getRuns().get(0).getData().size();
    }

    public double getSSE() {
        return sse;
    }

    public void setSSE(double sse) {
        this.sse = sse;
    }

    public double getSSA() {
        return ssa;
    }
    
    public String getCode(){
        stringBuilder.setLength(0);
        stringBuilder.append(Double.toString(this.alpha));
        stringBuilder.append(Integer.toString(this.runsCounter));
        stringBuilder.append(Double.toString(this.conversion));
        return stringBuilder.toString();
    }
    
    public void setSSA(double ssa) {
        this.ssa = ssa;
    }

    public double getSST() {
        return sse + ssa;
    }

    public double getF() {
        return (calculatedF * Settings.NUMBER_AFTER_COMMA) / Settings.NUMBER_AFTER_COMMA;
    }
    
    public void setF(double f) {
        this.calculatedF = f;
    }
}
