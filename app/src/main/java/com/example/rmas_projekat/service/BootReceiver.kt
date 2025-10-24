package com.example.rmas_projekat.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == "android.intent.action.BOOT_COMPLETED") {
            GeofenceManager.registerAllObjectGeofences(context)
        }
    }
}
