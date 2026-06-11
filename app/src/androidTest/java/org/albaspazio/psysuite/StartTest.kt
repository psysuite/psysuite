package org.albaspazio.psysuite
/*

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.UiThreadTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class StartTest {

    lateinit var navC:TestNavHostController

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule = GrantPermissionRule.grant("android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE")

    @Rule
    @JvmField
    var uiThreadTestRule = UiThreadTestRule()

    //==================================================================================================
    @Before
    fun setNavigation(){
        navC = TestNavHostController(ApplicationProvider.getApplicationContext<MainApplication>())
        navC.setViewModelStore(ViewModelStore())    // This allows fragments to use:  by navGraphViewModels()
        // for currentBackStackEntry?.savedStateHandle
        uiThreadTestRule.runOnUiThread {
            navC.setGraph(R.navigation.main_navigation)
        }
    }

    //==================================================================================================
    @Test
    fun startTest() {
        val btBindings = onView(allOf(
                withId(R.id.bt_start_bindings_test), withText("Temporal Bindings"),
                childAtPosition(childAtPosition(withId(R.id.my_nav_host_fragment), 0), 5), isDisplayed()
                )
        ).perform(click())
//        Truth.assertThat(navC.currentDestination?.id).isEqualTo(R.id.bindingsFragment)


        val btATB = onView(allOf(
                withId(R.id.bt_start_atb_test), withText("AT Binding (AT-B)"),
                childAtPosition(childAtPosition(withId(R.id.my_nav_host_fragment), 0), 1),
                isDisplayed()
                )
        ).perform(click())

        // SubjectDialog created
        val txtName = onView(allOf(
                withId(R.id.txtName),
                childAtPosition(childAtPosition(withId(android.R.id.content), 0), 5),
                isDisplayed()
                )
        )
        txtName.perform(click())
        txtName.perform(replaceText("a"), closeSoftKeyboard())

        val txtAge = onView(allOf(
                withId(R.id.txtAge),
                childAtPosition(childAtPosition( withId(android.R.id.content), 0), 4),
                isDisplayed()
            )
        ).perform(replaceText("1"), closeSoftKeyboard())

        val rbMale = onView(allOf(
                withId(R.id.rb_male),
                withText("M"),
                childAtPosition(allOf(
                            withId(R.id.radioGroupGender),
                            childAtPosition(withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")), 6)
                            ), 0),
                            isDisplayed()
                )
        ).perform(click())

        pressBack()

        val btConfirm = onView(allOf(
                withId(R.id.bt_confirm),
                childAtPosition(childAtPosition(withId(android.R.id.content), 0), 1),
                isDisplayed()
            )
        ).perform(click())
        // SubjectDialog closed....test should start

        // AnswerDialog appeared
        val radioNO = onView(
            allOf(
                withId(R.id.rb_a_1), withText("NO"),
                childAtPosition(allOf(
                        withId(R.id.radioGroupAudio),
                        childAtPosition(withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),3)
                    ), 1
                ),
                isDisplayed()
            )
        ).perform(click())

        val appCompatImageButton2 = onView(allOf(
                withId(R.id.bt_confirm),
                childAtPosition(childAtPosition(withId(android.R.id.content), 0), 1), isDisplayed()
                )
        )
        appCompatImageButton2.perform(click())

        val btAnswerCancel = onView(allOf(
                withId(R.id.bt_abort_test),
                withText("ANNULLA TEST"),
                childAtPosition(childAtPosition(withId(android.R.id.content), 0),2),
                isDisplayed()
            )
        ).perform(click())

        val btCancelSaveData = onView(allOf(
                withId(android.R.id.button2), withText("cancella"),
                childAtPosition(childAtPosition(withClassName(`is`("android.widget.ScrollView")), 0), 2)
                )
        ).perform(scrollTo(), click())
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent) && view == parent.getChildAt(position)
            }
        }
    }
}
*/