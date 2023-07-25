package com.dexcom.sdk.locationreminders.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.dexcom.sdk.locationreminders.data.source.RemindersDataSource
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.Result.Success
import com.dexcom.sdk.locationreminders.data.Result.Error
import com.dexcom.sdk.locationreminders.database.RemindersDao
import com.dexcom.sdk.locationreminders.reminder.Reminder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemindersLocalDataSource internal constructor(
    private val remindersDao: RemindersDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RemindersDataSource {
    override fun observeReminders(): LiveData<Result<List<Reminder>>> {

        //When the RemindersDatabase is accessed, then it most be that a given List of Reminders is ontained
        return remindersDao.observeReminders().map {
            Success(it)
        }
    }


    override suspend fun getReminders(): Result<List<Reminder>> = withContext(ioDispatcher) {
        /*
        //https://developer.android.com/kotlin/coroutines/coroutines-adv
                try {
                  Success(remindersDao.getReminders())
                } catch (e: Exception) {
                    Error(e)
                }

         */
        TODO("Not yet implemented")
            }


    override suspend fun refreshReminders() {
        TODO("Not yet implemented")
    }

    override fun observeReminder(reminderId: String): LiveData<Result<Reminder>> {
        TODO("Not yet implemented")
    }

    override suspend fun getReminder(reminderId: String): Result<Reminder> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshReminder(reminderId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun saveReminder(reminder: Reminder) = withContext(ioDispatcher) {
        remindersDao.insertReminder(reminder)
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

    override suspend fun deleteReminder(reminderId: String) {
        TODO("Not yet implemented")
    }

}