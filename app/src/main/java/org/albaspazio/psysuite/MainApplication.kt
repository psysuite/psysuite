package org.albaspazio.psysuite


import android.app.Application
import android.os.Build
import android.util.Log
import org.albaspazio.psysuite.core.R
import org.albaspazio.psysuite.core.models.preferences.ProjectPreferencesManager
import org.albaspazio.psysuite.core.models.preferences.ProjectPreferences
import org.albaspazio.psysuite.core.stimuli.DelaysAligner
import org.albaspazio.psysuite.python.SPython

// this is called before any activity onCreate

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it manage SharedPreference and DelaysAligner
//
// if pref file does not exist  => fill it with values taken from defaultDelays (devicesDelays["${Build.MODEL}_${Build.VERSION.RELEASE}"]),
// otherwise                    =>
// I need delaysAligner to be :
// - Parcelable (thus must have to-be-parceled properties in the costructor)
// - a static property of MainApplication to be accessed by everywhere


class MainApplication : Application(){

    // define the list of validated devices and their delays
    private var devicesDelays:HashMap<String, DelaysAligner> = hashMapOf(
        "Mi A2 Lite"  to DelaysAligner(4L,  40L, 4L, 0L, 5L, 0L, 30L, 53L),
        "SM-A405FN"   to DelaysAligner(0L, 165L, 4L, 0L,28L, 0L, 20L,  0L),
        "UNKNOWN"     to DelaysAligner(0L,   0L, 0L, 0L, 0L, 0L, 0L,   0L),
    )

    // set the default device's delays accessing current device model or setting a default model
    private val defaultDelays:DelaysAligner = devicesDelays[Build.MODEL] ?: devicesDelays["UNKNOWN"]!!

    companion object {
        @JvmStatic var delaysAligner = DelaysAligner()
    }

    override fun onCreate() {
        super.onCreate()

        // create preference file (if not exist), init preferences
        ProjectPreferencesManager.init(applicationContext, ProjectPreferences(defaultDelays), overwrite = true)
        delaysAligner = ProjectPreferencesManager.preferences.delaysAligner

        SPython.getInstance(applicationContext)   // init python here that we have a context. in Test Classes we get the instance created here

        Log.d("MainApplication", "OnCreate: SYSTEM DELAYS=> $delaysAligner")
    }
}