object Configs {

    const val applicationId     = "org.albaspazio.psysuite" //  Play Store package name
    const val psysuitenamespace = "org.albaspazio.psysuite" //  Internal code identifier
    const val versionCode       = 67
    const val versionName       = "2.0.6.${versionCode}"

    // org.albaspazio.psysuite.core & org.albaspazio.core
    const val psysuitecorenamespace     = "org.albaspazio.psysuite.core"
    const val corenamespace             = "org.albaspazio.core"
    const val psysuitepythonnamespace   = "org.albaspazio.psysuite.python"
    const val psysuitetestsnamespace    = "org.albaspazio.psysuite.tests"

    const val compileSdkVersion = 34
    const val minSdkVersion     = 26
    const val targetSdkVersion  = 26
}

object ProGuards {
    const val androidDefault = "proguard-rules.pro"
    const val proguardTxt = "proguard-android.txt"
}
