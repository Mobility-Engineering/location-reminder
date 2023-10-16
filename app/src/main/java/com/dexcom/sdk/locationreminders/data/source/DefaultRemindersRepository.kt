package com.dexcom.sdk.locationreminders.data.source

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.dexcom.sdk.locationreminders.data.source.local.RemindersLocalDataSource
import com.dexcom.sdk.locationreminders.database.ReminderDatabase
import com.dexcom.sdk.locationreminders.data.Result

import com.dexcom.sdk.locationreminders.reminder.Reminder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class DefaultRemindersRepository(
    private val remindersLocalDataSource: RemindersDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : RemindersRepository {



    /*companion object {
        @Volatile
        private var INSTANCE: DefaultRemindersRepository? = null


        fun getRepository(app: Application): DefaultRemindersRepository {
            return INSTANCE ?: synchronized(this) {
                val database = Room.databaseBuilder(app, ReminderDatabase::class.java, "Reminders.db")
                    .build()
                DefaultRemindersRepository(RemindersLocalDataSource(database.remindersDao())).also {
                    INSTANCE = it
                }
            }
        }

         */




    override suspend fun getReminders(forceUpdate: Boolean): Result<List<Reminder>> {

            return remindersLocalDataSource.getReminders()
    }

    override suspend fun getReminder(reminderId: Long): Result<Reminder> {
        return remindersLocalDataSource.getReminder(reminderId)
    }

    override fun observeReminders(): LiveData<Result<List<Reminder>>> {
        return remindersLocalDataSource.observeReminders()
    }
    override suspend fun refreshReminders(){
        updateReminders()
    }

    override suspend fun saveReminder(reminder: Reminder):Long {
        var id = 0L
        coroutineScope {
            launch { remindersLocalDataSource.remoteSaveReminder(reminder)} //for EspressoIdlingResource testing
            launch { id = remindersLocalDataSource.saveReminder(reminder) }
        }
        return id
    }

    override suspend fun deleteAllReminders() {

        remindersLocalDataSource.deleteAllReminders()
    }

    override suspend fun updateReminder(reminder: Reminder) {
        remindersLocalDataSource.updateReminder(reminder)
    }

    override suspend fun deleteReminder(reminderId: Long) {
        remindersLocalDataSource.deleteReminder(reminderId)
    }

    private suspend fun updateReminders(){
        //Not such TasksRemoteDataSource

        // val remoteTasks = tasksRemoteDataSource.getTasks()
        // Same
        // if (remoteTasks is Success) {

            // Real apps might want to do a proper sync.

        //Therefore no sync is needed in the first place
            //remindersLocalDataSource.deleteAllReminders()
        /* SYNC
            remoteTasks.data.forEach { task ->
                tasksLocalDataSource.saveTask(task)
            }
        } else if (remoteTasks is Result.Error) {
            throw remoteTasks.exception
        }

         */
    }

}
