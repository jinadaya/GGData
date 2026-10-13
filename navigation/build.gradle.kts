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
        namespace = "com.royaal.navigation"
        compileSdk = 37
        minSdk = 26

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
            // api, not implementation: every feature module declares its routes (NavKey) and
            // `screen<Route> { ... }` entries (Koin Module/Scope) against these types, so depending
            // on :navigation alone should be enough to add a new screen.
            api(libs.compose.multiplatform.runtime)
            api(libs.jetbrains.navigation3.ui)
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            api(libs.kotlinx.serialization.json)

            implementation(libs.compose.multiplatform.foundation)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
        }
    }
}
