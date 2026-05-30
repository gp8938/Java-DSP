package com.gpoole.dsp.signal;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import static org.junit.jupiter.api.Assertions.*;

class DSPPropertyTest {

    @Property
    void zeroPadToPowerOfTwoAlwaysReturnsPowerOfTwo(
            @ForAll @IntRange(min = 1, max = 5000) int len
    ) {
        double[] data = new double[len];
        // Seed with unique non-zero values to verify copy correctness
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

    @Property
    void magnitudeSpectrumLengthDependsOnNextPow2(
            @ForAll @IntRange(min = 2, max = 4096) int len
    ) {
        double[] signal = new double[len];
        double[] mag = DSP.magnitudeSpectrum(signal);
        int nextPow2 = 1;
        while (nextPow2 < len) nextPow2 <<= 1;
        int expectedMagLen = nextPow2 / 2 + 1;
        assertEquals(expectedMagLen, mag.length);
    }

    @Property
    void powerSpectrumOfSilenceIsZero(
            @ForAll @IntRange(min = 2, max = 4096) int len
    ) {
        double[] silence = new double[len];
        double[] power = DSP.powerSpectrum(silence);
        for (double v : power) {
            assertEquals(0.0, v, 1e-20);
        }
    }
}
