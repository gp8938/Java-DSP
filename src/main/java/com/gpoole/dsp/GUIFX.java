package com.gpoole.dsp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX-based GUI replacement for the original Swing UI.
 * <p>Provides basic controls for selecting audio source/sample rate and
 * starting/stopping capture. Visualization is handled by the JavaFX
 * `XYLineChart` implementation.</p>
 */
public class GUIFX extends Application {

    private volatile boolean isCapturing = false;
    private Thread captureThread = null;
    private byte[] audioByteBuffer;
    private final XYLineChart xy = new XYLineChart("Frequency Spectrum");
    private int channels = 2;

    // UI controls
    private ComboBox<String> audioSourceComboBox;
    private ComboBox<String> samplingFrequencyComboBox;
    private ComboBox<String> fftSizeComboBox;
    private TextField mainFrequencyField;
    private TextField fftVendorField;
    private TextField fftProcessorField;
    private TextField openCLVersionField;
    private TextField driverVersionField;
    private TextField channelsField;
    private TextField bitsPerSampleField;
    private TextField executionPeriodField;
    private TextField samplePeriodField;
    private Spinner<Integer> bytesPerSampleSpinner;
    private Button captureButton;
    private Button stopButton;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Digital Signal Processing - JavaFX");

        // Controls column
        GridPane controls = new GridPane();
        controls.setVgap(6);
        controls.setHgap(6);
        controls.setPadding(new Insets(8));

        audioSourceComboBox = new ComboBox<>();
        samplingFrequencyComboBox = new ComboBox<>();
        fftSizeComboBox = new ComboBox<>();
        fftSizeComboBox.getItems().addAll("16","32","64","128","256","512","1024","2048","4096","8192","16384","32768","65536");

        mainFrequencyField = createReadOnlyTextField();
        fftVendorField = createReadOnlyTextField();
        fftProcessorField = createReadOnlyTextField();
        openCLVersionField = createReadOnlyTextField();
        driverVersionField = createReadOnlyTextField();
        channelsField = createReadOnlyTextField();
        bitsPerSampleField = createReadOnlyTextField();
        executionPeriodField = createReadOnlyTextField();
        samplePeriodField = createReadOnlyTextField();

        bytesPerSampleSpinner = new Spinner<>(1, 4, 2);

        int r = 0;
        controls.add(new Label("Audio Source:"), 0, r);
        controls.add(audioSourceComboBox, 1, r++);
        controls.add(new Label("Sampling Frequency:"), 0, r);
        controls.add(samplingFrequencyComboBox, 1, r++);
        controls.add(new Label("FFT Size:"), 0, r);
        controls.add(fftSizeComboBox, 1, r++);
        controls.add(new Label("Main Frequency:"), 0, r);
        controls.add(mainFrequencyField, 1, r++);
        controls.add(new Label("Channels:"), 0, r);
        controls.add(channelsField, 1, r++);
        controls.add(new Label("Bits Per Sample:"), 0, r);
        controls.add(bitsPerSampleField, 1, r++);
        controls.add(new Label("Sample Period:"), 0, r);
        controls.add(samplePeriodField, 1, r++);
        controls.add(new Label("Bytes Per Sample:"), 0, r);
        controls.add(bytesPerSampleSpinner, 1, r++);

        captureButton = new Button("Start Capture");
        stopButton = new Button("Stop");
        stopButton.setDisable(true);
        captureButton.setOnAction(e -> onCaptureButtonClicked());
        stopButton.setOnAction(e -> onStopButtonClicked());

        HBox buttons = new HBox(6, captureButton, stopButton);
        controls.add(buttons, 0, r, 2, 1);

        statusLabel = new Label("Ready");
        controls.add(statusLabel, 0, ++r, 2, 1);

        // Layout: left controls, center chart
        BorderPane root = new BorderPane();
        root.setLeft(controls);
        Node chartNode = xy.getNode();
        VBox chartBox = new VBox(chartNode);
        chartBox.setPadding(new Insets(8));
        root.setCenter(chartBox);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Populate audio sources after UI is visible
        populateAudioSources();
        audioSourceComboBox.setOnAction(e -> updateSamplingFrequencies());
    }

    private TextField createReadOnlyTextField() {
        TextField tf = new TextField();
        tf.setEditable(false);
        return tf;
    }

    private void populateAudioSources() {
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            int added = 0;
            for (Mixer.Info mi : mixers) {
                try {
                    Mixer m = AudioSystem.getMixer(mi);
                    if (m.getTargetLineInfo().length > 0) {
                        audioSourceComboBox.getItems().add(mi.getName());
                        added++;
                    }
                } catch (IllegalArgumentException | SecurityException ex) {
                    Logger.getLogger(GUIFX.class.getName()).log(Level.WARNING, "Could not access mixer: " + mi.getName(), ex);
                }
            }
            if (added > 0) {
                audioSourceComboBox.getSelectionModel().selectFirst();
                updateSamplingFrequencies();
            } else {
                statusLabel.setText("Warning: No audio input devices found");
            }
        } catch (SecurityException ex) {
            statusLabel.setText("Error: Could not enumerate audio devices");
            Logger.getLogger(GUIFX.class.getName()).log(Level.SEVERE, "Failed to populate audio sources", ex);
        }
    }

    private void updateSamplingFrequencies() {
        samplingFrequencyComboBox.getItems().clear();
        String selected = audioSourceComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            for (Mixer.Info mi : mixers) {
                if (mi.getName().equals(selected)) {
                    Mixer m = AudioSystem.getMixer(mi);
                    Line.Info[] infos = m.getTargetLineInfo();
                    Set<String> unique = new LinkedHashSet<>();
                    for (Line.Info info : infos) {
                        if (info instanceof DataLine.Info) {
                            DataLine.Info d = (DataLine.Info) info;
                            AudioFormat[] formats = d.getFormats();
                            for (AudioFormat f : formats) {
                                int sr = (int) f.getSampleRate();
                                if (sr > 0 && f.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                                    unique.add(String.valueOf(sr));
                                }
                            }
                        }
                    }
                    unique.stream().sorted((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b))).forEach(samplingFrequencyComboBox.getItems()::add);
                    if (samplingFrequencyComboBox.getItems().contains("48000")) samplingFrequencyComboBox.getSelectionModel().select("48000");
                    else if (!samplingFrequencyComboBox.getItems().isEmpty()) samplingFrequencyComboBox.getSelectionModel().selectFirst();
                    break;
                }
            }
        } catch (Exception ex) {
            statusLabel.setText("Error: Could not get sampling frequencies");
            Logger.getLogger(GUIFX.class.getName()).log(Level.SEVERE, "Failed to update sampling frequencies", ex);
        }
    }

    private void onStopButtonClicked() {
        stopCapture();
    }

    private void onCaptureButtonClicked() {
        if (isCapturing) return;

        String selectedSource = audioSourceComboBox.getSelectionModel().getSelectedItem();
        if (selectedSource == null) {
            showError("No audio source selected.", "Please select an audio input device.");
            return;
        }
        if (samplingFrequencyComboBox.getItems().isEmpty()) {
            showError("No sampling frequencies available.", "Please select a different audio source.");
            return;
        }

        Mixer.Info selectedMixerInfo = null;
        for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
            if (mi.getName().equals(selectedSource)) { selectedMixerInfo = mi; break; }
        }
        if (selectedMixerInfo == null) {
            showError("Device Unavailable", "Selected audio source is no longer available.");
            return;
        }

        Mixer mixer = AudioSystem.getMixer(selectedMixerInfo);
        int sampleRate = Integer.parseInt(samplingFrequencyComboBox.getSelectionModel().getSelectedItem());
        final int bytes = bytesPerSampleSpinner.getValue();
        final AudioFormat requestedFormat = new AudioFormat((float) sampleRate, bytes * 8, channels, true, true);

        AudioFormat fallbackFormat = requestedFormat;
        if (!mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallbackFormat))) {
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            boolean found = false;
            for (Line.Info info : lineInfos) {
                if (info instanceof DataLine.Info) {
                    AudioFormat[] formats = ((DataLine.Info) info).getFormats();
                    for (AudioFormat f : formats) {
                        if (f.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && f.getSampleRate() > 0) {
                            fallbackFormat = f;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }
            if (!mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallbackFormat))) {
                showError("Unsupported Format", "No supported audio format found for this device.");
                return;
            }
        }

        try {
            final TargetDataLine microphone = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, fallbackFormat));
            microphone.open();
            microphone.start();

            final AudioFormat finalFormat = fallbackFormat;

            isCapturing = true;
            captureButton.setDisable(true);
            stopButton.setDisable(false);
            audioSourceComboBox.setDisable(true);
            samplingFrequencyComboBox.setDisable(true);
            fftSizeComboBox.setDisable(true);
            statusLabel.setText("Capturing...");

            samplePeriodField.setText(String.format("%.2f us", 1000000.0 / finalFormat.getSampleRate()));
            bitsPerSampleField.setText(String.valueOf(finalFormat.getSampleSizeInBits()));
            channelsField.setText(String.valueOf(finalFormat.getChannels()));

            captureThread = new Thread(() -> {
                try {
                    FrequencyScanner fs = new FrequencyScanner();
                    final int bytesPerSample = bytes;
                    while (isCapturing) {
                        int fftSize = Integer.parseInt(fftSizeComboBox.getSelectionModel().getSelectedItem());
                        int bytesNeeded = (channels * bytesPerSample) * fftSize;
                        if (microphone.available() >= bytesNeeded) {
                            audioByteBuffer = new byte[bytesNeeded];
                            double[] audioBuffer = new double[fftSize];
                            microphone.read(audioByteBuffer, 0, audioByteBuffer.length);

                            for (int i = 0; i < fftSize; i++) {
                                if (channels == 2) {
                                    int leftSample = (audioByteBuffer[i * bytesPerSample * 2] << 8) | (audioByteBuffer[i * bytesPerSample * 2 + 1] & 0xFF);
                                    int rightSample = (audioByteBuffer[i * bytesPerSample * 2 + bytesPerSample] << 8) | (audioByteBuffer[i * bytesPerSample * 2 + bytesPerSample + 1] & 0xFF);
                                    audioBuffer[i] = (leftSample + rightSample) / 2.0;
                                } else {
                                    audioBuffer[i] = (audioByteBuffer[i * bytesPerSample] << 8) | (audioByteBuffer[i * bytesPerSample + 1] & 0xFF);
                                }
                            }

                            applyHammingWindow(audioBuffer);
                            double frequency = fs.extractFrequency(audioBuffer, (int) finalFormat.getSampleRate());
                            Platform.runLater(() -> mainFrequencyField.setText(String.format("%.2f Hz", frequency)));
                        } else {
                            try { Thread.sleep(10); } catch (InterruptedException ex) { break; }
                        }
                    }
                } catch (InterruptedException | IOException ex) {
                    if (isCapturing) Logger.getLogger(GUIFX.class.getName()).log(Level.WARNING, "Error during capture", ex);
                } finally {
                    try { microphone.stop(); microphone.flush(); microphone.close(); } catch (RuntimeException e) { Logger.getLogger(GUIFX.class.getName()).log(Level.WARNING, "Error closing microphone", e); }
                    Platform.runLater(() -> {
                        statusLabel.setText("Ready");
                        captureButton.setDisable(false);
                        stopButton.setDisable(true);
                        audioSourceComboBox.setDisable(false);
                        samplingFrequencyComboBox.setDisable(false);
                        fftSizeComboBox.setDisable(false);
                    });
                }
            });
            captureThread.setDaemon(true);
            captureThread.setName("AudioCaptureThread-JavaFX");
            captureThread.start();

        } catch (LineUnavailableException ex) {
            isCapturing = false;
            Logger.getLogger(GUIFX.class.getName()).log(Level.SEVERE, "Audio line unavailable", ex);
            showError("Audio Device Error", "Unable to access the audio input device.\nError: " + ex.getMessage());
        }
    }

    private void stopCapture() {
        isCapturing = false;
        captureButton.setDisable(false);
        stopButton.setDisable(true);
        audioSourceComboBox.setDisable(false);
        samplingFrequencyComboBox.setDisable(false);
        fftSizeComboBox.setDisable(false);
        statusLabel.setText("Stopped");
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    private void applyHammingWindow(double[] samples) {
        com.gpoole.dsp.signal.WindowFunction.HAMMING.apply(samples);
    }

    public class FrequencyScanner {

        private final FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        private double[] recentFrequencies = new double[5];
        private int freqIndex = 0;
        private double lastFrequency = 0;

        public FrequencyScanner() {
            updateDeviceInfo();
        }

        private void updateDeviceInfo() {
            Platform.runLater(() -> {
                fftVendorField.setText("Apache Commons Math");
                fftProcessorField.setText("Pure Java FFT");
                openCLVersionField.setText("N/A");
                driverVersionField.setText("N/A");
            });
        }

        public double extractFrequency(double[] sampleData, int sampleRate) throws InterruptedException, IOException {
            long startTime = System.currentTimeMillis();

            int n = sampleData.length;
            int powerOf2 = 1;
            while (powerOf2 < n) powerOf2 *= 2;
            double[] padded = new double[powerOf2];
            System.arraycopy(sampleData, 0, padded, 0, n);

            org.apache.commons.math3.complex.Complex[] fftResult = fft.transform(padded, TransformType.FORWARD);
            double[] b = new double[fftResult.length / 2];
            double maxMag = Double.NEGATIVE_INFINITY;
            int maxInd = -1;
            double mean = 0.0;

            for (int i = 1; i < fftResult.length / 2; i++) {
                double real = fftResult[i].getReal();
                double imag = fftResult[i].getImaginary();
                double mag = Math.sqrt(real * real + imag * imag);
                mean += mag;
                b[i] = mag;
            }

            double avgMag = mean / (double) (fftResult.length / 2 - 1);
            double threshold = avgMag * 3.0;

            for (int i = 1; i < fftResult.length / 2; i++) {
                if (b[i] > threshold && b[i] > maxMag) {
                    maxMag = b[i];
                    maxInd = i;
                }
            }

            for (int i = 0; i < b.length; i++) {
                if (b[i] > 0) b[i] = 20 * FastMath.log10(b[i] / 0.776);
                else b[i] = -100;
            }

            xy.setData(b, sampleRate);
            long endTime = System.currentTimeMillis();
            Platform.runLater(() -> executionPeriodField.setText(String.valueOf(endTime - startTime) + "ms"));

            if (maxInd > 0) {
                double frequency = (double) (sampleRate * maxInd / fftResult.length);
                recentFrequencies[freqIndex] = frequency;
                freqIndex = (freqIndex + 1) % recentFrequencies.length;
                double smoothed = 0;
                for (double f : recentFrequencies) smoothed += f;
                smoothed /= recentFrequencies.length;
                lastFrequency = smoothed;
                return smoothed;
            }
            return lastFrequency;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
