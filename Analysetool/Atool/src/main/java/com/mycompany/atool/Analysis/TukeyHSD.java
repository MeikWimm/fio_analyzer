/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool.Analysis;

import com.mycompany.atool.DataPoint;
import com.mycompany.atool.Job;
import com.mycompany.atool.Run;
import com.mycompany.atool.Settings;
import com.mycompany.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import net.sourceforge.jdistlib.Tukey;


/**
 *
 * @author meni1999
 */
public class TukeyHSD implements Initializable{
    private static final Logger LOGGER = Logger.getLogger( TukeyHSD.class.getName() );
    
    private class TukeyDataPoint{
        private double mean;
        private double qHSD;
        
        public TukeyDataPoint(double mean, double qHSD){
            this.mean = mean;
            this.qHSD = qHSD;
        }
        
        public double getQHSD(){
            return this.qHSD;
        }
        
        public double getMean(){
            return this.mean;
        }
    }
    
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new Utils.CustomFormatter("TukeyHSD"));
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);      
    }    

    @FXML public Label qCritLabel;

    @FXML public Button drawTukey;
    
    @FXML public TableView<Run> TukeyTable;
    @FXML public TableColumn<Run,Double> averageSpeedColumn;
    @FXML public TableColumn<Run, Integer> runIDColumn;
    @FXML public TableColumn<Run, Integer> compareToRunColumn;
    @FXML public TableColumn<Run, String> QColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    
    private final Job job;
    private Tukey tukey;
    private double qHSD;
    private Map<Integer, TukeyDataPoint> tukeyData;
    
    public TukeyHSD(Job job){
        this.job = job;
        this.job.clearRuns();
        this.tukeyData = new HashMap<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("PairwiseRunToCompareToAsString"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("QAsString"));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> drawTukeyGraph(this.job));
        
        TukeyTable.setItems(this.job.getRuns());   
        setLabeling();
    }
    
    public void drawTukeyGraph(Job job){
            Stage anovaGraphStage = new Stage();
            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Run");
            yAxis.setLabel("Mean");
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Tukey-HSD Test");
            lineChart.setHorizontalGridLinesVisible(false);
            lineChart.setVerticalGridLinesVisible(false);
            
            XYChart.Series<Number, Number> overallmeanSeries = new XYChart.Series<>();
            overallmeanSeries.setName("Mean Between Runs");
            
            XYChart.Series<Number, Number> qHSDSeries = new XYChart.Series<>();
            qHSDSeries.setName("Q-HSD");
            

            for (Map.Entry<Integer, TukeyDataPoint> entry : tukeyData.entrySet()) {
                Integer key = entry.getKey();
                TukeyDataPoint tukeyDataPoint = entry.getValue();
                
                overallmeanSeries.getData().add(new XYChart.Data<>(key, tukeyDataPoint.getMean()));
                qHSDSeries.getData().add(new XYChart.Data<>(key, tukeyDataPoint.getQHSD()));
            }
            


            lineChart.getData().add(overallmeanSeries);
            lineChart.getData().add(qHSDSeries);

            
 
            Scene scene  = new Scene(lineChart,800,600);
            anovaGraphStage.setScene(scene);
            anovaGraphStage.show();
    }
    
    public void calculateTukeyHSD(){
        if(job.getRuns().size() <= 1) return;
        for (int i = 0; i < job.getRuns().size(); i += 2) {
            Run run1 = job.getRuns().get(i);
            Run run2 = job.getRuns().get(i + 1);
            double overallMean = Math.abs(run1.getAverageSpeed() - run2.getAverageSpeed());
            double sse = calculateSSE(run1, run2);
            
            this.tukey = new Tukey(1, 2, 2 * (job.getRunDataSize() - 1));
            
            this.qHSD = tukey.inverse_survival(job.getAlpha(), false) * Math.sqrt((sse / (2.0 * (this.job.getRunDataSize()))) / job.getRunDataSize());
            
            run1.setQ(overallMean);
            
            tukeyData.put(run1.getID(), new TukeyDataPoint(overallMean, qHSD));
            
            run2.setQ(Run.UNDEFINED_VALUE);
            run2.setNullhypothesis(Run.UNDEFIND_NULLHYPOTHESIS);
            
            if(qHSD < overallMean){
                run1.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            } else {
                run1.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
            }
        }
    }
    
        private double calculateSSE(Run run1, Run run2){
        double sse = 0;
        double averageSpeed = (run1.getAverageSpeed() + run2.getAverageSpeed()) / 2.0;
        
        for (DataPoint dp : run1.getData()) {
                    sse += (Math.pow((dp.getSpeed() - averageSpeed), 2));
        }
    
        for (DataPoint dp : run2.getData()) {
                    sse += (Math.pow((dp.getSpeed() - averageSpeed), 2));
        }
        
        return sse;
    }
    
    private void setLabeling(){
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
    }

    public ConInt.STATUS openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/atool/TukeyHSD.fxml"));
            fxmlLoader.setController(this);
            Parent root1 = (Parent) fxmlLoader.load();

            Stage stage = new Stage();
            stage.setMaxWidth(1200);      
            stage.setMaxHeight(600);
            stage.setMinHeight(600);
            stage.setMinWidth(800);
            stage.setTitle("Calculated Tukey HSD");
            stage.setScene(new Scene(root1));
            stage.show();
            
    } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, String.format("Couldn't open Window for Anova! App state: %s", ConInt.STATUS.IO_EXCEPTION));
            return ConInt.STATUS.IO_EXCEPTION;
        }
        return ConInt.STATUS.SUCCESS;
    }
}