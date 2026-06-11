package org.albaspazio.psysuite

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PsySuiteTest {


    @Test
    fun startTest() {
        val scenario = launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)        // check initial state

//        onView(withId(R.id.bt_start_bis))
//            .perform(click())
//            .check(matches(isDisplayed()))    // check result

    }

}