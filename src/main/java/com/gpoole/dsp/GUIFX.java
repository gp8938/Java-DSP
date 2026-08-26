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
import com.gpoole.dsp.signal.DSP;
import com.gpoole.dsp.signal.WindowFunction;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

        channelsField = createReadOnlyTextField();
        bitsPerSampleField = createReadOnlyTextField();
        executionPeriodField = createReadOnlyTextField();
        samplePeriodField = createReadOnlyTextField();

        bytesPerSampleSpinner = new Spinner<>(2, 2, 2);

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
        stopButton.setOnAction(e -> stopCapture());

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
                    if (hasTargetDataLine(m)) {
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

    private static boolean hasTargetDataLine(Mixer m) {
        for (Line.Info info : m.getTargetLineInfo()) {
            if (info instanceof DataLine.Info di && TargetDataLine.class.isAssignableFrom(di.getLineClass())) {
                return true;
            }
        }
        return false;
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

        Mixer mixer = resolveMixer(selectedSource);
        if (mixer == null) return;

        int sampleRate = Integer.parseInt(samplingFrequencyComboBox.getSelectionModel().getSelectedItem());
        final int bytes = bytesPerSampleSpinner.getValue();

        AudioFormat format = negotiateFormat(mixer, sampleRate, bytes);
        if (format == null) return;

        try {
            final AudioFormat finalFormat = format;
            final TargetDataLine microphone = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, finalFormat));
            microphone.open();
            microphone.start();

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
                            System.arraycopy(
                                    DSP.bytesToSamples(audioByteBuffer, channels, lockedFftSize, lockedBytesPerSample, bigEndian),
                                    0, audioBuffer, 0, lockedFftSize);

                            WindowFunction.HAMMING.apply(audioBuffer);
                            double frequency = processFrame(audioBuffer, (int) finalFormat.getSampleRate());
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

    private void stopCapture() {
        isCapturing.set(false);
        captureButton.setDisable(false);
        stopButton.setDisable(true);
        audioSourceComboBox.setDisable(false);
        samplingFrequencyComboBox.setDisable(false);
        fftSizeComboBox.setDisable(false);
        statusLabel.setText("Stopped");
    }

    private Mixer resolveMixer(String sourceName) {
        for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
            if (mi.getName().equals(sourceName)) return AudioSystem.getMixer(mi);
        }
        showError("Device Unavailable", "Selected audio source is no longer available.");
        return null;
    }

    private AudioFormat negotiateFormat(Mixer mixer, int sampleRate, int bytes) {
        AudioFormat requested = new AudioFormat((float) sampleRate, bytes * 8, channels, true, true);
        if (mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, requested))) {
            return requested;
        }
        AudioFormat fallback = requested;
        for (Line.Info info : mixer.getTargetLineInfo()) {
            if (info instanceof DataLine.Info) {
                for (AudioFormat f : ((DataLine.Info) info).getFormats()) {
                    if (f.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && f.getSampleRate() > 0) {
                        fallback = f;
                        break;
                    }
                }
                if (fallback != requested) break;
            }
        }
        if (mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, fallback))) {
            return fallback;
        }
        showError("Unsupported Format", "No supported audio format found for this device.");
        return null;
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

    // -------- per-frame spectrum analysis (delegates to DSP) --------------

    private final double[] recentFrequencies = new double[FREQUENCY_SMOOTHING_SAMPLES];
    private int freqIndex = 0;
    private boolean freqWindowFilled = false;
    private double lastFrequency = 0;

    /**
     * Analyse one windowed frame: update the spectrum chart and
     * execution-period readout, and return the smoothed dominant frequency.
     */
    private double processFrame(double[] buffer, int sampleRate) {
        long startTime = System.currentTimeMillis();

        double[] mag = DSP.magnitudeSpectrum(buffer);
        int peakBin = findPeakBinAboveNoiseFloor(mag);
        toDecibels(mag);

        xy.setData(mag, sampleRate);
        updateExecutionPeriod(startTime);

        if (peakBin > 0) {
            double frequency = sampleRate * (double) peakBin / (2 * (mag.length - 1));
            lastFrequency = smoothFrequency(frequency);
        }
        return lastFrequency;
    }

    private int findPeakBinAboveNoiseFloor(double[] magnitudes) {
        double mean = 0.0;
        for (int i = 1; i < magnitudes.length; i++) {
            mean += magnitudes[i];
        }
        double threshold = (mean / (magnitudes.length - 1)) * NOISE_THRESHOLD_MULTIPLIER;
        int maxInd = -1;
        double maxMag = Double.NEGATIVE_INFINITY;
        for (int i = 1; i < magnitudes.length; i++) {
            if (magnitudes[i] > threshold && magnitudes[i] > maxMag) {
                maxMag = magnitudes[i];
                maxInd = i;
            }
        }
        return maxInd;
    }

    private void toDecibels(double[] magnitudes) {
        double peakMag = Double.NEGATIVE_INFINITY;
        for (double m : magnitudes) {
            if (m > peakMag) peakMag = m;
        }
        peakMag = peakMag > 0 ? peakMag : 1.0;
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = magnitudes[i] > 0
                ? 20 * Math.log10(magnitudes[i] / peakMag)
                : MAGNITUDE_FLOOR_DB;
        }
    }

    private void updateExecutionPeriod(long startTime) {
        long now = System.currentTimeMillis();
        if (now - lastPeriodUpdateTime >= PERIOD_UPDATE_INTERVAL_MS) {
            lastPeriodUpdateTime = now;
            Platform.runLater(() -> executionPeriodField.setText((now - startTime) + "ms"));
        }
    }

    private double smoothFrequency(double frequency) {
        if (freqWindowFilled) {
            recentFrequencies[freqIndex] = frequency;
            freqIndex = (freqIndex + 1) % FREQUENCY_SMOOTHING_SAMPLES;
            double smoothed = 0;
            for (double f : recentFrequencies) smoothed += f;
            smoothed /= FREQUENCY_SMOOTHING_SAMPLES;
            lastFrequency = smoothed;
            return smoothed;
        }
        recentFrequencies[freqIndex] = frequency;
        freqIndex++;
        if (freqIndex == FREQUENCY_SMOOTHING_SAMPLES) {
            freqWindowFilled = true;
            freqIndex = 0;
        }
        return frequency;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
