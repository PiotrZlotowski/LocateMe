package com.locateme.project.service

import android.location.Location
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import java.util.*

class LocationTask(val definedInterval: Long, var googleApiClient: GoogleApiClient): TimerTask() {

    private val locationService = LocationService()
    var lastReportedLocation: Location? = null
    var elapsedTime: Long = 0
    var lastReportedLatitude: Double = 0.0
    var lastReportedLongitude: Double = 0.0

    override fun run() {
        val currentLocation = locationService.requestLocation(googleApiClient) ?: return

        if (lastReportedLocation == null) {
            lastReportedLocation = currentLocation
            return
        }
        val currentLatitude = currentLocation.latitude
        val currentLongitude = currentLocation.longitude
        if (currentLatitude == lastReportedLatitude && currentLongitude == lastReportedLongitude) {
            elapsedTime++
        }
        else {
            lastReportedLatitude = currentLatitude
            lastReportedLongitude = currentLongitude
            elapsedTime = 0
        }
        if (elapsedTime > definedInterval) {
            // NOTIFICATION THERE
            Log.d("Notification", "Notification should be there")
        }

    }

}