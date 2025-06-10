plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.marcosanz.app"

    defaultConfig {
        applicationId = "com.marcosanz.app"
        minSdk = 24
        targetSdk = 36
        compileSdk = 36
        versionCode = 4
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    publishing {
        singleVariant("debug") {}
    }
    publishing {
        singleVariant("release") {}
    }
    buildTypes {
        debug {
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
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
