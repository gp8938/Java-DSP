/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gpoole.dsp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Line;

/**
 *
 * @author geoff
 */
public class GUI extends javax.swing.JFrame {

    /**
     * Creates new form GUI
     */
    boolean open = true;
    private boolean isCapturing = false;
    private Thread captureThread = null;
    byte[] AudioByteBuffer;
    double[] input;
    long fftcalctime;
    FastFourierTransformer fft;
    XYLineChart xy = new XYLineChart("Chart");

    byte bits = 16;
    byte channels = 2;

    ;
    public GUI() {
        initComponents();
        this.bytesPerSampleSpinner.setValue(2);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated AudioByteBuffer the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setTitle("Digital Signal Processing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new java.awt.Dimension(450, 500));

        // Set layout for the main frame
        setLayout(new BorderLayout(10, 10));

        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Add components to the main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Audio Source:"), gbc);

        gbc.gridx = 1;
        audioSourceComboBox = new JComboBox<>();
        mainPanel.add(audioSourceComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Sampling Frequency:"), gbc);

        gbc.gridx = 1;
        samplingFrequencyComboBox = new JComboBox<>();
        mainPanel.add(samplingFrequencyComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("FFT Size:"), gbc);

        gbc.gridx = 1;
        fftSizeComboBox = new JComboBox<>(new String[]{"16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768", "65536"});
        mainPanel.add(fftSizeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Main Frequency:"), gbc);

        gbc.gridx = 1;
        mainFrequencyField = createReadOnlyTextField();
        mainPanel.add(mainFrequencyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("FFT Vendor:"), gbc);

        gbc.gridx = 1;
        fftVendorField = createReadOnlyTextField();
        mainPanel.add(fftVendorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("FFT Processor:"), gbc);

        gbc.gridx = 1;
        fftProcessorField = createReadOnlyTextField();
        mainPanel.add(fftProcessorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("OpenCL Version:"), gbc);

        gbc.gridx = 1;
        openCLVersionField = createReadOnlyTextField();
        mainPanel.add(openCLVersionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        mainPanel.add(new JLabel("Driver Version:"), gbc);

        gbc.gridx = 1;
        driverVersionField = createReadOnlyTextField();
        mainPanel.add(driverVersionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        mainPanel.add(new JLabel("Channels:"), gbc);

        gbc.gridx = 1;
        channelsField = createReadOnlyTextField();
        mainPanel.add(channelsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        mainPanel.add(new JLabel("Bits Per Sample:"), gbc);

        gbc.gridx = 1;
        bitsPerSampleField = createReadOnlyTextField();
        mainPanel.add(bitsPerSampleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        mainPanel.add(new JLabel("Execution Period:"), gbc);

        gbc.gridx = 1;
        executionPeriodField = createReadOnlyTextField();
        mainPanel.add(executionPeriodField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 11;
        mainPanel.add(new JLabel("Sample Period:"), gbc);

        gbc.gridx = 1;
        samplePeriodField = createReadOnlyTextField();
        mainPanel.add(samplePeriodField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 12;
        mainPanel.add(new JLabel("Bytes Per Sample:"), gbc);

        gbc.gridx = 1;
        bytesPerSampleSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        mainPanel.add(bytesPerSampleSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.gridwidth = 1;
        captureButton = new JButton("Start Capture");
        captureButton.addActionListener(this::onCaptureButtonClicked);
        mainPanel.add(captureButton, gbc);

        gbc.gridx = 1;
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(this::onStopButtonClicked);
        mainPanel.add(stopButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(statusLabel, gbc);

        // Add main panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        
        // Populate audio sources after UI is initialized
        populateAudioSources();
        audioSourceComboBox.addActionListener(e -> updateSamplingFrequencies());
    }

    private JTextField createReadOnlyTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        return textField;
    }

    private void populateAudioSources() {
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            int addedCount = 0;
            for (Mixer.Info mixer : mixers) {
                try {
                    Mixer selectedMixer = AudioSystem.getMixer(mixer);
                    if (selectedMixer.getTargetLineInfo().length > 0) {
                        audioSourceComboBox.addItem(mixer.getName());
                        addedCount++;
                    }
                } catch (Exception e) {
                    // Skip mixers that can't be accessed
                    System.err.println("Could not access mixer: " + mixer.getName());
                }
            }
            
            if (addedCount == 0) {
                statusLabel.setText("Warning: No audio input devices found");
            } else {
                // Auto-select first available source
                audioSourceComboBox.setSelectedIndex(0);
                updateSamplingFrequencies();
            }
        } catch (Exception e) {
            statusLabel.setText("Error: Could not enumerate audio devices");
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Failed to populate audio sources", e);
        }
    }

    private void updateSamplingFrequencies() {
        samplingFrequencyComboBox.removeAllItems();

        String selectedSource = (String) audioSourceComboBox.getSelectedItem();
        if (selectedSource == null) {
            return;
        }

        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            for (Mixer.Info mixer : mixers) {
                if (mixer.getName().equals(selectedSource)) {
                    Mixer selectedMixer = AudioSystem.getMixer(mixer);
                    Line.Info[] targetLineInfo = selectedMixer.getTargetLineInfo();

                    java.util.Set<String> uniqueRates = new java.util.LinkedHashSet<>();
                    for (Line.Info info : targetLineInfo) {
                        if (info instanceof DataLine.Info dataLineInfo) {
                            AudioFormat[] formats = dataLineInfo.getFormats();

                            for (AudioFormat format : formats) {
                                int sampleRate = (int) format.getSampleRate();
                                if (sampleRate > 0 && format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                                    uniqueRates.add(String.valueOf(sampleRate));
                                }
                            }
                        }
                    }
                    
                    // Add sorted sample rates
                    uniqueRates.stream().sorted((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)))
                              .forEach(samplingFrequencyComboBox::addItem);
                    
                    // Select 48000 if available, otherwise first item
                    if (uniqueRates.contains("48000")) {
                        samplingFrequencyComboBox.setSelectedItem("48000");
                    } else if (samplingFrequencyComboBox.getItemCount() > 0) {
                        samplingFrequencyComboBox.setSelectedIndex(0);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            statusLabel.setText("Error: Could not get sampling frequencies");
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Failed to update sampling frequencies", e);
        }
    }

    private void onStopButtonClicked(ActionEvent event) {
        isCapturing = false;
        captureButton.setEnabled(true);
        stopButton.setEnabled(false);
        audioSourceComboBox.setEnabled(true);
        samplingFrequencyComboBox.setEnabled(true);
        fftSizeComboBox.setEnabled(true);
        statusLabel.setText("Stopped");
    }

    private void onCaptureButtonClicked(ActionEvent event) {
        if (isCapturing) {
            return; // Already capturing
        }

        try {
            String selectedSource = (String) audioSourceComboBox.getSelectedItem();
            if (selectedSource == null) {
                JOptionPane.showMessageDialog(this, "No audio source selected.\nPlease select an audio input device.", "No Audio Source", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (samplingFrequencyComboBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No sampling frequencies available for this device.\nPlease select a different audio source.", "Configuration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            Mixer.Info selectedMixerInfo = null;
            for (Mixer.Info mixer : mixers) {
                if (mixer.getName().equals(selectedSource)) {
                    selectedMixerInfo = mixer;
                    break;
                }
            }

            if (selectedMixerInfo == null) {
                JOptionPane.showMessageDialog(this, "Selected audio source is no longer available.\nDevice may have been disconnected.", "Device Unavailable", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Mixer mixer = AudioSystem.getMixer(selectedMixerInfo);

            int sampleRate = Integer.parseInt(this.samplingFrequencyComboBox.getSelectedItem().toString());
            final int bytes = (int) this.bytesPerSampleSpinner.getValue();

            final AudioFormat requestedFormat = new AudioFormat((float) sampleRate, bytes * 8, channels, true, true);

            AudioFormat fallbackFormat = requestedFormat;
            if (!mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallbackFormat))) {
                Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
                boolean foundFormat = false;
                for (Line.Info info : targetLineInfo) {
                    if (info instanceof DataLine.Info dataLineInfo) {
                        AudioFormat[] formats = dataLineInfo.getFormats();

                        for (AudioFormat format : formats) {
                            if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleRate() > 0) {
                                fallbackFormat = format;
                                foundFormat = true;
                                break;
                            }
                        }
                        if (foundFormat) break;
                    }
                }

                if (!mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallbackFormat))) {
                    JOptionPane.showMessageDialog(this, "No supported audio format found for this device.\nTry a different sample rate or audio source.", "Unsupported Format", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            TargetDataLine microphone = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, fallbackFormat));
            microphone.open();
            microphone.start();

            // Make final copies for lambda
            final AudioFormat finalFormat = fallbackFormat;
            final TargetDataLine mic = microphone;

            // Update UI state
            isCapturing = true;
            captureButton.setEnabled(false);
            stopButton.setEnabled(true);
            audioSourceComboBox.setEnabled(false);
            samplingFrequencyComboBox.setEnabled(false);
            fftSizeComboBox.setEnabled(false);
            statusLabel.setText("Capturing...");
            
            this.samplePeriodField.setText(String.format("%.2f us", 1000000.0 / finalFormat.getSampleRate()));
            this.bitsPerSampleField.setText(String.valueOf(finalFormat.getSampleSizeInBits()));
            this.channelsField.setText(String.valueOf(finalFormat.getChannels()));

            captureThread = new Thread(() -> {
                FrequencyScanner fs = new FrequencyScanner();
                final int bytesPerSample = bytes; // Make final for lambda
                while (isCapturing) {
                    int fftSize = Integer.parseInt(GUI.this.fftSizeComboBox.getSelectedItem().toString());
                    int bytesNeeded = (channels * bytesPerSample) * fftSize;
                    if (mic.available() >= bytesNeeded) {
                        try {
                            AudioByteBuffer = new byte[bytesNeeded];
                            double[] AudioBuffer = new double[fftSize];
                            mic.read(AudioByteBuffer, 0, AudioByteBuffer.length);

                            // Convert bytes to samples, handling stereo by averaging channels
                            for (int i = 0; i < fftSize; i++) {
                                if (channels == 2) {
                                    // Stereo: average left and right channels
                                    int leftSample = (AudioByteBuffer[i * bytesPerSample * 2] << 8) | (AudioByteBuffer[i * bytesPerSample * 2 + 1] & 0xFF);
                                    int rightSample = (AudioByteBuffer[i * bytesPerSample * 2 + bytesPerSample] << 8) | (AudioByteBuffer[i * bytesPerSample * 2 + bytesPerSample + 1] & 0xFF);
                                    AudioBuffer[i] = (leftSample + rightSample) / 2.0;
                                } else {
                                    // Mono
                                    AudioBuffer[i] = (AudioByteBuffer[i * bytesPerSample] << 8) | (AudioByteBuffer[i * bytesPerSample + 1] & 0xFF);
                                }
                            }

                            // Apply Hamming window
                            applyHammingWindow(AudioBuffer);

                            double frequency = fs.extractFrequency(AudioBuffer, (int) finalFormat.getSampleRate());
                            GUI.this.mainFrequencyField.setText(String.format("%.2f Hz", frequency));
                        } catch (Exception ex) {
                            if (isCapturing) {
                                Logger.getLogger(GUI.class.getName()).log(Level.WARNING, "Error during capture", ex);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                try {
                    mic.stop();
                    mic.flush();
                    mic.close();
                } catch (Exception e) {
                    Logger.getLogger(GUI.class.getName()).log(Level.WARNING, "Error closing microphone", e);
                }
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Ready");
                });
            });
            captureThread.setPriority(Thread.NORM_PRIORITY);
            captureThread.setName("AudioCaptureThread");
            captureThread.start();

        } catch (LineUnavailableException ex) {
            isCapturing = false;
            captureButton.setEnabled(true);
            stopButton.setEnabled(false);
            audioSourceComboBox.setEnabled(true);
            samplingFrequencyComboBox.setEnabled(true);
            fftSizeComboBox.setEnabled(true);
            statusLabel.setText("Error: Device unavailable");
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Audio line unavailable", ex);
            JOptionPane.showMessageDialog(this, 
                "Unable to access the audio input device.\n" +
                "The device may be in use by another application.\n" +
                "Error: " + ex.getMessage(), 
                "Audio Device Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            isCapturing = false;
            captureButton.setEnabled(true);
            stopButton.setEnabled(false);
            audioSourceComboBox.setEnabled(true);
            samplingFrequencyComboBox.setEnabled(true);
            fftSizeComboBox.setEnabled(true);
            statusLabel.setText("Error occurred");
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Unexpected error during capture", ex);
            JOptionPane.showMessageDialog(this, 
                "An unexpected error occurred.\n" +
                "Error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Apply Hamming window to reduce spectral leakage in FFT
     * @param samples The audio samples to window
     */
    private void applyHammingWindow(double[] samples) {
        int n = samples.length;
        for (int i = 0; i < n; i++) {
            double window = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
            samples[i] *= window;
        }
    }

    public class FrequencyScanner {

        long lastmean = 0;
        long nextmean = 0;
        FastFourierTransformer fft;
        private double[] recentFrequencies = new double[5]; // For smoothing
        private int freqIndex = 0;
        private double lastFrequency = 0;
        
        public FrequencyScanner() {
            fft = new FastFourierTransformer(DftNormalization.STANDARD);
            updateDeviceInfo();
        }
        
        private void updateDeviceInfo() {
            GUI.this.fftVendorField.setText("Apache Commons Math");
            GUI.this.fftProcessorField.setText("Pure Java FFT");
            GUI.this.openCLVersionField.setText("N/A");
            GUI.this.driverVersionField.setText("N/A");
        }

        /**
         * extract the dominant frequency from 16bit PCM data.
         *
         * @param sampleData
         * @param sampleRate
         * @return an approximation of the dominant frequency in sampleData
         * @throws java.lang.InterruptedException
         * @throws java.io.IOException
         */
        public double extractFrequency(double[] sampleData, int sampleRate) throws InterruptedException, IOException {
            long startTime = System.currentTimeMillis();

            // Ensure the data length is a power of 2
            int n = sampleData.length;
            int powerOf2 = 1;
            while (powerOf2 < n) {
                powerOf2 *= 2;
            }
            double[] paddedData = new double[powerOf2];
            System.arraycopy(sampleData, 0, paddedData, 0, n);
            
            // Perform FFT
            org.apache.commons.math3.complex.Complex[] fftResult = fft.transform(paddedData, TransformType.FORWARD);
            double[] b = new double[fftResult.length / 2];
            /* find the peak magnitude and it's index */
            double maxMag = Double.NEGATIVE_INFINITY;
            double maxInd = -1;
            double mag;
            long mean = 0;
            
            // Calculate magnitudes and find mean
            for (int i = 1; i < fftResult.length / 2; i++) { // Start from 1 to skip DC component
                double real = fftResult[i].getReal();
                double imag = fftResult[i].getImaginary();
                mag = Math.sqrt(real * real + imag * imag);
                mean += mag;
                b[i] = mag;
            }
            
            double avgMag = mean / (double)(fftResult.length / 2 - 1);
            double threshold = avgMag * 3.0; // Noise threshold - signal must be 3x average
            
            // Find peak above threshold
            for (int i = 1; i < fftResult.length / 2; i++) {
                if (b[i] > threshold && b[i] > maxMag) {
                    maxMag = b[i];
                    maxInd = i;
                }
            }
            
            // Convert magnitudes to dB for display
            for (int i = 0; i < b.length; i++) {
                if (b[i] > 0) {
                    b[i] = 20 * FastMath.log10(b[i] / 0.776);
                } else {
                    b[i] = -100; // Very low value for zero magnitude
                }
            }
            
            xy.setData(b, sampleRate);
            long endTime = System.currentTimeMillis();
            GUI.this.executionPeriodField.setText(String.valueOf(endTime - startTime) + "ms");
            
            // Calculate frequency and apply smoothing
            if (maxInd > 0) {
                double frequency = (double) (sampleRate * maxInd / fftResult.length);
                
                // Smooth frequency by averaging recent values
                recentFrequencies[freqIndex] = frequency;
                freqIndex = (freqIndex + 1) % recentFrequencies.length;
                
                double smoothedFreq = 0;
                for (double f : recentFrequencies) {
                    smoothedFreq += f;
                }
                smoothedFreq /= recentFrequencies.length;
                
                lastFrequency = smoothedFreq;
                return smoothedFreq;
            }
            
            return lastFrequency; // Return last valid frequency if no peak found
        }

    }

    public static void main(String[] args) {
        /* Set the look and feel based on system dark mode */
        try {
            boolean isDarkMode = java.awt.Toolkit.getDefaultToolkit().getDesktopProperty("win.darkMode") != null;
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Ensure only one instance of the GUI is created */
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bitsPerSampleField;
    private javax.swing.JTextField channelsField;
    public javax.swing.JComboBox<String> fftSizeComboBox;
    private javax.swing.JTextField driverVersionField;
    private javax.swing.JTextField openCLVersionField;
    private javax.swing.JTextField executionPeriodField;
    private javax.swing.JTextField samplePeriodField;
    private javax.swing.JTextField fftVendorField;
    private javax.swing.JSpinner bytesPerSampleSpinner;
    private javax.swing.JButton captureButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JComboBox<String> samplingFrequencyComboBox;
    private javax.swing.JTextField mainFrequencyField;
    private javax.swing.JTextField fftProcessorField;
    private javax.swing.JComboBox<String> audioSourceComboBox;
    // End of variables declaration//GEN-END:variables
}
