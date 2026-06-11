package org.albaspazio.psysuite.view

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import org.albaspazio.psysuite.MainActivity
import org.albaspazio.psysuite.MainApplication
import org.albaspazio.psysuite.core.managers.ResultsManager
import org.albaspazio.psysuite.tests.SettingsBasic
import org.albaspazio.psysuite.tests.TestBasic
import org.albaspazio.psysuite.core.ui.dialogs.SubjectBasicDialogFragment
import org.albaspazio.psysuite.core.utils.TestResult
import org.albaspazio.psysuite.view.MainFragment.Companion.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager

/**
 * Base class for test fragments that handle subject dialog and test navigation
 */
abstract class TestLaunchFragment(
    layout: Int,
    landscape: Boolean = false,
    hideAndroidControls: Boolean = false
) : BaseFragment(layout, landscape, hideAndroidControls) {

    protected lateinit var subject: SettingsBasic
    protected var isSubjectDFopening: Boolean = false

    /**
     * Override this to provide the navigation action to TestFragment
     */
    abstract fun getTestFragmentNavigationAction(): Int

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
//                    subject.device = Device().setRam(requireContext())
//                    subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
//                    subject.stimuliDelays = MainApplication.delaysAligner
//                    subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info

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
     * Sets up the fragment result listener for subject dialog. here I start each test with the user filled subject parcel
     */
    private fun setupSubjectDialogResultListener() {
        parentFragmentManager.setFragmentResultListener(
            TARGET_FRAGMENT_SUBJECT_REQUEST_CODE.toString(),
            viewLifecycleOwner
        ) { _, result ->
            isSubjectDFopening = false
            val subj = result.getParcelable<SettingsBasic>(SubjectBasicDialogFragment.SUBJECT_PARCEL)
            if (subj == null) return@setFragmentResultListener

            subject = subj
            subject.device = Device().setRam(requireContext())
            subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
            subject.stimuliDelays = MainApplication.delaysAligner
            subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
            
            // Lock orientation to landscape for tests (tablets only)
            (requireActivity() as MainActivity).lockOrientationToLandscape()
            
            // Navigate to TestFragment with the specific action for this fragment
            MainFragment.startTest(subject, requireView(), getTestFragmentNavigationAction())
        }
    }

    /**
     * Helper method to show subject dialog
     */
    protected fun showSubjectDialog(subjectParcel: SettingsBasic, dialogFragment: androidx.fragment.app.DialogFragment = SubjectBasicDialogFragment()) {
        if (!isSubjectDFopening) {
            isSubjectDFopening = true
            subject = subjectParcel
            MainFragment.showDialog(
                subject,
                dialogFragment,
                TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
                this,
                parentFragmentManager
            )
        }
    }
}