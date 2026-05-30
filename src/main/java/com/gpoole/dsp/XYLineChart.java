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

    JFreeChart xylineChart;
    ChartPanel chartPanel;
    JFrame frame = new JFrame("Charts");
    double[] xy;
    int Fs;

    public XYLineChart(String chartTitle) {
        chartPanel = new ChartPanel(xylineChart);
        chartPanel.setAutoscrolls(true);

        chartPanel.setPreferredSize(new Dimension(1366, 768));

        frame.setAutoRequestFocus(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Thread repaint = new Thread(() -> {
            while (true) {
                if (xy != null) {
                    xylineChart = ChartFactory.createXYLineChart(
                            chartTitle,
                            "Freq (Hz)",
                            "Power (Db)",
                            createDataset(xy, Fs),
                            PlotOrientation.VERTICAL,
                            true, true, false);
                    chartPanel.setChart(xylineChart);
                    XYPlot plot = xylineChart.getXYPlot();

                    NumberAxis range = (NumberAxis) plot.getRangeAxis();
                    range.setRange(-30.0, 70);
                    range.setAutoTickUnitSelection(true);
                    NumberAxis domain = (NumberAxis) plot.getDomainAxis();
                    domain.setAutoTickUnitSelection(true);
                    domain.setVerticalTickLabels(true);
                    StandardXYItemRenderer renderer = new StandardXYItemRenderer();
                    renderer.setAutoPopulateSeriesStroke(true);
                    renderer.setAutoPopulateSeriesShape(true);
                    plot.setRenderer(renderer);
                    chartPanel.repaint();
                    frame.getContentPane().add(chartPanel);
                    frame.pack();
                    frame.repaint();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(XYLineChart.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        repaint.start();
    }

    public void setData(double[] inxy, int inFs) {
        xy = inxy;
        Fs = inFs;
    }

    private XYDataset createDataset(double[] xy, int Fs) {
        final XYSeries data = new XYSeries("data");
        for (int q = 0; q < xy.length; q++) {
            if (xy[q] == 0) {
                data.add((int) (q * (Fs / (xy.length))) / 2, xy[q]);
            } else {
                data.add((int) (q * (Fs / (xy.length))) / 2, xy[q] - 15);
            }
        }
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(data);
        return dataset;
    }
}
