package com.dexcom.sdk.locationreminders.reminder

import android.app.Application
import android.graphics.Point
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.data.source.DefaultRemindersRepository
//import com.dexcom.sdk.locationreminders.database.DatabaseReminder
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.Result.Success
import com.dexcom.sdk.locationreminders.database.ReminderDatabase
import com.dexcom.sdk.locationreminders.reminders.RemindersFilterType
import com.dexcom.sdk.locationreminders.util.Event
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.launch

class ReminderViewModel(val app: Application) : AndroidViewModel(app) {


    private val remindersRepository = DefaultRemindersRepository.getRepository(app)
    val database: ReminderDatabase = ReminderDatabase.getDatabase(app)
    private var currentFiltering = RemindersFilterType.ALL_REMINDERS

    /*LOCATION Data Holders
    *
    */
// Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

    private val latLng = MutableLiveData<LatLng>()

    private val _poi = MutableLiveData<PointOfInterest>()
    val poi = _poi

    /* Not used at the moment
    *
    *
    */

    private val _reminderUpdatedEvent = MutableLiveData<Event<Unit>>()
    val reminderUpdatedEvent: LiveData<Event<Unit>> = _reminderUpdatedEvent

    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    /*
    private var _reminders = MutableLiveData<List<DatabaseReminder>>()
    val reminders: LiveData<List<DatabaseReminder>> get() = _reminders

     */

    private var _noDataVisibility = MutableLiveData<Int>()
    val noDataVisibility get() = _noDataVisibility

    /* START OF ALTERNATIVE CODE IMPLEMENTATION
    *
    *
    *
    *
 */
    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noRemindersLabel = MutableLiveData<Int>()
    val noTasksLabel: LiveData<Int> = _noRemindersLabel

    private val _noReminderIconRes = MutableLiveData<Int>()
    val noTaskIconRes: LiveData<Int> = _noReminderIconRes

    private val _remindersAddViewVisible = MutableLiveData<Boolean>()
    val tasksAddViewVisible: LiveData<Boolean> = _remindersAddViewVisible

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _items: LiveData<List<Reminder>> = _forceUpdate.switchMap { forceUpdate ->
        if (forceUpdate) {
            _dataLoading.value = true
            viewModelScope.launch {
                remindersRepository.refreshReminders()
                _dataLoading.value = false
            }
        }
        remindersRepository.observeReminders().switchMap { filterReminders(it) }
    }

    private fun filterReminders(tasksResult: Result<List<Reminder>>): LiveData<List<Reminder>> {
        // TODO: This is a good case for liveData builder. Replace when stable.
        val result = MutableLiveData<List<Reminder>>()

        if (tasksResult is Success) {
            isDataLoadingError.value = false
            viewModelScope.launch {
                result.value = filterItems(tasksResult.data, currentFiltering)
            }
        } else {
            result.value = emptyList()
            showSnackbarMessage(R.string.loading_reminders_error)
            isDataLoadingError.value = true
        }
        return result
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    private fun filterItems(
        reminders: List<Reminder>,
        filteringType: RemindersFilterType
    ): List<Reminder> {
        val remindersToShow = ArrayList<Reminder>()
        // We filter the tasks based on the requestType
        for (reminder in reminders) {
            when (filteringType) {
                RemindersFilterType.ALL_REMINDERS -> remindersToShow.add(reminder)
                RemindersFilterType.ACTIVE_REMINDERS -> if (reminder.isActive) {
                    remindersToShow.add(reminder)
                }
                RemindersFilterType.COMPLETED_REMINDERS -> if (reminder.isCompleted) {
                    remindersToShow.add(reminder)
                }
            }
        }
        return remindersToShow
    }

    val items: LiveData<List<Reminder>> = _items

    init {
        //updateReminders()
        setFiltering(RemindersFilterType.ALL_REMINDERS)
        loadReminders(true)
    }

    fun setFiltering(requestType: RemindersFilterType) {
        currentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            RemindersFilterType.ALL_REMINDERS -> {
                setFilter(
                    R.string.label_all, R.string.no_reminders_all,
                    R.drawable.logo_no_fill, true
                )
            }
            RemindersFilterType.ACTIVE_REMINDERS -> {
                setFilter(
                    R.string.label_active, R.string.no_reminders_active,
                    R.drawable.ic_check_circle_96dp, false
                )
            }
            RemindersFilterType.COMPLETED_REMINDERS -> {
                setFilter(
                    R.string.label_completed, R.string.no_reminders_completed,
                    R.drawable.ic_verified_user_96dp, false
                )
            }
        }
        // Refresh list
        loadReminders(false)
    }

    fun loadReminders(forceUpdate: Boolean) {
        _forceUpdate.value = forceUpdate
    }

    fun saveReminder() = viewModelScope.launch {
        /*
        val latLng = lastLatLng
        val poi = lastPoi

         */

        val newReminder = poi.value?.let { poi ->
            title.value?.let { title ->
                description.value?.let { description ->
                    Reminder(
                        0,
                        poi.name,
                        poi.latLng.latitude,//latLng.latitude,
                        poi.latLng.longitude,//latLng.longitude,
                        title,
                        description
                    )
                }
            }
        }

        if (newReminder != null) {
            remindersRepository.saveReminder(newReminder)
        }
        _reminderUpdatedEvent.value = Event(Unit)
    }

    /* INCLUDED AS PART OF ADD_EDIT_TASK_VIEW_MODEL
    fun saveTask() {
    val currentTitle = title.value
    val currentDescription = description.value

    if (currentTitle == null || currentDescription == null) {
    _snackbarText.value = Event(R.string.empty_task_message)
    return
    }
    if (Task(currentTitle, currentDescription).isEmpty) {
    _snackbarText.value = Event(R.string.empty_task_message)
    return
    }

    val currentTaskId = taskId
    if (isNewTask || currentTaskId == null) {
    createTask(Task(currentTitle, currentDescription))
    } else {
    val task = Task(currentTitle, currentDescription, taskCompleted, currentTaskId)
    updateTask(task)
    }
    }

    */
    fun refresh() {
        _forceUpdate.value = true
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int, @StringRes noRemindersLabelString: Int,
        @DrawableRes noRemindersIconDrawable: Int, remindersAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noRemindersLabel.value = noRemindersLabelString
        _noReminderIconRes.value = noRemindersIconDrawable
        _remindersAddViewVisible.value = remindersAddVisible
    }

    fun showReminderDataIcon() {
        _noDataVisibility.value = View.VISIBLE
    }

    fun updateLastLocation(latLng: LatLng) {
        this.latLng.value = latLng
    }

    fun updateLastPoi(poi: PointOfInterest) {
        this.poi.value = poi
    }

/*
fun insertReminderToDatabase(databaseReminder: DatabaseReminder) {
viewModelScope.launch {
withContext(Dispatchers.IO) {
    database.remindersDao().insert(databaseReminder)
}
}

}

*/

/*
fun updateReminders() {
var reminders: List<Reminder> = emptyList()
viewModelScope.launch {
withContext(Dispatchers.IO) {
    try {
        reminders = database.remindersDao().getReminders()
        val got = true
    } catch (e: Exception) {
        print(e.message)
    }
}

_reminders.value = remindersPlaceholder
_noDataVisibility.value =
    if ((_reminders.value as List<DatabaseReminder>).isEmpty()) {
        View.VISIBLE
    } else View.INVISIBLE
}
}

*/


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