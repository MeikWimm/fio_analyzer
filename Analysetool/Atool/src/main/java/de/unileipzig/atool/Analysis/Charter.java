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
    private boolean isJobSpeedStageInitialized;
    private boolean isJobFreqStageInitialized;
    private Stage stageJobSpeed;
    private Stage stageJobFreq;


    /**
     * Zeichnet den Verlauf eines Jobs über die Zeit (I/O-Speed vs. Zeit).
     * Bei großen Datenmengen (> 10000) wird eine Warnung angezeigt.
     *
     * @param job Das Job-Objekt mit den zu zeichnenden Daten.
     */
//    public void drawJob(Job job) {
//        boolean proceedDrawing = true;
//
//        // Warnung bei sehr großen Datenmengen
//        if (job.getData().size() > 10000) {
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
//        // Nur initialisieren, wenn es noch nicht gemacht wurde
//        if (!isJobSpeedStageInitialized) {
//            stageJobSpeed = new Stage();
//
//            final NumberAxis xAxis = new NumberAxis();
//            final NumberAxis yAxis = new NumberAxis();
//            xAxis.setLabel("Time in milliseconds");
//            yAxis.setLabel("I/O-Speed in Kibibytes");
//
//            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
//            lineChart.setHorizontalGridLinesVisible(false);
//            lineChart.setVerticalGridLinesVisible(false);
//            lineChart.setTitle("Job");
//
//            XYChart.Series<Number, Number> series = new XYChart.Series<>();
//
//            // Datenreduktion mit dem Ramer-Douglas-Peucker-Algorithmus
//            List<DataPoint> data = job.getData();
//            List<DataPoint> reduced_data = RamerDouglasPeucker.douglasPeucker(data, job.getEpsilon());
//
//            for (DataPoint p : reduced_data) {
//                series.getData().add(new XYChart.Data<>(p.getTime(), p.getSpeed()));
//            }
//
//            Scene scene = new Scene(lineChart, 800, 600);
//            lineChart.getData().add(series);
//            stageJobSpeed.setScene(scene);
//            isJobSpeedStageInitialized = true;
//        }
//
//        stageJobSpeed.show();
//    }

    /**
     * Zeichnet die Frequenzverteilung der I/O-Geschwindigkeit eines Jobs.
     * RDP wird hier nicht verwendet.
     *
     * @param job Das Job-Objekt mit Frequenzdaten.
     */
    public void drawJobFreqeuncy(Job job) {
        boolean proceedDrawing = true;

        // Warnung bei großen Datenmengen
        if (job.getFrequency().size() > 10000) {
            ButtonType goodButton = new ButtonType("Ok");
            ButtonType badButton = new ButtonType("Cancel");
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "This could take a while because of more than 10000 data points from this job.",
                    goodButton, badButton);
            Window window = alert.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(e -> alert.hide());
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                proceedDrawing = (result.get() == goodButton);
            }
        }

        if (!proceedDrawing) return;

        if (!isJobFreqStageInitialized) {
            stageJobFreq = new Stage();

            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("I/O-Speed in Kibibytes");
            yAxis.setLabel("Frequency");

            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Job Frequency");

            XYChart.Series<Number, Number> series = new XYChart.Series<>();

            // Frequenzdaten
            Map<Integer, Integer> data = job.getFrequency();
            for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                series.getData().add(new XYChart.Data<>(key, value));
            }

            Scene scene = new Scene(lineChart, 800, 600);
            lineChart.getData().add(series);
            stageJobFreq.setScene(scene);
            isJobFreqStageInitialized = true;
        }

        stageJobFreq.show();
    }

    /**
     * Zeichnet ein allgemeines Diagramm für statistische Auswertungen (z.B. ANOVA).
     * Optional kann eine kritische Linie eingezeichnet werden.
     *
     * @param title      Titel des Diagramms
     * @param xAxisLabel Bezeichnung der X-Achse
     * @param yAxisLabel Bezeichnung der Y-Achse
     * @param lineLabel  Bezeichnung der Datenlinie
     * @param data       Die darzustellenden Daten als Map (X: Integer (RunID), Y: Double (z.B korrespondierender F-Wert))
     * @param runSize    Das Job-Objekt.
     * @param critValue  Optionaler kritischer Schwellenwert
     */
    public final void drawGraph(String title, String xAxisLabel, String yAxisLabel, String lineLabel, Map<Integer, Double> data, int runSize,
                          double critValue) {
        Stage graphStage = new Stage();

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        //LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        Utils.CustomLineChart<Number, Number> lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setHorizontalGridLinesVisible(false);
        lineChart.setVerticalGridLinesVisible(false);

        // Hauptdatenlinie
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(lineLabel);
        for (Map.Entry<Integer, Double> entry : data.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
            series.getData().add(new XYChart.Data<>(key, value));
        }

        // Kritische Linie
        XYChart.Series<Number, Number> criticalLine = new XYChart.Series<>();
        if (critValue != Run.UNDEFINED_DOUBLE_VALUE) {
            criticalLine.getData().add(new XYChart.Data<>(0, critValue));
            criticalLine.getData().add(new XYChart.Data<>(runSize, critValue));
            criticalLine.setName("critical Value");
            lineChart.getData().add(criticalLine);
        }

        lineChart.getData().add(series);
        Scene scene = new Scene(lineChart, 800, 600);
        graphStage.setScene(scene);
        graphStage.show();
    }
    @SafeVarargs
    public final void drawGraph(String title, String xAxisLabel, String yAxisLabel, String lineLabel, double constant, List<XYChart.Data<Number, Number>>... dataList) {
        Stage graphStage = new Stage();

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        Utils.CustomLineChart<Number, Number> lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);


        for (List<XYChart.Data<Number, Number>> data: dataList){
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.getData().addAll(data);
            lineChart.getData().add(series);
        }

        //min = getMinX(seriesList);
        //max = getMaxX(seriesList);

        for (int i = 0; i < 1; i++) {
            lineChart.getData().add(createConstantSeries(constant, dataList));
        }

        Scene scene = new Scene(lineChart, 800, 600);
        graphStage.setScene(scene);
        graphStage.show();
    }

    @SafeVarargs
    public final void drawGraph(String title, String xAxisLabel, String yAxisLabel, String lineLabel, XYChart.Series<Number, Number>... seriesList) {
        Stage graphStage = new Stage();

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        Utils.CustomLineChart<Number, Number> lineChart = new Utils.CustomLineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);

        for (XYChart.Series<Number, Number> serie: seriesList){
            serie.setName(lineLabel);
            lineChart.getData().add(serie);
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
