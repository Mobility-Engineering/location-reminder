package com.dexcom.sdk.locationreminders

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.dexcom.sdk.locationreminders.databinding.ActivityRemindersBinding
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.dexcom.sdk.locationreminders.reminder.ReminderViewModel
import com.dexcom.sdk.locationreminders.util.GeofencingConstants
import com.dexcom.sdk.locationreminders.util.createChannel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import timber.log.Timber

class RemindersActivity : AppCompatActivity(){

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityRemindersBinding
    private lateinit var reminderViewModel: ReminderViewModel
    lateinit var geofencingClient: GeofencingClient

    //REMOVE
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reminderViewModel = ViewModelProvider(this, ReminderViewModel.RemindersViewModelFactory(
                ServiceLocator.provideRemindersRepository(this))).get(
                ReminderViewModel::class.java)

                createChannel(this)
                /*DefaultRemindersRepository.getRepository(application))).get(
                ReminderViewModel::class.java)

                 */

            //)//Factory(application)).get(
                //                ReminderViewModel::class.java)
        //
        geofencingClient = LocationServices.getGeofencingClient(this)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        checkDeviceLocationSettingsAndStartGeofence()
        //addFakeGeofence()
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(    this@RemindersActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
               //Checks for failure
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                //addFakeGeofence()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun addFakeGeofence(){
        try {
            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                if (ActivityCompat.checkSelfPermission(
                        this@RemindersActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }

                val request = createFakeGeofenceRequest()
                geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                    geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
                        addOnSuccessListener {
                            // Geofences added.
                            Toast.makeText(
                                this@RemindersActivity, R.string.geofences_added,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        Log.e("Add Geofence", request.geofences.get(0).requestId)
                        addOnFailureListener {
                            // Failed to add geofences.
                            Toast.makeText(
                                this@RemindersActivity, R.string.geofences_not_added,
                                Toast.LENGTH_SHORT
                            ).show()
                            if ((it.message != null)) {
                                Timber.w(it)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e.message)
        }
    }

    //private fun createPendingIntent

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }



    //This got implemented in MapsFragment you bet, so now lets fix that addition of weird markers every now and then

     private fun startLocationUpdates(){
         val locationRequest = LocationRequest.create().apply {
             priority = Priority.PRIORITY_LOW_POWER


         }

     }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.locationReminder.action.ACTION_GEOFENCE_EVENT"
    }
}

private fun createFakeGeofenceRequest():GeofencingRequest {
        val geofence = Geofence.Builder()

            //Set the request ID, string to identify the geofence.
            .setRequestId(29.toString())
            .setCircularRegion(
                /*home-lat */20.556721,/*20.556768,*/
                /*home long */-100.507261,/*- 100.505811,*/
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(60000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        return GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()
    }



class ActivityLifeCycleObserver(private val update:()->Unit):
    DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycle.removeObserver(this)
        update()
    }
}

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29