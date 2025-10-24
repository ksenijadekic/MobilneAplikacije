package com.example.rmas_projekat.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.rmas_projekat.service.LocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var objs by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var table by remember { mutableStateOf(true) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("objects").addSnapshotListener { s, _ ->
            val list = mutableListOf<Map<String, Any>>()
            if (s != null) {
                for (d in s.documents) {
                    val data = d.data
                    if (data != null) list.add(data)
                }
            }
            objs = list
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate("maps") }) { Text("Mapa") }
            Button(onClick = { navController.navigate("addObject") }) { Text("Dodaj") }
            Button(onClick = { navController.navigate("leaderboard") }) { Text("Rang") }
            Button(onClick = { table = !table }) { Text(if (table) "Kartice" else "Tabela") }
            Button(onClick = {
                val ctx = navController.context
                if (!running) {
                    ContextCompat.startForegroundService(ctx, Intent(ctx, LocationService::class.java))
                    running = true
                } else {
                    ctx.stopService(Intent(ctx, LocationService::class.java))
                    running = false
                }
            }) { Text(if (running) "Stop" else "Start") }
        }
        Spacer(Modifier.height(12.dp))

        if (table) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text("Naziv", Modifier.weight(2f))
                Text("Tip", Modifier.weight(1f))
                Text("Autor", Modifier.weight(1f))
                Text("Ocena", Modifier.weight(1f))
                Text("Datum", Modifier.weight(1.2f))
            }
            Divider()
            LazyColumn(Modifier.fillMaxSize()) {
                items(objs) { o ->
                    val nameStr = getStr(o["name"])
                    val id = nameStr.replace(" ", "_")
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp).clickable {
                            navController.navigate("maps?focusId=$id")
                        }
                    ) {
                        Text(nameStr, Modifier.weight(2f))
                        Text(getStr(o["type"]), Modifier.weight(1f))
                        Text(getStr(o["added_by"]), Modifier.weight(1f))
                        val rAny = o["rating"]; val r = if (rAny is Number) rAny.toDouble() else 0.0
                        Text(r.toString(), Modifier.weight(1f))
                        val tAny = o["last_interaction_at"]; val t2Any = o["created_at"]
                        val ts = if (tAny is Number) tAny.toLong() else if (t2Any is Number) t2Any.toLong() else 0L
                        Text(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ts)), Modifier.weight(1.2f))
                    }
                    Divider()
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(objs) { o ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text(getStr(o["name"]))
                            Text("Tip: " + getStr(o["type"]))
                            Text("Autor: " + getStr(o["added_by"]))
                            val rAny = o["rating"]; val r = if (rAny is Number) rAny.toDouble() else 0.0
                            Text("Ocena: $r")
                        }
                    }
                }
            }
        }
    }
}

private fun getStr(x: Any?): String {
    return if (x is String) x else ""
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
