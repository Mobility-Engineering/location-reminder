package com.dexcom.sdk.locationreminders.data.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.source.RemindersRepository
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.runBlocking

class FakeAndroidTestRepository: RemindersRepository {


    var remindersServiceData: LinkedHashMap<Long, Reminder> = LinkedHashMap()

    var remindersList:MutableList<Reminder> = mutableListOf()//mutableListOf(createReminder( PointOfInterest(LatLng(0.0,0.0), "Greca", "South Korea"), "Title", "Description",1))
    var _observableReminders:MutableLiveData<Result<List<Reminder>>> = MutableLiveData(Result.Success(remindersList))




     val observableReminders
         get() = _observableReminders


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

    fun addReminder( reminder: Reminder) {
        //I want to update the observable mutable value which will be done via equal operator, so all that there is, is to do so via
        // a list of reminders that gets updated
        //Now that the list is ready
        //remindersList.
        remindersList.add(reminder)//createReminder( PointOfInterest(LatLng(0.0,0.0), "Greca", "South Korea"), "Title", "Description",remindersList.size))
        _observableReminders.postValue(Result.Success(remindersList))

    }
    override suspend fun getReminders(forceUpdate: Boolean): Result<List<Reminder>> {
        return observableReminders.value as Result.Success
    }

    override suspend fun getReminder(reminderId: Long): Result<Reminder> {
        TODO("Not yet implemented")
    }

    override fun observeReminders(): LiveData<Result<List<Reminder>>> {

        return observableReminders

    }

    override suspend fun refreshReminders() {
        //Does nothing on code
    }

    override suspend fun saveReminder(reminder: Reminder): Long {

     remindersServiceData[reminder.id] = reminder
        if (observableReminders.value == null)
            return reminder.id

        addReminder(reminder)
        //observableReminders.value = Result.Success(arrayListOf(reminder))
        //((observableReminders.value as Result.Success).data as ArrayList).add(reminder)
        return 0

    }

    fun addReminders(vararg reminders: Reminder){
        for (reminder in reminders) {
            remindersServiceData[reminder.id] = reminder
        }
        _observableReminders.value

        //runBlocking { refreshReminders() }
    }

    override suspend fun deleteAllReminders(){
        TODO("Not Yet implemented ")
    }

    override suspend fun updateReminder(reminder: Reminder) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReminder(reminderId:Long){
        TODO("Not Yet implemented ")
    }




}