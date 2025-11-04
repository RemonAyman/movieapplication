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
        maven { url = uri("https://jitpack.io") } // مهم جدًا لـ Toasty
    }

    plugins {
        id("org.jetbrains.kotlin.android") version "1.9.25"
        id("org.jetbrains.kotlin.kapt") version "1.9.25"
        id("com.google.dagger.hilt.android") version "2.52"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // مهم جدًا
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // مهم جدًا
    }
}

rootProject.name = "MyApplication6"
include(":app")
