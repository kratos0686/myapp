# Contributing to Room Scanner

Thank you for your interest in contributing to Room Scanner! This document provides guidelines for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Submitting Changes](#submitting-changes)
- [Testing](#testing)
- [Documentation](#documentation)

## Code of Conduct

This project adheres to a code of conduct that all contributors are expected to follow:

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17
- Android SDK (API level 26+)
- Git
- An Android device with ARCore support (for full testing)

### Initial Setup

1. **Fork the repository** on GitHub

2. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR-USERNAME/Android2.0roomscanner.git
   cd Android2.0roomscanner
   ```

3. **Set up upstream remote:**
   ```bash
   git remote add upstream https://github.com/kratos0686/Android2.0roomscanner.git
   ```

4. **Configure Firebase:**
   - Copy `app/google-services.json.example` to `app/google-services.json`
   - For Firebase features, replace with your actual Firebase configuration
   - See [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) for details

5. **Build the project:**
   ```bash
   ./gradlew build
   ```

6. **Run tests:**
   ```bash
   ./gradlew test
   ```

## Development Workflow

### Creating a Feature Branch

Always create a new branch for your work:

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions or updates

### Keeping Your Fork Updated

Regularly sync your fork with the upstream repository:

```bash
git fetch upstream
git checkout main
git merge upstream/main
git push origin main
```

### Making Changes

1. **Make your changes** following the [coding standards](#coding-standards)
2. **Test your changes** thoroughly
3. **Update documentation** if needed
4. **Run linters and tests:**
   ```bash
   ./gradlew lint
   ./gradlew test
   ./gradlew check
   ```

## Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation
- Place opening braces at the end of the line
- Use camelCase for function and variable names
- Use PascalCase for class names
- Use UPPER_SNAKE_CASE for constants

### Code Organization

- **Package Structure**: Follow the existing package structure
  - `data/` - Database entities, DAOs, repositories
  - `arcore/` - ARCore integration
  - `ml/` - Machine Learning components
  - `firebase/` - Firebase integration
  - `ui/` - UI components and screens

- **File Naming**: Match class names (e.g., `ScanEntity.kt`)

- **Import Organization**: Group and sort imports
  - Android imports
  - Third-party imports
  - Project imports

### Architecture Patterns

- **MVVM**: Use ViewModels for UI logic
- **Repository Pattern**: Abstract data sources
- **Single Responsibility**: Each class should have one purpose
- **Dependency Injection**: Use constructor injection where possible

### Compose Best Practices

- Keep composables small and focused
- Hoist state to the appropriate level
- Use `remember` and `derivedStateOf` to optimize recomposition
- Follow Material Design 3 guidelines
- Add `@Preview` annotations for UI components

### Testing Standards

- Write unit tests for business logic
- Use meaningful test names: `functionName_scenario_expectedResult()`
- Follow Arrange-Act-Assert pattern
- Mock external dependencies (Firebase, ARCore, etc.)
- Aim for >80% code coverage on new code

## Submitting Changes

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, semicolons, etc.)
- `refactor` - Code refactoring
- `perf` - Performance improvements
- `test` - Adding or updating tests
- `chore` - Maintenance tasks
- `ci` - CI/CD changes

**Examples:**
```
feat(database): add scan metadata support
fix(arcore): resolve camera permission crash on Android 13
docs(readme): update Firebase setup instructions
test(scan-repository): add unit tests for delete operations
```

### Pull Request Process

1. **Update your branch** with the latest main:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Push to your fork:**
   ```bash
   git push origin your-branch-name
   ```

3. **Create a Pull Request** on GitHub

4. **PR Description**: Include:
   - What changes were made and why
   - Link to related issues
   - Screenshots (for UI changes)
   - Testing performed
   - Breaking changes (if any)

5. **Code Review**: Address feedback from reviewers

6. **CI Checks**: Ensure all CI checks pass

7. **Merge**: Once approved, a maintainer will merge your PR

### Pull Request Checklist

Before submitting, verify:

- [ ] Code follows the style guidelines
- [ ] Tests added/updated and passing
- [ ] Documentation updated (if needed)
- [ ] Commit messages follow conventions
- [ ] No merge conflicts with main branch
- [ ] All CI checks pass
- [ ] No sensitive data or secrets committed
- [ ] Self-reviewed the changes
- [ ] Screenshots included (for UI changes)

## Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# All checks (lint + tests)
./gradlew check
```

### Writing Tests

- **Location**:
  - Unit tests: `app/src/test/`
  - Instrumentation tests: `app/src/androidTest/`

- **Dependencies**:
  - JUnit 4 for test framework
  - MockK for mocking
  - Espresso for UI tests

- **Example**:
  ```kotlin
  @Test
  fun insertScan_validData_returnsId() {
      // Arrange
      val scan = ScanEntity(roomName = "Test Room", length = 5.0f)
      
      // Act
      val id = repository.insertScan(scan)
      
      // Assert
      assertTrue(id > 0)
  }
  ```

## Documentation

### When to Update Documentation

Update documentation when:
- Adding new features
- Changing APIs or interfaces
- Modifying build/setup process
- Fixing bugs that affect usage
- Adding new dependencies

### Documentation Files

- **README.md** - Project overview and quick start
- **LAUNCH_GUIDE.md** - Detailed setup instructions
- **docs/ARCHITECTURE.md** - Architecture overview
- **docs/ROOM_DATABASE.md** - Database documentation
- **docs/FIREBASE_SETUP.md** - Firebase configuration
- **docs/ARCORE_INTEGRATION.md** - ARCore implementation
- **docs/TFLITE_DEPLOYMENT.md** - ML model deployment
- **Code Comments** - Complex logic and algorithms

### Documentation Style

- Use clear, concise language
- Include code examples where helpful
- Add diagrams for complex concepts
- Keep documentation up-to-date with code changes

## Issue Reporting

### Bug Reports

Include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Device/OS information
- Relevant logs or screenshots
- Minimal code example (if applicable)

### Feature Requests

Include:
- Problem statement (what need does this address?)
- Proposed solution
- Alternative solutions considered
- Willingness to implement

## Questions and Support

- **Documentation**: Check the `docs/` directory first
- **Issues**: Search existing issues before creating new ones
- **Discussions**: Use GitHub Discussions for questions

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

## Recognition

Contributors will be recognized in the project. Thank you for making Room Scanner better!

---

**For GitHub Copilot coding agent contributors**: See [.github/copilot-instructions.md](.github/copilot-instructions.md) for additional guidelines.
