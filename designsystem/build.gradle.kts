import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.types.error.ErrorModuleDescriptor.platform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.royaal.designsystem"
        compileSdk = 37
        minSdk = 26

        // Without this, Compose Multiplatform resources (fonts, etc.) silently don't get
        // packaged into the Android APK's assets with the new androidLibrary KMP plugin -
        // known upstream gap, see https://youtrack.jetbrains.com/issue/CMP-9547
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

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    sourceSets {
        commonMain.dependencies {
            // api, not implementation: Modifier/Color/TextStyle/ColorScheme/DrawableResource
            // all leak into this module's public composable signatures (GGButton, AppTheme, ...),
            // so consumers need these resolvable on their own compile classpath too.
            api(libs.compose.multiplatform.runtime)
            api(libs.compose.multiplatform.foundation)
            api(libs.compose.multiplatform.material3)
            api(libs.compose.multiplatform.ui)
            api(libs.compose.multiplatform.resources)
            api(libs.compose.multiplatform.ui.tooling.preview)

            implementation(libs.coil.compose)
            implementation(libs.kotlinx.coroutines.core)

            implementation(project(":entities"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            // target's own compilation, so the real tooling artifacts (not the CMP annotation-only
            // one above) belong here - they're Android-only and version-less without this BOM.
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.ui.tooling)
        }
    }
}

compose.resources {
    packageOfResClass = "com.royaal.designsystem.generated.resources"
}