# Java-DSP

[![Build and Test](https://github.com/YOUR_USERNAME/Java-DSP/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/YOUR_USERNAME/Java-DSP/actions/workflows/build-and-test.yml)
[![Release](https://github.com/YOUR_USERNAME/Java-DSP/actions/workflows/release.yml/badge.svg)](https://github.com/YOUR_USERNAME/Java-DSP/actions/workflows/release.yml)

A professional real-time digital signal processing application for audio analysis and frequency visualization. Built with Java 21, featuring an intuitive GUI and advanced signal processing capabilities.

## Features

### Core Functionality
- **Real-time Audio Capture**: Multi-device support with automatic source selection
- **FFT Analysis**: Fast Fourier Transform using Apache Commons Math (pure Java, cross-platform)
- **Frequency Visualization**: Live frequency spectrum chart with JFreeChart
- **Advanced Signal Processing**:
  - Hamming window function for reduced spectral leakage
  - Noise threshold filtering (3x average magnitude)
  - DC component removal
  - 5-sample frequency smoothing for stable readings
  - Configurable FFT size (512, 1024, 2048, 4096, 8192 samples)

### User Interface
- Modern look and feel with FlatLaf theme support (Dark/Light modes)
- Intuitive controls with real-time status updates
- Dynamic audio device selection
- Configurable sample rates (8kHz - 48kHz)
- Responsive chart with auto-scaling axes
- Performance optimized with 50ms update throttling

## Requirements

- **Java 21** or higher
- **Maven 3.6+** for dependency management
- Audio input device (microphone)
- Supported Platforms: Windows, macOS (including Apple Silicon), Linux

## Quick Start

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Java-DSP.git
   cd Java-DSP
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.gpoole.dsp.GUI"
   ```
   
   Or run the JAR directly:
   ```bash
   java -jar target/DSP-Java-1.0-SNAPSHOT.jar
   ```

### Usage

1. **Select Audio Source**: Choose your microphone from the dropdown menu
2. **Configure Settings**: 
   - Select sample rate (recommended: 48000 Hz)
   - Choose FFT size (recommended: 2048 for balanced resolution/performance)
3. **Start Capture**: Click "Start Capture" to begin real-time analysis
4. **View Results**: Watch the frequency spectrum update in real-time
5. **Stop Capture**: Click "Stop" when finished

## Technical Details

### Signal Processing Pipeline

```
Audio Input → Stereo to Mono → Hamming Window → FFT → Magnitude Calculation → 
Noise Filtering → DC Removal → Frequency Smoothing → Visualization
```

### Key Components

- **GUI.java**: Main application window with audio capture and control logic
- **XYLineChart.java**: Optimized chart component with update throttling
- **FFT Processing**: Apache Commons Math FastFourierTransformer
- **Audio API**: javax.sound.sampled for cross-platform audio capture

### Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Apache Commons Math | 3.6.1 | FFT computation |
| JFreeChart | 1.0.13 | Frequency visualization |
| FlatLaf | 3.6 | Modern UI theme |
| JUnit Jupiter | 5.12.2 | Unit testing |

## Development

### Building from Source

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Generate test reports
mvn surefire-report:report

# Package
mvn package
```

### Running Tests

The project includes comprehensive test suites:

- **FFTProcessingTest**: FFT functionality, peak detection, windowing, noise filtering
- **AudioFormatTest**: Audio format handling, sample conversion, device compatibility
- **XYLineChartTest**: Chart rendering, data updates, performance

```bash
mvn test
```

### Project Structure

```
Java-DSP/
├── src/
│   ├── main/java/com/gpoole/dsp/
│   │   ├── GUI.java              # Main application
│   │   └── XYLineChart.java      # Chart component
│   └── test/java/com/gpoole/dsp/
│       ├── FFTProcessingTest.java
│       ├── AudioFormatTest.java
│       └── XYLineChartTest.java
├── .github/workflows/
│   ├── build-and-test.yml        # CI pipeline
│   ├── release.yml               # Release automation
│   └── codeql.yml                # Security scanning
├── pom.xml
└── README.md
```

## CI/CD Pipeline

This project uses GitHub Actions for automated building, testing, and releases:

- **Build and Test**: Runs on every push/PR across Windows, macOS, and Linux
- **CodeQL Analysis**: Automated security vulnerability scanning
- **Release**: Automated releases on version tags (v*)

### Creating a Release

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This automatically triggers the release workflow, building artifacts for all platforms.

## Recent Improvements

- ✅ Fixed audio buffer calculation for stereo/mono handling
- ✅ Replaced OpenCL with pure Java FFT (Apple Silicon compatible)
- ✅ Implemented Hamming windowing for better frequency resolution
- ✅ Added frequency smoothing and noise threshold for stable readings
- ✅ Enhanced UI with Stop button and status indicators
- ✅ Optimized chart performance with throttled updates
- ✅ Improved error handling throughout the application
- ✅ Added comprehensive test coverage
- ✅ Set up automated CI/CD pipeline

## Troubleshooting

### No audio devices detected
- Ensure your microphone is connected and enabled in system settings
- Check system permissions for microphone access
- Try running with administrator/sudo privileges

### Poor frequency detection
- Increase FFT size for better resolution
- Ensure adequate signal strength (speak/whistle loudly)
- Try different sample rates
- Check for background noise

### Performance issues
- Reduce FFT size (e.g., 1024 instead of 4096)
- Lower sample rate
- Close other audio applications

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Apache Commons Math for robust FFT implementation
- JFreeChart for excellent charting capabilities
- FlatLaf for modern UI theming
- Java Sound API for cross-platform audio support