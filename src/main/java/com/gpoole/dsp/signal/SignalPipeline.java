package com.gpoole.dsp.signal;

import com.gpoole.dsp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A fluent, immutable-style builder for chaining DSP operations.
 * <p>
 * Each method records a processing stage; calling {@link #execute()} runs them
 * in order and returns the final result. The original signal is never modified.
 * </p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * double[] filtered = SignalPipeline.of(rawSamples, 48_000)
 *         .window(WindowFunction.HAMMING)
 *         .removeDC()
 *         .apply(s -> Arrays.stream(s).map(v -> v * 2.0).toArray())
 *         .execute();
 * }</pre>
 *
 * @since 1.1
 */
public final class SignalPipeline {

    private final double[] signal;
    private final double samplingRate;
    private final List<UnaryOperator<double[]>> stages;

    private SignalPipeline(double[] signal, double samplingRate, List<UnaryOperator<double[]>> stages) {
        this.signal = signal.clone();
        this.samplingRate = samplingRate;
        this.stages = new ArrayList<>(stages);
    }

    /**
     * Start a new pipeline from the given signal and sampling rate.
     *
     * @param signal       time-domain samples (cloned internally)
     * @param samplingRate sampling rate in Hz
     * @return a new pipeline builder
     */
    public static SignalPipeline of(double[] signal, double samplingRate) {
        Preconditions.checkArrayLength(signal, 1, "signal");
        Preconditions.checkSampleRate(samplingRate);
        return new SignalPipeline(signal, samplingRate, new ArrayList<>());
    }

    // -------- Built-in stages --------------------------------------------

    /**
     * Apply a window function.
     *
     * @param window the window to apply
     * @return this pipeline (for chaining)
     */
    public SignalPipeline window(WindowFunction window) {
        Preconditions.checkNotNull(window, "window");
        stages.add(window::apply);
        return this;
    }

    /**
     * Remove the DC offset (mean) from the signal.
     *
     * @return this pipeline
     */
    public SignalPipeline removeDC() {
        stages.add(s -> {
            double mean = 0;
            for (double v : s) mean += v;
            mean /= s.length;
            double dc = mean;
            for (int i = 0; i < s.length; i++) {
                s[i] -= dc;
            }
            return sanitise(s);
        });
        return this;
    }

    /**
     * Normalise the signal so the absolute peak equals 1.0.
     *
     * @return this pipeline
     */
    public SignalPipeline normalise() {
        stages.add(s -> {
            double peak = 0;
            for (double v : s) peak = Math.max(peak, Math.abs(v));
            if (peak > 0) {
                for (int i = 0; i < s.length; i++) s[i] /= peak;
            }
            return sanitise(s);
        });
        return this;
    }

    /**
     * Zero-pad the signal to the next power of two.
     *
     * @return this pipeline
     */
    public SignalPipeline zeroPad() {
        stages.add(DSP::zeroPadToPowerOfTwo);
        return this;
    }

    /**
     * Add an arbitrary custom transform stage.
     *
     * @param transform function that receives and returns a {@code double[]}
     * @return this pipeline
     */
    public SignalPipeline apply(UnaryOperator<double[]> transform) {
        Preconditions.checkNotNull(transform, "transform");
        stages.add(transform);
        return this;
    }

    // -------- Terminal operations -----------------------------------------

    /**
     * Execute all recorded stages sequentially and return the resulting signal.
     *
     * @return processed signal
     */
    public double[] execute() {
        double[] result = signal.clone();
        for (UnaryOperator<double[]> stage : stages) {
            result = stage.apply(result);
        }
        return result;
    }

    /**
     * Execute all stages, then compute and return the power spectrum (dB).
     *
     * @param refLevel reference level for dB conversion (e.g. 0.776)
     * @return power spectrum in dB
     */
    public double[] executeToPowerSpectrumDB(double refLevel) {
        return DSP.powerSpectrumDB(execute(), refLevel);
    }

    /**
     * Execute all stages, then detect and return the dominant frequency.
     *
     * @return dominant frequency in Hz
     */
    public double executeToDominantFrequency() {
        return DSP.dominantFrequency(execute(), samplingRate, WindowFunction.RECTANGULAR);
    }

    private static double[] sanitise(double[] s) {
        if (s.length > 0 && java.util.Arrays.stream(s).anyMatch(v -> !Double.isFinite(v))) {
            java.util.Arrays.fill(s, 0.0);
        }
        return s;
    }

    /**
     * Return the sampling rate configured for this pipeline.
     *
     * @return sampling rate in Hz
     */
    public double getSamplingRate() {
        return samplingRate;
    }
}
