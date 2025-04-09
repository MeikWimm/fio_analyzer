/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

/**
 *
 * @author meni1999
 */
public class Job {
    public final static Integer MAX_RUN_COUNT = 1000;
    public final static Integer MIN_RUN_COUNT = 1;
    public final static Integer DEFAULT_RUN_COUNT = 1;
    
    public final static Double DEFAULT_ALPHA = 0.95;
    public final static Double MAX_ALPHA = 0.999;
    public final static Double MIN_ALPHA = 0.001;
    
    public final static Double DEFAULT_EPSILON = 1.0;
    public final static Double MAX_EPSILON = 1000.0;
    public final static Double MIN_EPSILON = 1.0;
    
    private File file;
    private List<Point2D> data;
    private List<Run> runs;
    private Map<Integer, Integer> frequency;
    private int runsCounter = 1;
    private int time;
    private double averageSpeed;
    private BasicFileAttributes attr;
    private double epsilon = 1;
    private double alpha = 0.95;
    private static int d = 1;
    private final int ID = d;

    public void setFileAttributes(BasicFileAttributes attr) {
        this.attr = attr;
    }
    
    private enum STATUS {
        SUCCESS,
        COULDNT_READ_FILE_ATTRIBUTE
    }
    
    public Job(){
        data = new ArrayList<>();
        frequency = new TreeMap<>();
        d++;
    }
    
    public List<Point2D> getData(){
        return this.data;
    }
    
    public void setData(List<Point2D> data){
        this.epsilon = data.size() / 1000;
        if(this.epsilon > MAX_EPSILON){
            this.epsilon = MAX_EPSILON;
        }
        this.data = data;
        setRuns();
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
        return  Math.floor(this.averageSpeed * 100) / 100;
    }
    
    public void setFrequency(Map<Integer, Integer> freq){
        this.frequency = freq;
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

    public void setRuns() {
        runs = new ArrayList<>();
        
        if(runsCounter <= 0){
            runsCounter = 1;
            System.out.println("com.mycompany.atool.Job.setRuns() Runs Counter below 1!");
        }
        
        int run_size = (int) (data.size() / runsCounter);
        int i = 0;
        for (int j = 1; j <= runsCounter; j++) {
                ArrayList<Point2D> run_data = new ArrayList<>();
            for (; i < run_size*j; i++) {
                run_data.add(this.data.get(i));
            }
                Run run = new Run(j, run_data);
                runs.add(run);
        }
        System.out.println(runs);
    }

    public ObservableList<Run> getRuns() {
        return FXCollections.observableArrayList(this.runs);
    }
}
