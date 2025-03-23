/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.util.List;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 *
 * @author meni1999
 */
class Tester {
    public void executeANOVA(Job job){
        System.out.println(job.toString());
    }
    
    public void drawJob(Job job) {
        if(job.getData().size() > 10000){
            Reporter.showInfoForGraph();
        }
        Stage stage = new Stage();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time in milliseconds");
        yAxis.setLabel("I/O-Speed in Kibibytes");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setHorizontalGridLinesVisible(false);
        lineChart.setVerticalGridLinesVisible(false);
        XYChart.Series series = new XYChart.Series();
        lineChart.setTitle("Job");
        List<Point2D> data = job.getData();
        List<Point2D> reduced_data = RamerDouglasPeucker.douglasPeucker(data, job.getEpsilon());

        //System.err.println(reduced_data.size() + "       | old size: " + data.size());
        
        for (Point2D p : reduced_data) {
            series.getData().add(new XYChart.Data<>(p.getX(), p.getY()));
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
       
        stage.setScene(scene);
        stage.show();
    }
        
    public void drawJobFreqeuncy(Job job){
        if(job.getData().size() > 10000){
            Reporter.showInfoForGraph();
        }
        Stage stage = new Stage();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("I/O-Speed in Kibibytes");
        yAxis.setLabel("Frequency");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        XYChart.Series series = new XYChart.Series();
        lineChart.setTitle("Job Frequency");
        Map<Integer, Integer> data = job.getFrequency();
        for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            series.getData().add(new XYChart.Data<>(key, value));
        }
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
       
        stage.setScene(scene);
        stage.show();
    }
    
}
