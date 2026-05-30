# Contributing to Java-DSP

Thank you for your interest in contributing to Java-DSP! This document provides guidelines and instructions for contributing.

## Development Setup

### Prerequisites

- Java 25 JDK or higher
- Maven 3.6+
- Git

### Clone and Build

```bash
git clone https://github.com/gpoole/Java-DSP.git
cd Java-DSP
mvn clean compile
```

### Running the Application

```bash
# Run with JavaFX plugin (recommended)
mvn javafx:run

# Or package and run
mvn clean package
java -jar target/DSP-Java-*.jar
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report
# View report at target/site/jacoco/index.html
```

## Code Style

- Follow standard Java conventions (Google Java Style Guide recommended)
- Use meaningful variable and method names
- Add Javadoc for all public APIs
- Keep methods focused and single-purpose
- Maximum line length: 120 characters

## Pull Request Process

1. **Fork and Branch**
   ```bash
   git checkout -b feature/YourFeatureName
   # or
   git checkout -b fix/YourFixDescription
   ```

2. **Make Changes**
   - Write clean, documented code
   - Add tests for new functionality
   - Ensure all tests pass: `mvn test`

3. **Commit**
   - Use clear, descriptive commit messages
   - Reference issue numbers if applicable
   - Example: `Add Hann window function support (#123)`

4. **Submit Pull Request**
   - Fill out the PR template
   - Describe what changed and why
   - Link any related issues
   - Ensure CI checks pass

## Testing Guidelines

### Test Coverage

- Maintain or improve code coverage (currently 92%)
- Write unit tests for all public methods
- Use parameterized tests where appropriate
- Test edge cases and error conditions

### Test Categories

- **Unit Tests**: Test individual classes in isolation
- **Integration Tests**: Test component interactions
- **Headless Tests**: Tests that run in CI (no GUI)

### JavaFX Tests

JavaFX tests are automatically skipped in headless environments. To test JavaFX components locally:

```bash
# Run with display available
mvn test

# Force headless mode (CI simulation)
mvn test -Djava.awt.headless=true
```

## Release Process

Releases are automated via GitHub Actions:

1. Update version in `pom.xml` if needed
2. Update `CHANGELOG.md`
3. Create and push a tag:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```
4. GitHub Actions builds and uploads artifacts for all platforms

## Code Review

All submissions require review before merging:

- At least one approval from a maintainer
- All CI checks must pass
- No merge conflicts
- Code coverage maintained

## Reporting Issues

When reporting bugs, please include:

- Java version (`java -version`)
- Operating system and version
- Steps to reproduce
- Expected vs actual behavior
- Any error messages or stack traces

## Feature Requests

Feature requests are welcome! Please:

- Check existing issues first
- Describe the use case
- Explain why it would be valuable
- Consider contributing the feature yourself

## Questions?

Feel free to open an issue for questions or join discussions.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
