/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author meni1999
 */
public class Job {
    File file;
    List<Integer> data;
    int runs = 1;
    
    public Job(){
        data = new ArrayList<>();
    }
    
    public List<Integer> getData(){
        return this.data;
    }
    
    public void setData(List<Integer> data){
        this.data = data;
    }
    
    public String getFile(){
        return this.file.getName();
    }
    
    public void setFile(File file){
        this.file = file;
    }
    
    public int getRuns(){
        return this.runs;
    }
    
    public int getRunCount(){
        return runs;
    }
    
    public void setRuns(int runs){
        this.runs = runs;
    }
    
    
}
