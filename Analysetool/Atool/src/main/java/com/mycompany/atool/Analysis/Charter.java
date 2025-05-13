/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.DataPoint;
import com.mycompany.atool.Job;
import com.mycompany.atool.RamerDouglasPeucker;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author meni1999
 */
public class Charter {
    private boolean isJobSpeedStageInitialized;
    private boolean isJobFreqStageInitialized;
    private Stage stageJobSpeed;
    private Stage stageJobFreq;

    public void executeANOVA(Job job){
        System.out.println(job.toString());
    }
    
    public void drawJob(Job job) {
        
        if(!infoWindowLargeData(job)) return;
        
        if(!isJobSpeedStageInitialized){
            stageJobSpeed = new Stage();
            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Time in milliseconds");
            yAxis.setLabel("I/O-Speed in Kibibytes");
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setHorizontalGridLinesVisible(false);
            lineChart.setVerticalGridLinesVisible(false);
            XYChart.Series series = new XYChart.Series();
            lineChart.setTitle("Job");
            List<DataPoint> data = job.getData();
            List<DataPoint> reduced_data = RamerDouglasPeucker.douglasPeucker(data, job.getEpsilon());

            for (DataPoint p : reduced_data) {
                series.getData().add(new XYChart.Data<>(p.getTime(), p.getSpeed()));
            }
            Scene scene  = new Scene(lineChart,800,600);
            lineChart.getData().add(series);

            stageJobSpeed.setScene(scene);
            isJobSpeedStageInitialized = true;
        }

        stageJobSpeed.show();
    }
       
    /**
     * TODO: RDP currently not working for draw job frequency
     * @param job 
     */
    public void drawJobFreqeuncy(Job job){


        if(!infoWindowLargeData(job)) return;
        
        if(!isJobFreqStageInitialized){
            stageJobFreq = new Stage();
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

            stageJobFreq.setScene(scene);
            isJobFreqStageInitialized = true;
        }

        stageJobFreq.show();
    }
    
    /**
     * Gets called when data point exceed 10000 to inform User.
     */
    private boolean infoWindowLargeData(Job job){
        boolean flag = false;
        if(job.getData().size() > 10000 && !isJobFreqStageInitialized){
            ButtonType goodButton = new ButtonType("Ok");
            ButtonType badButton = new ButtonType("Cancel");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "This could take a while because of more then 10000 data points from this job.", goodButton, badButton);
            Window window = alert.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(e -> alert.hide());
            Optional<ButtonType> result = alert.showAndWait();
            
            if(!result.isEmpty()){
                flag = (result.get() == goodButton);
            }

        }
        return flag;
    }
    
}
