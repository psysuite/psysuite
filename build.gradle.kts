// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.chaquopy) apply false
}

ext["javaVersion"] = libs.versions.javaVersion.get()
ext["ndkVersion"] = libs.versions.ndkVersion.get()

tasks.register("clean", Delete::class){
    description = "Deletes the build directory"
    delete(rootProject.layout.buildDirectory)
}
