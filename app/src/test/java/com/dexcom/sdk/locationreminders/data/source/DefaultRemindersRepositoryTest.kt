package com.dexcom.sdk.locationreminders.data.source

import com.dexcom.sdk.locationreminders.MainCoroutineRule
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.tools.ant.Main
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DefaultRemindersRepositoryTest {

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()
    val poi = PointOfInterest(LatLng(1.0, 1.0), "name", "placeId")
    val title = "title"
    val description = "description"

    private val reminder1  = createReminder(poi, "title", "description", 1)
    private val reminder2  = createReminder(poi, "title", "description",2 )
    private val reminder3  = createReminder(poi, "title", "description",3 )
    private val reminder4  = createReminder(poi, "title", "description",4 )
    private val localReminders:List<Reminder> = listOf(reminder1, reminder2).sortedBy{it.id}
    private val newReminders:List<Reminder> = listOf(reminder3).sortedBy { it.id }

    private lateinit var remindersLocalDataSource:FakeDataSource
    //class under test
    private lateinit var remindersRepository: DefaultRemindersRepository

    fun createReminder(poi: PointOfInterest, title: String, description: String, unit: Int): Reminder {
        return Reminder(
            unit.toLong(),
            poi.name,
            poi.latLng.latitude + unit.toDouble() ,//latLng.latitude,
            poi.latLng.longitude + unit.toDouble() ,//latLng.longitude,
            title + unit.toString(),
            description + unit.toString()
        )
    }

    @Before
    fun createRepository(){
        remindersLocalDataSource = FakeDataSource(localReminders.toMutableList())
        //class under test
        remindersRepository  = DefaultRemindersRepository(
            //Dispatchers Unconfined should be replaced with Dispatchers.Main
            // this requires understanding more about coroutines + testing, so we will keep this as Unconfined for now
            remindersLocalDataSource, Dispatchers.Main)//Dispatchers.Unconfined)
                                                        //Use before MainCoroutineRule was used which is needed for
                                                        //tests implying interaction with UI elments, thus the Main
                                                        // thread implicitly
    }

    @Test
    fun getReminders_requestAllReminders()= runTest(){
    val reminders = remindersRepository.getReminders(true) as Result.Success
        MatcherAssert.assertThat(reminders.data, IsEqual(localReminders))
    }

}