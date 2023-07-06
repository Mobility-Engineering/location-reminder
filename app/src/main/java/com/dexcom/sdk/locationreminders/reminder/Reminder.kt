package com.dexcom.sdk.locationreminders.reminder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Reminder constructor(
    val id: Long,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
    ): Parcelable
