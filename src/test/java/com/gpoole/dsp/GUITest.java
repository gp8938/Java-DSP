package com.gpoole.dsp;

import org.junit.jupiter.api.Test;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

public class GUITest {

    @Test
    public void testSyntheticAudioSource() throws Exception {
        // Create a synthetic audio source (sine wave)
        float sampleRate = 48000.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

        // Generate a sine wave
        int durationInSeconds = 1;
        int totalSamples = (int) (sampleRate * durationInSeconds);
        byte[] audioData = new byte[totalSamples * 2]; // 2 bytes per sample for 16-bit audio

        double frequency = 440.0; // A4 note
        for (int i = 0; i < totalSamples; i++) {
            short sample = (short) (Math.sin(2 * Math.PI * frequency * i / sampleRate) * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        InputStream audioStream = new ByteArrayInputStream(audioData);
        AudioInputStream syntheticStream = new AudioInputStream(audioStream, format, totalSamples);

        // Test if the synthetic source is supported
        assertTrue(AudioSystem.isConversionSupported(format, syntheticStream.getFormat()));

        // Simulate processing the synthetic audio source
        TargetDataLine line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();

        byte[] buffer = new byte[1024];
        int bytesRead = syntheticStream.read(buffer);
        assertTrue(bytesRead > 0, "Synthetic audio source should produce data.");

        line.stop();
        line.close();
    }
}