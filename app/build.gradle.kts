import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key) ?: System.getenv(key) ?: defaultValue
}

plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    compileSdk = Configs.compileSdkVersion
    ndkVersion = rootProject.ext["ndkVersion"] as String
    namespace = "org.albaspazio.psysuite"

    defaultConfig {
        applicationId = Configs.applicationId
        minSdk = Configs.minSdkVersion
        targetSdk = Configs.targetSdkVersion
        versionCode = Configs.versionCode
        versionName = Configs.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(getDefaultProguardFile(ProGuards.proguardTxt), ProGuards.androidDefault)
            signingConfig = if (signingConfigs.findByName("release") != null)
                signingConfigs.getByName("release")
            else
                signingConfigs.getByName("debug")
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_RELEASE", "https://your-server.com/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_RELEASE", "release-key-not-configured")}\"")
        }
        getByName("debug") {
            isDebuggable = true
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_DEBUG", "http://localhost:5000/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_DEBUG", "debug-key-not-configured")}\"")
        }
    }

    compileOptions {
        val javaVer = JavaVersion.toVersion(rootProject.ext["javaVersion"] as String)
        sourceCompatibility = javaVer
        targetCompatibility = javaVer
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = rootProject.ext["javaVersion"] as String
    }

    signingConfigs {
        val signingPropsFile = rootProject.file(".signing/password.properties")
        if (signingPropsFile.exists()) {
            val props = Properties().apply { load(FileInputStream(signingPropsFile)) }
            create("release") {
                storeFile     = file("../.signing/psysuite_keystore.jks")
                keyAlias      = props.getProperty("keyAlias")
                storePassword = props.getProperty("storePassword")
                keyPassword   = props.getProperty("keyPassword")
            }
        }
    }

    packaging {
        jniLibs {
            keepDebugSymbols.add("lib/arm64-v8a/libnativeaudio.so")
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":psysuitepython"))
    implementation(project(":psysuitecore"))
    implementation(project(":psysuitetests"))

    implementation(libs.permissions)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.viewmodel)
    
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.json)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.navigation.testing)
    
    androidTestImplementation(libs.androidx.test.junit.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.monitor)
    androidTestImplementation(libs.androidx.test.junit.ktx)
    
    configurations.all {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}
