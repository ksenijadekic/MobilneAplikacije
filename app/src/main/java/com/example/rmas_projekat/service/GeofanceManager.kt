package com.example.rmas_projekat.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

object GeofenceManager {
    fun registerAllObjectGeofences(context: Context) {
        val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return

        val client: GeofencingClient = LocationServices.getGeofencingClient(context)
        val intent = Intent(context, GeofenceReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        client.removeGeofences(pending).addOnCompleteListener {
            val db = FirebaseFirestore.getInstance()
            db.collection("objects").get().addOnSuccessListener { snap ->
                val list = ArrayList<Geofence>()
                var count = 0
                for (d in snap.documents) {
                    val nAny = d.get("name"); val laAny = d.get("latitude"); val loAny = d.get("longitude")
                    if (nAny is String && laAny is Number && loAny is Number) {
                        val g = Geofence.Builder()
                            .setRequestId(nAny.replace(" ", "_"))
                            .setCircularRegion(laAny.toDouble(), loAny.toDouble(), 150f)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
                            .setLoiteringDelay(3000)
                            .build()
                        list.add(g)
                        count++
                        if (count >= 90) break
                    }
                }
                if (list.isEmpty()) return@addOnSuccessListener
                val req = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(list)
                    .build()

                val hasBg = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasBg || android.os.Build.VERSION.SDK_INT < 29) {
                    client.addGeofences(req, pending)
                }
            }
        }
    }
}
