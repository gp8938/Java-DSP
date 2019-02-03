/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gpoole.dsp;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.util.fft.DoubleFFTPow2;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import static java.lang.Thread.MAX_PRIORITY;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JOptionPane;
import org.apache.commons.math3.util.FastMath;

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
        this.bytesjSpinner1.setValue(2);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
 regenerated AudioByteBuffer the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        freqComboBox = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        DFTSizeComboBox = new javax.swing.JComboBox<>();
        freqTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        VendorTextField = new javax.swing.JTextField();
        processorTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        OpenCLTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        DriverTextField1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        ChannelsTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        BitsTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        PeriodTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        SampleTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        bytesjSpinner1 = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        captureButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        freqComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "8000", "44100", "48000", "96000" }));

        jLabel1.setText("Sampling Frequency");

        jLabel2.setText("FFT Size");

        DFTSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768", "65536" }));
        DFTSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DFTSizeComboBoxActionPerformed(evt);
            }
        });

        freqTextField.setEditable(false);

        jLabel3.setText("Main Frequency ");

        jLabel4.setText("FFT Vendor");

        VendorTextField.setEditable(false);

        processorTextField1.setEditable(false);

        jLabel5.setText("FFT Processor");

        OpenCLTextField.setEditable(false);

        jLabel6.setText("OpenCL Version");

        DriverTextField1.setEditable(false);

        jLabel7.setText("Driver Version");

        ChannelsTextField.setEditable(false);

        jLabel8.setText("# of Channels");

        BitsTextField.setEditable(false);

        jLabel9.setText("Bits Per Sample");

        PeriodTextField.setEditable(false);

        jLabel10.setText("Execution Period");

        SampleTextField.setEditable(false);

        jLabel11.setText("Sample Period");

        jLabel12.setText("# Byte per Sample");

        captureButton.setText("Capture");
        captureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(42, 42, 42)
                        .addComponent(DriverTextField1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(31, 31, 31)
                        .addComponent(freqTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(captureButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(VendorTextField)
                                    .addComponent(processorTextField1)
                                    .addComponent(OpenCLTextField)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(freqComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bytesjSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(DFTSizeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(46, 46, 46)
                        .addComponent(ChannelsTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(32, 32, 32)
                        .addComponent(BitsTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(27, 27, 27)
                        .addComponent(PeriodTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(40, 40, 40)
                        .addComponent(SampleTextField)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bytesjSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(captureButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(freqComboBox)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(DFTSizeComboBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(VendorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(processorTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OpenCLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DriverTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PeriodTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SampleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ChannelsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BitsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(freqTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void DFTSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DFTSizeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DFTSizeComboBoxActionPerformed

    private void captureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureButtonActionPerformed
        try {
            //JOptionPane.showMessageDialog(null,"pressed");
            
            int sampleRate = (int) this.freqComboBox.getSelectedItem();//get the sample size
            int bytes = (int) this.bytesjSpinner1.getValue();//get number of bytes per sample
            
            AudioFormat format = new AudioFormat((float) sampleRate, bytes*8, channels, true, true);//set audio capture format:Freq,bit depth(bits),#channels,,
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);//set data source
            microphone.open();//open up port
            microphone.start();//start polling data
            
            this.freqComboBox.setEnabled(false);//disable changing the sample frequency
            this.SampleTextField.setText(String.valueOf((int) (1000000 * (float) 1 / Integer.parseInt(freqComboBox.getSelectedItem().toString()))) + "us");//calculate sample period from sample frequency
            this.BitsTextField.setText(String.valueOf(bits));//set # of bits being collected per channel per sample in GUI
            this.ChannelsTextField.setText(String.valueOf(channels));//set # of channels being collected per sample in GUI
            
            Thread t1 = new Thread(() -> {//create thread
                FrequencyScanner fs = new FrequencyScanner();//this object takes array, calculates fft, and displays largest magnitude frequency
                while (open) {//while the fft chart window is open
                    
                    if (microphone.available() >= Integer.parseInt(GUI.this.DFTSizeComboBox.getSelectedItem().toString())) {//if the #of bytes available is larger than what is requested
                        
                        try {
                            AudioByteBuffer = null;//null input byte data array
                            AudioByteBuffer = new byte[(channels * bytes) * Integer.parseInt(this.DFTSizeComboBox.getSelectedItem().toString())];//set input array size AudioByteBuffer taking the #samples * bytes/channel * #channels
                            double[] AudioBuffer = new double[AudioByteBuffer.length / 4];//create a double array for the reconsstructed data value from AudioByteBuffer
                            microphone.read(AudioByteBuffer, 0, AudioByteBuffer.length); //read to AudioByteBuffer the length of AudioByteBuffer from a offset of 0

                            for (int index = 0; index < AudioByteBuffer.length; index++) {//go through all of the array
                                
                                if (!((index << ((channels*bytes)/2) + 1) > AudioByteBuffer.length)) {//if the index doest overflow the byte buffer (index)
                                    
                                    for (int d = 0; d < AudioByteBuffer.length<<2; d++) {//reconstruct original value from the bytes by shifting arthmitic
                                        
                                        AudioBuffer[index] = (short) (AudioByteBuffer[index << 1 + 1] + AudioBuffer[index]);
                                    }
                                }
                                GUI.this.freqTextField.setText(String.valueOf((double) fs.extractFrequency(AudioBuffer, sampleRate)) + "Hz");//preform fft and extract max magintude signal
                            }
                        } catch (IOException | InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    microphone.stop();//stop audio capture
                    microphone.flush();//clear audio buffer
                    microphone.close();//close audio targetDataSource
                }
            });
            t1.setPriority(MAX_PRIORITY);//set the thread to have max priority in the CPU
            t1.start();//start this thread
            
        } catch (LineUnavailableException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_captureButtonActionPerformed
    public class FrequencyScanner {

        CLQueue queue;
        CLContext contextpc;
        List<CLDevice> devices;
        private float[] window;

        public FrequencyScanner() {
            window = null;
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

            /* find the peak magnitude and it's index */
            double maxMag = Double.NEGATIVE_INFINITY;
            double maxInd = -1;
            double mag;

            for (int i = 0; i < a.length >> 1; i++) {
                mag = Math.sqrt(FastMath.pow((a[i << 1]), 2) + FastMath.pow((a[i << 1] + 1), 2));
                if (mag > maxMag) {
                    maxMag = mag;
                    maxInd = i;
                }
            }
            long endTime = System.currentTimeMillis();
            GUI.this.PeriodTextField.setText(String.valueOf(endTime - startTime) + "ms");
            return (double) ((sampleRate * maxInd / (a.length / 2)) / 2);
        }

        private List<CLDevice> getDevices() {
            List<CLDevice> devices = new ArrayList<>();
            for (CLPlatform platform : JavaCL.listPlatforms()) {
                for (CLDevice device : platform.listAllDevices(true)) {
                    devices.add(device);
                    GUI.this.VendorTextField.setText(device.getVendor());
                    GUI.this.processorTextField1.setText(device.getName());
                    GUI.this.OpenCLTextField.setText(device.getOpenCLCVersion());
                    GUI.this.DriverTextField1.setText(device.getDriverVersion());
                }
            }
            return devices;
        }

    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new GUI().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField BitsTextField;
    private javax.swing.JTextField ChannelsTextField;
    private javax.swing.JComboBox<String> DFTSizeComboBox;
    private javax.swing.JTextField DriverTextField1;
    private javax.swing.JTextField OpenCLTextField;
    private javax.swing.JTextField PeriodTextField;
    private javax.swing.JTextField SampleTextField;
    private javax.swing.JTextField VendorTextField;
    private javax.swing.JSpinner bytesjSpinner1;
    private javax.swing.JButton captureButton;
    private javax.swing.JComboBox<String> freqComboBox;
    private javax.swing.JTextField freqTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField processorTextField1;
    // End of variables declaration//GEN-END:variables
}
