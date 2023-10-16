package com.dexcom.sdk.locationreminders.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.dexcom.sdk.locationreminders.data.Result.Success
import com.dexcom.sdk.locationreminders.data.Result.Error
import com.dexcom.sdk.locationreminders.data.succeeded
import com.dexcom.sdk.locationreminders.database.ReminderDatabase
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
internal class RemindersLocalDataSourceTest {
    private lateinit var localDataSource: RemindersLocalDataSource
    private lateinit var database: ReminderDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()



    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ReminderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        localDataSource = RemindersLocalDataSource(database.remindersDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp(){
        database.close()
    }

    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        //GIVEN - A new reminder saved in the database
        val newReminder = createReminder(
                PointOfInterest(LatLng(0.0, 0.0), "Greca", "South Korea"),
        "Greca-",
        "South Korea-",
        1
        )
        var id = localDataSource.saveReminder(newReminder)

        val result = localDataSource.getReminder(id)

        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`("Greca-1"))
        assertThat(result.data.description, `is`("South Korea-1"))
        assertThat(result.data.isCompleted, `is`(false))
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
