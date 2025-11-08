// ✅ Top-level build.gradle.kts
// الملف الرئيسي للمشروع كله

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ✅ Hilt Plugin
    id("com.google.dagger.hilt.android") version "2.52" apply false

    // ✅ Google Services Plugin (علشان Firebase)
    id("com.google.gms.google-services") version "4.3.15" apply false // ✅ آخر نسخة محدثة
}

// ✅ تنظيف المشروع
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
