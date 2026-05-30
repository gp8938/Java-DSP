/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gpoole.dsp;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * JavaFX-based line chart for displaying the audio frequency spectrum.
 * <p>
 * This replaces the previous Swing/JFreeChart implementation and provides a
 * thread-safe `setData` method that updates the JavaFX `LineChart` on the
 * JavaFX Application Thread.
 * </p>
 */
public class XYLineChart {

    private final LineChart<Number, Number> chart;
    private volatile double[] frequencyData;
    private volatile int samplingRate;
    private volatile long lastUpdateTime = 0;
    private volatile boolean cssApplied = false;
    private static final long UPDATE_INTERVAL_MS = 50;

    public XYLineChart(String title) {
        NumberAxis xAxis = new NumberAxis(0, 22050, 1000);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        xAxis.setLabel("Frequency (Hz)");
        yAxis.setLabel("Power (dB)");
        xAxis.setTickLabelFormatter(new HzFormatter());
        xAxis.setMinorTickVisible(true);
        yAxis.setMinorTickVisible(true);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setPrefSize(800, 600);
        chart.setMinSize(400, 300);
        chart.setLegendVisible(false);
    }

    private static class HzFormatter extends javafx.scene.chart.NumberAxis.DefaultFormatter {
        HzFormatter() {
            super(new NumberAxis(), "", "");
        }
        @Override
        public String toString(Number value) {
            int hz = value.intValue();
            if (hz >= 1000) return (hz / 1000) + "k";
            return String.valueOf(hz);
        }
    }

    public Node getNode() {
        return chart;
    }

    public void setData(double[] data, int rate) {
        frequencyData = data;
        samplingRate = rate;
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) {
            return; // throttle updates
        }
        lastUpdateTime = now;

        // Update chart on JavaFX thread
        Platform.runLater(() -> {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Spectrum");
            double nyquist = samplingRate / 2.0;
            double binWidth = nyquist / frequencyData.length;
            for (int i = 0; i < frequencyData.length; i++) {
                double frequency = i * binWidth;
                series.getData().add(new XYChart.Data<>(frequency, frequencyData[i]));
            }
            ((NumberAxis) chart.getXAxis()).setUpperBound(nyquist);
            chart.getData().setAll(java.util.Collections.singletonList(series));
            // Apply CSS once on first render
            if (!cssApplied) {
                chart.applyCss();
                Node line = chart.lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: #00C853; -fx-stroke-width: 2px;");
                    cssApplied = true;
                }
            }
        });
    }

}
