import java.io.FileInputStream
import java.util.Properties

// Read from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// Helper function to get properties with fallbacks
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key) ?: System.getenv(key) ?: defaultValue
}

plugins {
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

android {

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

    namespace           = Configs.psysuitenamespace
    compileSdk          = Configs.compileSdkVersion

    defaultConfig {

        applicationId   = Configs.applicationId
        versionCode     = Configs.versionCode
        versionName     = Configs.versionName

        minSdk          = Configs.minSdkVersion
        targetSdk       = Configs.targetSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable 16 KB page alignment for native libraries
        ndk {
            abiFilters.add("arm64-v8a")
        }
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
            
            // Enable 16 KB page alignment
            packaging {
                jniLibs {
                    keepDebugSymbols.add("lib/arm64-v8a/libnativeaudio.so")
                }
            }
        }

        getByName("debug") {
            isDebuggable = true
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_DEBUG", "http://localhost:5000/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_DEBUG", "debug-key-not-configured")}\"")
            
            // Enable 16 KB page alignment
            packaging {
                jniLibs {
                    keepDebugSymbols.add("lib/arm64-v8a/libnativeaudio.so")
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        disable.add("MissingTranslation")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Configure native library alignment
    ndkVersion = "28.0.13004108"

}

dependencies {

    implementation(project(":core"))
    implementation(project(":psysuitepython"))
    implementation(project(":psysuitecore"))
    implementation(project(":psysuitetests"))

    implementation(Dependencies.permissions)
    implementation(Dependencies.AndroidX.legacy_support)
    implementation(Dependencies.AndroidX.fragment)
    implementation(Dependencies.AndroidX.lifecycleviewmodel)
    
    // Test dependencies
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockito_core)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.json)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.navTesting)
    
    // Android instrumented test dependencies
    androidTestImplementation(Dependencies.AndroidX.junitExt)
    androidTestImplementation(Dependencies.AndroidX.testEspressoCore)
    androidTestImplementation(Dependencies.AndroidX.testRunner)
    androidTestImplementation(Dependencies.AndroidX.testMonitor)
    androidTestImplementation(Dependencies.AndroidX.junitKtx)
    
    // Exclude problematic dependencies
    configurations.all {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}