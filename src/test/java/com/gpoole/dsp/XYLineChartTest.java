package com.gpoole.dsp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import javax.swing.JFrame;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for XYLineChart visualization component
 * These tests are skipped in headless environments (CI)
 */
public class XYLineChartTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testChartCreation() {
        XYLineChart chart = new XYLineChart("Test Chart");
        assertNotNull(chart, "Chart should be created successfully");
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testDataUpdate() {
        XYLineChart chart = new XYLineChart("Test Chart");
        
        double[] magnitudes = new double[100];
        
        for (int i = 0; i < 100; i++) {
            magnitudes[i] = Math.random() * 100;
        }
        
        int sampleRate = 48000;
        
        // This should not throw an exception
        assertDoesNotThrow(() -> chart.setData(magnitudes, sampleRate));
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testEmptyDataHandling() {
        XYLineChart chart = new XYLineChart("Test Chart");
        
        double[] emptyMag = new double[0];
        int sampleRate = 48000;
        
        // Should handle empty arrays gracefully
        assertDoesNotThrow(() -> chart.setData(emptyMag, sampleRate));
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testLargeDataSet() {
        XYLineChart chart = new XYLineChart("Test Chart");
        
        int size = 10000;
        double[] magnitudes = new double[size];
        
        for (int i = 0; i < size; i++) {
            magnitudes[i] = Math.sin(i * 0.01) * 100;
        }
        
        int sampleRate = 48000;
        
        // Should handle large datasets
        assertDoesNotThrow(() -> chart.setData(magnitudes, sampleRate));
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testChartThrottling() throws InterruptedException {
        XYLineChart chart = new XYLineChart("Test Chart");
        
        double[] magnitudes = new double[100];
        
        for (int i = 0; i < 100; i++) {
            magnitudes[i] = 50.0;
        }
        
        int sampleRate = 48000;
        
        // Rapid updates should be throttled
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            chart.setData(magnitudes, sampleRate);
        }
        long duration = System.currentTimeMillis() - startTime;
        
        // Should complete quickly due to throttling
        assertTrue(duration < 1000, "Updates should be throttled efficiently");
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testMultipleSampleRates() {
        XYLineChart chart = new XYLineChart("Test Chart");
        
        double[] magnitudes = new double[100];
        for (int i = 0; i < 100; i++) {
            magnitudes[i] = Math.random() * 50;
        }
        
        int[] sampleRates = {8000, 16000, 44100, 48000};
        
        for (int rate : sampleRates) {
            assertDoesNotThrow(() -> chart.setData(magnitudes, rate),
                             "Should handle sample rate: " + rate);
        }
    }
}
