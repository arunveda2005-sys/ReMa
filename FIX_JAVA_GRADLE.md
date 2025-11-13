# Fix Gradle to Use Java 21

## Step 1: Stop the current Gradle daemon
```cmd
./gradlew --stop
```

## Step 2: Clear Gradle cache (optional but recommended)
```cmd
rmdir /s /q .gradle
```

## Step 3: Verify Java 21 is being used
```cmd
./gradlew --version
```

## Step 4: Build the project
```cmd
./gradlew assembleDebug
```

## Alternative: Set JAVA_HOME temporarily
If the gradle.properties doesn't work, you can set JAVA_HOME for this session:

```cmd
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
./gradlew assembleDebug
```

## Verify it's working
After running `./gradlew --version`, you should see:
- JVM: 21.0.9 (Eclipse Adoptium)
- Instead of JVM: 11.0.28 (Microsoft)