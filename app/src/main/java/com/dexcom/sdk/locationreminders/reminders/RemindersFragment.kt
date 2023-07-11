package com.dexcom.sdk.locationreminders.reminders

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.dexcom.sdk.locationreminders.MainActivity
import com.dexcom.sdk.locationreminders.R
import com.dexcom.sdk.locationreminders.database.asDomainModel
import com.dexcom.sdk.locationreminders.databinding.FragmentRemindersBinding
import com.dexcom.sdk.locationreminders.reminder.ReminderViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton



class RemindersFragment : Fragment() {
    private lateinit var _binding:FragmentRemindersBinding
    private val binding get() = _binding!!
    private lateinit var adapter:RemindersAdapter
    private lateinit var fab:FloatingActionButton

        val viewModel by activityViewModels<ReminderViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fab = (requireActivity() as MainActivity).findViewById(R.id.fab) as FloatingActionButton
        // Inflate the layout for this fragment
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        adapter = RemindersAdapter(
            OnClickListener{
                Log.d("TAG", it.name)

            })

        binding.recycler.adapter = adapter
        viewModel.reminders.observe(viewLifecycleOwner, Observer{

            adapter.submitList(it.asDomainModel())
            for (i in it){
                Log.d("REMINDERS_FRAGMENT",i.toString() )
            }
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateReminders()
    }


}