package com.dexcom.sdk.locationreminders.reminder

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.dexcom.sdk.locationreminders.RemindersActivity
import com.dexcom.sdk.locationreminders.ServiceLocator
import com.dexcom.sdk.locationreminders.data.source.FakeAndroidTestRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.dexcom.sdk.locationreminders.R
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
internal class ReminderFragmentTest{

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(RemindersActivity::class.java)
    val poi = PointOfInterest(LatLng(1.0, 1.0), "name", "placeId")
    val title = "title"
    val description = "description"
    var reminders = arrayListOf<Reminder>()
private lateinit var repository: FakeAndroidTestRepository

//View Model
private lateinit var viewModel: ReminderViewModel
    @Before
    fun initRepository()= runBlocking(){
        repository = FakeAndroidTestRepository()
        val reminder1  = createReminder(poi, "title", "description", 1)
        val reminder2  = createReminder(poi, "title", "description",2 )
        val reminder3  = createReminder(poi, "title", "description",3 )
        val reminder4  = createReminder(poi, "title", "description",4 )
        /*
        reminders.add(reminder1)
        reminders.add(reminder2)
        reminders.add(reminder3)
        reminders.add(reminder4)

         */



      // repository.addReminders(reminder1, reminder2, reminder3, reminder4)
        ServiceLocator.remindersRepository = repository

        /*
        viewModel = mock(ReminderViewModel::class.java)


        // Stub out so we have control over LiveData's value
        val latLng = LatLng(90.0, 90.0)
        val poi = PointOfInterest(latLng,"Location", "Cumafat")
         `when` (viewModel.poi.value) .thenReturn(poi)

         */
    }

    @After
    fun cleanUpDb() = runTest{

        ServiceLocator.resetRepository()
    }

    private fun createReminder(poi: PointOfInterest, title: String, description: String, unit: Int): Reminder {
        return Reminder(
            unit.toLong(),
            poi.name,
            poi.latLng.latitude + unit.toDouble() ,//latLng.latitude,
            poi.latLng.longitude + unit.toDouble() ,//latLng.longitude,
            title + unit.toString(),
            description + unit.toString()
        )
    }

    @Test
    fun remindersFragment_DisplayedInUI() = runBlocking {
        //GIVEN - Add Reminder as if set by chosen on Google Map screen

    val reminder = createReminder(poi, "title", "description", 1)
        //
        //repository.addReminders((reminder)
        runBlocking {

            //repository.saveReminder(reminder)
            //repository.saveReminder(reminder)
        }

       // val viewModel = ServiceLocator.remindersRepository?.let { ReminderViewModel(it) }


        //WHEN - Authentication screen is meant to be showed
        val scenario = launchFragmentInContainer<ReminderFragment>(null, com.dexcom.sdk.locationreminders.R.style.Theme_LocationReminders)
        /*
        var list = mutableListOf<Reminder>()
        onView(withId(R.id.button_login)).//check(matches(withText("Login")))
            perform(click())

         */

        //for (reminder in reminders)

            //repository.observableReminders.value = Result.Success(reminders)
        val  navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

            onView(withId(R.id.editTextTextTitle))
                .perform(setTextInTextView("Works like a charm"))



            onView(withId(R.id.editTextTextDescription))
                .perform(setTextInTextView("Works like a charm"))

             onView(withId(R.id.textViewLocation))
                 .perform(setTextInTextView("DummyLocation"))

        //ViewModel poi attribute most be set to a given value in order to proceed.

        onView(withId(R.id.floatingActionButtonSaveReminder))
            .perform(click())


        verify(navController).navigate(
            ReminderFragmentDirections.actionReminderFragmentToRemindersFragment(
            )
        )


        Thread.sleep(5000)
        /*
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(com.dexcom.sdk.locationreminders.R.string.no_title)))


         */

    }

    fun setTextInTextView(value: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return CoreMatchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(
                    TextView::class.java))
            }

            override fun perform(uiController: UiController, view: View) {
                (view as TextView).text = value
            }

            override fun getDescription(): String {
                return "replace text"
            }
        }
    }
}