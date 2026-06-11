pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://chaquo.com/maven") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven") }
    }
}


include(":nativeaudio")
project(":nativeaudio").projectDir = File(settingsDir, "../modules/psysuitecore/nativeaudio")

include(":psysuitepython")
project(":psysuitepython").projectDir = File(settingsDir, "../modules/psysuitepython/psysuitepython")

include(":psysuitecore")
project(":psysuitecore").projectDir = File(settingsDir, "../modules/psysuitecore/psysuitecore")

include(":psysuitetests")
project(":psysuitetests").projectDir = File(settingsDir, "../modules/psysuitetests/psysuitetests")

include(":core")
project(":core").projectDir = File(settingsDir, "../modules/core/core")

include(":app")
