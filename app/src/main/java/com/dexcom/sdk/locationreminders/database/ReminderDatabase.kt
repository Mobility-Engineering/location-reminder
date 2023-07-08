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

@Entity(tableName = "reminder_table")
data class DatabaseReminder constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name= "name")
    val name:String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
)

fun List<DatabaseReminder>.asDomainModel(): List<Reminder> {
    return map {
        Reminder(
            id = it.id,
            name =it.name,
            title = it.title,
            description = it.description,
            latitude =  it.latitude,
            longitude  = it.longitude
        
        )
    }
}

@Dao
interface ReminderDao {
    @Query("SELECT * from reminder_table")
    fun getReminders(): List<DatabaseReminder>

    //As mentioned in the comments, remove suspend. When a method returns an observable, there is no reason to make it suspend
    // since it just returns and object, does not run any query until it is observed.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg reminder: DatabaseReminder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reminder: DatabaseReminder)

    @Query("DELETE FROM reminder_table")
    fun clear()

}

@Database(entities = [DatabaseReminder::class], version = 2)
abstract class ReminderDatabase : RoomDatabase() {
    abstract val reminderDao: ReminderDao

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