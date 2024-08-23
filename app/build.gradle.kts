plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("kotlin-kapt") // Correct way to add the kotlin-kapt plugin
}

android {
    namespace = "com.bussiness.pickup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bussiness.pickup"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
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
        buildFeatures {
            buildConfig = true
            viewBinding = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true // Enable Data Binding
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.circleimageview)
    implementation(libs.countrycodepicker)
    // Firebase Storage with Kotlin extensions
    implementation(libs.firebase.storage)
    implementation(platform(libs.firebase.bom))
    implementation(libs.glide)
    kapt(libs.glide.compiler)  // Annotation processor for Glide
    implementation(libs.karumi.dexter)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.geofire.android)
    implementation(libs.geofire.android.common)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.libphonenumber)
}