package com.dexcom.sdk.locationreminders.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dexcom.sdk.locationreminders.database.ReminderDatabase

class ReminderViewModel(val app:Application):AndroidViewModel(app) {
        val database: ReminderDatabase = ReminderDatabase.getDatabase(app)
}