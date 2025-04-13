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

    public XYLineChart(String chartTitle) {
        chartPanel = new ChartPanel(xyLineChart);
        chartPanel.setAutoscrolls(true);
        chartPanel.setPreferredSize(new Dimension(1366, 768));

        chartFrame.setAutoRequestFocus(false);
        chartFrame.setVisible(true);
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Thread repaintThread = new Thread(() -> {
            while (true) {
                if (frequencyData != null) {
                    xyLineChart = ChartFactory.createXYLineChart(
                            chartTitle,
                            "Frequency (Hz)",
                            "Power (dB)",
                            createDataset(frequencyData, samplingRate),
                            PlotOrientation.VERTICAL,
                            true, true, false);
                    chartPanel.setChart(xyLineChart);
                    XYPlot plot = xyLineChart.getXYPlot();

                    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                    rangeAxis.setRange(-30.0, 70);
                    rangeAxis.setAutoTickUnitSelection(true);

                    NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
                    domainAxis.setAutoTickUnitSelection(true);
                    domainAxis.setVerticalTickLabels(true);

                    StandardXYItemRenderer renderer = new StandardXYItemRenderer();
                    renderer.setAutoPopulateSeriesStroke(true);
                    renderer.setAutoPopulateSeriesShape(true);
                    plot.setRenderer(renderer);

                    chartPanel.repaint();
                    chartFrame.getContentPane().add(chartPanel);
                    chartFrame.pack();
                    chartFrame.repaint();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(XYLineChart.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        repaintThread.start();
    }

    public void setData(double[] data, int rate) {
        frequencyData = data;
        samplingRate = rate;
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
