package com.dexcom.sdk.locationreminders.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import timber.log.Timber

class LocationRequestWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private var fusedLocationProviderClient = FusedLocationProviderClient(appContext)
    private val context = appContext


    override suspend fun doWork(): Result {
        //Might comment to validate that initialization on class property definition took place correctly
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        //StartLocationRequest from GeofencingClient
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                createLocationRequest(),
                createLocationCallback(),
                Looper.getMainLooper()
            )
            Timber.d("Location updates request")
        }


        //Thread.sleep(15000)
        //while(true)
        return Result.success()
    }
    companion object {
        const val WORK_NAME = "LocationRequestWorker"
    }
}



private fun createLocationRequest(): LocationRequest {
    return  LocationRequest.create().apply {
        interval = 2000
        fastestInterval = 2000
        priority = Priority.PRIORITY_HIGH_ACCURACY
        maxWaitTime = 2000
    }

}

private fun keeWorkAlive(){


}


private fun createLocationCallback(): LocationCallback {
    return object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            Timber.i("latitude: ${locationResult.lastLocation?.latitude}, longitude: ${locationResult.lastLocation?.longitude}")
        }

    }
}