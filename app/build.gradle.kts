plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.luntik.store"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.luntik.store"
        minSdk = 26
        targetSdk = 35
        versionCode = 20
        versionName = "1.1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // Подпись: ключ НЕ хранится в репозитории. CI берёт его из GitHub Secrets
    // (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD), раскодирует в
    // keystore/luntik.keystore и передаёт пароли через переменные окружения.
    // Если ключа нет — сборка подписывается обычным debug-ключом (только для теста).
    val luntikKeystore = file(System.getenv("LUNTIK_KEYSTORE_FILE") ?: "../keystore/luntik.keystore")
    val hasLuntikKey = luntikKeystore.exists() && !System.getenv("KEYSTORE_PASSWORD").isNullOrEmpty()
    signingConfigs {
        if (hasLuntikKey) {
            create("luntik") {
                storeFile = luntikKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        debug { if (hasLuntikKey) signingConfig = signingConfigs.getByName("luntik") }
        release { isMinifyEnabled = false; signingConfig = if (hasLuntikKey) signingConfigs.getByName("luntik") else signingConfigs.getByName("debug"); proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
