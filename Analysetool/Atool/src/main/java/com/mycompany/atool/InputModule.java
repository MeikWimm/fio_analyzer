/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
public class InputModule {
    FileChooser chooser = new FileChooser();
    File file;
    Job job;

    
    
    public InputModule(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            
        }
        
        File[] files;
        files = selectedDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".log"));
        for (File file1 : files) {
            System.out.println(file1);
        }
        
        chooser = new FileChooser();
        chooser.setTitle("Open File");
        chooser.getExtensionFilters().addAll(
        new ExtensionFilter("Log Files", "*.log"),
        new ExtensionFilter("All Files", "*.*"));
    }

    public void loadFile(){
        file = chooser.showOpenDialog(new Stage()); 
        readFile();
    }

    private void readFile() {
        job = new Job();
        job.setFile(file);
        List<Integer> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(", ");
                data.add(Integer.valueOf(s[1]));
            }
            
            job.setData(data);
        } catch (IOException ex) {
            ex.toString();
        }
    }

    private long sum (ArrayList<Integer> list){
        long n = 0;
        for (Integer integer : list) {
            n += integer;
        }
        return n;
    }
    
    public Job getJob(){
        return job;
    }
    
    public void setFile(File file){
        this.file = file;
    }
}
