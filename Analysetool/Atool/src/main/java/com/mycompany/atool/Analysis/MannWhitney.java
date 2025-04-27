/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.DataPoint;
import com.mycompany.atool.Job;
import com.mycompany.atool.Utils;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;


/**
 *
 * @author meni1999
 */
public class MannWhitney implements Initializable{
    
    public MannWhitney(){
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }
    
    public static void calculateMannWhitneyTest(Job job){
        if(job.getRuns().size() <= 1) return;
        List<DataPoint> runData1 = new ArrayList<>(job.getRuns().get(0).getData());
        List<DataPoint> runData2 = new ArrayList<>(job.getRuns().get(1).getData());
        
        
        for (int i = 0; i < runData1.size(); i++) {
            runData1.get(i).setX(0);
            runData2.get(i).setX(1);
        }
        
        List<DataPoint> mergedData = new ArrayList<>(runData1);
        mergedData.addAll(runData2);
        
       
        
        Collections.sort(mergedData, new Utils.DataComparator());

        System.err.println("_________________________________________________");
        double r = 1;
        for (DataPoint p : mergedData) {
            p.rank = r;
            System.err.println("Speed: " + p.getY() + " | Time: " + p.getX() + " | Rank: " + p.rank);
            r++;
        }
        System.err.println("_________________________________________________");
    } 
}
