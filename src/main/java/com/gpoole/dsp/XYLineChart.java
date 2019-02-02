/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gpoole.dsp;

import java.awt.Dimension;
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.xy.XYSeriesCollection; 
import javax.swing.JFrame;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
/**
 *
 * @author geoff
 */
public class XYLineChart{
    JFreeChart xylineChart;
    ChartPanel chartPanel ;
    JFrame frame = new JFrame("Charts");
    public XYLineChart(String chartTitle) {
        chartPanel = new ChartPanel( xylineChart );
        chartPanel.setAutoscrolls(true);
        //chartPanel.setPreferredSize(new Dimension(1920,1080));
        chartPanel.setPreferredSize(new Dimension(1366,768));
        //frame.setSize(1280, 1024);
        frame.setAutoRequestFocus(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
    
   public boolean update (String applicationTitle, String chartTitle, double[] xy, int Fs){
       if(!frame.isShowing()){
           frame.dispose();
           return false;
       }
       xylineChart = ChartFactory.createXYLineChart(
         chartTitle ,
         "Freq (Hz)" ,
         "Power (Db)" ,
         createDataset(xy, Fs) ,
         PlotOrientation.VERTICAL ,
         true , true , false);
       chartPanel.setChart(xylineChart);
      XYPlot plot = xylineChart.getXYPlot();
     
      //plot.setDomainCrosshairVisible(true);
      //plot.setRangeCrosshairVisible(true);
      NumberAxis range = (NumberAxis) plot.getRangeAxis();
      range.setRange(-80.0,30);
      range.setAutoTickUnitSelection(true);
      NumberAxis domain = (NumberAxis) plot.getDomainAxis();
      domain.setAutoTickUnitSelection(true);
      domain.setVerticalTickLabels(true);
      //XYSplineRenderer renderer = new XYSplineRenderer();
      StandardXYItemRenderer renderer = new StandardXYItemRenderer();
      //renderer.setSeriesPaint( 0 , Color.BLUE );
      //renderer.setSeriesStroke( 0 , new BasicStroke(0.1f) );
      renderer.setAutoPopulateSeriesStroke(true);
      //renderer.setAutoPopulateSeriesFillPaint(true);
      //renderer.setAutoPopulateSeriesPaint(true);
      renderer.setAutoPopulateSeriesShape(true);
      
      
      //renderer.setUseFillPaint(true);
      plot.setRenderer( renderer );

      frame.getContentPane().add(chartPanel);
      frame.pack();
      return true;
   }
   private XYDataset createDataset(double[] xy,int Fs ) {
      final XYSeries data= new XYSeries( "data" );
      for (int q =0;q<xy.length;q++){
        data.add((int)(q*(Fs/(xy.length)))/2, xy[q]-50 );       
      }             
      final XYSeriesCollection dataset = new XYSeriesCollection( );          
      dataset.addSeries( data );          
      return dataset;
   }
}
