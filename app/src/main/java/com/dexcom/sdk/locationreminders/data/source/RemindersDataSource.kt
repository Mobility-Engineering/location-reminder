package com.dexcom.sdk.locationreminders.data.source

import androidx.lifecycle.LiveData
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.Result.Success
interface RemindersDataSource {


    fun observeReminders(): LiveData<Result<List<Reminder>>>

    suspend fun getReminders(): Result<List<Reminder>>

    suspend fun refreshReminders()

    fun observeReminder(reminderId: String): LiveData<Result<Reminder>>

    suspend fun getReminder(reminderId: String): Result<Reminder>

    suspend fun refreshReminder(reminderId: String)

    suspend fun saveReminder(reminder: Reminder)

    suspend fun completeReminder(reminder: Reminder)

    suspend fun completeReminder(reminderId: String)

    suspend fun activateReminder(reminder: Reminder)

    suspend fun activateReminder(reminderId: String)

    suspend fun clearCompletedReminders()

    suspend fun deleteAllReminders()

    suspend fun deleteReminder(reminderId: String)
}