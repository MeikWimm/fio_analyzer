/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.geometry.Point2D;

/**
 *
 * @author meni1999
 */
public class Job {
    private File file;
    private List<Point2D> data;
    private Map<Integer, Integer> frequency;
    private int runs = 1;
    private int time;
    private double averageSpeed;
    private BasicFileAttributes attr;
    private double epsilon = 1;
    
    public Job(){
        data = new ArrayList<>();
        frequency = new TreeMap<>();
    }
    
    public List<Point2D> getData(){
        return this.data;
    }
    
    public void setData(List<Point2D> data){
        this.epsilon = data.size() / 1000;
        this.data = data;
    }
    
    public String getFile(){
        return this.file.getName();
    }
    
    public void setFile(File file){
        try {
            this.file = file;
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException ex) {
            System.out.println("com.mycompany.atool.Job.setFile()");
            System.err.println("Couldn't read File!");
        }
    }
    
    public String getFileCreationDate(){
        return attr.creationTime().toString();   
    }

    public String getFileLastModifiedDate(){
        return attr.lastModifiedTime().toString();   
    }
    
    public int getRuns(){
        return this.runs;
    }

    public void setRuns(int runs){
        this.runs = runs;
    }
    
    @Override
    public String toString(){
        
        return String.format("Job: %s | Average Speed %s | Runs: %d", file.toString(), new DecimalFormat("#.##").format(this.averageSpeed), this.runs);
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
        return this.averageSpeed;
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
}
