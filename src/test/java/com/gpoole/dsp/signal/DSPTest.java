package com.gpoole.dsp.signal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link DSP} convenience class.
 * <p>
 * "Golden-value" tests compare output against values computed with known-good
 * reference implementations (pure-math sine waves at exact bin frequencies).
 * </p>
 */
class DSPTest {

    // -------- magnitudeSpectrum / powerSpectrum ---------------------------

    @Test
    void magnitudeSpectrumLength() {
        double[] signal = new double[1024];
        double[] mag = DSP.magnitudeSpectrum(signal);
        assertEquals(513, mag.length, "N/2 + 1 bins expected for N=1024");
    }

    @Test
    void powerSpectrumOfSilenceIsZero() {
        double[] silence = new double[256];
        double[] power = DSP.powerSpectrum(silence);
        for (double v : power) {
            assertEquals(0.0, v, 1e-20, "Silence should produce zero power");
        }
    }

    /**
     * A pure cosine at exactly bin k should concentrate its energy in that bin.
     * This is a "golden value" test – the expected peak bin is deterministic.
     */
    @ParameterizedTest(name = "cosine at bin {0} / N={1}")
    @CsvSource({
            "5,  256,  48000",
            "10, 512,  44100",
            "20, 1024, 48000",
            "50, 2048, 96000",
    })
    void peakBinMatchesCosineFrequency(int expectedBin, int n, double sampleRate) {
        double freq = expectedBin * sampleRate / n;
        double[] signal = generateCosine(freq, sampleRate, n);
        double[] mag = DSP.magnitudeSpectrum(signal);

        int peakBin = 0;
        double peakVal = 0;
        for (int i = 0; i < mag.length; i++) {
            if (mag[i] > peakVal) {
                peakVal = mag[i];
                peakBin = i;
            }
        }
        assertEquals(expectedBin, peakBin,
                "Peak bin should match the cosine frequency bin");
    }

    // -------- dominantFrequency ------------------------------------------

    @Test
    void dominantFrequencyDetects440Hz() {
        double sampleRate = 48_000;
        int n = 2048;
        double freq = 440.0;
        double[] signal = generateCosine(freq, sampleRate, n);

        double detected = DSP.dominantFrequency(signal, sampleRate, WindowFunction.RECTANGULAR);
        assertEquals(freq, detected, sampleRate / n,
                "Should detect 440 Hz within one bin resolution");
    }

    @Test
    void dominantFrequencyReturnZeroForSilence() {
        double[] silence = new double[1024];
        assertEquals(0.0, DSP.dominantFrequency(silence, 48_000));
    }

    // -------- frequencyBins ----------------------------------------------

    @Test
    void frequencyBinsFirstAndLast() {
        double[] bins = DSP.frequencyBins(1024, 48_000);
        assertEquals(513, bins.length);
        assertEquals(0.0, bins[0], 1e-10);
        assertEquals(24_000.0, bins[bins.length - 1], 1e-6, "Last bin = Nyquist");
    }

    // -------- zeroPadToPowerOfTwo ----------------------------------------

    @Test
    void zeroPadAlreadyPowerOfTwo() {
        double[] data = new double[512];
        assertSame(data, DSP.zeroPadToPowerOfTwo(data), "No copy needed");
    }

    @Test
    void zeroPadNonPowerOfTwo() {
        double[] data = new double[500];
        data[0] = 42;
        double[] padded = DSP.zeroPadToPowerOfTwo(data);
        assertEquals(512, padded.length);
        assertEquals(42, padded[0]);
        assertEquals(0.0, padded[511]);
    }

    // -------- powerSpectrumDB --------------------------------------------

    @Test
    void powerSpectrumDBSilenceIsFloor() {
        double[] silence = new double[256];
        double[] db = DSP.powerSpectrumDB(silence, 1.0);
        for (double v : db) {
            assertEquals(-120.0, v, 1e-10);
        }
    }

    // -------- bytesToSamples ---------------------------------------------

    @Test
    void bytesToSamplesMono() {
        // 16-bit big-endian: value = 256 → bytes [0x01, 0x00]
        byte[] bytes = {0x01, 0x00, 0x02, 0x00};
        double[] samples = DSP.bytesToSamples(bytes, 1, 2, 2);
        assertEquals(256.0, samples[0], 1e-10);
        assertEquals(512.0, samples[1], 1e-10);
    }

    // -------- validation -------------------------------------------------

    @Test
    void nullSignalThrows() {
        assertThrows(NullPointerException.class,
                () -> DSP.magnitudeSpectrum(null));
    }

    @Test
    void invalidSampleRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DSP.dominantFrequency(new double[256], -1));
    }

    // -------- test tone generation and detection --------------------------

    @ParameterizedTest(name = "test tone {0} Hz at {1} Hz sample rate")
    @CsvSource({
            "440,  48000",
            "1000, 48000",
            "440,  44100",
            "1000, 44100",
            "8000, 48000",
    })
    void testToneDetection(double testFreq, double sampleRate) {
        int numSamples = (int) (sampleRate * 0.1); // 100ms of audio
        double[] tone = TestToneGenerator.generateSineWaveSamples(testFreq, sampleRate, numSamples);

        double detected = DSP.dominantFrequency(tone, sampleRate);
        double tolerance = 5.0; // ±5 Hz tolerance
        assertEquals(testFreq, detected, tolerance,
                "Should detect test tone frequency within 5 Hz");
    }

    @Test
    void testTone440HzPowerSpectrum() {
        double sampleRate = 48_000;
        int numSamples = 4096;
        double freq = 440.0;
        double[] tone = TestToneGenerator.generateSineWaveSamples(freq, sampleRate, numSamples);

        double[] power = DSP.powerSpectrum(tone);
        double[] freqs = DSP.frequencyBins(numSamples, sampleRate);

        // Find peak bin
        int peakBin = 0;
        double peakPower = 0;
        for (int i = 0; i < power.length; i++) {
            if (power[i] > peakPower) {
                peakPower = power[i];
                peakBin = i;
            }
        }

        double expectedFreq = freqs[peakBin];
        // Tolerance accounts for FFT bin resolution (48000/4096 ≈ 11.7 Hz per bin)
        assertEquals(freq, expectedFreq, 15.0,
                "Peak in power spectrum should correspond to test tone frequency");
    }

    @Test
    void testToneMultipleFrequencies() {
        double[] frequencies = {100, 440, 1000, 5000, 10000};
        double sampleRate = 48_000;
        int numSamples = 2048;

        for (double freq : frequencies) {
            double[] tone = TestToneGenerator.generateSineWaveSamples(freq, sampleRate, numSamples);
            double detected = DSP.dominantFrequency(tone, sampleRate);
            assertEquals(freq, detected, 10.0,
                    "Should detect " + freq + " Hz tone");
        }
    }

    // -------- helpers ----------------------------------------------------

    private static double[] generateCosine(double freq, double sampleRate, int n) {
        double[] signal = new double[n];
        for (int i = 0; i < n; i++) {
            signal[i] = Math.cos(2.0 * Math.PI * freq * i / sampleRate);
        }
        return signal;
    }
}
