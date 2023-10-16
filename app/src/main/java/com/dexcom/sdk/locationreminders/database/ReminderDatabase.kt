package com.dexcom.sdk.locationreminders.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.dexcom.sdk.locationreminders.reminder.Reminder

/*
data class Reminder(val id: Long, val codename: String, val closeApproachDate: String,
                    val absoluteMagnitude: Double, val estimatedDiameter: Double,
                    val relativeVelocity: Double, val distanceFromEarth: Double,
                    val isPotentiallyHazardous: Boolean) : Parcelable
                    */



fun List<Reminder>.asDomainModel(): List<Reminder> {
    return map {
        Reminder(
            id = it.id,
            name = it.name,
            title = it.title,
            description = it.description,
            latitude = it.latitude,
            longitude = it.longitude

        )
    }
}

@Dao
interface RemindersDao {

    /**
     * Observes list of reminders.
     *
     * @return all reminders.
     */
    @Query("SELECT * from reminder_table")
    fun observeReminders(): LiveData<List<Reminder>>

    /*
    @Query("SELECT * from reminder_table")
    suspend fun getReminders(): List<Reminder>

     */

    //As mentioned in the comments, remove suspend. When a method returns an observable, there is no reason to make it suspend
    // since it just returns and object, does not run any query until it is observed.


    @Update
     fun updateReminder(reminder: Reminder): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg reminder: Reminder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminder(reminder: Reminder): Long

    @Query("SELECT * FROM reminder_table WHERE id = :remId")
    fun getReminderById(remId: Long): Reminder?

    @Query("DELETE FROM reminder_table")
    fun clear()

    @Query("DELETE FROM reminder_table WHERE id = :remId")
    fun deleteReminder(remId:Long)
}

    @Database(entities = [Reminder::class], version = 2)
    abstract class ReminderDatabase : RoomDatabase() {
    abstract fun remindersDao(): RemindersDao

    companion object {
        /* The value of a volatile variable will never be cached, and all writes and
        *  reads will be done to and from the main memory. It means that changes made by one
        *  thread to shared data are visible to other threads.
         */
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {

            /* Multiple threads can ask for the database at the same time, ensure we only initialize
         * it once by using synchronized. Only one thread may enter a synchronized block at a
         * time.
         */
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ReminderDatabase::class.java,
                        "database_asteroids"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}