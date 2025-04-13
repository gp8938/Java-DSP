/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gpoole.dsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.util.fft.DoubleFFTPow2;

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
    byte[] AudioByteBuffer;
    double[] input;
    long fftcalctime;
    boolean gotProc = false;
    DoubleFFTPow2 fft1;
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
        setResizable(false);

        // Set layout for the main frame
        setLayout(new BorderLayout(10, 10));

        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add components to the main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Audio Source:"), gbc);

        gbc.gridx = 1;
        audioSourceComboBox = new JComboBox<>();
        populateAudioSources();
        audioSourceComboBox.addActionListener(e -> updateSamplingFrequencies());
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
        gbc.gridwidth = 2;
        captureButton = new JButton("Capture");
        captureButton.addActionListener(this::onCaptureButtonClicked);
        mainPanel.add(captureButton, gbc);

        // Add main panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JTextField createReadOnlyTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        return textField;
    }

    private void populateAudioSources() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixer : mixers) {
            Mixer selectedMixer = AudioSystem.getMixer(mixer);
            if (selectedMixer.getTargetLineInfo().length > 0) { // Only add mixers with usable target lines
                audioSourceComboBox.addItem(mixer.getName());
            }
        }
    }

    private void updateSamplingFrequencies() {
        samplingFrequencyComboBox.removeAllItems();

        String selectedSource = (String) audioSourceComboBox.getSelectedItem();
        if (selectedSource == null) {
            return;
        }

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixer : mixers) {
            if (mixer.getName().equals(selectedSource)) {
                Mixer selectedMixer = AudioSystem.getMixer(mixer);
                Line.Info[] targetLineInfo = selectedMixer.getTargetLineInfo();

                for (Line.Info info : targetLineInfo) {
                    if (info instanceof DataLine.Info) {
                        DataLine.Info dataLineInfo = (DataLine.Info) info;
                        AudioFormat[] formats = dataLineInfo.getFormats();

                        System.out.println("Supported formats for " + selectedSource + ":");
                        for (AudioFormat format : formats) {
                            System.out.println(format);
                            int sampleRate = (int) format.getSampleRate();
                            if (sampleRate > 0 && format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                                samplingFrequencyComboBox.addItem(String.valueOf(sampleRate));
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    private void onCaptureButtonClicked(ActionEvent event) {
        try {
            String selectedSource = (String) audioSourceComboBox.getSelectedItem();
            if (selectedSource == null) {
                JOptionPane.showMessageDialog(this, "No audio source selected.", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Selected audio source is not available.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Mixer mixer = AudioSystem.getMixer(selectedMixerInfo);

            int sampleRate = Integer.parseInt(this.samplingFrequencyComboBox.getSelectedItem().toString());
            int bytes = (int) this.bytesPerSampleSpinner.getValue();

            // Marked requestedFormat as final to resolve the finality issue
            final AudioFormat requestedFormat = new AudioFormat((float) sampleRate, bytes * 8, channels, true, true);

            // Introduced a new variable for reassignment to resolve the finality issue
            AudioFormat fallbackFormat = requestedFormat;
            if (!mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallbackFormat))) {
                System.err.println("Unsupported format: " + fallbackFormat);

                // Fallback to the first supported format
                Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
                for (Line.Info info : targetLineInfo) {
                    if (info instanceof DataLine.Info) {
                        DataLine.Info dataLineInfo = (DataLine.Info) info;
                        AudioFormat[] formats = dataLineInfo.getFormats();

                        for (AudioFormat format : formats) {
                            if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleRate() > 0) {
                                System.out.println("Falling back to supported format: " + format);
                                fallbackFormat = format;
                                break;
                            }
                        }
                    }
                }

                // If no supported format is found, show an error
                if (!mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallbackFormat))) {
                    JOptionPane.showMessageDialog(this, "No supported audio format found for the selected source.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            TargetDataLine microphone = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, fallbackFormat));
            microphone.open();
            microphone.start();

            this.samplingFrequencyComboBox.setEnabled(false);
            this.samplePeriodField.setText(String.valueOf((int) (1000000 * (float) 1 / requestedFormat.getSampleRate())) + "us");
            this.bitsPerSampleField.setText(String.valueOf(requestedFormat.getSampleSizeInBits()));
            this.channelsField.setText(String.valueOf(requestedFormat.getChannels()));

            Thread t1 = new Thread(() -> {
                FrequencyScanner fs = new FrequencyScanner();
                while (open) {
                    if (microphone.available() >= Integer.parseInt(GUI.this.fftSizeComboBox.getSelectedItem().toString())) {
                        try {
                            AudioByteBuffer = new byte[(channels * bytes) * Integer.parseInt(this.fftSizeComboBox.getSelectedItem().toString())];
                            double[] AudioBuffer = new double[AudioByteBuffer.length / 4];
                            microphone.read(AudioByteBuffer, 0, AudioByteBuffer.length);

                            for (int i = 0; i < AudioBuffer.length; i++) {
                                AudioBuffer[i] = AudioByteBuffer[i * 2] << 8 | (AudioByteBuffer[i * 2 + 1] & 0xFF);
                            }

                            GUI.this.mainFrequencyField.setText(String.valueOf(fs.extractFrequency(AudioBuffer, (int) requestedFormat.getSampleRate())) + "Hz");
                        } catch (IOException | InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                microphone.stop();
                microphone.flush();
                microphone.close();
            });
            t1.setPriority(Thread.MAX_PRIORITY);
            t1.start();

        } catch (LineUnavailableException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Unable to access the audio input device.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public class FrequencyScanner {

        CLQueue queue;
        CLContext contextpc;
        List<CLDevice> devices;
        long lastmean = 0;
        long nextmean = 0;
        public FrequencyScanner() {
            if (!gotProc) {
                devices = getDevices();
                contextpc = JavaCL.createBestContext(DeviceFeature.Accelerator);
                queue = contextpc.createDefaultOutOfOrderQueueIfPossible();
            }
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

            double[] a = new DoubleFFTPow2(contextpc).transform(queue, sampleData, true);
            double[] b = new double[a.length];
            /* find the peak magnitude and it's ByteBufferIndex */
            double maxMag = Double.NEGATIVE_INFINITY;
            double maxInd = -1;
            double mag;
            long mean = 0;
            for (int i = 0; i < a.length; i++) {
                mag = Math.sqrt(FastMath.pow((a[i]), 2) + FastMath.pow((a[i] + 1), 2));
                mag = 20 * FastMath.log10(mag / 0.776);
                mean += FastMath.abs(mag);
                
                /*if((mag*threshold) < lastmean){//noise rejection
                    b[i] = 0;
                }*/
                //else{
                    b[i] = mag;
                //}
                
                if (mag > maxMag) {//find the largest signal by magnitude
                    maxMag = mag;
                    maxInd = i;
                }
                
            }
            lastmean = mean / a.length;
            //System.out.println("avg: "+ mean);
            xy.setData(b, sampleRate);
            long endTime = System.currentTimeMillis();
            GUI.this.executionPeriodField.setText(String.valueOf(endTime - startTime) + "ms");
            return (double) ((sampleRate * maxInd / (a.length)) / 2);
        }

        private List<CLDevice> getDevices() {
            List<CLDevice> devices = new ArrayList<>();
            for (CLPlatform platform : JavaCL.listPlatforms()) {
                for (CLDevice device : platform.listAllDevices(true)) {
                    devices.add(device);
                    GUI.this.fftVendorField.setText(device.getVendor());
                    GUI.this.fftProcessorField.setText(device.getName());
                    GUI.this.openCLVersionField.setText(device.getOpenCLCVersion());
                    GUI.this.driverVersionField.setText(device.getDriverVersion());
                }
            }
            return devices;
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
    private javax.swing.JComboBox<String> samplingFrequencyComboBox;
    private javax.swing.JTextField mainFrequencyField;
    private javax.swing.JTextField fftProcessorField;
    private javax.swing.JComboBox<String> audioSourceComboBox;
    // End of variables declaration//GEN-END:variables
}
