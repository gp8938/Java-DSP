package com.gpoole.dsp.signal;

/**
 * Utility for generating test tones (sine waves) for DSP testing.
 * <p>
 * Produces known-frequency signals for validating FFT analysis, frequency detection,
 * and other DSP operations against ground truth.
 * </p>
 */
public final class TestToneGenerator {

    private TestToneGenerator() {
        // utility class
    }

    /**
     * Generate a sine wave at a specified frequency.
     *
     * @param frequency       target frequency in Hz
     * @param sampleRate      sampling rate in Hz
     * @param durationSeconds duration of the tone in seconds
     * @param amplitude       peak amplitude (typically 0.0 to 1.0)
     * @return array of samples representing the sine wave
     */
    public static double[] generateSineWave(double frequency, double sampleRate, double durationSeconds, double amplitude) {
        int numSamples = (int) (sampleRate * durationSeconds);
        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }
        return samples;
    }

    /**
     * Generate a sine wave with unit amplitude (1.0).
     *
     * @param frequency       target frequency in Hz
     * @param sampleRate      sampling rate in Hz
     * @param durationSeconds duration of the tone in seconds
     * @return array of samples representing the sine wave
     */
    public static double[] generateSineWave(double frequency, double sampleRate, double durationSeconds) {
        return generateSineWave(frequency, sampleRate, durationSeconds, 1.0);
    }

    /**
     * Generate a sine wave with specified number of samples.
     *
     * @param frequency  target frequency in Hz
     * @param sampleRate sampling rate in Hz
     * @param numSamples number of samples to generate
     * @param amplitude  peak amplitude (typically 0.0 to 1.0)
     * @return array of samples representing the sine wave
     */
    public static double[] generateSineWaveSamples(double frequency, double sampleRate, int numSamples, double amplitude) {
        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }
        return samples;
    }

    /**
     * Generate a sine wave with specified number of samples and unit amplitude.
     *
     * @param frequency  target frequency in Hz
     * @param sampleRate sampling rate in Hz
     * @param numSamples number of samples to generate
     * @return array of samples representing the sine wave
     */
    public static double[] generateSineWaveSamples(double frequency, double sampleRate, int numSamples) {
        return generateSineWaveSamples(frequency, sampleRate, numSamples, 1.0);
    }
}
