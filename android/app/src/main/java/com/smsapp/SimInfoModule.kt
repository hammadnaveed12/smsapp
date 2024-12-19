package com.smsapp

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import org.json.JSONArray
import org.json.JSONObject

class SimInfoModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val context: Context = reactContext

    override fun getName(): String {
        return "SimInfo"
    }

    @ReactMethod
    fun getSimDetails(promise: Promise) {
        try {
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            // Check if the required permission is granted
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                promise.reject("PERMISSION_DENIED", "READ_PHONE_STATE permission not granted")
                return
            }

            val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
            val simDetails = JSONArray()

            if (activeSubscriptions != null && activeSubscriptions.isNotEmpty()) {
                for (subscription in activeSubscriptions) {
                    val simData = JSONObject()
                    simData.put("carrierName", subscription.carrierName?.toString() ?: "Unknown Carrier")
                    simData.put("simSlotIndex", subscription.simSlotIndex)
                    simData.put("subscriptionId", subscription.subscriptionId)

                // Use SubscriptionInfo.getNumber() for the phone number
                    val phoneNumber = subscription.number
                    simData.put("phoneNumber", phoneNumber ?: "Unavailable")

                    simDetails.put(simData)
                }

                // Check if any SIM slots are empty and add placeholders
                val totalSlots = subscriptionManager.activeSubscriptionInfoCount
                for (i in 0 until totalSlots) {
                    val isSlotFilled = (0 until simDetails.length()).any { index ->
                        simDetails.getJSONObject(index).getInt("simSlotIndex") == i
                    }

                    if (!isSlotFilled) {
                        val emptySimData = JSONObject()
                        emptySimData.put("carrierName", "No SIM")
                        emptySimData.put("phoneNumber", "Unavailable")
                        emptySimData.put("simSlotIndex", i)
                        emptySimData.put("subscriptionId", -1)
                        simDetails.put(emptySimData)
                    }
                }

                promise.resolve(simDetails.toString())
            } else {
                // If no SIMs are detected, add placeholders for all available slots
                val totalSlots = 2 // Assume dual-SIM as the default
                for (i in 0 until totalSlots) {
                    val emptySimData = JSONObject()
                    emptySimData.put("carrierName", "No SIM")
                    emptySimData.put("phoneNumber", "Unavailable")
                    emptySimData.put("simSlotIndex", i)
                    emptySimData.put("subscriptionId", -1)
                    simDetails.put(emptySimData)
                }

                promise.resolve(simDetails.toString())
            }
        } catch (e: Exception) {
            promise.reject("SIM_ERROR", "Unable to fetch SIM details: ${e.message}")
        }
    }
}