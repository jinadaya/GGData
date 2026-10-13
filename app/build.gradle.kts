import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// IGDB credentials live in local.properties (gitignored), never in source. Android Studio only
// ever rewrites the sdk.dir line in that file, so adding our own keys here is safe.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val igdbClientId: String = localProperties.getProperty("IGDB_CLIENT_ID", "")
val igdbClientSecret: String = localProperties.getProperty("IGDB_CLIENT_SECRET", "")
if (igdbClientId.isBlank() || igdbClientSecret.isBlank()) {
    logger.warn("IGDB_CLIENT_ID / IGDB_CLIENT_SECRET missing from local.properties - IGDB calls will fail with 401 until set.")
}

android {
    namespace = "com.royaal.ggdata"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.royaal.ggdata"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "IGDB_CLIENT_ID", "\"$igdbClientId\"")
        buildConfigField("String", "IGDB_CLIENT_SECRET", "\"$igdbClientSecret\"")
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
        buildConfig = true
    }
}

dependencies {
    // UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DEBUG
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Project - shared KMP UI/logic (Android + iOS), see :composeApp
    implementation(project(":composeApp"))

    // Koin bootstrap (initKoin) for the IGDB client, see :data's NetworkModule/AppKoin
    implementation(project(":data"))
    implementation(libs.koin.android)
}
