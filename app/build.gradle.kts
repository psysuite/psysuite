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

        val folder = ".signing"
        val props = Properties()
        try {
            props.load(FileInputStream(rootProject.file("${folder}/password.properties")))
        } catch (e: java.io.IOException) {
            println("File con password non esiste. non puoi continuare, $e")
        }

        create("release") {
            storeFile       = file("../${folder}/psysuite_keystore.jks")
            keyAlias        = props.getProperty("keyAlias")
            storePassword   = props.getProperty("storePassword")
            keyPassword     = props.getProperty("keyPassword")
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
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_RELEASE", "https://your-server.com/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_RELEASE", "release-key-not-configured")}\"")
            
            // Enable 16 KB page alignment
            packagingOptions {
                doNotStrip.add("lib/arm64-v8a/libnativeaudio.so")
            }
        }

        getByName("debug") {
            isDebuggable = true
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_DEBUG", "http://localhost:5000/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_DEBUG", "debug-key-not-configured")}\"")
            
            // Enable 16 KB page alignment
            packagingOptions {
                doNotStrip.add("lib/arm64-v8a/libnativeaudio.so")
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

    packagingOptions {
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
    implementation("androidx.test:monitor:1.7.1")
    implementation("androidx.test.ext:junit-ktx:1.2.1")
    implementation("androidx.navigation:navigation-testing:2.7.7")
    
    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.json:json:20231013")
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    // Android instrumented test dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    
    // Exclude problematic dependencies
    configurations.all {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}