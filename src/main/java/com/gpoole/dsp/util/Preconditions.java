package com.gpoole.dsp.util;

/**
 * Shared validation utility for fail-fast input checking.
 * <p>Every public DSP method should call these helpers at its entry point
 * so callers receive clear, immediate error messages instead of
 * downstream {@link ArrayIndexOutOfBoundsException}s or silent corruption.</p>
 *
 * @since 1.1
 */
public final class Preconditions {

    private Preconditions() {
        // utility class – no instances
    }

    /**
     * Throws {@link IllegalArgumentException} if {@code condition} is {@code false}.
     *
     * @param condition the expression that must be {@code true}
     * @param message   description shown when the check fails
     * @throws IllegalArgumentException if {@code condition} is false
     */
    public static void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws {@link NullPointerException} if {@code obj} is {@code null}.
     *
     * @param obj  the reference to test
     * @param name a human-readable name for the parameter (used in the message)
     * @throws NullPointerException if {@code obj} is null
     */
    public static void checkNotNull(Object obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name + " must not be null");
        }
    }

    /**
     * Validates that an array is non-null and has at least {@code minLength} elements.
     *
     * @param array     the array to validate
     * @param minLength minimum acceptable length (inclusive)
     * @param name      human-readable name for error messages
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if the array is too short
     */
    public static void checkArrayLength(double[] array, int minLength, String name) {
        checkNotNull(array, name);
        checkArgument(array.length >= minLength,
                name + " length must be >= " + minLength + ", got " + array.length);
    }

    /**
     * Validates that {@code n} is a positive power of two.
     *
     * @param n the value to check
     * @throws IllegalArgumentException if {@code n} is not a power of 2
     */
    public static void checkPowerOfTwo(int n) {
        checkArgument(n > 0 && (n & (n - 1)) == 0,
                "Length must be a power of 2, got " + n);
    }

    /**
     * Validates that a sample rate is positive and within a reasonable range.
     *
     * @param sampleRate the sample rate in Hz
     * @throws IllegalArgumentException if outside [1, 384000]
     */
    public static void checkSampleRate(double sampleRate) {
        checkArgument(sampleRate > 0 && sampleRate <= 384_000,
                "Sample rate must be in (0, 384000], got " + sampleRate);
    }
}
