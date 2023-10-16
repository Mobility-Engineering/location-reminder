package com.dexcom.sdk.locationreminders.reminder

import android.location.LocationManager
import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.dexcom.sdk.locationreminders.R
//import com.dexcom.sdk.locationreminders.database.DatabaseReminder
import com.dexcom.sdk.locationreminders.data.Result
import com.dexcom.sdk.locationreminders.data.Result.Success
import com.dexcom.sdk.locationreminders.data.source.RemindersRepository
import com.dexcom.sdk.locationreminders.reminders.RemindersFilterType
import com.dexcom.sdk.locationreminders.util.Event
import com.dexcom.sdk.locationreminders.util.GeofencingConstants
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.launch

class ReminderViewModel(val remindersRepository: RemindersRepository) : ViewModel() {

    /* class ReminderViewModel(app:Application) : AndroidViewModel(app){
        private val remindersRepository = DefaultRemindersRepository.getRepository(app)
        val database: ReminderDatabase = ReminderDatabase.getDatabase(app)

     */
    @VisibleForTesting
    private var fakePoi: PointOfInterest? = null
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
    var poi = _poi

    private val _geofencingRequest = MutableLiveData<GeofencingRequest>()
    var geofencingRequest  = _geofencingRequest


    /* Not used at the moment
    *
    *
    */

    private val _reminderUpdatedEvent = MutableLiveData<Event<Unit>>()
    val reminderUpdatedEvent: LiveData<Event<Unit>> = _reminderUpdatedEvent


    private val _isDataLoadingError = MutableLiveData<Boolean>()
    val isDataLoadingError: LiveData<Boolean> = _isDataLoadingError

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    /*
    private var _reminders = MutableLiveData<List<DatabaseReminder>>()
    val reminders: LiveData<List<DatabaseReminder>> get() = _reminders

     */

    //Unused
    private var _noDataVisibility = MutableLiveData<Int>()
    val noDataVisibility get() = _noDataVisibility

    /* START OF ALTERNATIVE CODE IMPLEMENTATION
    *
    *
    *
    *
 */


    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noRemindersLabel = MutableLiveData<Int>()
    val noRemindersLabel: LiveData<Int> = _noRemindersLabel

    private val _noReminderIconRes = MutableLiveData<Int>()
    val noRemindersIconRes: LiveData<Int> = _noReminderIconRes

    private val _remindersAddViewVisible = MutableLiveData<Boolean>()
    val remindersAddViewVisible: LiveData<Boolean> = _remindersAddViewVisible


    private val _forceUpdate = MutableLiveData<Boolean>(false)
    val forceUpdate: LiveData<Boolean> = _forceUpdate
    private val _items: LiveData<List<Reminder>> = _forceUpdate.switchMap { forceUpdate ->
        if (forceUpdate) {
            //TODO: Implement the progress bar
            loadData()
            /*
            _dataLoading.value = true
            viewModelScope.launch {
                remindersRepository.refreshReminders()
                _dataLoading.value = false
            }

             */
        }
        remindersRepository.observeReminders().switchMap { filterReminders(it) }
    }

    fun loadData() {
        _dataLoading.value = true
        viewModelScope.launch {
            remindersRepository.refreshReminders()
            _dataLoading.value = false
        }
    }


    private fun filterReminders(tasksResult: Result<List<Reminder>>): LiveData<List<Reminder>> {
        // TODO: This is a good case for liveData builder. Replace when stable.
        val result = MutableLiveData<List<Reminder>>()

        if (tasksResult is Success) {
            _isDataLoadingError.value = false
            viewModelScope.launch {
                result.value = filterItems(tasksResult.data, currentFiltering)
            }
        } else {
            result.value = emptyList()
            showSnackbarMessage(R.string.loading_reminders_error)
            _isDataLoadingError.value = true
        }
        return result
    }

    fun showSnackbarMessage(message: Int) {
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

    private val _dataLoading = MutableLiveData<Boolean>(false)

    val items: LiveData<List<Reminder>> = _items

    val loading: LiveData<Boolean> = _dataLoading
    val error = _isDataLoadingError
    val empty: LiveData<Boolean> = _items.map { it.isEmpty() }

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
        var id = 0L


        if (poi.value == null)
            poi.value = PointOfInterest(LatLng(0.0, 0.0), "MockPoi", "id")


        val newReminder = poi.value?.let { poi ->
            title.value?.let { title ->
                description.value?.let { description ->
                    Reminder(
                        0,
                        poi.name,

                        //poi.latLng.latitude,//latLng.latitude,
                        //poi.latLng.longitude,//latLng.longitude,

                        //*home latitude*/
                         20.556721,
                        //*home longitude*/
                        -100.507261,
                        title,
                        description
                    )
                }
            }
        }

        //TODO:Add on FragmentReminder preventive non null either Description or Title as POI is known for granted not to be NULL
        newReminder?.let {
            if ((title.value as String).isEmpty())
                showSnackbarMessage(R.string.no_title)
            else
                if ((description.value as String).isEmpty())
                    showSnackbarMessage(R.string.no_description)
                else
                    try {
                        id = remindersRepository.saveReminder(newReminder)

                        _reminderUpdatedEvent.value = Event(Unit)
                        if (id > 0L) {
                            //Updated with generated id

                            addReminderGeofence(newReminder, id)
                            //showSnackbarMessage(R.string.no_description)
                        } else {

                        }


                    } catch (e: Exception) {
                        e?.message?.let { Log.e("REMINDER_VIEW_MODEL", it) }
                    }
        } ?: run {
            showSnackbarMessage(if (title.value == null) R.string.no_title else R.string.no_description)
        }
        //TODO:Update reminderId LiveData for ReminderFragment to generate the intent, geofence, request
        //and removeGeofence.OnCompleteListener
        // _reminderUpdatedEvent.value = Event(Unit)
    }

    private fun addReminderGeofence(geofenceData: Reminder, id: Long) {
        val geofence = Geofence.Builder()

            //Set the request ID, string to identify the geofence.
            .setRequestId(id.toString())
            .setCircularRegion(
                geofenceData.latitude,
                geofenceData.longitude,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(60000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        _geofencingRequest.value  = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            //.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)



            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()
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

    fun geofenceAdded(){
        geofencingRequest.value = null
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


    @Suppress("UNCHECKED_CAST")
    class RemindersViewModelFactory(private val remindersRepository: RemindersRepository) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {

                return ReminderViewModel(remindersRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }


}