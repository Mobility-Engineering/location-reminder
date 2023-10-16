package com.dexcom.sdk.locationreminders

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.dexcom.sdk.locationreminders.data.source.DefaultRemindersRepository
import com.dexcom.sdk.locationreminders.data.source.RemindersDataSource
import com.dexcom.sdk.locationreminders.data.source.RemindersRepository
import com.dexcom.sdk.locationreminders.data.source.local.RemindersLocalDataSource
import com.dexcom.sdk.locationreminders.database.ReminderDatabase
import kotlinx.coroutines.runBlocking

object ServiceLocator {
    val lock = Any()
    private var database: ReminderDatabase? = null

    @Volatile
    var remindersRepository: RemindersRepository? = null
        @VisibleForTesting
        set

    fun provideRemindersRepository(context: Context): RemindersRepository {
        synchronized(this) {
            return remindersRepository ?: createRemindersRepository(context)
        }
    }

    private fun createRemindersRepository(context: Context): RemindersRepository {
        val newRepo = DefaultRemindersRepository(createReminderLocalDataSource(context))
        remindersRepository = newRepo
        return newRepo
    }

    private fun createReminderLocalDataSource(context: Context): RemindersDataSource {
        val database = database ?: createDataBase(context)
        return RemindersLocalDataSource(database.remindersDao())
    }

    private fun createDataBase(context: Context): ReminderDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            ReminderDatabase::class.java, "Reminders.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock)
        {  // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            remindersRepository = null
        }
    }
}
