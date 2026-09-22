package com.example.rmas_projekat.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.rmas_projekat.ui.theme.Orange
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeLight
import com.example.rmas_projekat.ui.theme.OrangeText
import com.example.rmas_projekat.ui.theme.TextMuted
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import java.util.Locale

// Centar Niša
private const val NIS_LNG = 21.8958
private const val NIS_LAT = 43.3209

// Drži menadžera oznaka i mapu između rekompozicija
private class MapHolder {
    var mapView: MapView? = null
    var manager: PointAnnotationManager? = null
    var focused = false

    fun refresh(list: List<Restaurant>, icon: Bitmap) {
        val m = manager ?: return
        m.deleteAll()
        val options = list.mapNotNull { r ->
            val lat = r.lat
            val lng = r.lng
            if (lat == null || lng == null) null
            else PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lng, lat))
                .withIconImage(icon)
                .withIconAnchor(IconAnchor.BOTTOM)
                .withIconSize(1.2)
                .withTextField(r.name)
                .withTextAnchor(TextAnchor.TOP)
                .withTextOffset(listOf(0.0, 0.2))
                .withTextSize(12.0)
                .withTextColor("#3E2723")
                .withTextHaloColor("#FFFFFF")
                .withTextHaloWidth(1.5)
                .withData(JsonPrimitive(r.id))
        }
        if (options.isNotEmpty()) m.create(options)
    }
}

// Narandžasta oznaka (pin) nacrtana kodom, da ne treba dodatna slika
private fun createMarkerBitmap(): Bitmap {
    val w = 84
    val h = 108
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFFE65100.toInt()
    val tip = Path()
    tip.moveTo(w / 2f, h.toFloat())
    tip.lineTo(w / 2f - 26f, 60f)
    tip.lineTo(w / 2f + 26f, 60f)
    tip.close()
    canvas.drawPath(tip, paint)
    canvas.drawCircle(w / 2f, 42f, 36f, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(w / 2f, 42f, 15f, paint)
    return bmp
}

@Composable
fun MapsScreen(navController: NavController, focusId: String? = null) {
    val context = LocalContext.current
    val restaurants by rememberRestaurants()
    val restaurantsState = rememberUpdatedState(restaurants)
    var selected by remember { mutableStateOf<Restaurant?>(null) }
    val holder = remember { MapHolder() }
    val marker = remember { createMarkerBitmap() }

    var hasLocation by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasLocation =
            (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) hasLocation = true
        else launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    AppScaffold(nav = navController, current = "maps", title = "Mapa · Niš") { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            MapboxMap(modifier = Modifier.fillMaxSize()) {
                // stil, početna kamera (Niš) i oznake restorana
                MapEffect(Unit) { mapView ->
                    holder.mapView = mapView
                    mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
                        val mgr = mapView.annotations.createPointAnnotationManager()
                        mgr.addClickListener(OnPointAnnotationClickListener { annotation ->
                            val id = annotation.getData()?.asString
                            selected = restaurantsState.value.firstOrNull { it.id == id }
                            true
                        })
                        holder.manager = mgr
                        holder.refresh(restaurantsState.value, marker)
                    }
                    if (focusId == null) {
                        mapView.mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(NIS_LNG, NIS_LAT))
                                .zoom(12.5)
                                .build()
                        )
                    }
                }

                // plava tačka sa mojom lokacijom
                MapEffect(hasLocation) { mapView ->
                    mapView.location.updateSettings {
                        enabled = hasLocation
                        pulsingEnabled = hasLocation
                    }
                }

                // osvežavanje oznaka + fokus na restoran iz liste
                MapEffect(restaurants, focusId) { mapView ->
                    holder.refresh(restaurants, marker)
                    if (focusId != null && !holder.focused) {
                        val r = restaurants.firstOrNull { it.id == focusId }
                        val lat = r?.lat
                        val lng = r?.lng
                        if (r != null && lat != null && lng != null) {
                            mapView.mapboxMap.setCamera(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(lng, lat))
                                    .zoom(16.0)
                                    .build()
                            )
                            selected = r
                            holder.focused = true
                        }
                    }
                }
            }

            selected?.let { r ->
                RestaurantInfoCard(
                    r = r,
                    onClose = { selected = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun RestaurantInfoCard(r: Restaurant, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(OrangeLight, Orange))),
                contentAlignment = Alignment.Center
            ) {
                if (r.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = r.imageUrl,
                        contentDescription = r.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    androidx.compose.material3.Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    r.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = OrangeText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(r.rating, size = 15.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        String.format(Locale.getDefault(), "%.1f", r.rating),
                        fontWeight = FontWeight.Bold,
                        color = OrangeDark,
                        fontSize = 13.sp
                    )
                }
                val line = listOf(r.type, r.address).filter { it.isNotBlank() }.joinToString(" · ")
                if (line.isNotBlank()) {
                    Text(line, color = TextMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onClose) {
                androidx.compose.material3.Icon(Icons.Filled.Close, contentDescription = "Zatvori", tint = TextMuted)
            }
        }
    }
}
