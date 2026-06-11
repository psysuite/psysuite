package org.albaspazio.psysuite.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.albaspazio.psysuite.MainApplication
import org.albaspazio.psysuite.R
import org.albaspazio.psysuite.core.managers.ProjectManager
import org.albaspazio.psysuite.navigation.config.ConfigurationParser
import org.albaspazio.psysuite.navigation.manager.TestsNavigationManager
import org.albaspazio.psysuite.navigation.resolution.StringResolver
import org.albaspazio.psysuite.navigation.ui.DynamicButtonGenerator
import org.albaspazio.psysuite.navigation.resolution.TestParcelInstantiator
import org.albaspazio.psysuite.tests.SettingsBasic
import org.albaspazio.psysuite.tests.TestBasic
import org.albaspazio.psysuite.core.ui.dialogs.SubjectBasicDialogFragment.Companion.PROJECTS_PARCEL
import org.albaspazio.psysuite.core.ui.dialogs.SubjectBasicDialogFragment.Companion.SUBJECT_PARCEL
import androidx.navigation.findNavController


class MainFragment : TestLaunchFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
) {
    override val LOG_TAG: String = MainFragment::class.java.simpleName

    // Dynamic menu components
    private lateinit var testsNavigationManager: TestsNavigationManager
    private lateinit var stringResolver: StringResolver
    private lateinit var buttonGenerator: DynamicButtonGenerator
    private lateinit var buttonContainer: LinearLayout
    private lateinit var menuTitleView: TextView

    companion object {
        @JvmStatic val isDebug: Boolean = false
        @JvmStatic val TARGET_FRAGMENT_SUBJECT_REQUEST_CODE: Int = 1

        fun showDialog(subj: SettingsBasic, df: DialogFragment, rc: Int, frg: Fragment, pfm: FragmentManager) {
            subj.isDebug = isDebug
            if (isDebug) {
                subj.label = "a"
                subj.age = 1
                subj.gender = 0
            }

            val bundle = Bundle()
            bundle.putParcelable(SUBJECT_PARCEL, subj)

            // Get available projects and pass them to the dialog
            val projectManager = ProjectManager.getInstance(frg.requireContext())
            val availableProjects = projectManager.getAllProjects()
            bundle.putStringArrayList(PROJECTS_PARCEL, ArrayList(availableProjects))

            df.arguments = bundle
            df.setTargetFragment(frg, rc)
            df.isCancelable = false
            df.show(pfm, "Modifica Soggetto")
        }

        fun startTest(subj: SettingsBasic, v: View, nav_action: Int = R.id.action_mainFragment_to_testFragment) {
            subj.stimuliDelays = MainApplication.delaysAligner

            val bundle = Bundle()
            bundle.putParcelable(TestBasic.TESTINFO_BUNDLE_LABEL, subj)
            v.findNavController().navigate(nav_action, bundle)
        }
    }

    override fun getTestFragmentNavigationAction(): Int = R.id.action_mainFragment_to_testFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setVersionText(view)
        setDeviceIdText(view)
        initializeDynamicMenu(view, savedInstanceState)
    }

    private fun setVersionText(view: View) {
        try {
            val tvVersion = view.findViewById<TextView>(R.id.tv_version)
            if (tvVersion != null) {
                tvVersion.text = "v${requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName}"
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error setting version", e)
        }
    }

    private fun setDeviceIdText(view: View) {
        try {
            val tvDeviceId = view.findViewById<TextView>(R.id.tv_device_id)
            if (tvDeviceId != null) {
                val deviceManager = org.albaspazio.psysuite.core.managers.DeviceIdentificationManager.getInstance(requireActivity())
                tvDeviceId.text = deviceManager.deviceId ?: "N/A"
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error setting device ID", e)
        }
    }

    private fun initializeDynamicMenu(view: View, savedInstanceState: Bundle?) {
        try {
            // Initialize views
            buttonContainer = view.findViewById(R.id.button_container)
            menuTitleView = view.findViewById(R.id.tv_menu_title)

            Log.d(LOG_TAG, "Views initialized: container=$buttonContainer, title=$menuTitleView")

            // Initialize components
            stringResolver = StringResolver(requireContext())
            buttonGenerator = DynamicButtonGenerator(requireContext(), stringResolver)

            // Load configuration from assets
            val configJson = requireContext().assets.open("fulltests_menu.json")
                .bufferedReader()
                .use { it.readText() }

            Log.d(LOG_TAG, "Config JSON loaded, length: ${configJson.length}")

            val rootNode = ConfigurationParser.parse(configJson)
            testsNavigationManager = TestsNavigationManager(rootNode)
            Log.d(LOG_TAG, "Dynamic menu loaded successfully: ${rootNode.label}")
            Log.d(LOG_TAG, "Root node type: ${rootNode.type}, children count: ${rootNode.getChildren().size}")

            // Restore navigation state if available
            if (savedInstanceState != null) {
                val navState = savedInstanceState.getBundle("navigation_state")
                if (navState != null) {
                    testsNavigationManager.restoreState(navState)
                    Log.d(LOG_TAG, "Navigation state restored from savedInstanceState")
                }
            }

            // Setup UI
            displayCurrentMenu()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to initialize dynamic menu", e)
            e.printStackTrace()
            showError("Menu Error: ${e.message}")
        }
    }

    private fun displayCurrentMenu() {
        try {
            val currentNode = testsNavigationManager.getCurrentNode()
            Log.d(LOG_TAG, "getCurrentNode returned: ${currentNode.label}, type: ${currentNode.type}")

            // Update title
            val resolvedLabel = stringResolver.resolve(currentNode.label)
            menuTitleView.text = resolvedLabel
            Log.d(LOG_TAG, "Resolved label: $resolvedLabel")

            // Clear previous buttons
            buttonContainer.removeAllViews()

            // Generate buttons using DynamicButtonGenerator
            val children = currentNode.getChildren()
            Log.d(LOG_TAG, "Current node has ${children.size} children")
            
            if (children.isEmpty()) {
                Log.w(LOG_TAG, "WARNING: No children found for node: ${currentNode.label}")
                showError("No menu items found for: ${currentNode.label}")
                return
            }
            
            buttonGenerator.generateButtons(
                currentNode,
                buttonContainer,
                onMenuNodeClicked = { node ->
                    testsNavigationManager.navigateTo(node)
                    displayCurrentMenu()
                },
                onTestNodeClicked = { node ->
                    try {
                        val testInstance = TestParcelInstantiator.instantiate(node.getTestClassName())
                        Log.d(LOG_TAG, "Test instantiated: ${node.label}")
                        showSubjectDialog(testInstance)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to instantiate test", e)
                        showError("Test Error: ${e.message}")
                    }
                }
            )
            Log.d(LOG_TAG, "Successfully displayed ${children.size} buttons")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to display menu", e)
            e.printStackTrace()
            showError("Menu Error: ${e.message}")
        }
    }

    private fun showError(message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::buttonContainer.isInitialized) {
            buttonContainer.removeAllViews()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::testsNavigationManager.isInitialized) {
            val navState = testsNavigationManager.saveState()
            outState.putBundle("navigation_state", navState)
            Log.d(LOG_TAG, "Navigation state saved to outState")
        }
    }

    fun setRegistrationName(deviceId: String) {
        // This method is called from MainActivity when device is registered
        // We can optionally display it in the menu if needed
        Log.d(LOG_TAG, "Device registered: $deviceId")
        setDeviceIdText(requireView())
    }

    /**
     * Check if we can go back in the dynamic menu (not at root)
     */
    fun canGoBackInMenu(): Boolean {
        return ::testsNavigationManager.isInitialized && testsNavigationManager.canGoBack()
    }

    /**
     * Go back one level in the dynamic menu
     */
    fun goBackInMenu() {
        if (::testsNavigationManager.isInitialized && testsNavigationManager.canGoBack()) {
            testsNavigationManager.goBack()
            displayCurrentMenu()
        }
    }
}
