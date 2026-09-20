plugins {
    id("com.android.application")
}

val releaseKeystorePath = providers.environmentVariable("KINLINK_KEYSTORE_PATH").orNull
val releaseStorePassword = providers.environmentVariable("KINLINK_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("KINLINK_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("KINLINK_KEY_PASSWORD").orNull
val releaseSigningAvailable = listOf(
    releaseKeystorePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.terminator364.kinlink"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.terminator364.kinlink"
        minSdk = 30
        targetSdk = 37
        versionCode = 5
        versionName = "0.4.0-rc1"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningAvailable) {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.matching { it.name == "validateSigningRelease" }.configureEach {
    doFirst {
        check(releaseSigningAvailable) {
            "Release signing is not configured. Use the offline KINLINK signing station."
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.16.0")
    testImplementation("junit:junit:4.13.2")
}
