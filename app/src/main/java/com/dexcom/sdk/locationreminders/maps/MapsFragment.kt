package com.dexcom.sdk.locationreminders.maps


import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.dexcom.sdk.locationreminders.BuildConfig
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var checkForPermission = false
    private var lastPermissionRequest = PERMISSION_HOLDER
    private val runningROrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapsFragment: SupportMapFragment
    private lateinit var map: GoogleMap


    val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("PERMISSIONS", "${it.key} = ${it.value}")
        }

    }


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

        snackbar.setAction(R.string.next_steps) {
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
        animator.duration = 2500
        animator.disableViewDuringAnimation(binding.saveButton)

        animator.start()
    }


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


    private fun grantPermissionManually(permission: String) {
        permissionLauncher.launch(permission)
    }

    private fun checkForPermissions(permissions: Array<String>) {
//Most check if the version of Android is greater than Q
        //multPermissionsLauncher.launch(permissions)
    }

    override fun onResume() {

        super.onResume()
        //If permissions were granted when returning from Settings screen update Map
        if (foregroundAndBackgroundPermissionApproved() && ::map.isInitialized)
            updateMap()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap
            setMapStyle(map)
            updateMap()
        }

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
            map.isMyLocationEnabled = true

            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val zoomLevel = 15f
                    val overlaySize = 100f

                    val homeLatLng = LatLng(latitude, longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
                    map.addMarker(MarkerOptions().position(homeLatLng))
                }
            }

        }

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
                    view.isEnabled = true
                }
            })


        }

        companion object {
            const val TAG = "MAPS_FRAGMENT"
            const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 1
            const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2
            const val PERMISSION_HOLDER = "PERMISSION_HOLDER"

        }
    }