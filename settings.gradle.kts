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
        // ✅ لو في مكتبات بتتسحب من JitPack (زي Toasty أو Hilt)
        maven { url = uri("https://jitpack.io") }
    }

    plugins {
        // ✅ نضيف إصدارات الـ Plugins الأساسية هنا علشان كل حاجة تبقى متسقة
        id("org.jetbrains.kotlin.kapt") version "2.0.21"
        id("com.google.dagger.hilt.android") version "2.52"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ✅ للتأكد إن JitPack متاح لأي Dependency
    }
}

rootProject.name = "My Application"
include(":app")
