import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.royaal.ggdata.composeapp"
        compileSdk = 37
        minSdk = 26

        // Assets from :designsystem's Compose resources flow through this module's own
        // AAR too - needs the same fix. See https://youtrack.jetbrains.com/issue/CMP-9547
        androidResources.enable = true
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    // iosX64 (Intel simulator) dropped: Compose Multiplatform 1.12.1 doesn't publish
    // klibs for it anymore, and this machine is Apple Silicon anyway.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.multiplatform.runtime)
            implementation(libs.compose.multiplatform.foundation)
            implementation(libs.compose.multiplatform.material3)
            implementation(libs.compose.multiplatform.ui)

            implementation(libs.kotlinx.coroutines.core)

            implementation(project(":designsystem"))
            implementation(project(":arch"))
            implementation(project(":data"))
            implementation(project(":navigation"))

            // Features - each one also has to be listed in di/FeatureModules.kt
            implementation(project(":feature:main"))
            implementation(project(":feature:search"))
            implementation(project(":feature:collections"))
            implementation(project(":feature:game"))
            implementation(project(":feature:journal"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}
