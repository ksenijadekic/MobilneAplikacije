package com.example.rmas_projekat.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.rmas_projekat.service.LocationService
import com.example.rmas_projekat.ui.theme.Orange
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeLight
import com.example.rmas_projekat.ui.theme.OrangeSoft
import com.example.rmas_projekat.ui.theme.OrangeText
import com.example.rmas_projekat.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Da li je servis za praćenje lokacije uključen (živi dok traje proces aplikacije)
object TrackingState {
    var running by mutableStateOf(false)
}

@Composable
fun MainScreen(navController: NavController) {
    val ctx = LocalContext.current
    val restaurants by rememberRestaurants()
    var query by remember { mutableStateOf("") }
    var table by remember { mutableStateOf(false) }

    fun startTracking() {
        ContextCompat.startForegroundService(ctx, Intent(ctx, LocationService::class.java))
        TrackingState.running = true
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val granted = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) startTracking()
        else Toast.makeText(ctx, "Za praćenje lokacije je potrebna dozvola", Toast.LENGTH_SHORT).show()
    }

    fun toggleTracking(on: Boolean) {
        if (!on) {
            ctx.stopService(Intent(ctx, LocationService::class.java))
            TrackingState.running = false
            return
        }
        val hasLocation =
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocation) {
            startTracking()
        } else {
            val need = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= 33) need += Manifest.permission.POST_NOTIFICATIONS
            permLauncher.launch(need.toTypedArray())
        }
    }

    val shown = restaurants
        .filter {
            query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.type.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
        }
        .sortedByDescending { maxOf(it.lastInteraction, it.createdAt) }

    AppScaffold(
        nav = navController,
        current = "main",
        title = "Restorani",
        actions = {
            IconButton(onClick = { table = !table }) {
                Icon(Icons.Filled.List, contentDescription = "Promeni prikaz", tint = Color.White)
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Pretraži restorane...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = orangeFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { TrackingCard(checked = TrackingState.running, onChange = { toggleTracking(it) }) }

            if (shown.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = OrangeLight, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (restaurants.isEmpty()) "Još nema restorana.\nDodaj prvi preko dugmeta „Dodaj“." else "Nema rezultata za pretragu.",
                            color = TextMuted,
                            fontSize = 15.sp
                        )
                    }
                }
            } else if (table) {
                item { TableHeader() }
                items(shown, key = { it.id }) { r ->
                    TableRow(r) { navController.navigate("maps?focusId=${r.id}") }
                }
            } else {
                items(shown, key = { it.id }) { r ->
                    RestaurantCard(r) { navController.navigate("maps?focusId=${r.id}") }
                }
            }
        }
    }
}

@Composable
private fun TrackingCard(checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Praćenje lokacije", fontWeight = FontWeight.Bold, color = OrangeText)
                Text("Obaveštenja kad su restorani i korisnici u blizini", fontSize = 12.sp, color = TextMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OrangeDark
                )
            )
        }
    }
}

@Composable
private fun RestaurantCard(r: Restaurant, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (r.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = r.imageUrl,
                        contentDescription = r.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(OrangeLight, Orange))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                if (r.type.isNotBlank()) {
                    Text(
                        text = r.type,
                        color = OrangeDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                if (r.priceRange.isNotBlank()) {
                    Text(
                        text = r.priceRange,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(OrangeDark, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = r.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    RatingStars(r.rating)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", r.rating),
                        fontWeight = FontWeight.Bold,
                        color = OrangeDark
                    )
                }
                if (r.address.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(r.address, color = TextMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (r.description.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(r.description, color = OrangeText, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Dodao: ${r.addedBy.ifBlank { "-" }} · ${formatDate(r)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        listOf("Naziv" to 1.6f, "Tip" to 1f, "Autor" to 1f, "Ocena" to 0.8f, "Datum" to 1.2f).forEach { (t, w) ->
            Text(t, Modifier.weight(w), color = OrangeDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TableRow(r: Restaurant, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(r.name, Modifier.weight(1.6f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OrangeText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(r.type, Modifier.weight(1f), fontSize = 12.sp, color = OrangeText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(r.addedBy, Modifier.weight(1f), fontSize = 12.sp, color = OrangeText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(String.format(Locale.getDefault(), "%.1f", r.rating), Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangeDark)
            Text(formatDate(r), Modifier.weight(1.2f), fontSize = 12.sp, color = TextMuted)
        }
    }
}

private fun formatDate(r: Restaurant): String {
    val ts = if (r.lastInteraction > 0) r.lastInteraction else r.createdAt
    if (ts <= 0) return "-"
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ts))
}

fun addPoints(action: String) {
    val u = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(u.uid).get().addOnSuccessListener { d ->
        val curAny = d.getLong("points"); val cur = if (curAny != null) curAny else 0
        var plus = 0
        if (action == "visit") plus = 3
        if (action == "review") plus = 5
        if (action == "add") plus = 5
        if (action == "like_bonus") plus = 1
        if (action == "dislike_penalty") plus = -1
        db.collection("users").document(u.uid).update("points", cur + plus)
    }
}
