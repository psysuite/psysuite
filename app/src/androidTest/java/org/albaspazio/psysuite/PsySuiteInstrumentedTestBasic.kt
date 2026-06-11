package org.albaspazio.psysuite

//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.lifecycle.ViewModelStore
//import androidx.navigation.Navigation
//import androidx.navigation.testing.TestNavHostController
//import androidx.test.core.app.ApplicationProvider.getApplicationContext
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
//import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
//import androidx.test.rule.UiThreadTestRule
//import com.google.common.truth.Truth.assertThat
//import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
//import org.robolectric.annotation.LooperMode
/*

@RunWith(AndroidJUnit4ClassRunner::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class PsySuiteInstrumentedTestBasic {

//    lateinit var navC:TestNavHostController

    @Rule
//    @JvmField
//    var uiThreadTestRule = UiThreadTestRule()

    //==================================================================================================
    @Before
    fun setNavigation(){
//        navC = TestNavHostController(getApplicationContext<MainApplication>())
//        navC.setViewModelStore(ViewModelStore())    // This allows fragments to use:  by navGraphViewModels()
//                                                    // for currentBackStackEntry?.savedStateHandle
//        uiThreadTestRule.runOnUiThread {
//            navC.setGraph(R.navigation.main_navigation)
//        }
    }

    //==================================================================================================
    @Test
    fun testNavigationToBindingsFragment() {

//        val titleScenario = launchFragmentInContainer {
//            MainFragment().also { fragment ->
//
//                // In addition to returning a new instance of our Fragment,
//                // get a callback whenever the fragment’s view is created or destroyed so that we can set the NavController
//                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
//                    if (viewLifecycleOwner != null) {
//                        // The fragment’s view has just been created
//                        Navigation.setViewNavController(fragment.requireView(), navC)
//                    }
//                }
//            }
//        }
//        // to Set a property after onViewCreated : titleScenario.onFragment { fragment -> }
//
//        onView(withId(R.id.bt_start_bindings_test)).perform(click())
//        assertThat(navC.currentDestination?.id).isEqualTo(R.id.bindingsFragment)
//
//        onView(allOf(
//            withId(R.id.bt_start_atb_test),
//            isDisplayed()))
//        .perform(click())

    }
}


/*
    @get:Rule
    val mainActivityTestRule = ActivityTestRule(MainActivity::class.java)
 */

 */
