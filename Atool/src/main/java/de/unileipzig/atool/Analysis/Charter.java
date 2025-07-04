/*
 * Lizenz- und Klassenvorlagen – automatisch von der IDE generiert.
 */
package de.unileipzig.atool.Analysis;

import de.unileipzig.atool.*;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.List;


/**
 * Diese Klasse stellt Methoden zur grafischen Darstellung von Jobs bereit.
 * Sie verwendet JavaFX, um Daten als Liniendiagramme darzustellen.
 *
 * @author meni1999
 */
public class Charter {

    public record ChartData(String label, List<XYChart.Data<Number, Number>> data) {}
    private Utils.CustomLineChart<Number, Number> lineChart;
    private Scene scene;
    public final Scene drawGraph(String title, String xAxisLabel, String yAxisLabel, String constantLabel, double constant, ChartData... chartDataList) {
        if (chartDataList.length == 0) {
            throw new IllegalArgumentException("Chart data list cannot be empty");
        }

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
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
        scene = new Scene(lineChart, 800, 600);

        return scene;
    }

    public final Scene drawGraph(String title, String xAxisLabel, String yAxisLabel, ChartData... chartDataList) {
        if (chartDataList.length == 0) {
            throw new IllegalArgumentException("Chart data list cannot be empty");
        }

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);

        for (ChartData chartData: chartDataList){
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.getData().addAll(chartData.data);
            series.setName(chartData.label());
            lineChart.getData().add(series);
        }

        scene = new Scene(lineChart, 800, 600);

        return scene;
    }

    public void openWindow(){
        Stage graphStage = new Stage();
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
