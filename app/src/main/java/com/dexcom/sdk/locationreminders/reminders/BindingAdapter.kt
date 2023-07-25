package com.dexcom.sdk.locationreminders.reminders

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dexcom.sdk.locationreminders.reminder.Reminder


    @BindingAdapter("app:items")
    fun setItems(listView: RecyclerView, items: List<Reminder>?) {
        items?.let {
            if (listView.adapter != null)
            (listView.adapter as RemindersAdapter).submitList(items)
        }
    }




