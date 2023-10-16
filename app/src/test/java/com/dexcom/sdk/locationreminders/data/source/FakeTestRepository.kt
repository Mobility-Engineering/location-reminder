package com.dexcom.sdk.locationreminders.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.Result.Error
import com.dexcom.sdk.locationreminders.data.Result.Success
import com.dexcom.sdk.locationreminders.reminder.Reminder
import kotlinx.coroutines.runBlocking


class FakeTestRepository:RemindersRepository {

    var remindersServiceData: LinkedHashMap<Long, Reminder> = LinkedHashMap()
    private val observableReminders = MutableLiveData<Result<List<Reminder>>>()
    private var shouldReturnError = false
    override suspend fun getReminders(forceUpdate: Boolean): Result<List<Reminder>> {
        return observableReminders.value as Result.Success
    }

    override suspend fun getReminder(reminderId: Long): Result<Reminder> {
        TODO("Not yet implemented")
    }

    fun setReturnError(value:Boolean) {
        shouldReturnError =value
    }

    override fun observeReminders(): LiveData<Result<List<Reminder>>> {
        if(shouldReturnError) return MutableLiveData(Error(Exception("Test Exception")))
        return observableReminders
    }

    override suspend fun refreshReminders() {
        //Does nothing on code
    }

    override suspend fun saveReminder(reminder: Reminder): Long {
        ((observableReminders.value as Success).data as ArrayList).add(reminder)
        return reminder.id
    }

    override suspend fun deleteAllReminders(){
        TODO("Not yet implemented")
    }

    override suspend fun updateReminder(reminder: Reminder) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReminder(reminderId: Long) {
        TODO("Not yet implemented")
    }
    fun addReminders(vararg reminders: Reminder) {
        /*for (reminder in reminders) {
            remindersServiceData[reminder.id] = reminder
            }

         */
        observableReminders.value = Success(reminders.toList())


        runBlocking { refreshReminders() }
    }


}