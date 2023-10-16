package com.dexcom.sdk.locationreminders.reminders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dexcom.sdk.locationreminders.ActivityLifeCycleObserver
import com.dexcom.sdk.locationreminders.RemindersActivity
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.ReminderApplication
import com.dexcom.sdk.locationreminders.database.asDomainModel
import com.dexcom.sdk.locationreminders.databinding.FragmentRemindersBinding
import com.dexcom.sdk.locationreminders.reminder.Reminder
import com.dexcom.sdk.locationreminders.reminder.ReminderViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class   RemindersFragment : Fragment() {
    private lateinit var _binding: FragmentRemindersBinding
    private val binding get() = _binding!!
    private lateinit var adapter: RemindersAdapter
    private lateinit var fab: FloatingActionButton

    //val viewModel by activityViewModels<ReminderViewModel>() //by vieModels
    val viewModel:ReminderViewModel by viewModels<ReminderViewModel> {
        ReminderViewModel.RemindersViewModelFactory((requireContext().applicationContext as ReminderApplication).remindersRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*
        fab = (requireActivity() as MainActivity).findViewById(R.id.fab) as FloatingActionButton
        fab.visibility = View.INVISIBLE

         */
        // Inflate the layout for this fragment

        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.executePendingBindings()

        adapter = RemindersAdapter(
            OnClickListener {
                Log.d("TAG", it.name)

            })

        binding.recycler.adapter = adapter


        /*viewModel.reminders.observe(viewLifecycleOwner, Observer {

            adapter.submitList(it.asDomainModel())
            if (it.isNotEmpty()) viewModel.showReminderDataIcon()
        })

         */

        viewModel.noDataVisibility.observe(
            viewLifecycleOwner,
            Observer {
                binding.emptyRemindersImageView.visibility = it
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*
        fab.backgroundTintList(context?.let { ContextCompat.getColor(it, Color.WHITE) }
            ?.let { ColorStateList.valueOf(it) }
 */
        //viewModel.updateReminders()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(ActivityLifeCycleObserver {
            //binding.lifecycleOwner = this.viewLifecycleOwner
        Log.i("REMINDERS_FRAGMENT", "ActivityLifeCycleObserver works!")
        })
    }

}