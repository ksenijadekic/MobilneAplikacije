plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.rmas_projekat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rmas_projekat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.1" }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    // Compose / AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigacija (Compose)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // (opciono)
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.accompanist:accompanist-permissions:0.21.0-beta")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("androidx.activity:activity-compose:1.9.3")

    // ✅ GOOGLE MAPS za MapsActivity (klasična View/Fmg API)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // (samo ako koristiš Google Maps Compose — za ovu aktivnost NE treba)
    // implementation("com.google.maps.android:maps-compose:4.4.1")

    // ✅ MAPBOX za MapsScreen (Compose ekran koji smo sredili)
    implementation("com.mapbox.maps:android:11.15.3")
    implementation("com.mapbox.extension:maps-compose:11.15.3")
}
