package com.dexcom.sdk.locationreminders.data.source

import androidx.lifecycle.LiveData
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.reminder.Reminder

interface RemindersRepository {
    suspend fun getReminders(forceUpdate: Boolean = false): Result<List<Reminder>>
    suspend fun getReminder(reminderId: Long): Result<Reminder?>
    fun observeReminders(): LiveData<Result<List<Reminder>>>
    suspend fun refreshReminders()
    suspend fun saveReminder(reminder: Reminder): Long
    suspend fun deleteAllReminders()
    suspend fun updateReminder(reminder:Reminder)
    suspend fun deleteReminder(reminderId: Long)
}