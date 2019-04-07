package com.locateme.project.callback

import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

class ConnectionCallback: GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {


    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }


    override fun onConnected(p0: Bundle?) {
    }
}