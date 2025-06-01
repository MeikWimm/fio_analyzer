/*
 * Lizenz- und Klassenvorlagen – automatisch von der IDE generiert.
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Diese Klasse stellt Methoden zur grafischen Darstellung von Jobs bereit.
 * Sie verwendet JavaFX, um Daten als Liniendiagramme darzustellen.
 *
 * @author meni1999
 */
public class Charter {

    public record ChartData(String label, List<XYChart.Data<Number, Number>> data) {}

    private boolean isJobSpeedStageInitialized;
    private boolean isJobFreqStageInitialized;
    private Stage stageJobSpeed;
    private Stage stageJobFreq;

//    public void drawJobFreqeuncy(Job job) {
//        boolean proceedDrawing = true;
//
//        // Warnung bei großen Datenmengen
//        if (job.getFrequency().size() > 10000) {
//            ButtonType goodButton = new ButtonType("Ok");
//            ButtonType badButton = new ButtonType("Cancel");
//            Alert alert = new Alert(Alert.AlertType.INFORMATION,
//                    "This could take a while because of more than 10000 data points from this job.",
//                    goodButton, badButton);
//            Window window = alert.getDialogPane().getScene().getWindow();
//            window.setOnCloseRequest(e -> alert.hide());
//            Optional<ButtonType> result = alert.showAndWait();
//
//            if (result.isPresent()) {
//                proceedDrawing = (result.get() == goodButton);
//            }
//        }
//
//        if (!proceedDrawing) return;
//
//        if (!isJobFreqStageInitialized) {
//            stageJobFreq = new Stage();
//
//            final NumberAxis xAxis = new NumberAxis();
//            final NumberAxis yAxis = new NumberAxis();
//            xAxis.setLabel("I/O-Speed in Kibibytes");
//            yAxis.setLabel("Frequency");
//
//            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
//            lineChart.setTitle("Job Frequency");
//
//            XYChart.Series<Number, Number> series = new XYChart.Series<>();
//
//            // Frequenzdaten
//            Map<Integer, Integer> data = job.getFrequency();
//            for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
//                Integer key = entry.getKey();
//                Integer value = entry.getValue();
//                series.getData().add(new XYChart.Data<>(key, value));
//            }
//
//            Scene scene = new Scene(lineChart, 800, 600);
//            lineChart.getData().add(series);
//            stageJobFreq.setScene(scene);
//            isJobFreqStageInitialized = true;
//        }
//
//        stageJobFreq.show();
//    }

    public final void drawGraph(String title, String xAxisLabel, String yAxisLabel, String constantLabel, double constant, ChartData... chartDataList) {
        Stage graphStage = new Stage();

        if (chartDataList.length == 0) {
            throw new IllegalArgumentException("Chart data list cannot be empty");
        }

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        Utils.CustomLineChart<Number, Number> lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);

        List<XYChart.Data<Number, Number>> dataList = null;
        for (ChartData chartData: chartDataList){
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.getData().addAll(chartData.data);
            series.setName(chartData.label());
            lineChart.getData().add(series);
            dataList = chartData.data;
        }

        XYChart.Series<Number, Number> constantSeries = createConstantSeries(constant, dataList);
        constantSeries.setName(constantLabel);
        lineChart.getData().add(constantSeries);
        Scene scene = new Scene(lineChart, 800, 600);
        graphStage.setScene(scene);
        graphStage.show();
    }

    public final void drawGraph(String title, String xAxisLabel, String yAxisLabel, ChartData... chartDataList) {
        Stage graphStage = new Stage();

        if (chartDataList.length == 0) {
            throw new IllegalArgumentException("Chart data list cannot be empty");
        }

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        Utils.CustomLineChart<Number, Number> lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);

        for (ChartData chartData: chartDataList){
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.getData().addAll(chartData.data);
            series.setName(chartData.label());
            lineChart.getData().add(series);
        }

        Scene scene = new Scene(lineChart, 800, 600);
        graphStage.setScene(scene);
        graphStage.show();
    }

    @SafeVarargs
    private XYChart.Series<Number, Number> createConstantSeries(double constant, List<XYChart.Data<Number, Number>>... dataList){
        double min = getMinX(dataList);
        double max = getMaxX(dataList);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(min, constant));
        series.getData().add(new XYChart.Data<>(max, constant));
        return series;
    }

    @SafeVarargs
    private static double getMinX(List<XYChart.Data<Number, Number>>... dataList) {
        double minX = Double.POSITIVE_INFINITY;

        for (List<XYChart.Data<Number, Number>> data : dataList) {
            for (XYChart.Data<Number, Number> datum : data) {
                double x = datum.getXValue().doubleValue();
                if (x < minX) {
                    minX = x;
                }
            }
        }

        return (minX == Double.POSITIVE_INFINITY) ? Double.NaN : minX;
    }

    @SafeVarargs
    private static double getMaxX(List<XYChart.Data<Number, Number>>... dataList) {
        double maxX = Double.NEGATIVE_INFINITY;

        for (List<XYChart.Data<Number, Number>> data : dataList) {
            for (XYChart.Data<Number, Number> datum : data) {
                double x = datum.getXValue().doubleValue();
                if (x > maxX) {
                    maxX = x;
                }
            }
        }

        return (maxX == Double.NEGATIVE_INFINITY) ? Double.NaN : maxX;
    }

}
