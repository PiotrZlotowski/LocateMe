package com.locateme.project.service

import android.location.Location
import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.locateme.project.callback.ConnectionCallback


class LocationService {

    private val connectionCallback = ConnectionCallback()

    val locationRequest: LocationRequest by lazy {
        val lr = LocationRequest()
        lr.interval = 15
        lr.fastestInterval = 15
        lr.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        lr.smallestDisplacement = 0.5F
        lr
    }


    fun requestLocation(googleApiClient: GoogleApiClient): Location? {
        val fusedLocationApi = LocationServices.FusedLocationApi
        if (!googleApiClient.isConnected) {
            googleApiClient.connect()
        }
        return fusedLocationApi.getLastLocation(googleApiClient)
    }


}