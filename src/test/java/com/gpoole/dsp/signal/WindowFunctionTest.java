package com.gpoole.dsp.signal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WindowFunction} – verifies shape, symmetry, and edge behaviour
 * for Hamming window.
 */
class WindowFunctionTest {

    private static final int SIZE = 512;

    @Test
    void windowLengthMatchesInput() {
        double[] coefficients = WindowFunction.HAMMING.getCoefficients(SIZE);
        assertEquals(SIZE, coefficients.length);
    }

    @Test
    void windowCoefficientsAreFinite() {
        double[] coefficients = WindowFunction.HAMMING.getCoefficients(SIZE);
        for (double c : coefficients) {
            assertTrue(Double.isFinite(c), "Coefficient must be finite: " + c);
        }
    }

    @Test
    void hammingGoldenValues() {
        double[] w = WindowFunction.HAMMING.getCoefficients(8);
        double[] expected = {
            0.0800, 0.2532, 0.6424, 0.9544,
            0.9544, 0.6424, 0.2532, 0.0800
        };
        for (int i = 0; i < w.length; i++) {
            assertEquals(expected[i], w[i], 0.01,
                    "Hamming at index " + i);
        }
    }

    @Test
    void hammingEdgeValues() {
        double[] w = WindowFunction.HAMMING.getCoefficients(SIZE);
        // Hamming edges should be approximately 0.08
        assertEquals(0.08, w[0], 0.01, "Hamming edge ≈ 0.08");
        assertEquals(0.08, w[SIZE - 1], 0.01, "Hamming edge ≈ 0.08");
    }

    @Test
    void hammingCenter() {
        double[] w = WindowFunction.HAMMING.getCoefficients(SIZE);
        // Centre should be ≈ 1.0
        assertTrue(w[SIZE / 2] > 0.95, "Hamming centre should be near 1.0");
    }

    @Test
    void windowIsSymmetric() {
        double[] w = WindowFunction.HAMMING.getCoefficients(SIZE);
        for (int i = 0; i < SIZE / 2; i++) {
            assertEquals(w[i], w[SIZE - 1 - i], 1e-12,
                    "HAMMING must be symmetric at index " + i);
        }
    }

    @Test
    void applyModifiesInPlace() {
        double[] original = {1.0, 1.0, 1.0, 1.0};
        double[] result = WindowFunction.HAMMING.apply(original);
        assertSame(original, result, "apply() should return the same array");
        assertNotEquals(1.0, result[0], "Edge should be reduced by Hamming");
    }

    @Test
    void applyCopyDoesNotModifyOriginal() {
        double[] original = {1.0, 1.0, 1.0, 1.0};
        double[] copy = WindowFunction.HAMMING.applyCopy(original);
        assertEquals(1.0, original[0], "Original must not be modified");
        assertNotEquals(1.0, copy[0], "Copy should be windowed");
    }

    @Test
    void emptyArrayThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> WindowFunction.HAMMING.apply(new double[0]));
    }

    @Test
    void nullArrayThrows() {
        assertThrows(NullPointerException.class,
                () -> WindowFunction.HAMMING.apply(null));
    }
}
