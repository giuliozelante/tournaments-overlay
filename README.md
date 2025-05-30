# Tournaments Overlay

A Java web application that consumes the Start.GG GraphQL API to display tournament information, events, standings, and brackets.

## Features

- Browse tournaments from Start.gg
- View tournament events
- Check standings for events
- View bracket information

## Prerequisites

- Java 21 or higher
- Gradle
- An API key for the Start.GG API

## Setup

1. Clone the repository
2. Set your Start.gg API token using one of these methods:

   **Method 1 - Environment Variable (Recommended):**
   
   Windows PowerShell:
   ```powershell
   $env:GRAPHQL_TOKEN="your-token-here"
   ```
   
   Windows CMD:
   ```cmd
   set GRAPHQL_TOKEN=your-token-here
   ```
   
   Linux/macOS:
   ```bash
   export GRAPHQL_TOKEN="your-token-here"
   ```
   
   **Method 2 - Application Properties:**
   
   Add this to `src/main/resources/application.yml`:
   ```yaml
   graphql:
     token: your-token-here
   ```

3. Run the application:
   ```
   ./gradlew run
   ```

4. Open your browser to `http://localhost:8080`

## GraphiQL Interface

You can explore the GraphQL API using the built-in GraphiQL interface at `http://localhost:8080/graphiql`.

## Technology Stack

- Micronaut Framework
- Thymeleaf Templates
- GraphQL Client
- WebJars for frontend dependencies

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Building and Running

### Running in Development Mode

```bash
./gradlew run
```

The application will be available at http://localhost:8080

### Building a Native Image

```bash
./gradlew nativeCompile
```

The native executable will be located in `build/native/nativeCompile/`.

### Running the Native Image

```bash
build/native/nativeCompile/tournaments-overlay
```

## API Endpoints

- `/` - Home page
- `/tournaments` - List of tournaments
- `/events?tournamentId={id}` - Events for a specific tournament
- `/standings?eventId={id}` - Standings for a specific event
- `/sets?eventId={id}` - Bracket sets/matches for a specific event

## Configuration

Application configuration is stored in `src/main/resources/application.yml`.

## Logging

Logs are written to:
- Console
- `logs/application.log` (with daily rotation and size limits) 