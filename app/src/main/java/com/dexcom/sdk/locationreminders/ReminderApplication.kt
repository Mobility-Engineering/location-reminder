package com.dexcom.sdk.locationreminders

import android.app.Application
import com.dexcom.sdk.locationreminders.data.source.RemindersRepository
import timber.log.Timber.DebugTree
import timber.log.Timber

class ReminderApplication: Application() {
    
    val remindersRepository:RemindersRepository
        get() = ServiceLocator.provideRemindersRepository(this)

    override fun onCreate(){
        super.onCreate()
        if(BuildConfig.DEBUG) Timber.plant(DebugTree())
    }
}