package com.dexcom.sdk.locationreminders.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dexcom.sdk.locationreminders.databinding.ItemViewReminderBinding
import com.dexcom.sdk.locationreminders.reminder.Reminder

class RemindersAdapter(val onClickListener: OnClickListener) :
    ListAdapter<Reminder, RemindersAdapter.ReminderViewHolder>(DiffCallback) {
    /**
     * The ReminderViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [Reminder] information.
     */
    class ReminderViewHolder(private var binding: ItemViewReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onClickListener:OnClickListener, reminder: Reminder) {
            binding.clickListener =  onClickListener
            binding.reminder = reminder
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
        companion object {

            fun from(parent: ViewGroup): ReminderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                //A RecyclerView’s children should be inflated with attachToRoot passed in as false.
                // The child views are inflated in onCreateViewHolder().
                val binding = ItemViewReminderBinding.inflate(layoutInflater, parent, false)
                return ReminderViewHolder(binding)
            }
        }
    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Reminder]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReminderViewHolder {
        return ReminderViewHolder.from(parent)
        //return ReminderViewHolder(ItemViewReminderBinding.inflate(LayoutInflater.from(parent.context)))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)
        /*holder.itemView.setOnClickListener {
            onClickListener.onClick(reminder)
        }

         */
        holder.bind(onClickListener,reminder)
    }

    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [Reminder]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [Reminder]
     */
}
class OnClickListener(val clickListener: (reminder:Reminder) -> Unit) {
    fun onClick(marsProperty:Reminder) = clickListener(marsProperty)
}