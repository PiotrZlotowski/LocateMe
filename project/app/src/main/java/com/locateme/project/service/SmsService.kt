package com.locateme.project.service

import android.telephony.SmsManager


object SmsService {

    fun sendSms(phoneNumber: String, smsContent: String, latitude: String = "", longitude: String = "") {
        val smsManager = SmsManager.getDefault()
        val fullMessageContent = smsContent +  "I'm at https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}"
        smsManager.sendTextMessage(phoneNumber, null, fullMessageContent, null, null)
    }

}