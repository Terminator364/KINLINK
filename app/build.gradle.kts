plugins {
    id("com.android.application")
}

android {
    namespace = "com.terminator364.kinlink"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.terminator364.kinlink"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-m0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
