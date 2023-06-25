package com.dexcom.sdk.locationreminders.maps


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dexcom.sdk.locationreminders.BuildConfig
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MapsFragment : Fragment(), OnMapReadyCallback {
    //Estoy diciendo que sera un fragmento normal pero que implementa un SupportMapFragment por lo que sin problema debera de poderse
    //hacer la conversion una vez que
    private var lastPermissionRequest = PERMISSION_HOLDER
    private val runningROrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

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
                        }
                    }

                }
                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {

                }
            }



            requestForegroundAndBackgroundPermissionApproved()
            //Toast.makeText(context, R.string.poi_selection, Toast.LENGTH_LONG).show()

        } else {

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
    }


    private fun requestForegroundAndBackgroundPermissionApproved() {
        if (foregroundAndBackgroundPermissionApproved())
            return

        if (runningROrLater) { //Location Permissions have to be requested individually on R or greater
            if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                lastPermissionRequest = Manifest.permission.ACCESS_FINE_LOCATION

                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                lastPermissionRequest = Manifest.permission.ACCESS_FINE_LOCATION
                permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            return
        }





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


private fun grantPermissionManually(permission: String) {
    permissionLauncher.launch(permission)
}

private fun checkForPermissions(permissions: Array<String>) {
//Most check if the version of Android is greater than Q
    //multPermissionsLauncher.launch(permissions)
}

override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    _binding = FragmentMapsBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onStart() {
    super.onStart()
    requestForegroundAndBackgroundPermissionApproved()
    //checkPermissionsAndStartGeofencing()
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val mapsFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapsFragment.getMapAsync(this)
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


override fun onMapReady(googleMap: GoogleMap) {
    map = googleMap
    if (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
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
    map.isMyLocationEnabled = true
    val latitude = 37.422160
    val longitude = -122.084270
    val zoomLevel = 15f
    val overlaySize = 100f

    val homeLatLng = LatLng(latitude, longitude)
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
    map.addMarker(MarkerOptions().position(homeLatLng))
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}

companion object {
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 1
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2
    const val PERMISSION_HOLDER = "PERMISSION_HOLDER"
}
}