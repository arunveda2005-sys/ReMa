# 🍳 RecipeMatcher (ReMa)

An intelligent Android application that transforms pantry management and meal planning through AI-powered recipe recommendations.

## 📱 Overview

RecipeMatcher helps you reduce food waste, discover recipes based on available ingredients, and streamline your cooking experience. Never wonder "what can I cook?" again - let AI suggest recipes based on what's already in your pantry.

## 📥 Download

**Latest Version: v1.0.0**

[![Download APK](https://img.shields.io/badge/Download-APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/arunveda2005-sys/ReMa/raw/main/releases/RecipeMatcher-v1.0.0.apk)

Or visit the [Releases](https://github.com/arunveda2005-sys/ReMa/releases) page for all versions.

## ✨ Key Features

### 🥘 Smart Pantry Management
- Track ingredients with quantities, units, and expiry dates
- Automatic status monitoring (fresh, expiring soon, expired)
- Batch operations for efficient updates
- Category-based organization
- Quick search and filtering

### 🤖 AI-Powered Recipe Generation
- Generate personalized recipes using Google Gemini API
- Recipes tailored to your available ingredients
- Consider dietary preferences and restrictions
- Natural language recipe requests

### 📊 Intelligent Recipe Matching
- Real-time calculation of recipe feasibility
- Visual match percentage indicators
- Categorized by "Can Make", "Almost There", "Missing Many"
- Browse 1000+ curated recipes offline

### 🛒 Smart Shopping Lists
- Auto-generate lists from recipe requirements
- One-tap ingredient addition
- Check-off functionality
- Direct transfer to pantry when purchased

### ⏰ Proactive Notifications
- Expiry alerts to minimize food waste
- Suggest recipes using soon-to-expire ingredients
- Daily pantry health checks

### 🎨 Modern UI/UX
- Material Design 3 components
- Light and dark theme support
- Smooth animations and transitions
- Intuitive navigation

## 🛠️ Technology Stack

### Core
- **Language**: Java 21, Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle (Kotlin DSL)

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Async**: Kotlin Coroutines
- **DI**: Manual dependency injection
- **Navigation**: Android Navigation Component

### Libraries
- **UI**: Material Design 3, ViewBinding, ConstraintLayout
- **Networking**: Retrofit 2, OkHttp, Gson
- **Image Loading**: Glide
- **Background Tasks**: WorkManager
- **AI**: Google Gemini API

## 📂 Project Structure

```
com.example.receipematcher/
├── data/
│   ├── converters/          # Room type converters
│   ├── db/                  # Database and DAOs
│   ├── entities/            # Room entities
│   └── repository/          # Repository implementations
├── network/                 # API responses and models
├── ui/
│   ├── components/          # Reusable UI components
│   ├── pantry/              # Pantry management UI
│   ├── recipes/             # Recipe browsing and details
│   ├── shopping/            # Shopping list UI
│   ├── favorites/           # Favorites UI
│   └── settings/            # Settings and preferences
├── utils/                   # Utility classes and helpers
├── viewmodel/               # ViewModels for each feature
└── workers/                 # WorkManager background tasks
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 21
- Android SDK 34
- Gemini API key (optional, for AI features)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/arunveda2005-sys/ReMa.git
   cd ReMa
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Configure Gemini API (Optional)**
   - Get your API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Create `local.properties` in the root directory
   - Add: `GEMINI_API_KEY=your_api_key_here`

4. **Sync and Build**
   - Let Gradle sync
   - Build > Make Project
   - Run on emulator or physical device

### Configuration

**Java 21 Setup**: If you encounter Java version issues, see [JAVA_21_SETUP.md](JAVA_21_SETUP.md)

**Gradle Properties**: Update `gradle.properties` with your Java path:
```properties
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.9.10-hotspot
```

## 📖 Documentation

- **[Architecture Documentation](ARCHITECTURE.md)** - Detailed system architecture and design decisions
- **[UI Optimization Specs](.kiro/specs/ui-optimization/)** - UI/UX improvement specifications

## 🎯 Core Workflows

### Adding Ingredients to Pantry
1. Tap the "+" button in Pantry tab
2. Enter ingredient details (name, quantity, expiry)
3. Save - ingredient is tracked automatically

### Finding Recipes
1. Browse recipes in Recipe tab
2. View match percentage for each recipe
3. Filter by cuisine, difficulty, or cooking time
4. Tap to see detailed instructions

### Generating AI Recipes
1. Tap "AI Recipe" button
2. Describe what you want (optional)
3. AI generates recipe based on your pantry
4. Save to favorites or add missing items to shopping list

### Managing Shopping Lists
1. Add items manually or from recipes
2. Check off items as you shop
3. Transfer checked items to pantry
4. Clear completed items

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Arun Veda**
- GitHub: [@arunveda2005-sys](https://github.com/arunveda2005-sys)

## 🙏 Acknowledgments

- Google Gemini API for AI-powered recipe generation
- Material Design team for UI components
- Android Jetpack libraries
- Recipe data sourced from various culinary databases

## 📧 Contact

For questions or feedback, please open an issue on GitHub.

---

**Made with ❤️ for home cooks who want to cook smarter, not harder**
