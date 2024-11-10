plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    viewBinding {
        enable = true
    }
    namespace = "com.example.qiblacallbeta"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.qiblacallbeta"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
    implementation (libs.retrofit)
    implementation (libs.converter.moshi)
    implementation (libs.moshi.kotlin)
    implementation (libs.play.services.location.v2101) // Latest version
    implementation (libs.logging.interceptor)
    // Moshi and Moshi Kotlin dependencies
    implementation (libs.moshi) // Add the latest version
    implementation (libs.moshi.kotlin)// Kotlin adapter for Moshi

// Retrofit with Moshi converter
    implementation (libs.converter.moshi)// Add the latest version


    implementation (libs.kotlinx.coroutines.android)

    // Moshi core and Moshi Kotlin support
    implementation (libs.moshi)
    implementation( libs.moshi.kotlin)
    implementation (libs.moshi)
    implementation (libs.moshi.kotlin)
    implementation (libs.androidx.appcompat.v161)
    implementation (libs.material.v190)
    implementation (libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}