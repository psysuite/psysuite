package org.albaspazio.psysuite.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.albaspazio.psysuite.R
import org.albaspazio.psysuite.core.managers.ResultsManager
import org.albaspazio.psysuite.core.managers.DeviceIdentificationManager
import org.albaspazio.psysuite.core.managers.DeviceIdBackupManager
import org.albaspazio.psysuite.core.ui.dialogs.DeviceRegistrationDialog
import androidx.core.content.edit

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private lateinit var deviceManager: DeviceIdentificationManager
    private lateinit var resultsManager: ResultsManager
    private var deviceStatusPreference: Preference? = null
    private var webUploadStatusPreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        try {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        } catch (e: ClassCastException) {
            // Handle type conversion issues in SharedPreferences
            android.util.Log.w("SettingsFragment", "SharedPreferences type conversion issue, clearing problematic preferences", e)
            clearProblematicPreferences()
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
        
        deviceManager = DeviceIdentificationManager.getInstance(requireContext())
        resultsManager = ResultsManager.getInstance(requireActivity())
        
        setupDevicePreferences()
        setupWebUploadPreferences()
    }
    
    private fun clearProblematicPreferences() {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.edit {

            // Clear delay preferences that might have type conflicts
            val delayKeys = arrayOf(
                "pref_delay_a1", "pref_delay_a2", "pref_delay_a3",
                "pref_delay_t1", "pref_delay_t2", "pref_delay_v1", "pref_delay_v2"
            )

            delayKeys.forEach { key ->
                try {
                    // Try to read as string first
                    prefs.getString(key, null)
                } catch (_: ClassCastException) {
                    // Try to migrate from Long to String
                    try {
                        val longValue = prefs.getLong(key, 0L)
                        remove(key)
                        putString(key, longValue.toString())
                        android.util.Log.d(
                            "SettingsFragment",
                            "Migrated preference $key from Long ($longValue) to String"
                        )
                    } catch (_: Exception) {
                        // If migration fails, just remove the key
                        remove(key)
                        android.util.Log.d(
                            "SettingsFragment",
                            "Cleared problematic preference key: $key"
                        )
                    }
                }
            }

        }
    }
    
    private fun setupDevicePreferences() {
        // Device status preference (read-only)
        deviceStatusPreference = findPreference("device_status")
        deviceStatusPreference?.apply {
            title = "Device Registration Status"
            summary = deviceManager.registrationStatus
            isSelectable = false
        }

        // Register device preference (button)
        val registerDevicePreference = findPreference<Preference>("register_device")
        registerDevicePreference?.apply {
            title = "Register Device"
            summary = "Open device registration dialog"
            setOnPreferenceClickListener {
                showDeviceRegistrationDialog()
                true
            }
        }
        
        // Clear registration preference (button)
        val clearRegistrationPreference = findPreference<Preference>("clear_device_registration")
        clearRegistrationPreference?.apply {
            title = "Clear Device Registration"
            summary = "Remove device identifier"
            isVisible = deviceManager.isDeviceRegistered
            setOnPreferenceClickListener {
                showClearRegistrationDialog()
                true
            }
        }
    }
    
    private fun updateDevicePreferences() {
        deviceStatusPreference?.summary = deviceManager.registrationStatus
        findPreference<Preference>("clear_device_registration")?.isVisible = deviceManager.isDeviceRegistered
    }
    
    private fun showDeviceRegistrationDialog() {
        val dialog = DeviceRegistrationDialog.newInstance(isFirstLaunch = false, allowSkip = false)
        dialog.setOnDeviceRegisteredListener(object : DeviceRegistrationDialog.OnDeviceRegisteredListener {

            override fun onDeviceRegistered(deviceId: String) {
                DeviceIdBackupManager.getInstance(requireContext()).backupDeviceId(deviceId)
                updateDevicePreferences()
                Toast.makeText(context, "Device registered as: $deviceId", Toast.LENGTH_LONG).show()
            }
            
            override fun onRegistrationSkipped() { /* Not applicable in settings */ }
            override fun onRegistrationCancelled() { /* User cancelled, do nothing */ }
        })
        
        dialog.show(parentFragmentManager, "device_registration_settings")
    }
    
    private fun showClearRegistrationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Device Registration")
            .setMessage("Are you sure you want to clear the device registration? This will remove the device identifier from future experiment uploads.")
            .setPositiveButton("Clear") { _, _ ->
                deviceManager.clearRegistration()
                DeviceIdBackupManager.getInstance(requireContext()).clearBackup()
                updateDevicePreferences()
                Toast.makeText(context, "Device registration cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupWebUploadPreferences() {
        // Web API URL preference
        val webApiUrlPreference = findPreference<EditTextPreference>("web_api_url")
        webApiUrlPreference?.apply {
            text = resultsManager.webApiUrl
            summary = if (text.isNullOrBlank() || text == "https://your-server.com/api") {
                "Not configured - tap to set server URL"
            } else {
                "Current URL: $text"
            }

            setOnPreferenceChangeListener { _, newValue ->
                val newUrl = newValue.toString().trim()
                resultsManager.webApiUrl = newUrl
                updateWebUploadStatus()
                summary =   if (newUrl.isBlank() || newUrl == "https://your-server.com/api")    "Not configured - tap to set server URL"
                            else                                                                "Current URL: $newUrl"
                true
            }
        }

        // Web API Key preference
        val webApiKeyPreference = findPreference<EditTextPreference>("web_api_key")
        webApiKeyPreference?.apply {
            text = resultsManager.webApiKey
            summary = if (text.isNullOrBlank()) {
                "Not configured - tap to set API key"
            } else {
                "API key configured (${text?.length} characters)"
            }

            setOnPreferenceChangeListener { _, newValue ->
                val newKey = newValue.toString().trim()
                resultsManager.webApiKey = newKey
                updateWebUploadStatus()
                summary = if (newKey.isBlank()) {
                    "Not configured - tap to set API key"
                } else {
                    "API key configured (${newKey.length} characters)"
                }
                true
            }
        }
        
        // Web upload status preference (read-only)
        webUploadStatusPreference = findPreference("web_upload_status")
        updateWebUploadStatus()
    }

    private fun updateWebUploadStatus() {
        webUploadStatusPreference?.apply {
            val isConfigured = resultsManager.isWebUploadEnabled
            summary = if (isConfigured) {
                "✓ Web upload is properly configured"
            } else {
                "⚠ Web upload requires both URL and API key"
            }
        }
    }
    
//    private fun showValidationError() {
//        Toast.makeText(context,
//            "Device ID must be 3-50 characters, letters, numbers, hyphens and underscores only",
//            Toast.LENGTH_LONG).show()
//    }

    override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
        return true
    }
}
