package org.albaspazio.psysuite.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import org.albaspazio.psysuite.MainActivity
import org.albaspazio.psysuite.MainApplication
import org.albaspazio.psysuite.core.managers.ResultsManager
import org.albaspazio.psysuite.tests.SettingsBasic
import org.albaspazio.psysuite.tests.TestBasic
import org.albaspazio.psysuite.core.ui.dialogs.SubjectBasicDialogFragment
import org.albaspazio.psysuite.core.utils.TestResult
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager
import org.albaspazio.psysuite.R
import org.albaspazio.psysuite.core.managers.ProjectManager
import org.albaspazio.psysuite.core.ui.dialogs.SubjectBasicDialogFragment.Companion.PROJECTS_PARCEL
import org.albaspazio.psysuite.core.ui.dialogs.SubjectBasicDialogFragment.Companion.SUBJECT_PARCEL
import org.albaspazio.psysuite.navigation.resolution.TestParcelInstantiator

/**
 * Base class for test fragments that handle subject dialog and test navigation
 */
abstract class TestLaunchFragment(
    layout: Int,
    landscape: Boolean = false,
    hideAndroidControls: Boolean = false
) : BaseFragment(layout, landscape, hideAndroidControls) {

    protected val isDebug: Boolean = false


    /**
     * allows to launch a debug test from whichever button you press on the UI (allows skipping subjectDialog)
     */
    protected fun launchDebugTest(){

        // EDIT HERE TO SELECT TEST PARAMS TO LAUNCH
        val className   = "org.albaspazio.psysuite.tests.bis.SettingsBIS"
        val test_type   = TestBasic.TEST_BISECTION_AUDIO_VISUAL
        val trg_man     = TestBasic.TEST_TRMAN_FIXED
        val testDebug   = false     // this.isDebug is used to call this method, this testDebug to set whether running a regular task (false) or a debug task (true)
        val training    = TestBasic.TEST_SWITCH_DISABLED
        // ----------------------------------------------------------------

        val subj        = TestParcelInstantiator.instantiate(className)
        subj.type       = test_type
        subj.label      = "debug_test"
        subj.age        = 1
        subj.gender     = 0
        subj.doTraining = training
        subj.trman_type = trg_man
        subj.isDebug    = testDebug

        writeExperimentJson(subj)

        // Navigate to TestFragment with the specific action for this fragment
        startTest(subj, requireActivity(), requireView(), testFragmentNavigationAction)
    }

    protected lateinit var subject: SettingsBasic
    protected var isSubjectDFopening: Boolean = false

    companion object {
        @JvmStatic val TARGET_FRAGMENT_SUBJECT_REQUEST_CODE: Int = 1

        fun showDialog(subj: SettingsBasic, df: DialogFragment, rc: Int, frg: Fragment, pfm: FragmentManager) {

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

        fun startTest(subj: SettingsBasic, activity: Activity, v: View, nav_action: Int = R.id.action_mainFragment_to_testFragment) {
            subj.stimuliDelays = MainApplication.delaysAligner

            // Lock orientation to landscape for tests (tablets only)
            (activity as MainActivity).lockOrientationToLandscape()

            val bundle = Bundle()
            bundle.putParcelable(TestBasic.TESTINFO_BUNDLE_LABEL, subj)

            // Post navigation to ensure any pending orientation changes or lifecycle events settle
            v.post {
                try {
                    v.findNavController().navigate(nav_action, bundle)
                } catch (e: Exception) {
                    Log.e("TestLaunchFragment", "Navigation failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Override this to provide the navigation action to TestFragment
     */
    val testFragmentNavigationAction: Int = R.id.action_mainFragment_to_testFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTestResultObserver()
        setupSubjectDialogResultListener()
    }

    /**
     * Observes test results coming back from TestFragment
     */
    private fun setupTestResultObserver() {
        try {
            val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
            val resultLiveData = savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)

            resultLiveData?.observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    // Restore dynamic orientation when test finishes (tablets only)
                    (requireActivity() as MainActivity).restoreDynamicOrientation()

                    ResultsManager.getInstance(requireActivity()).onTestFinished(result)
                    savedStateHandle.remove<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)
                }
            }
        } catch (e: IllegalStateException) {
            // Fragment is not part of a navigation graph, skip observer setup
            // This is expected for menu-only fragments like MainFragment
        }
    }

    /**
     * Helper method to show subject dialog
     */
    protected fun showSubjectDialog(subjectParcel: SettingsBasic, dialogFragment: DialogFragment = SubjectBasicDialogFragment()) {
        if (!isSubjectDFopening) {
            isSubjectDFopening = true
            subject = subjectParcel
            showDialog(
                subject,
                dialogFragment,
                TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
                this,
                parentFragmentManager
            )
        }
    }

    /**
     * Sets up the fragment result listener for subject dialog.
     * add listener to SubjectDialog dismiss with a filled subject parcel. write experiment json file and launch test
     */
    private fun setupSubjectDialogResultListener() {
        parentFragmentManager.setFragmentResultListener(TARGET_FRAGMENT_SUBJECT_REQUEST_CODE.toString(), viewLifecycleOwner
        ) { _, result ->
            isSubjectDFopening = false
            var subj = result.getParcelable<SettingsBasic>(SUBJECT_PARCEL) ?: return@setFragmentResultListener

            subj = writeExperimentJson(subj) // write experiment json file

            // Navigate to TestFragment with the specific action for this fragment
            startTest(subj, requireActivity(), requireView(), testFragmentNavigationAction)
        }
    }

    /**
     * Helper method to add device/vercode to given SettingsBasic, update subject and write test's json file
     * This is called by all tests but TestSample that does not write results file and directly calls startTest()
     * thus, subject.stimuliDelays, that is needed also by TestSample, is set in startTest()
     */
    protected fun writeExperimentJson(subj: SettingsBasic): SettingsBasic {
        subject         = subj
        subject.device  = Device().setRam(requireContext())
        subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
        subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
        return subject
    }

}