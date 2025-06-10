plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.marcosanz.app" // Asignación de propiedad

    defaultConfig {
        applicationId = "com.marcosanz.app"
        minSdk = 24
        targetSdk = 36
        compileSdk = 36
        versionCode = 4 // Asignación de propiedad (corrección: debería ser versionCode = 4)
        versionName = "1.0.4" // Asignación de propiedad

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    buildTypes {
        debug {
            isDebuggable = true // Asignación de propiedad booleana
        }

        release {
            // Corrección de typo: isMinifyEnaled -> isMinifyEnabled
            isMinifyEnabled = false // Asignación de propiedad booleana
            isShrinkResources = false // Asignación de propiedad booleana
            proguardFiles( // Llamada a función
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21 // Asignación de propiedad
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget =
            JavaVersion.VERSION_21.toString() // Asignación de propiedad con llamada a toString()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.flexbox)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(path = ":gallerydialog"))
}
