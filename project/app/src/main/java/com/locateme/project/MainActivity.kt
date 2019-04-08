package com.locateme.project

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.LocationListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.locateme.project.service.LocationService
import com.locateme.project.service.LocationTask
import com.locateme.project.service.SmsService
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LocationListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var notificatonManager: NotificationManager
    lateinit var locationTask: LocationTask;

    private var channelId = "alertNotificationChanelId"
    private var notifcationId = 123456
    private var description = "Alert notification chanel"
    private var isTrackingStarted = false;
    private var notificationTimer = Timer()
    private var actionBroadcastReceiver = ActionBroadcastReceiver()


    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }
    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(p0: Location) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        notificatonManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS), 0)
        }
        val filter = getIntentFilter()
        registerReceiver(actionBroadcastReceiver, filter)

        val googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        if (!googleApiClient.isConnected) {
            googleApiClient.connect()
        }
        val timer = Timer()
//        fab.setOnClickListener { view ->
//            timer.schedule(LocationTask(15, googleApiClient, notificatonManager, packageName, this, resources, notificationTimer), 1000L, 1000L)
//        }

        btn_help.setOnClickListener{
            val location = LocationService.requestLocation(googleApiClient)
            SmsService.sendSms("+661-336-669", "I need help. Can you please rescue me?", location?.latitude.toString(), location?.longitude.toString())
        }

        btn_start.setOnClickListener{
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            if(isTrackingStarted) {
                locationTask.cancel()
                isTrackingStarted = false
            } else {
                locationTask = LocationTask(30, googleApiClient, notificatonManager, packageName, this, resources, notificationTimer, fusedLocationProviderClient)
                timer.schedule(locationTask, 1000L, 1000L)
                isTrackingStarted = true
            };
        }


        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun getIntentFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction("NOTIFICATION_BUTTON_YES")
        return filter
    }

    override fun onResume() {
        super.onResume()
        val filter = getIntentFilter()
        registerReceiver(actionBroadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(actionBroadcastReceiver)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(actionBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(actionBroadcastReceiver)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private inner class ActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "NOTIFICATION_BUTTON_YES" -> {
                    notificatonManager.cancel(notifcationId)
                    notificationTimer.cancel()
                }
            }
        }
    }


}
