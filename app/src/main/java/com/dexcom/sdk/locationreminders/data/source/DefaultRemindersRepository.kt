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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO){


    companion object {
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
    }


    suspend fun getReminders(forceUpdate: Boolean = false): Result<List<Reminder>> {

            return remindersLocalDataSource.getReminders()
    }

    fun observeReminders(): LiveData<Result<List<Reminder>>> {
        return remindersLocalDataSource.observeReminders()
    }
    suspend fun refreshReminders(){
        updateReminders()
    }

    suspend fun saveReminder(reminder: Reminder) {
        coroutineScope {
            //launch { tasksRemoteDataSource.saveTask(task) }
            launch { remindersLocalDataSource.saveReminder(reminder) }
        }
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
