package com.dexcom.sdk.locationreminders.reminder

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dexcom.sdk.locationreminders.ActivityLifeCycleObserver
import com.dexcom.sdk.locationreminders.MainActivity
import com.dexcom.sdk.locationreminders.R
//import com.dexcom.sdk.locationreminders.database.DatabaseReminder
import com.dexcom.sdk.locationreminders.databinding.FragmentMapsBinding
import com.dexcom.sdk.locationreminders.databinding.FragmentReminderBinding
import com.dexcom.sdk.locationreminders.map.MapsFragmentDirections
import com.dexcom.sdk.locationreminders.util.EventObserver
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

    private val viewModel by activityViewModels<ReminderViewModel>()
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
        _binding.viewModel = viewModel
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        /*activity?.lifecycle?.addObserver(ActivityLifeCycleObserver {
            Log.i("REMINDERS_FRAGMENT", "ActivityLifeCycleObserver works!")
        })

         */
        setupNavigation()
    }
    private fun setupNavigation() {
        viewModel.reminderUpdatedEvent.observe(this, EventObserver {
            val action = ReminderFragmentDirections
                .actionReminderFragmentToRemindersFragment()
            findNavController().navigate(action)

        })
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
    }
}