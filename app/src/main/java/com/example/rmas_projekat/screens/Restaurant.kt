package com.example.rmas_projekat.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

// Restoran = dokument iz kolekcije "objects" (ista polja kao i ranije + address, price_range)
data class Restaurant(
    val id: String,
    val name: String,
    val type: String,
    val description: String,
    val address: String,
    val priceRange: String,
    val rating: Double,
    val imageUrl: String,
    val addedBy: String,
    val addedEmail: String,
    val createdAt: Long,
    val lastInteraction: Long,
    val lat: Double?,
    val lng: Double?,
    val reviews: Long
)

private fun Any?.asLong(): Long = (this as? Number)?.toLong() ?: 0L

fun DocumentSnapshot.toRestaurant(): Restaurant? {
    val name = getString("name") ?: return null
    return Restaurant(
        id = id,
        name = name,
        type = getString("type") ?: "",
        description = getString("description") ?: "",
        address = getString("address") ?: "",
        priceRange = getString("price_range") ?: "",
        rating = (get("rating") as? Number)?.toDouble() ?: 0.0,
        imageUrl = getString("image_url") ?: "",
        addedBy = getString("added_by") ?: "",
        addedEmail = getString("added_email") ?: "",
        createdAt = get("created_at").asLong(),
        lastInteraction = get("last_interaction_at").asLong(),
        lat = (get("latitude") as? Number)?.toDouble(),
        lng = (get("longitude") as? Number)?.toDouble(),
        reviews = get("number_reviews").asLong()
    )
}

// Živa lista restorana; listener se skida kad ekran nestane
@Composable
fun rememberRestaurants(): State<List<Restaurant>> {
    val state = remember { mutableStateOf(listOf<Restaurant>()) }
    DisposableEffect(Unit) {
        val reg = FirebaseFirestore.getInstance().collection("objects")
            .addSnapshotListener { snap, _ ->
                if (snap != null) state.value = snap.documents.mapNotNull { it.toRestaurant() }
            }
        onDispose { reg.remove() }
    }
    return state
}
