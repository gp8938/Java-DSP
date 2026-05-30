package com.gpoole.dsp.signal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link SignalPipeline} fluent API.
 */
class SignalPipelineTest {

    @Test
    void emptyPipelineReturnsCloneOfInput() {
        double[] input = {1, 2, 3, 4};
        double[] output = SignalPipeline.of(input, 48_000).execute();
        assertArrayEquals(input, output, 1e-15);
        assertNotSame(input, output, "Must return a clone, not the original");
    }

    @Test
    void windowStageAppliesWindow() {
        double[] ones = {1, 1, 1, 1, 1, 1, 1, 1};
        double[] result = SignalPipeline.of(ones, 48_000)
                .window(WindowFunction.HANNING)
                .execute();
        assertEquals(0.0, result[0], 1e-10, "Hanning edge should be 0");
        assertTrue(result[4] > 0.5, "Hanning centre should be high");
    }

    @Test
    void removeDCCentresMean() {
        double[] biased = {10, 12, 10, 12, 10, 12, 10, 12};
        double[] result = SignalPipeline.of(biased, 48_000)
                .removeDC()
                .execute();
        double mean = 0;
        for (double v : result) mean += v;
        mean /= result.length;
        assertEquals(0.0, mean, 1e-10, "Mean should be zero after DC removal");
    }

    @Test
    void normaliseScalesToOne() {
        double[] signal = {0, 5, -10, 3};
        double[] result = SignalPipeline.of(signal, 48_000)
                .normalise()
                .execute();
        double peak = 0;
        for (double v : result) peak = Math.max(peak, Math.abs(v));
        assertEquals(1.0, peak, 1e-15);
    }

    @Test
    void zeroPadPipeline() {
        double[] signal = new double[500];
        double[] result = SignalPipeline.of(signal, 48_000)
                .zeroPad()
                .execute();
        assertEquals(512, result.length);
    }

    @Test
    void customApplyStage() {
        double[] signal = {1, 2, 3, 4};
        double[] result = SignalPipeline.of(signal, 48_000)
                .apply(s -> {
                    for (int i = 0; i < s.length; i++) s[i] *= 2;
                    return s;
                })
                .execute();
        assertArrayEquals(new double[]{2, 4, 6, 8}, result, 1e-15);
    }

    @Test
    void chainingMultipleStages() {
        double[] signal = {10, 20, 30, 40};
        double[] result = SignalPipeline.of(signal, 48_000)
                .removeDC()
                .normalise()
                .execute();

        // Mean should be 0
        double mean = 0;
        for (double v : result) mean += v;
        mean /= result.length;
        assertEquals(0.0, mean, 1e-10);

        // Peak should be 1
        double peak = 0;
        for (double v : result) peak = Math.max(peak, Math.abs(v));
        assertEquals(1.0, peak, 1e-10);
    }

    @Test
    void executeToDominantFrequency() {
        int n = 2048;
        double sampleRate = 48_000;
        double freq = 1000.0;
        double[] signal = new double[n];
        for (int i = 0; i < n; i++) {
            signal[i] = Math.cos(2.0 * Math.PI * freq * i / sampleRate);
        }

        double detected = SignalPipeline.of(signal, sampleRate)
                .executeToDominantFrequency();
        assertEquals(freq, detected, sampleRate / n);
    }

    @Test
    void nullSignalThrows() {
        assertThrows(NullPointerException.class,
                () -> SignalPipeline.of(null, 48_000));
    }

    @Test
    void invalidSampleRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SignalPipeline.of(new double[]{1}, -100));
    }

    @Test
    void pipelineWithNaNInputProducesZeros() {
        double[] signal = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        double[] result = SignalPipeline.of(signal, 48000)
                .removeDC()
                .normalise()
                .execute();
        for (double v : result) {
            assertEquals(0.0, v, 1e-15);
        }
    }

    // -------- test tone validation with pipeline ---------------------------

    @Test
    void pipelineWithTestTone440Hz() {
        int n = 2048;
        double sampleRate = 48_000;
        double freq = 440.0;
        double[] tone = TestToneGenerator.generateSineWaveSamples(freq, sampleRate, n);

        double detected = SignalPipeline.of(tone, sampleRate)
                .window(WindowFunction.HAMMING)
                .removeDC()
                .executeToDominantFrequency();

        assertEquals(freq, detected, sampleRate / n,
                "Pipeline with windowing should detect test tone");
    }

    @Test
    void pipelineTestToneWithMultipleStages() {
        int n = 4096;
        double sampleRate = 48_000;
        double freq = 1000.0;
        double[] tone = TestToneGenerator.generateSineWaveSamples(freq, sampleRate, n);

        double detected = SignalPipeline.of(tone, sampleRate)
                .window(WindowFunction.HAMMING)
                .removeDC()
                .zeroPad()
                .executeToDominantFrequency();

        assertEquals(freq, detected, 20.0,
                "Multi-stage pipeline should detect 1000 Hz test tone");
    }

    @Test
    void powerSpectrumWithTestTone() {
        int n = 2048;
        double sampleRate = 48_000;
        double freq = 8000.0;
        double[] tone = TestToneGenerator.generateSineWaveSamples(freq, sampleRate, n);

        double[] powerDb = SignalPipeline.of(tone, sampleRate)
                .window(WindowFunction.HAMMING)
                .executeToPowerSpectrumDB(0.776);

        // Find peak
        int peakBin = 0;
        double peakDb = powerDb[0];
        for (int i = 1; i < powerDb.length; i++) {
            if (powerDb[i] > peakDb) {
                peakDb = powerDb[i];
                peakBin = i;
            }
        }

        double[] freqs = DSP.frequencyBins(n, sampleRate);
        double peakFreq = freqs[peakBin];
        assertEquals(freq, peakFreq, 20.0,
                "Power spectrum peak should be at test tone frequency");
    }
}
