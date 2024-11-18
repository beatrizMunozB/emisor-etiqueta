

plugins {
    id("com.android.application")

    id ("org.jetbrains.kotlin.android" )
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 31
        versionCode = 1
        versionName = "1.1"
        // Habilita coroutines y Kotlin en el código.


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {


    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")  // Asegúrate de usar una versión actualizada de Kotlin

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")  // Usa la última versión estable
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")  // Asegúrate de usar la misma versión para coroutines
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")


    implementation ("com.google.zxing:android-core:3.3.0")
    implementation ("com.google.zxing:core:3.4.1")
    implementation("androidx.print:print:1.0.0")

   // implementation ("com.zebra.sdk:printer-android-sdk:1.1.14")
   // implementation ("com.zebra:zebra-android-sdk:2.15.5194")
    //implementation ("files(libs/ZSDK_ANDROID_API.jar)")
    implementation(files("libs/ZSDK_ANDROID_API.jar"))
   // implementation(files("libs/ZSDK_ANDROID_API.jar"))







    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")








}