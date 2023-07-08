package com.dexcom.sdk.locationreminders.reminder

import android.app.Application
import androidx.lifecycle.*
import com.dexcom.sdk.locationreminders.database.DatabaseReminder
import com.dexcom.sdk.locationreminders.database.ReminderDatabase
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderViewModel(val app:Application):AndroidViewModel(app) {
        val database: ReminderDatabase = ReminderDatabase.getDatabase(app)

        private lateinit var _reminderList:List<Reminder>
        val reminderList get() = _reminderList

          private lateinit var _lastLatLng: LatLng
          val lastLatLng  get() = _lastLatLng
        private lateinit var _name:String
        val name get() = _name
        private lateinit var _lastPoi:PointOfInterest
        val lastPoi get() = _lastPoi

        private lateinit var _reminders:MutableLiveData<List<DatabaseReminder>>
        val reminders: LiveData<List<DatabaseReminder>> get() = _reminders

        fun updateLastLocation( latLng: LatLng) {
                _lastLatLng = latLng
        }
        fun updateLastPoi (poi:PointOfInterest){
                _lastPoi = poi
        }


        fun insertReminderToDatabase(databaseReminder:DatabaseReminder){
                viewModelScope.launch{
                        withContext(Dispatchers.IO){
                                database.reminderDao.insert(databaseReminder)
                        }
                }

        }
        fun getReminders(){
                var remindersPlaceholder: List<DatabaseReminder> = emptyList()
                viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                                try {
                                        remindersPlaceholder = database.reminderDao.getReminders()
                                        val got = true
                                } catch (e: Exception) {
                                        print(e.message)
                                }
                        }
                        _reminders.value = remindersPlaceholder
                }
        }



        class Factory(val app: Application) : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                                @Suppress("UNCHECKED_CAST")
                                return ReminderViewModel(app) as T
                        }
                        throw IllegalArgumentException("Unable to construct viewmodel")
                }
        }
}