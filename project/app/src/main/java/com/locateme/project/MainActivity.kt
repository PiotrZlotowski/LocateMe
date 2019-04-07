package com.locateme.project

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.widget.RemoteViews
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.LocationListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.locateme.project.service.LocationTask
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LocationListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var notificatonManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder

    private var channelId = "alertNotificationChanelId"
    private var notifcationId = 123456
    private var description = "Alert notification chanel"

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

        val googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        val timer = Timer()
        fab.setOnClickListener { view ->
            var alertTitle = "I'm checking you"
            var alertContent = "Are you ok?"
            var vibrationOn = true
            var audioOn = true

            sendNotification(alertTitle, alertContent, true, vibrationOn, audioOn)

            startSmsTimer("+48 784 500 921", "Please help me!", 10000)
            timer.schedule(LocationTask(15, googleApiClient), 1000L, 1000L)
        }


        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

    }
    fun startSmsTimer(phoneNumber: String, smsContent: String, delayInMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler().postDelayed({
                // send sms
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, smsContent, null, null)

                // remove old notification
                notificatonManager.cancel(notifcationId)

                // post new notification
                sendNotification("Request fro the help  has been send!", "Please wait!", false,true, true)
            }, "smsToSendStartCountDown", delayInMs)
        }
    }


    fun sendNotification(alertTitle: String, alertContent: String, yesButtonVisible: Boolean, vibration: Boolean, audio: Boolean) {
        var remoteViews = createAlertView(alertTitle, alertContent, yesButtonVisible)

        var pendingIntent = PendingIntent.getActivities(
            this, 0, arrayOf(Intent(this, LauncherActivity::class.java)),
            PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(vibration)
            if(audio) {
                var audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
            }

            notificatonManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.red_allert_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.red_allert_icon))
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(this)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.red_allert_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.red_allert_icon))
                .setContentIntent(pendingIntent)
        }

        notificatonManager.notify(notifcationId, builder.build())
    }

    private fun createAlertView(title: String, content: String, confirmationButtonVisible: Boolean ): RemoteViews {
        var view = RemoteViews(packageName, R.layout.notification_layout)
        view.setTextViewText(R.id.notification_title, title)
        view.setTextViewText(R.id.notification_content, content)
//        view.setViewVisibility(R.id.button_yes, if (confirmationButtonVisible) View.VISIBLE else View.INVISIBLE))

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("NOTIFICATION_BUTTON_YES"),
            PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.button_yes, pendingIntent)

        return view
    }

    private inner class ActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "NOTIFICATION_BUTTON_YES" -> {
                    notificatonManager.cancel(notifcationId)
                    Handler().removeCallbacksAndMessages(null)
                }
            }
        }
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
}
