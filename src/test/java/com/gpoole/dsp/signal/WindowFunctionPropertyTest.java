package com.gpoole.dsp.signal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WindowFunctionPropertyTest {

    static Stream<Arguments> windowAndSize() {
        int[] sizes = {2, 3, 4, 7, 8, 15, 16, 31, 32, 63, 64, 127, 128, 255, 256, 511, 512, 1023, 1024, 2047, 2048};
        return Arrays.stream(WindowFunction.values())
                .flatMap(wf -> Arrays.stream(sizes).mapToObj(len -> Arguments.of(wf, len)));
    }

    static Stream<Arguments> windowAndSamples() {
        return Stream.of(
                Arguments.of(WindowFunction.RECTANGULAR, new double[]{1.0, 2.0, 3.0, 4.0}),
                Arguments.of(WindowFunction.HAMMING, new double[]{1.0, 2.0, 3.0, 4.0, 5.0}),
                Arguments.of(WindowFunction.HANNING, new double[]{0.5, -1.0, 0.5}),
                Arguments.of(WindowFunction.BLACKMAN, new double[]{10.0, 20.0, 30.0, 40.0, 50.0, 60.0}),
                Arguments.of(WindowFunction.FLAT_TOP, new double[]{-1.0, -2.0, -3.0, -4.0})
        );
    }

    @ParameterizedTest
    @MethodSource("windowAndSize")
    void coefficientsAreFinite(WindowFunction wf, int len) {
        double[] coeffs = wf.getCoefficients(len);
        assertEquals(len, coeffs.length);
        for (double c : coeffs) {
            assertTrue(Double.isFinite(c));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 8, 16, 32, 64, 128, 256, 512})
    void rectangularIsAllOnes(int len) {
        double[] coeffs = WindowFunction.RECTANGULAR.getCoefficients(len);
        for (double c : coeffs) {
            assertEquals(1.0, c, 1e-15);
        }
    }

    @ParameterizedTest
    @MethodSource("windowAndSize")
    void windowsAreSymmetric(WindowFunction wf, int len) {
        double[] coeffs = wf.getCoefficients(len);
        for (int i = 0; i < len / 2; i++) {
            assertEquals(coeffs[i], coeffs[len - 1 - i], 1e-12,
                    wf.name() + " at index " + i);
        }
    }

    @ParameterizedTest
    @MethodSource("windowAndSamples")
    void applyCopyDoesNotModifyOriginal(WindowFunction wf, double[] samples) {
        double[] original = samples.clone();
        wf.applyCopy(samples);
        assertArrayEquals(original, samples, 1e-15);
    }
}
