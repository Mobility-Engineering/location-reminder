package com.dexcom.sdk.locationreminders.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapsViewModel : ViewModel() {

    init {}

    private var _lastLatLong = MutableLiveData<LatLng>()
    val lastLatLong: LiveData<LatLng>
        get() = _lastLatLong

    fun updateLocation(location: LatLng) {
        _lastLatLong.value = location
    }
}