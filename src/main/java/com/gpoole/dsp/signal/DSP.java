package com.gpoole.dsp.signal;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;


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
    private static final double DB_MULTIPLIER = 20.0;
    private static final double DB_MIN = -120.0;
    private static final double DOMINANT_FREQ_THRESHOLD_MULTIPLE = 3.0;

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
        Objects.requireNonNull(signal, "signal");
        if (signal.length < 1) {
            throw new IllegalArgumentException("signal length must be >= 1, got " + signal.length);
        }
        for (double v : signal) {
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException("signal contains non-finite value: " + v);
            }
        }
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
        if (refLevel <= 0) {
            throw new IllegalArgumentException("refLevel must be > 0");
        }
        double[] mag = magnitudeSpectrum(signal);
        double[] db = new double[mag.length];
        for (int i = 0; i < mag.length; i++) {
            db[i] = mag[i] > 0 ? DB_MULTIPLIER * Math.log10(mag[i] / refLevel) : DB_MIN;
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
        if (fftLength <= 0 || (fftLength & (fftLength - 1)) != 0) {
            throw new IllegalArgumentException("Length must be a power of 2, got " + fftLength);
        }
        if (samplingRate <= 0 || samplingRate > 384_000) {
            throw new IllegalArgumentException("Sample rate must be in (0, 384000], got " + samplingRate);
        }
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
     * Applies a Hamming window, performs FFT, and returns the frequency of the
     * bin with the highest magnitude above a noise threshold (3× mean magnitude).
     * </p>
     *
     * @param signal       time-domain samples
     * @param samplingRate sampling rate in Hz
     * @return dominant frequency in Hz, or 0.0 if no peak above the noise floor
     */
    public static double dominantFrequency(double[] signal, double samplingRate) {
        Objects.requireNonNull(signal, "signal");
        if (signal.length < 2) {
            throw new IllegalArgumentException("signal length must be >= 2, got " + signal.length);
        }
        for (double v : signal) {
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException("signal contains non-finite value: " + v);
            }
        }
        if (samplingRate <= 0 || samplingRate > 384_000) {
            throw new IllegalArgumentException("Sample rate must be in (0, 384000], got " + samplingRate);
        }

        double[] windowed = WindowFunction.HAMMING.applyCopy(signal);
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
        double threshold = mean * DOMINANT_FREQ_THRESHOLD_MULTIPLE;

        if (maxIdx > 0 && maxMag > threshold) {
            return maxIdx * samplingRate / fft.length;
        }
        return 0.0;
    }

    // -------- Utilities --------------------------------------------------

    /**
     * Zero-pad an array to the next power of two if it isn't one already.
     *
     * @param data input array
     * @return padded array (or the original if it is already a power of 2)
     */
    public static double[] zeroPadToPowerOfTwo(double[] data) {
        Objects.requireNonNull(data, "data");
        int n = data.length;
        int padded = Integer.highestOneBit(n);
        if (padded < n) {
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
     * Convert byte pairs (signed 16-bit) to normalised {@code double} samples,
     * with configurable endianness.
     *
     * @param bytes      raw audio bytes
     * @param channels   number of interleaved channels (1 = mono, 2 = stereo)
     * @param fftSize    number of output samples to produce
     * @param bytesPerSample bytes per single-channel sample (typically 2 for 16-bit)
     * @param bigEndian  true for big-endian byte order, false for little-endian
     * @return mono {@code double} samples averaged across channels
     */
    public static double[] bytesToSamples(byte[] bytes, int channels, int fftSize, int bytesPerSample, boolean bigEndian) {
        Objects.requireNonNull(bytes, "bytes");
        if (channels < 1 || channels > 2) {
            throw new IllegalArgumentException("channels must be 1 or 2");
        }
        if (fftSize <= 0) {
            throw new IllegalArgumentException("fftSize must be > 0");
        }
        if (bytesPerSample != 2) {
            throw new IllegalArgumentException("Only 16-bit (2-byte) samples are supported, got " + bytesPerSample);
        }
        if (bytes.length < fftSize * channels * bytesPerSample) {
            throw new IllegalArgumentException("byte array too small: need " + (fftSize * channels * bytesPerSample) + " bytes, got " + bytes.length);
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        double[] samples = new double[fftSize];
        for (int i = 0; i < fftSize; i++) {
            if (channels == 2) {
                short left = buffer.getShort();
                short right = buffer.getShort();
                samples[i] = (left + right) / 2.0;
            } else {
                samples[i] = buffer.getShort();
            }
        }
        return samples;
    }
}
