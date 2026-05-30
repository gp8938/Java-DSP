/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gpoole.dsp;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author geoff
 */
public class XYLineChart {

    private JFreeChart xyLineChart;
    private ChartPanel chartPanel;
    private JFrame chartFrame = new JFrame("Frequency Chart");
    private double[] frequencyData;
    private int samplingRate;
    private boolean dataChanged = false;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 50; // Update at most every 50ms

    public XYLineChart(String chartTitle) {
        chartPanel = new ChartPanel(xyLineChart);
        chartPanel.setAutoscrolls(true);
        chartPanel.setPreferredSize(new Dimension(1366, 768));

        chartFrame.setAutoRequestFocus(false);
        chartFrame.setVisible(true);
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Thread repaintThread = new Thread(() -> {
            while (true) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (dataChanged && (currentTime - lastUpdateTime) >= UPDATE_INTERVAL_MS && frequencyData != null) {
                        dataChanged = false;
                        lastUpdateTime = currentTime;
                        
                        if (xyLineChart == null) {
                            // Create chart only once
                            xyLineChart = ChartFactory.createXYLineChart(
                                    "Frequency Spectrum",
                                    "Frequency (Hz)",
                                    "Power (dB)",
                                    createDataset(frequencyData, samplingRate),
                                    PlotOrientation.VERTICAL,
                                    false, true, false);
                            
                            XYPlot plot = xyLineChart.getXYPlot();
                            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                            rangeAxis.setAutoRange(true);
                            rangeAxis.setAutoRangeIncludesZero(false);

                            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
                            domainAxis.setAutoRange(true);
                            domainAxis.setAutoRangeIncludesZero(true);

                            chartPanel.setChart(xyLineChart);
                            chartFrame.getContentPane().add(chartPanel);
                            chartFrame.pack();
                        } else {
                            // Update existing chart data
                            XYPlot plot = xyLineChart.getXYPlot();
                            plot.setDataset(createDataset(frequencyData, samplingRate));
                        }
                    }
                    Thread.sleep(UPDATE_INTERVAL_MS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(XYLineChart.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
        });
        repaintThread.setDaemon(true);
        repaintThread.setName("ChartUpdateThread");
        repaintThread.start();
    }

    public void setData(double[] data, int rate) {
        frequencyData = data;
        samplingRate = rate;
        dataChanged = true;
    }

    private XYDataset createDataset(double[] data, int rate) {
        final XYSeries series = new XYSeries("Frequency Data");
        for (int i = 0; i < data.length; i++) {
            double frequency = (i * (rate / (double) data.length)) / 2;
            double power = data[i] == 0 ? 0 : data[i] - 15;
            series.add(frequency, power);
        }
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }
}
