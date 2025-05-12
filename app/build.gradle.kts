plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.prog7313_part2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.prog7313_part2"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildToolsVersion = "35.0.1"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    //noinspection UseTomlInstead
    implementation("androidx.room:room-runtime:2.7.1")
    implementation(libs.firebase.firestore.ktx)
    //noinspection UseTomlInstead,KaptUsageInsteadOfKsp
    kapt("androidx.room:room-compiler:2.7.1")
    //noinspection UseTomlInstead
    implementation("androidx.room:room-ktx:2.7.1")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.mpandroidchart)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-core:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-camera2:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-view:1.4.2")

    // ViewModel and LiveData dependencies
    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")  // ViewModel
    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.0")   // LiveData
    //noinspection UseTomlInstead
    implementation("androidx.fragment:fragment-ktx:1.8.6")  // Fragment KTX for viewModels()

    // Glide dependency for image loading
    //noinspection UseTomlInstead
    implementation("com.github.bumptech.glide:glide:4.12.0") // or the latest version
    //noinspection UseTomlInstead,KaptUsageInsteadOfKsp
    kapt("com.github.bumptech.glide:compiler:4.12.0") // For annotation processing


}
