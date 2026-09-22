package com.example.rmas_projekat.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rmas_projekat.service.LocationService
import com.example.rmas_projekat.ui.theme.Orange
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeSoft
import com.example.rmas_projekat.ui.theme.OrangeText
import com.example.rmas_projekat.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun ProfileScreen(navController: NavController) {
    val ctx = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: ""
    val uid = user?.uid

    var username by remember { mutableStateOf(email.substringBefore("@")) }
    var points by remember { mutableStateOf(0L) }
    var rank by remember { mutableStateOf<Int?>(null) }
    val restaurants by rememberRestaurants()
    val added = restaurants.count { it.addedEmail == email && email.isNotBlank() }

    // rang i poeni: isti upit kao rang lista, traži se dokument trenutnog korisnika
    DisposableEffect(uid) {
        val reg = FirebaseFirestore.getInstance().collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val docs = snap?.documents ?: return@addSnapshotListener
                val index = docs.indexOfFirst { it.id == uid }
                if (index >= 0) {
                    rank = index + 1
                    points = docs[index].getLong("points") ?: 0L
                    docs[index].getString("username")?.let { if (it.isNotBlank()) username = it }
                }
            }
        onDispose { reg.remove() }
    }

    AppScaffold(nav = navController, current = "profile", title = "Profil") { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // zaglavlje sa avatarom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(OrangeDark, Orange)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(username, 88.dp, Color.White.copy(alpha = 0.28f))
                    Spacer(Modifier.height(12.dp))
                    Text(username, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(email, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                }
            }

            // statistika
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Poeni", points.toString(), Modifier.weight(1f))
                StatTile("Rang", rank?.let { "#$it" } ?: "-", Modifier.weight(1f))
                StatTile("Restorana", added.toString(), Modifier.weight(1f))
            }

            SectionCard("O poenima") {
                Text("Za svaki restoran koji dodaš dobijaš +5 poena.", color = OrangeText, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("Rang lista prikazuje sve korisnike po broju poena.", color = TextMuted, fontSize = 13.sp)
            }

            AuthButton(text = "Odjavi se", enabled = true) {
                FirebaseAuth.getInstance().signOut()
                ctx.stopService(Intent(ctx, LocationService::class.java))
                TrackingState.running = false
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeSoft),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = OrangeDark, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, fontSize = 13.sp)
        }
    }
}
