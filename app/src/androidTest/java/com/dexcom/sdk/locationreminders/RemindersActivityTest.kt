package com.dexcom.sdk.locationreminders

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.source.RemindersRepository
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

//AndroidXtest code is used
@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest {

    private lateinit var repository:RemindersRepository

    @Before
    fun init(){
        repository = ServiceLocator.provideRemindersRepository(ApplicationProvider.getApplicationContext())
        runBlocking {
            repository.deleteAllReminders()
        }
    }
    @After
    fun reset(){
        ServiceLocator.resetRepository()

    }

    @Test
    fun showGoogleSignIn() = runBlocking {
        //Set initial state
        val reminder = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca_", "South Korea_"),
            "Greca",
            "South Korea",
            1

        )
        val id = repository.saveReminder(reminder)
        //Start up screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withText("LOGIN")).perform(click())
        onView(withText("Sign in with Google")).perform(click())


        //Espresso code will go here

        reminder.title = "Modified Title"
        repository.updateReminder(reminder)
        //Make sure the activity is closed before resetting the db:
        val updatedReminder = repository.getReminder(id) as Result.Success


        assertEquals(updatedReminder.data!!.title, "Modified Title")
        activityScenario.close()
    }


    private fun
            createReminder(
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
}