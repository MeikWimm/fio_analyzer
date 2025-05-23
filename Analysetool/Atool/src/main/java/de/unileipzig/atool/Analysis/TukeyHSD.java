/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.Job;
import de.unileipzig.atool.Run;
import de.unileipzig.atool.Settings;
import de.unileipzig.atool.Utils;
import java.io.IOException;
import java.net.URL;
import java.util.*;
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
public class TukeyHSD extends PostHocTest implements Initializable, PostHocAnalyzer {
    private static final Logger LOGGER = Logger.getLogger( TukeyHSD.class.getName() );

    private record TukeyDataPoint(double mean, double qHSD) {}
    
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
    @FXML public TableColumn<Run, Double> QColumn;
    @FXML public TableColumn<Run, Byte> hypothesisColumn;
    private double qHSD;
    private final Map<Integer, TukeyDataPoint> tukeyData;
    private double alpha = 0.05;

    public TukeyHSD(Anova anova , double alpha){
        super(anova);
        this.tukeyData = new HashMap<>();
        this.alpha = alpha;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("AverageSpeed"));
        averageSpeedColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));  

        runIDColumn.setCellValueFactory(new PropertyValueFactory<>("RunID"));
        compareToRunColumn.setCellValueFactory(new PropertyValueFactory<>("Group"));
        QColumn.setCellValueFactory(new PropertyValueFactory<>("Q"));
        QColumn.setCellFactory(TextFieldTableCell.<Run, Double>forTableColumn(new Utils.CustomStringConverter()));

        hypothesisColumn.setCellValueFactory(new PropertyValueFactory<>("Nullhypothesis"));
        hypothesisColumn.setCellFactory(Utils.getHypothesisCellFactory());

        drawTukey.setOnAction(e -> draw());
        
        TukeyTable.setItems(this.job.getRuns());
        qCritLabel.setText(String.format(Locale.ENGLISH, Settings.DIGIT_FORMAT, this.qHSD));
    }

    public void draw(){
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
                
                overallmeanSeries.getData().add(new XYChart.Data<>(key, tukeyDataPoint.mean()));
                qHSDSeries.getData().add(new XYChart.Data<>(key, tukeyDataPoint.qHSD()));
            }

            lineChart.getData().add(overallmeanSeries);
            lineChart.getData().add(qHSDSeries);
 
            Scene scene  = new Scene(lineChart,800,600);
            anovaGraphStage.setScene(scene);
            anovaGraphStage.show();
    }

    @Override
    public void apply(List<Run> sigRuns, List<List<Run>> result) {
        int runDataSize = job.getRunDataSize();
        for (int i = 0; i < result.size() - 1; i++) {
            Tukey tukey = new Tukey(1, 2, 2 * (runDataSize - 1));
            List<Run> group1 = result.get(i);
            List<Run> group2 = result.get(i + 1);
            double speedSumGroup1 = 0;
            double speedSumGroup2 = 0;


            for (int j = 0; j < group1.size(); j++) {
                Run run1 = group1.get(j);
                Run run2 = group2.get(j);

                speedSumGroup1 += run1.getAverageSpeed();
                speedSumGroup2 += run2.getAverageSpeed();
            }

            double averageSpeedGroup1 = speedSumGroup1 / group1.size();
            double averageSpeedGroup2 = speedSumGroup2 / group2.size();

            double sse = result.get(i).getFirst().getSSE();
            double overallMean = Math.abs(averageSpeedGroup1 - averageSpeedGroup2);

            this.qHSD = tukey.inverse_survival(alpha, false) * Math.sqrt((sse / (2.0 * (runDataSize))) / runDataSize);
            Run run = group1.getFirst();
            run.setQ(overallMean);

            tukeyData.put(run.getID(), new TukeyDataPoint(overallMean, qHSD));

            if(qHSD < overallMean){
                run.setNullhypothesis(Run.REJECTED_NULLHYPOTHESIS);
            } else {
                run.setNullhypothesis(Run.ACCEPTED_NULLHYPOTHESIS);
            }
        }
    }

    public void openWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/unileipzig/atool/TukeyHSD.fxml"));
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
        }
    }
}