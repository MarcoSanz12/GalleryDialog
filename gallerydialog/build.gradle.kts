plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("maven-publish")
    id("kotlin-parcelize")
}

android {
    namespace = "com.marcosanz.gallerydialog"

    defaultConfig {
        minSdk = 24
        compileSdk = 36
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
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}
dependencies {
    // BASIC
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.glide)
    // TouchImageView
    implementation(libs.touchImageView)

    // VRImageView
    implementation(libs.panoramagl)

}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.marcosanz"
            artifactId = "gallerydialog"
            version = "1.0.0"

            // 'from' debe ir fuera del afterEvaluate, pero puede necesitarlo si el componente aún no existe
            afterEvaluate {
                from(components.findByName("release") ?: return@afterEvaluate)
            }
        }
    }
}
