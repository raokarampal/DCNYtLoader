# DCN YtLoader

A modern **YouTube Downloader** built with **Kotlin Multiplatform** and **Compose Multiplatform**,
featuring a GraphQL backend powered by Ktor. This application allows users to download YouTube
videos and audio with a beautiful cross-platform UI.

## 🚀 Features

- **Cross-Platform Support**: Android, Desktop (JVM), and Server backends
- **Modern UI**: Built with Compose Multiplatform for consistent experience across platforms
- **GraphQL API**: Efficient backend communication with real-time updates
- **YouTube Downloads**: Powered by yt-dlp for reliable video/audio extraction
- **FFmpeg Integration**: Advanced media processing capabilities
- **Docker Support**: Easy deployment with containerization
- **MVI Architecture**: Clean, maintainable code following best practices

## 📁 Project Structure

```
DCNYtLoader/
├── composeApp/          # Compose Multiplatform UI application
│   ├── src/commonMain/  # Shared UI code
│   ├── src/androidMain/ # Android-specific code
│   └── src/jvmMain/     # Desktop-specific code
├── ytdlbackend/         # Ktor GraphQL server
├── shared/              # Shared business logic and models
├── gradle/              # Gradle configuration
└── Dockerfile           # Container configuration
```

### Modules Overview

- **`composeApp`**: Cross-platform UI built with Compose Multiplatform, targeting Android and
  Desktop (JVM)
- **`ytdlbackend`**: Ktor-based GraphQL server handling YouTube downloads with yt-dlp integration
- **`shared`**: Common code shared between all targets (models, utilities, etc.)

## 🛠️ Technologies Used

- **Kotlin Multiplatform** (2.2.10)
- **Compose Multiplatform** (1.9.0-beta03)
- **Ktor** (3.2.3) - Server framework
- **GraphQL Kotlin** (9.0.0-alpha.8) - GraphQL server implementation
- **Apollo Kotlin** (4.3.2) - GraphQL client
- **Koin** (4.1.0) - Dependency injection
- **Coil** (3.3.0) - Image loading
- **yt-dlp** - YouTube download engine
- **FFmpeg** - Media processing

## 🏗️ Setup and Installation

### Prerequisites

- **JDK 21** or higher
- **Android SDK** (for Android builds)
- **Docker** (for containerized deployment)
- **Git**

### 🔧 Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd DCNYtLoader
   ```

2. **Configure environment**

   Update `gradle.properties` with your preferred URLs:
   ```properties
   # Development URLs
   local.url=http://localhost:8083/graphql
   local.url.ws=ws://localhost:8083/graphql
   
   # Production URLs
   base.url=https://your-production-domain.com/graphql
   base.url.ws=wss://your-production-domain.com/graphql
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

## 🚀 Running the Application

### Backend Server

#### Development Mode

```bash
# Run the backend server in development mode
./gradlew :ytdlbackend:run

# Server will start on http://localhost:8083
# GraphQL endpoint: http://localhost:8083/graphql
```

#### Production Build

```bash
# Build distribution
./gradlew :ytdlbackend:installDist

# Run the distribution
./ytdlbackend/build/install/ytdlbackend/bin/ytdlbackend
```

### Desktop Application

```bash
# Run desktop app in development
./gradlew :composeApp:run

# Build desktop distribution
./gradlew :composeApp:packageDistributionForCurrentOS
```

### Android Application

```bash
# Install debug build on connected device
./gradlew :composeApp:installDebug

# Build release APK
./gradlew :composeApp:assembleRelease
```

## 🐳 Docker Deployment

### Build and Run with Docker

```bash
# Build the backend first
./gradlew -DincludeComposeApp=false :ytdlbackend:installDist

# Build Docker image
docker build --no-cache -t ytdl-dcn-backend .

# Run container
docker run -d \
  --name ytdlbackend \
  -p 8083:8083 \
  -v /path/to/downloads:/app/ytdlp \
  -e YTDLBACKEND_OPTS="-Dconfig.resource=application.conf -Dconfig.resource=application-prod.conf" \
  --restart always \
  ytdl-dcn-backend:latest
```

### Docker Compose (Recommended)

Create a `docker-compose.yml`:

```yaml
version: '3.8'
services:
  ytdl-backend:
    build: .
    ports:
      - "8083:8083"
    volumes:
      - ./downloads:/app/ytdlp
    environment:
      - YTDLBACKEND_OPTS=-Dconfig.resource=application.conf -Dconfig.resource=application-prod.conf
    restart: unless-stopped
```

Run with:

```bash
docker-compose up -d
```

## 📋 Essential Gradle Tasks

### Building

```bash
# Clean build
./gradlew clean build

# Build specific module
./gradlew :composeApp:build
./gradlew :ytdlbackend:build
./gradlew :shared:build
```

### Running

```bash
# Run backend server
./gradlew :ytdlbackend:run

# Run desktop app
./gradlew :composeApp:run

# Install Android debug
./gradlew :composeApp:installDebug
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :shared:test
```

### Distribution

```bash
# Create backend distribution
./gradlew :ytdlbackend:installDist

# Package desktop app
./gradlew :composeApp:packageDistributionForCurrentOS

# Build Android APK
./gradlew :composeApp:assembleRelease
```

## 🎯 Usage Examples

### Starting the Full Stack

1. **Start the backend server**:
   ```bash
   ./gradlew :ytdlbackend:run
   ```

2. **Launch the desktop app**:
   ```bash
   ./gradlew :composeApp:run
   ```

3. **Install Android app**:
   ```bash
   ./gradlew :composeApp:installDebug
   ```

### GraphQL API Examples

Access the GraphQL playground at `http://localhost:8083/graphql`

**Query video information**:

```graphql
query GetVideoInfo($url: String!) {
  videoInfo(url: $url) {
    title
    duration
    thumbnail
    formats {
      formatId
      ext
      quality
      filesize
    }
  }
}
```

**Start download**:

```graphql
mutation StartDownload($url: String!, $format: String!) {
  startDownload(url: $url, format: $format) {
    id
    status
    progress
  }
}
```

### Configuration Examples

**Development configuration** (`application-dev.conf`):

```hocon
ktor {
    development = true
    deployment {
        port = 8083
    }
}

ytdlp {
    outputPath = "./downloads"
    tempPath = "./temp"
}
```

**Production configuration** (`application-prod.conf`):

```hocon
ktor {
    development = false
    deployment {
        port = 8083
    }
}

ytdlp {
    outputPath = "/app/ytdlp"
    tempPath = "/tmp"
}
```

## 🏗️ Architecture

This project follows **MVI (Model-View-Intent)** architecture with:

- **Repository Pattern**: Data layer abstraction
- **ViewModels**: UI state management
- **Dependency Injection**: Koin for clean dependency management
- **GraphQL**: Efficient client-server communication
- **Multiplatform Shared Logic**: Business logic shared across platforms

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Troubleshooting

### Common Issues

1. **GraphQL schema issues**: Run `./gradlew :composeApp:downloadApolloSchema`
2. **Docker build fails**: Ensure backend is built first with `./gradlew :ytdlbackend:installDist`
3. **Android build issues**: Check Android SDK path in `local.properties`

### Support

For issues and questions, please open an issue on the GitHub repository.

---

**Built with ❤️ using Kotlin Multiplatform**