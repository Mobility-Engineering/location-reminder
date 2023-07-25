package com.dexcom.sdk.locationreminders.reminders

enum class RemindersFilterType {
    /**
     * Do not filter tasks.
     */
    ALL_REMINDERS,

    /**
     * Filters only the active (not completed yet) tasks.
     */
    ACTIVE_REMINDERS,

    /**
     * Filters only the completed tasks.
     */
    COMPLETED_REMINDERS
}
