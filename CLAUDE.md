# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**crp-flowable-ex** is a collection of extensions for [Flowable](https://www.flowable.org/), an open-source Business Process Management (BPM) and workflow engine. This is a multi-module Maven project that provides enhancements, utilities, and integrations for Flowable-based applications.

**Key Technologies:**
- Java 17, Maven
- Flowable 7.1.0
- Spring Framework 6.1.13 & Spring Boot 3.3.4
- Groovy 4.0.26 for script tasks
- JUnit 5, Spock framework, AssertJ
- Published to Maven Central Repository

## Project Structure

### Core Modules

**crp-flowable-form** (Most actively developed)
- Extensions to Flowable's form engine
- Contains 7 sub-modules:
  - `crp-flowable-form-engine`: Core form engine implementation
  - `crp-flowable-form-model`: Data models for forms
  - `crp-flowable-form-engine-configurator`: Engine configuration
  - `crp-flowable-form-json-converter`: JSON serialization/deserialization
  - `crp-flowable-form-rest`: REST API endpoints for forms
  - `crp-flowable-form-spring`: Spring integration
  - `crp-flowable-form-spring-configurator`: Spring Auto-configuration

**crp-flowable-groovy**
- Enables Groovy script debugging in Flowable tasks
- Generates local filesystem script files for IDE debugging
- Integrates with Java debuggers for script task execution

### Utility/Extension Modules

- **crp-flowable-assert**: Custom assertions for Flowable testing (AssertJ-based)
- **crp-flowable-spock**: Spock framework integration for Groovy-based tests
- **crp-flowable-coverage**: Code coverage utilities
- **crp-flowable-shell**: Shell/CLI utilities
- **crp-flowable-ai**: Spring AI integration with Flowable
- **crp-flowable-ui**: UI components (currently in development)
- **crp-bpm-sonarqube-plugin**: SonarQube plugin for BPM analysis
- **crp-flowable-ex-bom**: Bill of Materials (BOM) POM for dependency management

## Build & Test Commands

### Using Maven Wrapper (Recommended)

```bash
# Full build
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Run only unit tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl crp-flowable-form

# Run a single test class
./mvnw test -Dtest=YourTestClassTest

# Run a single test method
./mvnw test -Dtest=YourTestClassTest#testMethodName

# Build with linting (compile checks)
./mvnw clean compile

# Generate Javadoc
./mvnw javadoc:jar

# Package JAR files
./mvnw package -DskipTests
```

### Release Process

```bash
# Prepare and perform release (with GPG signing when on main/ci-cd)
./mvnw release:prepare release:perform -DskipTests=true -Pci/cd
```

### Key Maven Profiles

- **ci/cd**: Activates GPG signing and Maven Central publishing configuration

## Code Architecture

### Dependency Management

The root `pom.xml` acts as a centralized dependency manager with:
- **Parent BOM**: Manages all dependency versions across modules
- **Test Dependencies**: JUnit 5, Mockito, AssertJ, H2 database, HikariCP configured at root level
- **Framework Stack**: Spring Framework, Spring Boot, Flowable engine APIs

### Module Organization Pattern

Each module follows Maven conventions:
- Source code: `src/main/java/`
- Tests: `src/test/java/` (JUnit 5)
- Groovy tests: `src/test/groovy/` (Spock framework where applicable)
- Resources: `src/main/resources/` and `src/test/resources/`

### Testing Framework

- **Primary**: JUnit 5 (Jupiter) with assertj-core assertions
- **Alternative**: Spock framework for BDD-style Groovy tests
- **Test Database**: H2 (in-memory) for integration tests
- **Mocking**: Mockito with JUnit 5 integration
- **Runners**: Maven Surefire (includes `*Test.java` and `*Spec.java` by default, excludes `*IT.java`)

### Form Module Architecture (Key Reference)

The form module uses a dependency management layer:
- **Model** → **Engine** → **Engine Configurator** → **Spring Integration**
- **JSON Converter** handles serialization between form models and JSON
- **REST API** layer provides HTTP endpoints for form operations
- Spring Boot auto-configuration via `crp-flowable-form-spring-configurator`

## Common Development Tasks

### Adding a New Feature

1. **Make changes** in the appropriate module(s)
2. **Write tests** in `src/test/java` or `src/test/groovy`
3. **Run module tests**: `./mvnw test -pl module-name`
4. **Run full build**: `./mvnw clean install`
5. **Commit**: Use conventional commit format

### Debugging Groovy Script Tasks

1. Use the `crp-flowable-groovy` module configuration
2. Configure `ProcessScriptFileNameResolverFactory` in Flowable engine config
3. Scripts are extracted to `src/test/groovy/bpmn/` or `src/test/groovy/cmmn/`
4. Set breakpoints in generated `.groovy` files and debug JUnit tests normally

### Testing Flowable Processes

1. Place BPMN/CMMN files in `src/test/resources/`
2. Use `@Deployment` annotation to load process definitions
3. Use `crp-flowable-assert` module for domain-specific assertions
4. Inject Flowable services (ProcessEngine, RepositoryService, RuntimeService, etc.) as test parameters

## Important Notes

- The project publishes to **Maven Central Repository** automatically via the `central-publishing-maven-plugin`
- **Java Release**: Version 17 (enforced across all modules)
- **Source Encoding**: UTF-8
- **Compiler flags**: `-parameters` flag enabled for reflection-based dependency injection
- The `crp-flowable-form` module was recently added to an active development branch (`forms`); it's currently commented out in the root `pom.xml`
- The form module uses Spring Framework 6.0.14 (slightly older than root's 6.1.13) and Jackson 2.13.5; watch for version alignment issues
