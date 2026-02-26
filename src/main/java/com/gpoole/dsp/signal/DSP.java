package com.gpoole.dsp.signal;

import com.gpoole.dsp.util.Preconditions;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

/**
 * Single entry-point with static helpers for the most common DSP tasks.
 * <p>
 * All methods are stateless and thread-safe. They operate on plain
 * {@code double[]} arrays so callers are never forced to learn a framework API
 * just to compute a power spectrum.
 * </p>
 *
 * <p>Example – compute a power spectrum in three lines:</p>
 * <pre>{@code
 * double[] windowed = WindowFunction.HAMMING.apply(samples);
 * double[] power    = DSP.powerSpectrum(windowed);
 * double[] freqs    = DSP.frequencyBins(windowed.length, 48_000);
 * }</pre>
 *
 * @since 1.1
 */
public final class DSP {

    private static final FastFourierTransformer FFT =
            new FastFourierTransformer(DftNormalization.STANDARD);

    private DSP() {
        // utility class
    }

    // -------- FFT helpers ------------------------------------------------

    /**
     * Compute the magnitude spectrum of a real signal.
     * <p>Only the first {@code N/2 + 1} bins (positive frequencies) are returned.</p>
     *
     * @param signal time-domain samples (length is zero-padded to the next power of 2)
     * @return magnitude values for bins 0 … N/2
     */
    public static double[] magnitudeSpectrum(double[] signal) {
        Preconditions.checkArrayLength(signal, 1, "signal");
        double[] padded = zeroPadToPowerOfTwo(signal);
        Complex[] fft = FFT.transform(padded, TransformType.FORWARD);
        int half = fft.length / 2 + 1;
        double[] mag = new double[half];
        for (int i = 0; i < half; i++) {
            mag[i] = fft[i].abs();
        }
        return mag;
    }

    /**
     * Compute the power spectrum (magnitude²) of a real signal.
     *
     * @param signal time-domain samples
     * @return power values for bins 0 … N/2
     */
    public static double[] powerSpectrum(double[] signal) {
        double[] mag = magnitudeSpectrum(signal);
        for (int i = 0; i < mag.length; i++) {
            mag[i] = mag[i] * mag[i];
        }
        return mag;
    }

    /**
     * Compute the power spectrum in decibels (dBFS) relative to full-scale.
     *
     * @param signal   time-domain samples
     * @param refLevel reference level (e.g. 0.776 for dBu, or 1.0 for dBFS)
     * @return dB values for bins 0 … N/2
     */
    public static double[] powerSpectrumDB(double[] signal, double refLevel) {
        Preconditions.checkArgument(refLevel > 0, "refLevel must be > 0");
        double[] mag = magnitudeSpectrum(signal);
        double[] db = new double[mag.length];
        for (int i = 0; i < mag.length; i++) {
            db[i] = mag[i] > 0 ? 20.0 * FastMath.log10(mag[i] / refLevel) : -120.0;
        }
        return db;
    }

    /**
     * Return the centre frequency (Hz) for each FFT bin.
     *
     * @param fftLength    the FFT length (power-of-2 padded length)
     * @param samplingRate sampling rate in Hz
     * @return array of frequencies, length = fftLength / 2 + 1
     */
    public static double[] frequencyBins(int fftLength, double samplingRate) {
        Preconditions.checkPowerOfTwo(fftLength);
        Preconditions.checkSampleRate(samplingRate);
        int half = fftLength / 2 + 1;
        double[] bins = new double[half];
        for (int i = 0; i < half; i++) {
            bins[i] = i * samplingRate / fftLength;
        }
        return bins;
    }

    // -------- Peak / frequency detection --------------------------------

    /**
     * Find the dominant frequency in a real signal using FFT peak detection.
     * <p>
     * Applies the given window, performs FFT, and returns the frequency of the
     * bin with the highest magnitude above a noise threshold (3× mean magnitude).
     * </p>
     *
     * @param signal       time-domain samples
     * @param samplingRate sampling rate in Hz
     * @param window       window function to apply (use {@link WindowFunction#RECTANGULAR} for none)
     * @return dominant frequency in Hz, or 0.0 if no peak above the noise floor
     */
    public static double dominantFrequency(double[] signal, double samplingRate, WindowFunction window) {
        Preconditions.checkArrayLength(signal, 2, "signal");
        Preconditions.checkSampleRate(samplingRate);
        Preconditions.checkNotNull(window, "window");

        double[] windowed = window.applyCopy(signal);
        double[] padded = zeroPadToPowerOfTwo(windowed);
        Complex[] fft = FFT.transform(padded, TransformType.FORWARD);

        int half = fft.length / 2;
        double maxMag = 0;
        int maxIdx = -1;
        double sum = 0;

        for (int i = 1; i < half; i++) {
            double mag = fft[i].abs();
            sum += mag;
            if (mag > maxMag) {
                maxMag = mag;
                maxIdx = i;
            }
        }

        double mean = sum / (half - 1);
        double threshold = mean * 3.0;

        if (maxIdx > 0 && maxMag > threshold) {
            return maxIdx * samplingRate / fft.length;
        }
        return 0.0;
    }

    /**
     * Convenience overload that defaults to a {@link WindowFunction#HAMMING} window.
     *
     * @param signal       time-domain samples
     * @param samplingRate sampling rate in Hz
     * @return dominant frequency in Hz
     */
    public static double dominantFrequency(double[] signal, double samplingRate) {
        return dominantFrequency(signal, samplingRate, WindowFunction.HAMMING);
    }

    // -------- Utilities --------------------------------------------------

    /**
     * Zero-pad an array to the next power of two if it isn't one already.
     *
     * @param data input array
     * @return padded array (or the original if it is already a power of 2)
     */
    public static double[] zeroPadToPowerOfTwo(double[] data) {
        Preconditions.checkNotNull(data, "data");
        int n = data.length;
        int padded = 1;
        while (padded < n) {
            padded <<= 1;
        }
        if (padded == n) {
            return data;
        }
        double[] out = new double[padded];
        System.arraycopy(data, 0, out, 0, n);
        return out;
    }

    /**
     * Convert byte pairs (big-endian, signed 16-bit) to normalised {@code double} samples.
     *
     * @param bytes      raw audio bytes
     * @param channels   number of interleaved channels (1 = mono, 2 = stereo)
     * @param fftSize    number of output samples to produce
     * @param bytesPerSample bytes per single-channel sample (typically 2 for 16-bit)
     * @return mono {@code double} samples averaged across channels
     */
    public static double[] bytesToSamples(byte[] bytes, int channels, int fftSize, int bytesPerSample) {
        Preconditions.checkNotNull(bytes, "bytes");
        Preconditions.checkArgument(channels >= 1 && channels <= 2, "channels must be 1 or 2");
        Preconditions.checkArgument(fftSize > 0, "fftSize must be > 0");

        double[] samples = new double[fftSize];
        for (int i = 0; i < fftSize; i++) {
            if (channels == 2) {
                int left = (bytes[i * bytesPerSample * 2] << 8)
                        | (bytes[i * bytesPerSample * 2 + 1] & 0xFF);
                int right = (bytes[i * bytesPerSample * 2 + bytesPerSample] << 8)
                        | (bytes[i * bytesPerSample * 2 + bytesPerSample + 1] & 0xFF);
                samples[i] = (left + right) / 2.0;
            } else {
                samples[i] = (bytes[i * bytesPerSample] << 8)
                        | (bytes[i * bytesPerSample + 1] & 0xFF);
            }
        }
        return samples;
    }
}
