pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GGData"
include(":app")
include(":composeApp")
include(":designsystem")
include(":arch")
include(":data")
include(":feature:main")
include(":feature:search")
include(":feature:collections")
include(":feature:game")
include(":feature:journal")
include(":entities")
include(":navigation")
