package com.dexcom.sdk.locationreminders.reminders

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.dexcom.sdk.locationreminders.ServiceLocator
import com.dexcom.sdk.locationreminders.data.source.FakeAndroidTestRepository
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.dexcom.sdk.locationreminders.reminder.ReminderFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.reminder.ReminderFragmentDirections
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
internal class RemindersFragmentMockitoTest {

    val poi = PointOfInterest(LatLng(1.0, 1.0), "name", "placeId")
    val title = "title"
    val description = "description"
    var reminders = arrayListOf<Reminder>()
    lateinit var reminder1: Reminder
    lateinit var reminder2: Reminder
    lateinit var reminder3: Reminder
    lateinit var reminder4: Reminder
    private lateinit var repository: FakeAndroidTestRepository

    @Before
    fun initRepository() = runBlocking() {
        repository = FakeAndroidTestRepository()
        reminder1 = createReminder(poi, "title", "description", 1)
        reminder2 = createReminder(poi, "title", "description", 2)
        reminder3 = createReminder(poi, "title", "description", 3)
        reminder4 = createReminder(poi, "title", "description", 4)
        /*
        reminders.add(reminder1)
        reminders.add(reminder2)
        reminders.add(reminder3)
        reminders.add(reminder4)

         */


        // repository.addReminders(reminder1, reminder2, reminder3, reminder4)
        ServiceLocator.remindersRepository = repository
    }

    @After
    fun cleanUpDb() = runTest {

        ServiceLocator.resetRepository()
    }

    private fun createReminder(
        poi: PointOfInterest,
        title: String,
        description: String,
        unit: Int
    ): Reminder {
        return Reminder(
            unit.toLong(),
            poi.name,
            poi.latLng.latitude + unit.toDouble(),//latLng.latitude,
            poi.latLng.longitude + unit.toDouble(),//latLng.longitude,
            title + unit.toString(),
            description + unit.toString()
        )
    }
@Test
    fun clickReminder_navigateToRemindersFragmentOne() = runTest {
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val scenario = launchFragmentInContainer<ReminderFragment>(
            null,
            R.style.Theme_LocationReminders
        )
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        //WHEN Click on the first list item
        /*
        onView(withId(R.id.recycler)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
            hasDescendant(withText()), click()))

         */



Thread.sleep(2000)
    }

    @Test
    fun remindersFragment_DisplayedInUI() = runTest {
        //GIVEN - Add Reminder as if set by chosen on Google Map screen

        val reminder = createReminder(poi, "title", "description", 1)
        val reminder2 = createReminder(poi, "title", "description", 2)
        //repository.addReminders((reminder)
        runBlocking {

            repository.saveReminder(reminder)
            repository.saveReminder(reminder2)
        }

        // val viewModel = ServiceLocator.remindersRepository?.let { ReminderViewModel(it) }


        //WHEN - Authentication screen is meant to be showed
        val scenario = launchFragmentInContainer<RemindersFragment>(null, com.dexcom.sdk.locationreminders.R.style.Theme_LocationReminders)


        Thread.sleep(5000)
        /*
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(com.dexcom.sdk.locationreminders.R.string.no_title)))


         */

    }

}
