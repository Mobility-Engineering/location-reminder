package com.dexcom.sdk.locationreminders.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.dexcom.sdk.locationreminders.database.ReminderDatabase
import com.dexcom.sdk.locationreminders.getOrAwaitValue
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorrRule = InstantTaskExecutorRule()

    private lateinit var database: ReminderDatabase

    @Before
    fun initDb() {
        database =
            Room.inMemoryDatabaseBuilder(getApplicationContext(), ReminderDatabase::class.java)
                .build()
    }

    @After
    fun closedDb() = database.close()


    @Test
    fun insertReminderAndGetById() = runTest {
        //GIVEN
        val reminder = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
            "Title",
            "Description",
            1
        )
        database.remindersDao().insertReminder(reminder)

        val retrieved = database.remindersDao().getReminderById(reminder.id)

        assertThat(retrieved as Reminder, notNullValue())
        assertThat(retrieved.id, `is`(reminder.id))
        assertThat(retrieved.latitude, `is`(reminder.latitude))
        assertThat(retrieved.longitude, `is`(reminder.longitude))
        assertThat(retrieved.title, `is`(reminder.title))
        assertThat(retrieved.description, `is`(reminder.description))
        assertThat(retrieved.isCompleted, `is`(reminder.isCompleted))
    }

    @Test
    fun insertAllRemindersAndCompare() = runTest {
        //GIVEN
        var unit = 1
        //var linkedHashMap = LinkedHashMap<Long, Reminder>()
        val reminder1 = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
            "Title",
            "Description",
            unit++
        )
        val reminder2 = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
            "Title",
            "Description",
            unit++
        )
        val reminder3 = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
            "Title",
            "Description",
            unit
        )
        var reminders = listOf(reminder1, reminder2, reminder3)

        database.remindersDao().insertAll(reminder1, reminder2, reminder3)
        val cache = database.remindersDao().observeReminders()
        assertEquals(3, (cache.getOrAwaitValue()).size)
        assertEquals(true, compareRemindersWithCache(cache.getOrAwaitValue(), reminders))
    }

    @Test
    fun updateReminderAndGetById() = runTest {
        //GIVEN
        val reminder = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
            "Title",
            "Description",
            1
        )
        val updated = createReminder(
            PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
            "Title",
            "Description",
            2
        )

        var id = database.remindersDao().insertReminder(reminder)

        updateReminder(reminder, updated)


        id = database.remindersDao().updateReminder(reminder).toLong()

        val retrieved = database.remindersDao().getReminderById(reminder.id)

        retrieved?.let {
            assertThat(it.id, `is`(reminder.id))
            assertThat(it.title, `is`(reminder.title))
            assertThat(it.description, `is`(reminder.description))
            assertThat(it.isCompleted, `is`(reminder.isCompleted))
        }
    }

    private fun updateReminder(reminder: Reminder, updated: Reminder) {

        reminder.latitude = updated.latitude
        reminder.longitude = updated.longitude
        reminder.title = updated.title
        reminder.description = updated.description
        reminder.isCompleted = updated.isCompleted
    }


private fun compareRemindersWithCache(list: List<Reminder>?, comp: List<Reminder>): Boolean {
    if (list == null) return false

    for (reminder in list)
        if (!comp.contains(reminder)) return false

    return true
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
