plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ قراءة TMDB API Key من gradle.properties
        buildConfigField(
            "String",
            "API_KEY",
            "\"${project.findProperty("TMDB_API_KEY") ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "DEBUG_MODE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.0"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*"
            )
        }
    }
}

dependencies {
    // ✅ الأساسيات
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ✅ Compose BOM (توحيد الإصدارات)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.material3:material3")

    // ✅ Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.0")

    // ✅ Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ✅ Coil (تحميل الصور)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ✅ DataStore (تخزين بيانات المستخدم)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ✅ ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // ✅ Lifecycle Runtime for Compose (لتعامل Compose مع الـ Flow)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // ✅ Activity KTX
    implementation("androidx.activity:activity-ktx:1.9.3")

    // ✅ Accompanist Permissions (لو هتتعامل مع الصور)
    implementation("com.google.accompanist:accompanist-permissions:0.35.2-beta")

    // ✅ WebView (لو هتحتاجها في عرض محتوى ويب)
    implementation("androidx.webkit:webkit:1.9.0")

    // ✅ اختبارات
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
