/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
public class InputModule {
    FileChooser chooser = new FileChooser();
    File file;
    ArrayList<Integer> data = new ArrayList<>();
    
    public InputModule(){
        chooser = new FileChooser();
        chooser.setTitle("Open File");
    }

    public void loadFile(){
        file = chooser.showOpenDialog(new Stage()); 
        System.out.println("com.mycompany.atool.InputModule.loadFile()");
        System.out.println(file.toString());
        readFile();
    }

    private void readFile() {

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(", ");
                data.add(Integer.valueOf(s[1]));
            }
        } catch (IOException ex) {
            ex.toString();
        }
        System.out.println((double) sum(data)/data.size());
    }
    private long sum (ArrayList<Integer> list){
        long n = 0;
        for (Integer integer : list) {
            n += integer;
        }
        return n;
    }
}
