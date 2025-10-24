package com.example.rmas_projekat.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// koristi naš helper za galeriju/kameru koji vraća Uri

@Composable
fun AddObjectScreen(navController: NavController) {
    val ctx = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val st = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var review by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Naziv") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = desc, onValueChange = { desc = it },
            label = { Text("Opis") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = type, onValueChange = { type = it },
            label = { Text("Tip") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = rating,
            onValueChange = { if (it.isBlank() || it.toIntOrNull() in 1..5) rating = it },
            label = { Text("Ocena 1–5") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = review, onValueChange = { review = it },
            label = { Text("Komentar") }, modifier = Modifier.fillMaxWidth()
        )

        // --- Kamera / Galerija (ImagePickers vraća Uri) ---
        ImagePickers(
            onImagePicked = { picked = it },
            onError = { msg -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() }
        )

        // Prikaz izabrane / uslikane fotke (opciono)
        picked?.let { uri ->
            Spacer(Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val user = auth.currentUser
                if (user == null) {
                    Toast.makeText(ctx, "Uloguj se", Toast.LENGTH_SHORT).show(); return@Button
                }
                if (name.isBlank() || desc.isBlank() || type.isBlank() || rating.isBlank()) {
                    Toast.makeText(ctx, "Popuni polja", Toast.LENGTH_SHORT).show(); return@Button
                }

                busy = true
                db.collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        val username = doc.getString("username")
                            ?: (user.email ?: "user")
                        val email = user.email ?: ""

                        addObjectToFirestore(
                            ctx = ctx,
                            name = name,
                            desc = desc,
                            type = type,
                            rating = rating.toInt(),
                            review = review,
                            picked = picked,
                            username = username,
                            email = email,
                            db = db,
                            st = st,
                            onComplete = {
                                busy = false
                                navController.popBackStack()
                            },
                            onError = { e ->
                                busy = false
                                Toast.makeText(ctx, e, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .addOnFailureListener {
                        busy = false
                        Toast.makeText(ctx, "Greška pri čitanju profila", Toast.LENGTH_SHORT).show()
                    }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (busy) "Sačekaj..." else "Dodaj")
        }
    }
}

private fun addObjectToFirestore(
    ctx: Context,
    name: String,
    desc: String,
    type: String,
    rating: Int,
    review: String,
    picked: Uri?,
    username: String,
    email: String,
    db: FirebaseFirestore,
    st: FirebaseStorage,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val fused = LocationServices.getFusedLocationProviderClient(ctx)

    val fine = ActivityCompat.checkSelfPermission(
        ctx, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ActivityCompat.checkSelfPermission(
        ctx, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fine && !coarse) { onError("Dozvole za lokaciju"); return }

    fused.lastLocation.addOnSuccessListener { l: Location? ->
        if (l == null) { onError("Nema lokacije"); return@addOnSuccessListener }

        val ts = System.currentTimeMillis()
        val id = name.replace(" ", "_")

        val base = hashMapOf<String, Any>(
            "name" to name,
            "description" to desc,
            "type" to type,
            "rating" to rating.toDouble(),
            "latitude" to l.latitude,
            "longitude" to l.longitude,
            "image_url" to "",
            "added_by" to username,
            "added_email" to email,
            "created_at" to ts,
            "last_interaction_at" to ts,
            "number_reviews" to 1L,
            "comments" to listOf(
                mapOf(
                    "user_id" to username,
                    "comment_text" to review,
                    "timestamp" to ts,
                    "rating" to rating,
                    "likes" to 0L,
                    "dislikes" to 0L
                )
            )
        )

        fun save(map: Map<String, Any>) {
            db.collection("objects").document(id).set(map)
                .addOnSuccessListener {
                    // pretpostavljam da već imaš ovu funkciju negde
                    addPoints("add")
                    onComplete()
                }
                .addOnFailureListener { e -> onError(e.message ?: "Greška") }
        }

        if (picked != null) {
            val ref = st.reference.child("object_images/$id.jpg")
            ref.putFile(picked)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { u ->
                    val m = HashMap(base); m["image_url"] = u.toString(); save(m)
                }
                .addOnFailureListener { onError("Upload slike") }
        } else {
            save(base)
        }
    }.addOnFailureListener { onError("Lokacija") }
}
