package com.gpoole.dsp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.gpoole.dsp.util.Preconditions;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX-based GUI replacement for the original Swing UI.
 * <p>Provides basic controls for selecting audio source/sample rate and
 * starting/stopping capture. Visualization is handled by the JavaFX
 * `XYLineChart` implementation.</p>
 */
public class GUIFX extends Application {

    // Constants for signal processing
    private static final int NOISE_THRESHOLD_MULTIPLIER = 3;
    private static final int FREQUENCY_SMOOTHING_SAMPLES = 5;
    private static final double MAGNITUDE_FLOOR_DB = -80.0;
    private static final int THREAD_SHUTDOWN_TIMEOUT_MS = 5000;
    private static final int SLEEP_INTERVAL_MS = 1;
    private static final double MICROSECONDS_PER_SECOND = 1_000_000.0;
    private static final long UI_UPDATE_INTERVAL_MS = 50;
    private static final long PERIOD_UPDATE_INTERVAL_MS = 500;
    private volatile long lastUiUpdateTime = 0;
    private volatile long lastPeriodUpdateTime = 0;

    private final AtomicBoolean isCapturing = new AtomicBoolean(false);
    private ExecutorService captureExecutor;
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
        fftSizeComboBox.getSelectionModel().select("2048");

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
        VBox.setVgrow(chartNode, Priority.ALWAYS);
        root.setCenter(chartBox);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Populate audio sources after UI is visible
        populateAudioSources();
        audioSourceComboBox.setOnAction(e -> updateSamplingFrequencies());

        // Shutdown executor on close
        primaryStage.setOnCloseRequest(e -> shutdownExecutor());
    }

    @Override
    public void stop() {
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        if (captureExecutor != null) {
            captureExecutor.shutdownNow();
            try {
                captureExecutor.awaitTermination(THREAD_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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
        if (isCapturing.get()) return;

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

            isCapturing.set(true);
            captureButton.setDisable(true);
            stopButton.setDisable(false);
            audioSourceComboBox.setDisable(true);
            samplingFrequencyComboBox.setDisable(true);
            fftSizeComboBox.setDisable(true);
            statusLabel.setText("Capturing...");

            samplePeriodField.setText(String.format("%.2f us", MICROSECONDS_PER_SECOND / finalFormat.getSampleRate()));
            bitsPerSampleField.setText(String.valueOf(finalFormat.getSampleSizeInBits()));
            channelsField.setText(String.valueOf(finalFormat.getChannels()));

            // Update device info before starting thread
            updateDeviceInfo();

            // Lock configuration at capture start
            final int lockedFftSize = Integer.parseInt(fftSizeComboBox.getSelectionModel().getSelectedItem());
            final int lockedBytesPerSample = bytes;
            final boolean bigEndian = finalFormat.isBigEndian();

            // Pre-allocate buffers based on max expected size
            final int maxBytesNeeded = (channels * lockedBytesPerSample) * lockedFftSize;
            final byte[] audioByteBuffer = new byte[maxBytesNeeded];
            final double[] audioBuffer = new double[lockedFftSize];

            captureExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "AudioCaptureThread-JavaFX");
                t.setDaemon(true);
                return t;
            });

            captureExecutor.submit(() -> {
                try {
                    FrequencyScanner fs = new FrequencyScanner(lockedFftSize);
                    final int bytesPerFrame = channels * lockedBytesPerSample;

                    while (isCapturing.get()) {
                        int bytesNeeded = bytesPerFrame * lockedFftSize;
                        int available = microphone.available();

                        if (available >= bytesNeeded) {
                            // Handle partial reads - ensure we get complete frames
                            int bytesRead = microphone.read(audioByteBuffer, 0, bytesNeeded);
                            if (bytesRead < bytesNeeded) {
                                // Skip incomplete buffer
                                continue;
                            }

                            // Decode audio samples with proper endianness
                            decodeSamples(audioByteBuffer, audioBuffer, lockedFftSize,
                                         lockedBytesPerSample, channels, bigEndian);

                            applyHammingWindow(audioBuffer);
                            double frequency = fs.extractFrequency(audioBuffer, (int) finalFormat.getSampleRate());
                            long now = System.currentTimeMillis();
                            if (now - lastUiUpdateTime >= UI_UPDATE_INTERVAL_MS) {
                                lastUiUpdateTime = now;
                                Platform.runLater(() -> mainFrequencyField.setText(String.format("%.2f Hz", frequency)));
                            }
                        } else {
                            // Skip frames if not enough data (don't accumulate lag)
                            try {
                                Thread.sleep(SLEEP_INTERVAL_MS);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (IOException ex) {
                    if (isCapturing.get()) {
                        Logger.getLogger(GUIFX.class.getName()).log(Level.WARNING, "Error during capture", ex);
                    }
                } finally {
                    try {
                        microphone.stop();
                        microphone.flush();
                        microphone.close();
                    } catch (RuntimeException e) {
                        Logger.getLogger(GUIFX.class.getName()).log(Level.WARNING, "Error closing microphone", e);
                    }
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

        } catch (LineUnavailableException ex) {
            isCapturing.set(false);
            Logger.getLogger(GUIFX.class.getName()).log(Level.SEVERE, "Audio line unavailable", ex);
            showError("Audio Device Error", "Unable to access the audio input device.\nError: " + ex.getMessage());
        }
    }

    private void decodeSamples(byte[] audioByteBuffer, double[] audioBuffer, int fftSize,
                               int bytesPerSample, int channels, boolean bigEndian) {
        for (int i = 0; i < fftSize; i++) {
            if (channels == 2) {
                int leftSample = readSample(audioByteBuffer, i * bytesPerSample * 2, bytesPerSample, bigEndian);
                int rightSample = readSample(audioByteBuffer, i * bytesPerSample * 2 + bytesPerSample, bytesPerSample, bigEndian);
                audioBuffer[i] = (leftSample + rightSample) / 2.0;
            } else {
                audioBuffer[i] = readSample(audioByteBuffer, i * bytesPerSample, bytesPerSample, bigEndian);
            }
        }
    }

    private int readSample(byte[] buffer, int offset, int bytesPerSample, boolean bigEndian) {
        Preconditions.checkArgument(bytesPerSample == 2,
                "Only 16-bit (2-byte) samples are supported, got " + bytesPerSample);
        if (bigEndian) {
            int sample = buffer[offset] << 8;
            sample |= buffer[offset + 1] & 0xFF;
            return sample;
        } else {
            int sample = buffer[offset] & 0xFF;
            sample |= buffer[offset + 1] << 8;
            return sample;
        }
    }

    private void stopCapture() {
        isCapturing.set(false);
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

    private void updateDeviceInfo() {
        Platform.runLater(() -> {
            fftVendorField.setText("Apache Commons Math");
            fftProcessorField.setText("Pure Java FFT");
            openCLVersionField.setText("N/A");
            driverVersionField.setText("N/A");
        });
    }

    private void applyHammingWindow(double[] samples) {
        com.gpoole.dsp.signal.WindowFunction.HAMMING.apply(samples);
    }

    public class FrequencyScanner {

        private final FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        private final double[] recentFrequencies;
        private int freqIndex = 0;
        private boolean filled = false;
        private double lastFrequency = 0;
        private final int smoothingSamples;
        private final double[] fftBuffer;

        public FrequencyScanner(int fftSize) {
            this.smoothingSamples = FREQUENCY_SMOOTHING_SAMPLES;
            this.recentFrequencies = new double[smoothingSamples];
            // Pre-allocate FFT buffer at exact power of 2
            int powerOf2 = 1;
            while (powerOf2 < fftSize) powerOf2 <<= 1;
            this.fftBuffer = new double[powerOf2];
        }

        public double extractFrequency(double[] sampleData, int sampleRate) throws InterruptedException, IOException {
            long startTime = System.currentTimeMillis();

            int n = sampleData.length;
            int powerOf2 = fftBuffer.length;

            // Copy to pre-allocated buffer and pad with zeros
            System.arraycopy(sampleData, 0, fftBuffer, 0, n);
            // Normalize 16-bit samples to [-1.0, 1.0] for meaningful FFT scale
            double scale = 32768.0;
            for (int i = 0; i < n; i++) {
                fftBuffer[i] /= scale;
            }
            for (int i = n; i < powerOf2; i++) {
                fftBuffer[i] = 0.0;
            }

            org.apache.commons.math3.complex.Complex[] fftResult = fft.transform(fftBuffer, TransformType.FORWARD);
            int spectrumLength = fftResult.length / 2;
            double[] magnitudes = new double[spectrumLength];
            double maxMag = Double.NEGATIVE_INFINITY;
            int maxInd = -1;
            double mean = 0.0;

            // Skip DC component (i=0)
            for (int i = 1; i < spectrumLength; i++) {
                double real = fftResult[i].getReal();
                double imag = fftResult[i].getImaginary();
                double mag = Math.sqrt(real * real + imag * imag);
                mean += mag;
                magnitudes[i] = mag;
            }

            double avgMag = mean / (spectrumLength - 1);
            double threshold = avgMag * NOISE_THRESHOLD_MULTIPLIER;

            for (int i = 1; i < spectrumLength; i++) {
                if (magnitudes[i] > threshold && magnitudes[i] > maxMag) {
                    maxMag = magnitudes[i];
                    maxInd = i;
                }
            }

            // Normalize to peak magnitude for relative dB display
            double peakMag = maxMag > 0 ? maxMag : 1.0;
            for (int i = 0; i < magnitudes.length; i++) {
                if (magnitudes[i] > 0) {
                    magnitudes[i] = 20 * FastMath.log10(magnitudes[i] / peakMag);
                } else {
                    magnitudes[i] = MAGNITUDE_FLOOR_DB;
                }
            }

            xy.setData(magnitudes, sampleRate);
            long endTime = System.currentTimeMillis();
            long now2 = System.currentTimeMillis();
            if (now2 - lastPeriodUpdateTime >= PERIOD_UPDATE_INTERVAL_MS) {
                lastPeriodUpdateTime = now2;
                Platform.runLater(() -> executionPeriodField.setText((endTime - startTime) + "ms"));
            }

            if (maxInd > 0) {
                double frequency = (double) (sampleRate * maxInd / fftResult.length);
                if (filled) {
                    recentFrequencies[freqIndex] = frequency;
                    freqIndex = (freqIndex + 1) % smoothingSamples;
                    double smoothed = 0;
                    for (double f : recentFrequencies) smoothed += f;
                    smoothed /= smoothingSamples;
                    lastFrequency = smoothed;
                    return smoothed;
                }

                // Fill phase — collect samples without averaging
                recentFrequencies[freqIndex] = frequency;
                freqIndex++;
                if (freqIndex == smoothingSamples) {
                    filled = true;
                    freqIndex = 0;
                }
                return frequency;
            }
            return lastFrequency;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
