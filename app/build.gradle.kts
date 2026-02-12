import java.io.FileInputStream
import java.util.Properties





plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xshe.quantum"
    compileSdk = 36

    val localProperties = Properties().apply {
        val localPropFile = rootProject.file("local.properties")
        if (localPropFile.exists()) {
            load(FileInputStream(localPropFile))
        }
    }

    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            if (buildType.name == "release") {
                output.outputFileName = "Quantum_${versionName}.apk"
            }
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("release.storeFile", "")
            if (storeFilePath.isNotBlank()) {
                storeFile = file(storeFilePath)
            }
            storePassword = localProperties.getProperty("release.storePassword", "")
            keyAlias = localProperties.getProperty("release.keyAlias", "")
            keyPassword = localProperties.getProperty("release.keyPassword", "")
        }
    }

    defaultConfig {
        applicationId = "com.xshe.quantum"
        minSdk = 28
        targetSdk = 36
        versionCode = 12
        versionName = "V1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isProfileable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = true
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
    }
}

dependencies {
    // 核心基础
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    // 网络与解析
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.androidx.compose.foundation.layout)


    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug 专属
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Compose 编译器
    implementation(libs.androidx.compose.compiler)
}