package com.gpoole.dsp.signal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DSPPropertyTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 7, 8, 15, 16, 31, 32, 63, 64, 100, 127, 128,
            255, 256, 511, 512, 1000, 1023, 1024, 2048, 3000, 4096, 5000})
    void zeroPadToPowerOfTwoAlwaysReturnsPowerOfTwo(int len) {
        double[] data = new double[len];
        for (int i = 0; i < len; i++) {
            data[i] = i + 1.0;
        }
        double[] padded = DSP.zeroPadToPowerOfTwo(data);
        int n = padded.length;
        assertTrue(n > 0 && (n & (n - 1)) == 0,
                "Length " + n + " must be a power of 2");
        assertTrue(n >= len);
        for (int i = 0; i < len; i++) {
            assertEquals(i + 1.0, padded[i], 1e-15,
                    "Original value preserved at index " + i);
        }
        for (int i = len; i < n; i++) {
            assertEquals(0.0, padded[i], 1e-15,
                    "Padded zeros at index " + i);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 7, 8, 15, 16, 31, 32, 63, 64, 100, 127, 128,
            255, 256, 511, 512, 1000, 1023, 1024, 2048, 3000, 4096})
    void magnitudeSpectrumLengthDependsOnNextPow2(int len) {
        double[] signal = new double[len];
        double[] mag = DSP.magnitudeSpectrum(signal);
        int nextPow2 = 1;
        while (nextPow2 < len) nextPow2 <<= 1;
        int expectedMagLen = nextPow2 / 2 + 1;
        assertEquals(expectedMagLen, mag.length);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 7, 8, 15, 16, 31, 32, 63, 64, 100, 127, 128,
            255, 256, 511, 512, 1000, 1023, 1024, 2048, 3000, 4096})
    void powerSpectrumOfSilenceIsZero(int len) {
        double[] silence = new double[len];
        double[] power = DSP.powerSpectrum(silence);
        for (double v : power) {
            assertEquals(0.0, v, 1e-20);
        }
    }
}
