package com.example.rmas_projekat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.location

class MapsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        enableLocation(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapView = findViewById(R.id.mapView)

        // Učitaj stil i postavi početnu kameru (Beograd); potom (uslovno) uključi plavu tačku
        mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(20.4573, 44.7872)) // Beograd
                    .zoom(12.0)
                    .build()
            )
            checkAndRequestLocation()
        }
    }

    private fun checkAndRequestLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            enableLocation(true)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun enableLocation(enable: Boolean) {
        val plugin = mapView.location
        plugin.updateSettings {
            enabled = enable
            pulsingEnabled = enable
            // Puck po želji (nije obavezan):
            // locationPuck = com.mapbox.maps.plugin.locationcomponent.LocationPuck2D()
        }
    }

    // MapView lifecycle forwarderi (OBAVEZNO)
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}
