package com.example.rmas_projekat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LocationService : Service() {
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var cb: LocationCallback
    private val ch = "location_service"
    private val id = 1001
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(ch, "Location", NotificationManager.IMPORTANCE_LOW))
        }
        startForeground(id, NotificationCompat.Builder(this, ch).setContentTitle("RMAS").setContentText("Location").setSmallIcon(android.R.drawable.ic_menu_mylocation).build())

        fused = LocationServices.getFusedLocationProviderClient(this)
        cb = object : LocationCallback() {
            override fun onLocationResult(r: LocationResult) {
                val l = r.lastLocation ?: return
                saveUserLocation(l)
                checkNearbyObjects(l)
                checkNearbyUsers(l)
            }
        }
        try {
            fused.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000L).setMinUpdateIntervalMillis(8000L).build(),
                cb,
                mainLooper
            )
        } catch (_: SecurityException) { }
    }

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int = START_STICKY
    override fun onDestroy() { super.onDestroy(); fused.removeLocationUpdates(cb) }
    override fun onBind(i: Intent?): IBinder? = null

    private fun saveUserLocation(l: Location) {
        val u = auth.currentUser ?: return
        val m = mapOf("uid" to u.uid, "lat" to l.latitude, "lng" to l.longitude, "timestamp" to System.currentTimeMillis())
        db.collection("user_locations").document(u.uid).set(m)
    }

    private fun checkNearbyObjects(l: Location) {
        db.collection("objects").get().addOnSuccessListener { s ->
            var n = false
            val a = FloatArray(1)
            for (d in s.documents) {
                val lat = d.getDouble("latitude") ?: continue
                val lng = d.getDouble("longitude") ?: continue
                Location.distanceBetween(l.latitude, l.longitude, lat, lng, a)
                if (a[0] <= 100f) { n = true; break }
            }
            if (n) notify("Objekat u blizini")
        }
    }

    private fun checkNearbyUsers(l: Location) {
        val me = auth.currentUser?.uid ?: return
        db.collection("user_locations").get().addOnSuccessListener { s ->
            var n = false
            val a = FloatArray(1)
            for (d in s.documents) {
                val uid = d.getString("uid") ?: continue
                if (uid == me) continue
                val lat = d.getDouble("lat") ?: continue
                val lng = d.getDouble("lng") ?: continue
                Location.distanceBetween(l.latitude, l.longitude, lat, lng, a)
                if (a[0] <= 100f) { n = true; break }
            }
            if (n) notify("Korisnik u blizini")
        }
    }

    private fun notify(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((Math.random() * 100000).toInt(), NotificationCompat.Builder(this, ch).setContentTitle("RMAS").setContentText(text).setSmallIcon(android.R.drawable.ic_dialog_info).build())
    }
}
