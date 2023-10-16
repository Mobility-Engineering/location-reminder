package com.dexcom.sdk.locationreminders.reminder

import android.app.Application
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dexcom.sdk.locationreminders.worker.LocationRequestWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ReminderApplication : Application(),  LifecycleEventObserver {

    val applicationScope = CoroutineScope(Dispatchers.Default)
    lateinit var lifeCycleEvent: Lifecycle.Event 
    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()


        val locationUpdateRequest =
            OneTimeWorkRequest.Builder(
                LocationRequestWorker::class.java).setConstraints(constraints)
                .build()


        WorkManager.getInstance(applicationContext).enqueue(locationUpdateRequest)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        //delayedInit()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_STOP -> lifeCycleEvent  =Lifecycle.Event.ON_CREATE
            Lifecycle.Event.ON_CREATE -> lifeCycleEvent  =Lifecycle.Event.ON_CREATE
            Lifecycle.Event.ON_START -> lifeCycleEvent  =Lifecycle.Event.ON_START
            Lifecycle.Event.ON_RESUME -> lifeCycleEvent  =Lifecycle.Event.ON_RESUME
            Lifecycle.Event.ON_PAUSE -> lifeCycleEvent  =Lifecycle.Event.ON_PAUSE
            Lifecycle.Event.ON_DESTROY -> lifeCycleEvent  =Lifecycle.Event.ON_DESTROY
            Lifecycle.Event.ON_ANY -> lifeCycleEvent  =Lifecycle.Event.ON_ANY
        }
    }



}