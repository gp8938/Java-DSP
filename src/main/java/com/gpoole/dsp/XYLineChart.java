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
    private static final long UPDATE_INTERVAL_MS = 50;

    public XYLineChart(String title) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Frequency (Hz)");
        yAxis.setLabel("Power (dB)");
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
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
            for (int i = 0; i < frequencyData.length; i++) {
                double frequency = (i * (samplingRate / (double) frequencyData.length)) / 2.0;
                double power = frequencyData[i] == 0 ? 0 : frequencyData[i] - 15;
                series.getData().add(new XYChart.Data<>(frequency, power));
            }
            chart.getData().clear();
            chart.getData().add(series);
        });
    }

}
