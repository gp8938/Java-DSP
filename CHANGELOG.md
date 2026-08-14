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

## [1.1.0] - 2026-08-13

### Removed
- **SignalPipeline** fluent builder (173 lines) — unused abstraction; direct function calls are simpler
- **Preconditions** utility (97 lines) — replaced with `Objects.requireNonNull` + inline checks
- **4 unused WindowFunction types** (RECTANGULAR, HANNING, BLACKMAN, FLAT_TOP) — only HAMMING used in practice
- **FFTProcessingTest** (153 lines) — duplicated Commons Math tests
- **GUITest** (24 lines) — empty ApplicationTest
- **HeadlessCondition** (15 lines) — use `@DisabledIfSystemProperty` instead
- **SignalPipelineTest** (194 lines) — tests deleted class
- **WindowFunctionPropertyTest** (67 lines) — tests deleted windows
- **PreconditionsTest** (106 lines) — tests deleted class

### Changed
- **DSP.zeroPadToPowerOfTwo**: uses `Integer.highestOneBit` (native) instead of hand-rolled bit shift
- **DSP.bytesToSamples**: uses `ByteBuffer` (stdlib) instead of manual bitwise decode
- **DSP.dominantFrequency**: single overload with Hamming window (removed WindowFunction parameter)
- **WindowFunction**: enum shrunk to single `HAMMING` constant
- **Test suites**: reduced parameterized test matrices (27→8, 26→7, 26→7 cases)

### Fixed
- **Net reduction**: 925 lines removed (1007 deleted, 82 added)
- **Dependencies**: unchanged (commons-math3 still required for FFT)

[1.1.0]: https://github.com/gpoole/Java-DSP/compare/v1.0.0...v1.1.0

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
