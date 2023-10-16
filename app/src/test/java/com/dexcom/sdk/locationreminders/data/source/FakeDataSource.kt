package com.dexcom.sdk.locationreminders.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.Result.Success
import com.dexcom.sdk.locationreminders.database.RemindersDao
import com.dexcom.sdk.locationreminders.reminder.Reminder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FakeDataSource internal constructor(
    var reminders: MutableList<Reminder> = mutableListOf()
) : RemindersDataSource {
    override fun observeReminders(): LiveData<Result<List<Reminder>>> {

        TODO("Not yet implemented")
    }

    override suspend fun getReminders(): Result<List<Reminder>>{
  reminders?.let{ return Success(ArrayList(it))}
        TODO("Not yet implemented")
    }


    override suspend fun refreshReminders() {
        TODO("Not yet implemented")
    }

    override fun observeReminder(reminderId: String): LiveData<Result<Reminder>> {
        TODO("Not yet implemented")
    }

    override suspend fun getReminder(reminderId: Long): Result<Reminder> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshReminder(reminderId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun saveReminder(reminder:Reminder):Long{
       reminders?.add(reminder)
        return reminders?.size?.toLong() ?: 0

    }

    override suspend fun remoteSaveReminder(reminder: Reminder): Long {
        TODO("Not yet implemented")
    }

    override suspend fun completeReminder(reminder: Reminder) {
        TODO("Not yet implemented")
    }

    override suspend fun completeReminder(reminderId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun activateReminder(reminder: Reminder) {
        TODO("Not yet implemented")
    }

    override suspend fun activateReminder(reminderId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clearCompletedReminders() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllReminders() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReminder(reminderId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateReminder(reminder: Reminder) {
        TODO("Not yet implemented")
    }
}

