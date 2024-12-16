package com.smsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.database.Cursor
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

class IncomingCallModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val context = reactContext
    private var isReceiverRegistered = false

    override fun getName(): String {
        return "IncomingCallModule"
    }

    private val callReceiver = object : BroadcastReceiver() {
       override fun onReceive(context: Context?, intent: Intent?) {
    try {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            var incomingNumber: String? = null

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                // Get incoming number
                incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                // Fallback to Call Log if incoming number is null
                if (incomingNumber == null) {
                    incomingNumber = getLastIncomingCallNumber(context) ?: "Unknown"
                }
            }

            // Always send the current state and number (or null if not ringing)
            sendEvent("IncomingCall", incomingNumber ?: "")
        }
    } catch (e: Exception) {
        println("Error in BroadcastReceiver: ${e.message}")
    }
}
    }

    @ReactMethod
    fun startListening() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            context.registerReceiver(callReceiver, filter)
            isReceiverRegistered = true
        }
    }

    @ReactMethod
    fun stopListening() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(callReceiver)
                isReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                println("Receiver not registered: ${e.message}")
            }
        }
    }

    private fun sendEvent(eventName: String, params: String) {
        try {
            currentReactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit(eventName, params)
        } catch (e: Exception) {
            println("Error sending event to React Native: ${e.message}")
        }
    }

    private val currentReactContext
        get() = context

    // Helper function to fetch the last incoming call number from the Call Log
    private fun getLastIncomingCallNumber(context: Context?): String? {
        if (context == null) return null

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE),
            "${CallLog.Calls.TYPE} = ?",
            arrayOf(CallLog.Calls.INCOMING_TYPE.toString()),
            "${CallLog.Calls.DATE} DESC LIMIT 1"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
            }
        }

        return null
    }
}