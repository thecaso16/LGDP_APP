plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.lasgalletasdepau.lgdp_app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.lasgalletasdepau.lgdp_app"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.firestore)
    // Librería para activar todos los iconos de Material Design (Carrito, Add, etc.)
    implementation(libs.androidx.compose.material.icons.extended)
    // Mapeo e integración de ViewModels con Jetpack Compose (Para solucionar viewModel())
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Componentes en tiempo de ejecución para Compose (Para solucionar collectAsState())
    implementation(libs.androidx.compose.runtime)

    // Coil para imágenes
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Essential for Coroutines/Flow support
    ksp(libs.androidx.room.compiler)       // The compiler that reads your @Entity tags
}