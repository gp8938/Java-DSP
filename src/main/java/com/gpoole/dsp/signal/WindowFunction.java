package com.gpoole.dsp.signal;

import com.gpoole.dsp.util.Preconditions;

/**
 * Standard window functions for reducing spectral leakage in FFT analysis.
 * <p>
 * A window is multiplied element-wise with the time-domain signal <em>before</em>
 * the FFT. Different windows trade off main-lobe width (frequency resolution)
 * against side-lobe level (spectral leakage).
 * </p>
 *
 * <h3>Quick reference</h3>
 * <table>
 *   <tr><th>Window</th><th>Main-lobe width</th><th>Side-lobe level</th><th>Best for</th></tr>
 *   <tr><td>RECTANGULAR</td><td>narrowest</td><td>-13 dB</td><td>transient / impulse analysis</td></tr>
 *   <tr><td>HAMMING</td><td>medium</td><td>-43 dB</td><td>general-purpose audio</td></tr>
 *   <tr><td>HANNING</td><td>medium</td><td>-31 dB</td><td>smooth spectral analysis</td></tr>
 *   <tr><td>BLACKMAN</td><td>wide</td><td>-58 dB</td><td>high dynamic range</td></tr>
 *   <tr><td>FLAT_TOP</td><td>widest</td><td>-93 dB</td><td>amplitude-accurate measurement</td></tr>
 * </table>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * double[] windowed = WindowFunction.HAMMING.apply(samples);
 * }</pre>
 *
 * @since 1.1
 */
public enum WindowFunction {

    /** No windowing – all coefficients are 1.0. */
    RECTANGULAR {
        @Override
        protected double coefficient(int i, int n) {
            return 1.0;
        }
    },

    /**
     * Hamming window – good general-purpose choice.
     * {@code w(i) = 0.54 − 0.46 · cos(2π · i / (N − 1))}
     */
    HAMMING {
        @Override
        protected double coefficient(int i, int n) {
            return 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
        }
    },

    /**
     * Hann (Hanning) window – smoother roll-off than Hamming.
     * {@code w(i) = 0.5 · (1 − cos(2π · i / (N − 1)))}
     */
    HANNING {
        @Override
        protected double coefficient(int i, int n) {
            return 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (n - 1)));
        }
    },

    /**
     * Blackman window – lower side-lobes, wider main lobe.
     * {@code w(i) = 0.42 − 0.5 · cos(2π · i / (N − 1)) + 0.08 · cos(4π · i / (N − 1))}
     */
    BLACKMAN {
        @Override
        protected double coefficient(int i, int n) {
            double angle = 2.0 * Math.PI * i / (n - 1);
            return 0.42 - 0.5 * Math.cos(angle) + 0.08 * Math.cos(2.0 * angle);
        }
    },

    /**
     * Flat-top window – excellent amplitude accuracy, very wide main lobe.
     * Used primarily for calibration and amplitude measurement.
     */
    FLAT_TOP {
        @Override
        protected double coefficient(int i, int n) {
            double angle = 2.0 * Math.PI * i / (n - 1);
            return 0.21557895
                    - 0.41663158 * Math.cos(angle)
                    + 0.277263158 * Math.cos(2.0 * angle)
                    - 0.083578947 * Math.cos(3.0 * angle)
                    + 0.006947368 * Math.cos(4.0 * angle);
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
        Preconditions.checkArgument(length > 0, "Window length must be > 0, got " + length);
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
        Preconditions.checkNotNull(samples, "samples");
        Preconditions.checkArgument(samples.length > 0, "samples must not be empty");
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
        Preconditions.checkNotNull(samples, "samples");
        double[] copy = samples.clone();
        return apply(copy);
    }
}
