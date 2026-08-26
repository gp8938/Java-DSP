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
     * Generate a unit-amplitude sine wave with the specified number of samples.
     *
     * @param frequency  target frequency in Hz
     * @param sampleRate sampling rate in Hz
     * @param numSamples number of samples to generate
     * @return array of samples representing the sine wave
     */
    public static double[] generateSineWaveSamples(double frequency, double sampleRate, int numSamples) {
        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }
        return samples;
    }
}
