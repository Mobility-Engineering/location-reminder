package com.dexcom.sdk.locationreminders.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dexcom.sdk.locationreminders.ActivityLifeCycleObserver
import com.dexcom.sdk.locationreminders.GeofenceBroadcastReceiver
import com.dexcom.sdk.locationreminders.RemindersActivity
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.ReminderApplication
import com.dexcom.sdk.locationreminders.data.source.DefaultRemindersRepository
import com.dexcom.sdk.locationreminders.data.source.RemindersRepository
//import com.dexcom.sdk.locationreminders.database.DatabaseReminder
import com.dexcom.sdk.locationreminders.databinding.FragmentMapsBinding
import com.dexcom.sdk.locationreminders.databinding.FragmentReminderBinding
import com.dexcom.sdk.locationreminders.map.MapsFragmentDirections
import com.dexcom.sdk.locationreminders.util.EventObserver
import com.dexcom.sdk.locationreminders.util.setupSnackbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReminderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReminderFragment : Fragment() {
    private lateinit var fab: FloatingActionButton
    private lateinit var _binding: FragmentReminderBinding
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<ReminderViewModel> {
        //private val viewModel by viewModels<ReminderViewModel>{
        //ReminderViewModel.RemindersViewModelFactory(DefaultRemindersRepository.getRepository(requireActivity().application))
        ReminderViewModel.RemindersViewModelFactory((requireActivity().application as ReminderApplication).remindersRepository)
    }
    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.


    /*private var geofencingClient: GeofencingClient =
        (requireActivity() as RemindersActivity).geofencingClient

     */

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = RemindersActivity.ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }


    //private val viewModel by activityViewModels<ReminderViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*
        fab =
            (requireActivity() as MainActivity).findViewById(R.id.fab) as FloatingActionButton
        fab.visibility = View.VISIBLE

         */
        /*
        fab.setOnClickListener {
            val latLng = viewModel.lastLa   tLng
            val poi = viewModel.lastPoi
            /*viewModel.saveReminder(Reminder(
                    0,
                    poi.name,
                    poi.latLng.latitude,//latLng.latitude,
                    poi.latLng.longitude,//latLng.longitude,
                    binding.editTextTextTitle.text.toString(),
                    binding.editTextTextDescription.text.toString())
                )

             */
            findNavController().navigate(ReminderFragmentDirections.actionReminderFragmentToRemindersFragment())
        }

         */
        // Inflate the layout for this fragment
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.geofencingRequest.observe(viewLifecycleOwner) { request ->
            // Add the new geofence request with the new geofence
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showSnackBarMessage(R.string.permission_denied_explanation)
                return@observe
            }
            try {
                val geofencingClient = (requireActivity() as RemindersActivity).geofencingClient

                //geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                    geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
                        addOnSuccessListener {
                            // Geofences added.
                            Toast.makeText(
                                requireContext(), R.string.geofences_added,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        Log.e("Add Geofence", request.geofences.get(0).requestId)
                        addOnFailureListener {
                            // Failed to add geofences.
                            Toast.makeText(
                                requireContext(), R.string.geofences_not_added,
                                Toast.LENGTH_SHORT
                            ).show()
                            if ((it.message != null)) {
                                Timber.w(it)
                            }
                        }

                   // }
                        viewModel.geofenceAdded()
                }
            } catch (e: Exception) {
                Timber.w(e.message)
            }

        }

        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(ActivityLifeCycleObserver {
            setupNavigation()
            // Log.i("REMINDERS_FRAGMENT", "ActivityLifeCycleObserver works!")
        })


        /*view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()

         */
    }

    private fun setupNavigation() {

        viewModel.reminderUpdatedEvent.observe(this, EventObserver {
            val action = ReminderFragmentDirections
                .actionReminderFragmentToRemindersFragment()
            findNavController().navigate(action)
        })
    }

    private fun showSnackBarMessage(message: Int) {
        viewModel.showSnackbarMessage(message)
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true): Boolean {

        var result: Boolean = false
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.w("Error geting location settings resolution: %s", sendEx.message)

                }
                result = false
            } else {
                showSnackBarMessage(R.string.permission_denied_explanation)
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                result = true
            }
        }
        return result
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReminderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReminderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    }
}