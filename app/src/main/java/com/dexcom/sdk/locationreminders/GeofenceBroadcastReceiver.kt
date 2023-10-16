package com.dexcom.sdk.locationreminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.dexcom.sdk.locationreminders.RemindersActivity.Companion.ACTION_GEOFENCE_EVENT
import com.dexcom.sdk.locationreminders.reminder.ReminderViewModel
import com.dexcom.sdk.locationreminders.util.GeofencingConstants.EXTRA_GEOFENCE_INDEX
import com.dexcom.sdk.locationreminders.util.errorMessage
import com.dexcom.sdk.locationreminders.util.sendGeofenceEnteredNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/*
 * Triggered by the Geofence.  Since we only have one active Geofence at once, we pull the request
 * ID from the first Geofence, and locate it within our database. We
 * then pass the Geofence index into the notification, which allows us to have a custom "found"
 * message associated with each Geofence.
 */

/*Android OS sends broadcasts to apps when any event happens in the app or in the system.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.let {
            ContextCompat.getSystemService(
                it,
                NotificationManager::class.java
            )
        } as NotificationManager

        notificationManager.sendGeofenceEnteredNotification(
            context, 0
        )

            if (intent != null) {
                if (intent.action == ACTION_GEOFENCE_EVENT) {
                    val geofencingEvent = GeofencingEvent.fromIntent(intent)


                    if (geofencingEvent != null) {
                        if (geofencingEvent.hasError()) {
                            val errorMessage =
                                context?.let { errorMessage(it, geofencingEvent.errorCode) }
                            if (errorMessage != null) {
                                Log.e(TAG, errorMessage)
                            }
                            return
                        }


                        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                            Log.v(TAG, "Geofence entered")

                            val fenceId = when {
                                geofencingEvent.triggeringGeofences?.isNotEmpty() == true ->
                                    geofencingEvent.triggeringGeofences?.get(0)?.requestId

                                else -> {
                                    Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                                    return
                                }
                            }

                            /* It aint' working as putIntExtra EXTRA_GEOFENCE_INDEX is put on sendGeofenceEnteredNotification
                            val foundIndex = intent.getIntExtra(EXTRA_GEOFENCE_INDEX, -1)

                            // Unknown Geofences aren't helpful to us
                            if (-1 == foundIndex) {
                                Log.e(TAG, "Unknown Geofence: Abort Mission")
                                return
                            }
                             */
                            val notificationManager = context?.let {
                                ContextCompat.getSystemService(
                                    it,
                                    NotificationManager::class.java
                                )
                            } as NotificationManager

                            notificationManager.sendGeofenceEnteredNotification(
                                context, 0
                            )
                        }
                    }
                }
            }
        }
    }



        const val TAG = "GeofenceReceiver"