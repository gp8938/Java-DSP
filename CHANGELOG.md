# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Cross-platform release automation for Windows, macOS, and Linux
- Platform-specific JavaFX classifier detection via Maven profiles
- CONTRIBUTING.md with development guidelines
- CHANGELOG.md for version tracking

### Changed

- Updated Java version from 25 to 21 (current LTS)
- Modernized release workflow using `softprops/action-gh-release`
- Improved README with accurate feature descriptions

### Fixed

- JaCoCo exclusions for JavaFX GUI classes (0% coverage artifacts)
- XYLineChartTest conditional execution in headless environments

## [1.0.0] - 2024-XX-XX

### Added

- Real-time audio capture with multi-device support
- FFT analysis using Apache Commons Math
- JavaFX-based frequency visualization with XYLineChart
- Hamming window function for spectral leakage reduction
- Noise threshold filtering (3x average magnitude)
- DC component removal in signal processing pipeline
- 5-sample frequency smoothing algorithm
- Configurable FFT sizes (16 to 65536 samples)
- SignalPipeline fluent API for chaining DSP operations
- WindowFunction enum (Hamming, Hanning, Blackman, Flat-top)
- Preconditions utility for input validation
- Comprehensive test suite with 92% code coverage
- JaCoCo code coverage reporting
- GitHub Actions CI/CD pipeline
- Cross-platform builds (Windows, macOS, Linux)

### Changed

- Migrated UI from Swing/JFreeChart to JavaFX
- Removed PMD (compatibility issues)
- Removed FlatLaf (now using native JavaFX styling)

### Fixed

- Audio buffer calculation for stereo/mono handling
- CI test failures in headless environments

[Unreleased]: https://github.com/gpoole/Java-DSP/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/gpoole/Java-DSP/releases/tag/v1.0.0
