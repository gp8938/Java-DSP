package com.gpoole.dsp.signal;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import static org.junit.jupiter.api.Assertions.*;

class WindowFunctionPropertyTest {

    @Property
    void coefficientsAreFinite(
            @ForAll WindowFunction wf,
            @ForAll @IntRange(min = 2, max = 4096) int len
    ) {
        double[] coeffs = wf.getCoefficients(len);
        assertEquals(len, coeffs.length);
        for (double c : coeffs) {
            assertTrue(Double.isFinite(c));
        }
    }

    @Property
    void rectangularIsAllOnes(
            @ForAll @IntRange(min = 1, max = 512) int len
    ) {
        double[] coeffs = WindowFunction.RECTANGULAR.getCoefficients(len);
        for (double c : coeffs) {
            assertEquals(1.0, c, 1e-15);
        }
    }

    @Property
    void windowsAreSymmetric(
            @ForAll @IntRange(min = 2, max = 512) int len,
            @ForAll WindowFunction wf
    ) {
        double[] coeffs = wf.getCoefficients(len);
        for (int i = 0; i < len / 2; i++) {
            assertEquals(coeffs[i], coeffs[len - 1 - i], 1e-12,
                    wf.name() + " at index " + i);
        }
    }

    @Property
    void applyCopyDoesNotModifyOriginal(
            @ForAll WindowFunction wf,
            @ForAll @Size(min = 2, max = 256) double[] samples
    ) {
        double[] original = samples.clone();
        double[] result = wf.applyCopy(samples);
        assertArrayEquals(original, samples, 1e-15);
    }
}
