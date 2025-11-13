# Java 21 Setup Instructions

## Option 1: Microsoft OpenJDK 21 (Recommended for Windows)

1. Download Microsoft OpenJDK 21:
   - Go to: https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21
   - Download the Windows x64 MSI installer

2. Install Java 21:
   - Run the downloaded MSI file
   - Follow the installation wizard
   - The installer will automatically set up the PATH

3. Set JAVA_HOME (if needed):
   - Open System Properties → Advanced → Environment Variables
   - Add new system variable:
     - Variable name: `JAVA_HOME`
     - Variable value: `C:\Program Files\Microsoft\jdk-21.0.x.x-hotspot` (adjust version)

4. Verify installation:
   ```cmd
   java -version
   ```
   Should show: `openjdk version "21.0.x"`

## Option 2: Using Chocolatey (if you have it)

```cmd
choco install openjdk21
```

## Option 3: Using Scoop (if you have it)

```cmd
scoop install openjdk21
```

## After Installation

1. Restart your command prompt/PowerShell
2. Navigate to your project directory
3. Run the build:
   ```cmd
   ./gradlew assembleDebug
   ```

## Alternative: Use Project-Specific Java

If you don't want to change your system Java, you can set it just for this project by creating a `gradle.properties` file in your project root with:

```properties
org.gradle.java.home=C:\\Program Files\\Microsoft\\jdk-21.0.x.x-hotspot
```

Replace the path with your actual Java 21 installation path.