plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.acechase.kidclock"
    compileSdk = 34
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.acechase.kidclock"
        minSdk = 28
        targetSdk = 28
        versionCode = 1
        versionName = "0.1"
        ndk { abiFilters += listOf("arm64-v8a") }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    // The Vosk model is shipped uncompressed so it can be streamed out of the APK quickly.
    androidResources { noCompress += listOf("fst", "mdl", "ie", "mat", "dubm", "stats", "int", "conf", "txt") }
    packaging { jniLibs.useLegacyPackaging = true }
    lint { checkReleaseBuilds = false; abortOnError = false }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.alphacephei:vosk-android:0.3.47")
    implementation("net.java.dev.jna:jna:5.13.0@aar")

    testImplementation("junit:junit:4.13.2")
    // Real org.json for JVM unit tests; android.jar only has stubs.
    testImplementation("org.json:json:20240303")
}
