package com.locateme.project.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import com.google.android.gms.common.api.GoogleApiClient
import com.locateme.project.R
import java.util.*
import com.google.android.gms.location.*


class LocationTask(private val definedInterval: Long,
                   private var googleApiClient: GoogleApiClient,
                   private var notificatonManager: NotificationManager,
                   private val packageName: String,
                   private val context: Context,
                   private val resources: Resources,
                   val notificationTimer: Timer,
                   var fuesedLocationProviderClient: FusedLocationProviderClient): TimerTask(), LocationListener {

    private var channelId = "alertNotificationChanelId"
    private var notifcationId = 123456
    private var description = "Alert notification chanel"
    var elapsedTime: Long = 0
    var lastReportedLatitude: Double = 0.0
    var lastReportedLongitude: Double = 0.0
    var wasNotified: Boolean = false

    override fun onLocationChanged(currentLocation: Location?) {
        if (currentLocation == null) {
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
            wasNotified = false
        }
        if (elapsedTime > definedInterval && !wasNotified) { // in seconds
            var alertTitle = "I'm checking you"
            var alertContent = "Are you ok?"
            var vibrationOn = true
            var audioOn = true
            wasNotified = true
            sendNotification(alertTitle, alertContent, true, vibrationOn, audioOn)
            startSmsTimer("+48 784 500 921", "Please help me!", 10000)
        }
    }

    override fun run() {

        var locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.interval = 1
        locationRequest.fastestInterval = 1
        locationRequest.smallestDisplacement = 0.5F
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this, Looper.getMainLooper())
    }

    fun startSmsTimer(phoneNumber: String, smsContent: String, delayInMs: Long) {
        notificationTimer.schedule(object : TimerTask() {
            override fun run() {
                // send sms
//                val smsManager = SmsManager.getDefault()
//                smsManager.sendTextMessage(phoneNumber, null, smsContent, null, null)
                SmsService.sendSms(phoneNumber, smsContent, lastReportedLatitude.toString(), lastReportedLongitude.toString())

                // remove old notification
                notificatonManager.cancel(notifcationId)

                // post new notification
                sendNotification("Request fro the help  has been send!", "Please wait!", false,true, true)

            }
        }, delayInMs)
    }

    fun sendNotification(alertTitle: String, alertContent: String, yesButtonVisible: Boolean, vibration: Boolean, audio: Boolean) {
        var remoteViews = createAlertView(alertTitle, alertContent, yesButtonVisible)

        var pendingIntent = PendingIntent.getActivities(
            context, 0, arrayOf(Intent(context, LauncherActivity::class.java)),
            PendingIntent.FLAG_UPDATE_CURRENT)

        var builder: Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
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

            builder = Notification.Builder(context, channelId)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.red_allert_icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.red_allert_icon))
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(context)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.red_allert_icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.red_allert_icon))
                .setContentIntent(pendingIntent)
        }

        notificatonManager.notify(notifcationId, builder.build())
    }

    private fun createAlertView(title: String, content: String, confirmationButtonVisible: Boolean ): RemoteViews {
        var view = RemoteViews(packageName, R.layout.notification_layout)
        view.setTextViewText(R.id.notification_title, title)
        view.setTextViewText(R.id.notification_content, content)
        view.setViewVisibility(R.id.button_yes, if (confirmationButtonVisible) View.VISIBLE else View.INVISIBLE)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("NOTIFICATION_BUTTON_YES"),
            PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.button_yes, pendingIntent)

        return view
    }

}