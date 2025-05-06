/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author meni1999
 */
public class TTest {
    public static void tTtest(Job job){
        new Anova(job).calculateANOVA();
        for (Run r : job.getRuns()) {
            if(r.getRunToCompareTo().size() <= 1) return;
            Run run1 = r.getRunToCompareTo().get(0);
            Run run2 = r.getRunToCompareTo().get(1);
            
            TDistribution t = new TDistribution(run1.getData().size() + run2.getData().size() - 2);
            
            double runVariance1 = calculateVariance(run1);
            double runVariance2 = calculateVariance(run2);
            
            
            double runSize1 = run1.getData().size();
            double runSize2 = run2.getData().size();
            
            double nominator = (run1.getAverageSpeed() - run2.getAverageSpeed());
            double denominator = Math.sqrt((runVariance1 / runSize1) + (runVariance2 / runSize2));
            double tVal = nominator / denominator;
            
            System.err.println("T value: " + Math.abs(tVal));
            System.err.println("T crit: " + t.inverseCumulativeProbability(1.0 - (1.0 - job.getAlpha()) / 2.0));
            //System.err.println(nominator);
            //System.err.println(denominator);
            
        }
    }
    
    private static double calculateVariance(Run run){
        /**
        double sum = 0;

        for (DataPoint dp : run.getMinimizedData(Run.SPEED_PER_SEC)) {
            sum += Math.pow(dp.getSpeed() - Run.calculateAverageSpeedOfData(run.getMinimizedData(Run.SPEED_PER_SEC)), 2);
        }
        double standardVariance = (1.0 / (run.getData().size() - 1.0)) * sum;
        System.err.println("Standard without SSE: " + standardVariance);

        */
        //System.err.println("Standard SSE: " + standardVariance);
        return (1.0 / (run.getData().size() - 1.0)) * run.getSSE();
    }
}
