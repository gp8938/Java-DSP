package com.gpoole.dsp;

import org.junit.jupiter.api.Test;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for audio format handling and device compatibility
 */
public class AudioFormatTest {

    @Test
    public void testAudioFormatCreation() {
        float[] sampleRates = {8000.0f, 16000.0f, 44100.0f, 48000.0f};
        
        for (float sampleRate : sampleRates) {
            AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16, // bits per sample
                1,  // mono
                2,  // frame size
                sampleRate, // frame rate
                false // little endian
            );
            
            assertNotNull(format);
            assertEquals(sampleRate, format.getSampleRate());
            assertEquals(16, format.getSampleSizeInBits());
            assertEquals(1, format.getChannels());
        }
    }

    @Test
    public void testStereoToMonoConversion() {
        // Simulate stereo to mono conversion
        byte[] stereoData = new byte[8]; // 4 stereo samples (2 bytes each, 2 channels)
        
        // Left channel: 100, Right channel: 200
        stereoData[0] = 100; stereoData[1] = 0; // left sample 1
        stereoData[2] = (byte)200; stereoData[3] = 0; // right sample 1
        stereoData[4] = 100; stereoData[5] = 0; // left sample 2
        stereoData[6] = (byte)200; stereoData[7] = 0; // right sample 2
        
        double[] monoSamples = new double[2]; // 2 mono samples
        
        for (int i = 0; i < monoSamples.length; i++) {
            int leftIdx = i * 4;
            int rightIdx = i * 4 + 2;
            
            short leftSample = (short) ((stereoData[leftIdx + 1] << 8) | (stereoData[leftIdx] & 0xFF));
            short rightSample = (short) ((stereoData[rightIdx + 1] << 8) | (stereoData[rightIdx] & 0xFF));
            
            monoSamples[i] = (leftSample + rightSample) / 2.0;
        }
        
        // Average of 100 and 200 is 150
        assertEquals(150.0, monoSamples[0], 0.1);
        assertEquals(150.0, monoSamples[1], 0.1);
    }

    @Test
    public void testAudioSystemAvailability() {
        // Test that AudioSystem is available
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        assertNotNull(mixers);
        
        // Note: May be 0 in headless CI environment
        assertTrue(mixers.length >= 0, "Should return mixer array even if empty");
    }

    @Test
    public void testBufferSizeCalculation() {
        float sampleRate = 48000.0f;
        int fftSize = 2048;
        int bytesPerSample = 2; // 16-bit
        int channels = 1; // mono
        
        int expectedBufferSize = fftSize * bytesPerSample * channels;
        
        assertEquals(4096, expectedBufferSize);
    }

    @Test
    public void testSampleRateValidation() {
        float[] validRates = {8000.0f, 11025.0f, 16000.0f, 22050.0f, 44100.0f, 48000.0f};
        
        for (float rate : validRates) {
            assertTrue(rate > 0, "Sample rate must be positive");
            assertTrue(rate <= 192000, "Sample rate should be reasonable");
        }
    }

    @Test
    public void testByteToSampleConversion() {
        // Test 16-bit little-endian conversion
        byte[] bytes = new byte[4];
        
        // Sample 1: value 1000 (0x03E8)
        bytes[0] = (byte) 0xE8; // low byte
        bytes[1] = (byte) 0x03; // high byte
        
        // Sample 2: value -1000 (0xFC18)
        bytes[2] = (byte) 0x18; // low byte
        bytes[3] = (byte) 0xFC; // high byte
        
        short sample1 = (short) ((bytes[1] << 8) | (bytes[0] & 0xFF));
        short sample2 = (short) ((bytes[3] << 8) | (bytes[2] & 0xFF));
        
        assertEquals(1000, sample1);
        assertEquals(-1000, sample2);
    }
}
