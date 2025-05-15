Let me analyze the project and provide a detailed setup guide.

## Project Analysis

### Frontend (Android App)
- Built with Android Studio
- Uses Retrofit for API calls
- Requires Android SDK
- Uses Gradle for build management

### Backend (Spring Boot)
- Built with Spring Boot 3.4.4
- Uses Maven for build management
- Requires Docker for sandboxing
- Uses Docker Compose for orchestration

## Detailed Setup Guide

### 1. Prerequisites Installation

#### For Frontend:
```bash
# Install Android Studio
# Download from: https://developer.android.com/studio

# Install JDK 17 or higher
sudo apt update
sudo apt install openjdk-17-jdk

# Install Android SDK
# This comes with Android Studio installation
```

#### For Backend:
```bash
# Install Docker
sudo apt update
sudo apt install docker.io

# Install Docker Compose
sudo apt install docker-compose

# Install Maven
sudo apt install maven

# Install JDK 17 or higher (if not already installed)
sudo apt install openjdk-17-jdk
```

### 2. Project Setup

#### Clone the Repository:
```bash
git clone https://github.com/yourusername/online-compiler.git
cd online-compiler
```

### 3. Backend Setup

#### Build the Backend:
```bash
cd backend
mvn clean install
```

#### Run with Docker Compose:
```bash
# Make sure you're in the backend directory
docker-compose up --build
```

The `docker-compose.yml` should look like this:
```yaml
version: '3'
services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    ports:
      - "5050:5050"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    volumes:
      - ./target:/app/target
    networks:
      - compiler-network

  sandbox:
    build:
      context: .
      dockerfile: Dockerfile.sandbox
    networks:
      - compiler-network

networks:
  compiler-network:
    driver: bridge
```

### 4. Frontend Setup

#### Open in Android Studio:
1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `frontend` directory
4. Wait for Gradle sync to complete

#### Update API Configuration:
In `frontend/app/src/main/java/com/example/onlinecompiler/network/ApiClient.java`:
```java
private static final String BASE_URL = "http://100.73.126.4:5050/";  // Update with your backend IP
```

#### Build and Run:
1. Connect an Android device or start an emulator
2. Click "Run" in Android Studio
3. Select your device/emulator

### 5. Network Configuration

#### Update Network Security Config:
In `frontend/app/src/main/res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">100.73.126.4</domain>
    </domain-config>
</network-security-config>
```

### 6. Testing the Setup

#### Test Backend:
```bash
# Test backend health
curl http://localhost:5050/api/compiler/status

# Test compilation
curl -X POST http://localhost:5050/api/compiler/execute \
  -H "Content-Type: application/json" \
  -d '{"language":"python","code":"print(\"Hello, World!\")","stdin":""}'
```

#### Test Frontend:
1. Launch the app
2. Select a language
3. Write a simple program
4. Click compile

## Common Issues and Solutions

### 1. Docker Issues
```bash
# If Docker service isn't running
sudo systemctl start docker

# If permission issues
sudo usermod -aG docker $USER
# Log out and log back in
```

### 2. Port Conflicts
```bash
# Check if port 5050 is in use
sudo lsof -i :5050

# Kill process if needed
sudo kill -9 <PID>
```

### 3. Android Build Issues
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### 4. Network Issues
```bash
# Check if backend is accessible
ping 100.73.126.4

# Check if port is open
telnet 100.73.126.4 5050
```

## Development Workflow

1. **Backend Development**:
   ```bash
   cd backend
   mvn spring-boot:run  # For development without Docker
   ```

2. **Frontend Development**:
   - Use Android Studio
   - Enable "Instant Run" for faster development
   - Use Logcat for debugging

3. **Testing**:
   ```bash
   # Backend tests
   cd backend
   mvn test

   # Frontend tests
   cd frontend
   ./gradlew test
   ```

## Monitoring and Logs

### Backend Logs:
```bash
# View Docker logs
docker-compose logs -f backend

# View application logs
tail -f backend/logs/application.log
```

### Frontend Logs:
- Use Android Studio's Logcat
- Filter by your app's package name

Would you like me to provide more details about any specific part of the setup process?
