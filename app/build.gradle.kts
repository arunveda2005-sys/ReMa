// build.gradle.kts (Module: app)
import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt) // annotation processors
}

android {
    namespace = "com.example.receipematcher"
    compileSdk = 34 // stable

    // No remote API keys required (Gemini removed)

    defaultConfig {
        applicationId = "com.example.receipematcher"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // No BuildConfig fields needed for API keys
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true // XML + ViewBinding
        buildConfig = true // enable BuildConfig fields
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)

    // AppCompat + Material Components
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    // UI Widgets
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Lifecycle (ViewModel + LiveData)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // WorkManager (for expiry notifications)
    implementation(libs.androidx.work.runtime)

    // Glide (image loading)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Retrofit + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // Unit Testing
    testImplementation(libs.junit)

    // Android Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
