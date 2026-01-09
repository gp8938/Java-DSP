package com.gpoole.dsp;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FFT processing and frequency detection
 */
public class FFTProcessingTest {

    @Test
    public void testFFTBasicFunctionality() {
        // Test FFT with a simple sine wave
        int fftSize = 1024;
        double[] input = new double[fftSize];
        double frequency = 440.0; // A4 note
        double sampleRate = 48000.0;
        
        // Generate sine wave
        for (int i = 0; i < fftSize; i++) {
            input[i] = Math.sin(2 * Math.PI * frequency * i / sampleRate);
        }
        
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        org.apache.commons.math3.complex.Complex[] result = transformer.transform(input, TransformType.FORWARD);
        
        assertNotNull(result);
        assertEquals(fftSize, result.length);
    }

    @Test
    public void testFFTPeakDetection() {
        // Test that FFT correctly identifies the dominant frequency
        int fftSize = 2048;
        double[] input = new double[fftSize];
        double expectedFreq = 1000.0; // 1kHz test tone
        double sampleRate = 48000.0;
        
        // Generate pure tone
        for (int i = 0; i < fftSize; i++) {
            input[i] = Math.sin(2 * Math.PI * expectedFreq * i / sampleRate);
        }
        
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        org.apache.commons.math3.complex.Complex[] result = transformer.transform(input, TransformType.FORWARD);
        
        // Find peak frequency
        double maxMagnitude = 0;
        int peakIndex = 0;
        for (int i = 1; i < result.length / 2; i++) {
            double magnitude = Math.sqrt(result[i].getReal() * result[i].getReal() + 
                                       result[i].getImaginary() * result[i].getImaginary());
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
                peakIndex = i;
            }
        }
        
        double detectedFreq = peakIndex * sampleRate / fftSize;
        
        // Allow 5% tolerance
        assertTrue(Math.abs(detectedFreq - expectedFreq) / expectedFreq < 0.05,
                  "Detected frequency " + detectedFreq + " should be close to " + expectedFreq);
    }

    @Test
    public void testHammingWindow() {
        // Test Hamming window function
        int size = 512;
        double[] samples = new double[size];
        
        // Fill with constant value
        for (int i = 0; i < size; i++) {
            samples[i] = 1.0;
        }
        
        // Apply Hamming window
        for (int i = 0; i < samples.length; i++) {
            double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (samples.length - 1));
            samples[i] *= window;
        }
        
        // Check that edges are reduced
        assertTrue(samples[0] < 0.1, "Window should reduce edge values");
        assertTrue(samples[size - 1] < 0.1, "Window should reduce edge values");
        
        // Check that middle is close to 1
        assertTrue(samples[size / 2] > 0.9, "Window should preserve middle values");
    }

    @Test
    public void testNoiseThreshold() {
        // Test that noise filtering works correctly
        int fftSize = 1024;
        double[] input = new double[fftSize];
        
        // Add small random noise
        for (int i = 0; i < fftSize; i++) {
            input[i] = (Math.random() - 0.5) * 0.1;
        }
        
        // Add strong signal at specific frequency
        double signalFreq = 2000.0;
        double sampleRate = 48000.0;
        for (int i = 0; i < fftSize; i++) {
            input[i] += Math.sin(2 * Math.PI * signalFreq * i / sampleRate);
        }
        
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        org.apache.commons.math3.complex.Complex[] result = transformer.transform(input, TransformType.FORWARD);
        
        // Calculate average magnitude (noise floor)
        double avgMagnitude = 0;
        for (int i = 1; i < result.length / 2; i++) {
            double magnitude = Math.sqrt(result[i].getReal() * result[i].getReal() + 
                                       result[i].getImaginary() * result[i].getImaginary());
            avgMagnitude += magnitude;
        }
        avgMagnitude /= (result.length / 2 - 1);
        
        // Find peak above threshold
        double threshold = avgMagnitude * 3.0;
        boolean foundPeak = false;
        for (int i = 1; i < result.length / 2; i++) {
            double magnitude = Math.sqrt(result[i].getReal() * result[i].getReal() + 
                                       result[i].getImaginary() * result[i].getImaginary());
            if (magnitude > threshold) {
                foundPeak = true;
                break;
            }
        }
        
        assertTrue(foundPeak, "Should detect signal above noise threshold");
    }

    @Test
    public void testFFTSizesPowerOfTwo() {
        // Test that various FFT sizes work correctly
        int[] sizes = {512, 1024, 2048, 4096};
        
        for (int size : sizes) {
            double[] input = new double[size];
            for (int i = 0; i < size; i++) {
                input[i] = Math.sin(2 * Math.PI * i / size);
            }
            
            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
            org.apache.commons.math3.complex.Complex[] result = transformer.transform(input, TransformType.FORWARD);
            
            assertEquals(size, result.length, "FFT output size should match input for size " + size);
        }
    }
}
