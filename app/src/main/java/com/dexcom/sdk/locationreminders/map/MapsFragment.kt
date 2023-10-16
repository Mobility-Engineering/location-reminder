package com.dexcom.sdk.locationreminders.map


import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dexcom.sdk.locationreminders.BuildConfig
import com.dexcom.sdk.locationreminders.GeofenceBroadcastReceiver
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.RemindersActivity
import com.dexcom.sdk.locationreminders.databinding.FragmentMapsBinding
import com.dexcom.sdk.locationreminders.reminder.ReminderViewModel
import com.dexcom.sdk.locationreminders.worker.LocationRequestWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_maps.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient

    private val viewModel by activityViewModels<ReminderViewModel>()
    private var checkForPermission = false
    private var lastPermissionRequest = PERMISSION_HOLDER
    private val runningROrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private var lastMarker: Marker? = null
    private lateinit var mapsFragment: SupportMapFragment
    private lateinit var map: GoogleMap


    val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("PERMISSIONS", "${it.key} = ${it.value}")
        }

    }


    @RequiresApi(Build.VERSION_CODES.S)
    val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //if (!foregroundAndBackgroundPermissionApproved())

            when (lastPermissionRequest) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {

                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            map.isMyLocationEnabled = true
                        } catch (e: SecurityException) {
                            e.message?.let { Log.i(TAG, it) }
                        }
                    }

                }

                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                    updateMap()
                }
            }


            //TODO:When permissions are denied what are sent directly into the Seettings screen
            //Validate ACCESS_BACKGROUND_LOCATION | ACCESS_FINE_LOCATION denial scenarios to be handled correctly
            requestForegroundAndBackgroundPermissionApproved()
            //Toast.makeText(context, R.string.poi_selection, Toast.LENGTH_LONG).show()

        } else {


            showPermissionsWarning()
        }
    }

    private fun showPermissionsWarning() {
        Snackbar.make(
            requireActivity().findViewById(R.id.coordinator_layout),
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.settings) {
            // Displays App settings screen.
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }.show()
    }

    private fun showNextStepIndications() {

        val snackbar = Snackbar.make(
            requireActivity().findViewById(R.id.coordinator_layout),
            R.string.next_step_instructions,
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction(R.string.ok) {
            snackbar.dismiss()
            startSaveButtonAnimation()
        }
        snackbar.show()
    }

    private fun startSaveButtonAnimation() {

        val animator = ObjectAnimator.ofFloat(
            binding.saveButton,
            View.TRANSLATION_X,
            -binding.saveButton.width.toFloat(),
            0f
        )
        animator.duration = 1000
        animator.disableViewDuringAnimation(binding.saveButton)

        animator.start()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestForegroundAndBackgroundPermissionApproved() {
        if (foregroundAndBackgroundPermissionApproved())
            return

        if (runningROrLater) { //Location Permissions most be requested individually on R or greater

            if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                lastPermissionRequest = Manifest.permission.ACCESS_FINE_LOCATION
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                lastPermissionRequest = Manifest.permission.ACCESS_BACKGROUND_LOCATION
                permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                updateMap()
            }

            return
        } else { ///Device lower than R

            var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsArray += Manifest.permission.ACCESS_COARSE_LOCATION
            val resultCode = when {
                runningQOrLater -> {
                    permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }

                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }
            permissionsLauncher.launch(permissionsArray)
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun grantPermissionManually(permission: String) {
        permissionLauncher.launch(permission)
    }

    private fun checkForPermissions(permissions: Array<String>) {
//Most check if the version of Android is greater than Q
        //multPermissionsLauncher.launch(permissions)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {

        super.onResume()
        //If permissions were granted when returning from Settings screen update Map

        if (foregroundAndBackgroundPermissionApproved() && ::map.isInitialized) {
            map.clear()
            updateMap()
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        _binding = FragmentMapsBinding.inflate(inflater, container, false)


        initializePlaces()

        binding.saveButton.setOnClickListener {


            this.findNavController()
                .navigate(MapsFragmentDirections.actionMapsFragmentToReminderFragment())
        }
        return binding.root
    }

    private fun initializePlaces() {
        Places.initialize(requireContext().applicationContext, getString(R.string.google_api_key))
        placesClient = Places.createClient(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                menuInflater.inflate(R.menu.map_options, menu)
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                val format = "yyyy-MM-dd"
                val today = Date()
                return when (menuItem.itemId) {
                    R.id.normal_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        //showCurrentPlace()
                        true
                    }

                    R.id.hybrid_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }

                    R.id.satellite_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }

                    R.id.terrain_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }

                    else -> true //TODO:Settings action
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        mapsFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapsFragment.getMapAsync(this)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Called when user makes a long press gesture on the map.
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    //.title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }
    }

    // Places a marker on the map and displays an info window that contains POI name.
    private fun setLocationClick(map: GoogleMap) {
        map.setOnMapClickListener { loc ->
            lastMarker?.let {
                it.remove()
            }
            lastMarker = map.addMarker(
                MarkerOptions()
                    .position(loc)
            )
            viewModel.updateLastLocation(loc)
        }
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            binding.saveButton.isEnabled = true
            lastMarker?.let {
                it.remove()
            }
            lastMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            lastMarker?.showInfoWindow()
            viewModel.updateLastLocation(poi.latLng)
            viewModel.updateLastPoi(poi)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success =
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
            if (!success)
                Log.e(TAG, "Style parsing failed")
        } catch (e: Resources.NotFoundException) {
            e.message?.let {
                Log.e(TAG, "{Style error: $it) }")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        setPoiClick(map)
        setLocationClick(map)
        updateMap()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun updateMap() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {
                    Timber.w("$location.latitude, $location.longitude")
                }
                // Got last known location. In some rare situations this can be null.
            }
        map.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location: Location? = task.result
            location?.let {
                val latitude = /*20.55689053720646*/it.latitude
                val longitude = /*-100.50583720207214*/it.longitude
                val zoomLevel = 15f
                val overlaySize = 100f

                val homeLatLng = LatLng(latitude, longitude)
                viewModel.updateLastLocation(homeLatLng)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
                map.addMarker(MarkerOptions().position(homeLatLng))
            }
        }

        //UNCOMMENT for app to work as usual


        fusedLocationProviderClient.requestLocationUpdates(
            createLocationRequest(),
            //createLocationCallback(),
            //Looper.getMainLooper()
            createPendingIntent()
        )








            //setupLocationRequestWork()
    }

    private fun createPendingIntent(): PendingIntent {
        //val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            intent.action = RemindersActivity.ACTION_GEOFENCE_EVENT
            // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createLocationRequest(): LocationRequest {
        return  LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 2000
            priority = PRIORITY_HIGH_ACCURACY
            maxWaitTime = 2000
        }

    }

    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                Timber.i("latitude: ${locationResult.lastLocation?.latitude}, longitude: ${locationResult.lastLocation?.longitude}")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        requestForegroundAndBackgroundPermissionApproved()
        if (foregroundAndBackgroundPermissionApproved())
            showNextStepIndications()
        //checkPermissionsAndStartGeofencing()
    }


    private fun foregroundAndBackgroundPermissionApproved(): Boolean {
        val foregroundLocationApproved =
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        val backgroundPermissionApproved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
        return foregroundLocationApproved && backgroundPermissionApproved
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ObjectAnimator.disableViewDuringAnimation(view: View) {

        // This extension method listens for start/end events on an animation and disables
        // the given view for the entirety of that animation.

        addListener(object : AnimatorListenerAdapter() {


            override fun onAnimationStart(animation: Animator) {
                binding.saveButton.visibility = View.VISIBLE
                //view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator) {
            }
        })


    }

    /*
    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (map == null) {
            return
        }
        if (foregroundAndBackgroundPermissionApproved()) {

            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

            //Use the builder to create a FindCurrentPlaceRequest
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            //Get the likely places = that is, the bussiness and other points of interest that
            // are the best match for the devices' current location
            val placeResult = placesClient.findCurrentPlace(request)

            placeResult.addOnCompleteListener { task ->
                if (!task.isSuccessful){ //&& task.result != null) {

                    try {
                        if (task.result != null) {
                            val likelyPlaces = task.result


                            //Set the count, handling cases where less thatn 5 entries are returned
                            val count =
                                if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
                                    likelyPlaces.placeLikelihoods.size
                                } else {
                                    M_MAX_ENTRIES
                                }
                            var i = 0
                            likelyPlaceNames = arrayOfNulls(count)
                            likelyPlaceAddresses = arrayOfNulls(count)
                            likelyPlaceAttributions = arrayOfNulls(count)
                            likelyPlaceLatLngs = arrayOfNulls(count)
                            for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                                likelyPlaceNames[i] = placeLikelihood.place.name
                                likelyPlaceAddresses[i] = placeLikelihood.place.address
                                likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                                likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
                                i++
                                if (i > count - 1) {
                                    break
                                }
                            }


                            // Show a dialog offering the user the list of likely places, and add a
                            // maker at the selected place.
                            openPlacesDialog()
                        }
                    }catch(e:Exception){
                e.message?.let { Log.e(TAG, it) }
            }
                } else {
                    Log.e(TAG, "Exception: % %S", task.exception)

                }
            }
        } else {
            //No permission granted
        }
    }

    private fun openPlacesDialog() {
        //Ask the user to choose the place where they are now.
        val listener =
            DialogInterface.OnClickListener { dialog, which -> //the argument which contains the position of the selected item.
                val markerLatLng = likelyPlaceLatLngs[which]
                var markerSnippet = likelyPlaceAddresses[which]
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = """
                    $markerSnippet
                    ${likelyPlaceAttributions[which]}
                    """.trimIndent()
                }

                if (markerLatLng == null) {
                    //View shouldn't execute the code after this statement, but outside the OnClickListener the flow will still remain the same
                    return@OnClickListener
                }
                //Add a marker for the selected placce, with an info window
                //showing information about that place.
                map?.addMarker(
                    MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet)
                )

                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, DEFAULT_ZOOM.toFloat()))
            }

        AlertDialog.Builder(requireContext())//When only Context is required as opossed to Activity, use this
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
    }
*/
    private fun setupLocationRequestWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .build()
                /*
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }

                 */



        //COMMENT starLocationRequestWork(constraints)
    }

    private fun extracted(constraints: Constraints) {
        val locationUpdateRequest = PeriodicWorkRequest.Builder(
            LocationRequestWorker::class.java, 15L, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()


        activity?.let {
            WorkManager.getInstance(it.applicationContext).enqueueUniquePeriodicWork(
                LocationRequestWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                locationUpdateRequest
            )
        }
    }

    companion object {
        private const val TAG = "MAPS_FRAGMENT"
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 1
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2
        private const val PERMISSION_HOLDER = "PERMISSION_HOLDER"
        private const val M_MAX_ENTRIES = 5
        private const val DEFAULT_ZOOM = 15

    }
}