package com.gpoole.dsp.signal;

import java.util.Objects;

/**
 * Hamming window – good general-purpose choice for reducing spectral leakage.
 * {@code w(i) = 0.54 − 0.46 · cos(2π · i / (N − 1))}
 *
 * <p>Usage:</p>
 * <pre>{@code
 * double[] windowed = WindowFunction.HAMMING.apply(samples);
 * }</pre>
 *
 * @since 1.1
 */
public enum WindowFunction {

    /**
     * Hamming window – good general-purpose choice.
     * {@code w(i) = 0.54 − 0.46 · cos(2π · i / (N − 1))}
     */
    HAMMING {
        @Override
        protected double coefficient(int i, int n) {
            return 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
        }
    };

    /**
     * Compute the window coefficient for index {@code i} in a window of length {@code n}.
     *
     * @param i sample index (0-based)
     * @param n total window length
     * @return window coefficient in [0, 1] (approximately)
     */
    protected abstract double coefficient(int i, int n);

    /**
     * Generate the full window-coefficient array of the given length.
     *
     * @param length number of samples (must be &gt; 0)
     * @return array of window coefficients
     * @throws IllegalArgumentException if {@code length} &le; 0
     */
    public double[] getCoefficients(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Window length must be > 0, got " + length);
        }
        double[] w = new double[length];
        for (int i = 0; i < length; i++) {
            w[i] = coefficient(i, length);
        }
        return w;
    }

    /**
     * Apply this window to the given samples <b>in place</b>.
     *
     * @param samples the time-domain samples to window (modified in place)
     * @return the same array, for chaining convenience
     * @throws NullPointerException     if {@code samples} is null
     * @throws IllegalArgumentException if the array is empty
     */
    public double[] apply(double[] samples) {
        Objects.requireNonNull(samples, "samples");
        if (samples.length == 0) {
            throw new IllegalArgumentException("samples must not be empty");
        }
        int n = samples.length;
        for (int i = 0; i < n; i++) {
            samples[i] *= coefficient(i, n);
        }
        return samples;
    }

    /**
     * Apply this window to a <b>copy</b> of the given samples, leaving the original untouched.
     *
     * @param samples the time-domain samples
     * @return a new windowed copy
     */
    public double[] applyCopy(double[] samples) {
        Objects.requireNonNull(samples, "samples");
        double[] copy = samples.clone();
        return apply(copy);
    }
}
