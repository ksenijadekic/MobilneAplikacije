package com.example.rmas_projekat.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        try {
            fused.lastLocation.addOnSuccessListener { l: Location? ->
                if (l == null) return@addOnSuccessListener
                val uidAny = FirebaseAuth.getInstance().currentUser
                val uid = if (uidAny != null) uidAny.uid else "anonymous"
                val data = hashMapOf<String, Any>(
                    "user_id" to uid,
                    "latitude" to l.latitude,
                    "longitude" to l.longitude,
                    "timestamp" to System.currentTimeMillis(),
                    "source" to "geofence"
                )
                FirebaseFirestore.getInstance().collection("user_locations").add(data)
            }
        } catch (_: SecurityException) { }
    }
}
