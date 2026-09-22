package com.example.rmas_projekat.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeSoft
import com.example.rmas_projekat.ui.theme.TextMuted
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Ako lokacija uređaja nije dostupna, restoran se stavlja u centar Niša
private const val NIS_LAT = 43.3209
private const val NIS_LNG = 21.8958

private val CUISINES = listOf(
    "Domaća kuhinja", "Pizza", "Roštilj", "Riba",
    "Brza hrana", "Poslastičarnica", "Kafić", "Ostalo"
)
private val PRICES = listOf("€", "€€", "€€€")

@Composable
fun AddObjectScreen(navController: NavController) {
    val ctx = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val st = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var review by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = null
        val user = auth.currentUser
        if (user == null) {
            error = "Uloguj se"; return
        }
        if (name.isBlank() || address.isBlank() || type.isBlank() || rating == 0) {
            error = "Popuni naziv, adresu, tip kuhinje i ocenu"; return
        }

        busy = true

        fun save(username: String) {
            addObjectToFirestore(
                ctx = ctx,
                name = name.trim(),
                address = address.trim(),
                desc = desc.trim(),
                type = type,
                price = price,
                rating = rating,
                review = review.trim(),
                picked = picked,
                username = username,
                email = user.email ?: "",
                db = db,
                st = st,
                onComplete = {
                    busy = false
                    Toast.makeText(ctx, "Restoran je dodat!", Toast.LENGTH_SHORT).show()
                    if (!navController.popBackStack("main", false)) navController.navigate("main")
                },
                onError = { e ->
                    busy = false
                    error = e
                }
            )
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc -> save(doc.getString("username") ?: (user.email ?: "user")) }
            .addOnFailureListener { save(user.email ?: "user") }
    }

    AppScaffold(nav = navController, current = "addObject", title = "Novi restoran") { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Podeli omiljeni restoran sa ostalima i zaradi poene.",
                color = TextMuted,
                fontSize = 14.sp
            )

            SectionCard("Osnovni podaci") {
                OrangeField(name, { name = it }, "Naziv restorana")
                Spacer(Modifier.height(12.dp))
                OrangeField(address, { address = it }, "Adresa", icon = Icons.Filled.Place)
            }

            SectionCard("Tip kuhinje") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CUISINES.forEach { c -> OrangeChip(c, selected = type == c) { type = c } }
                }
            }

            SectionCard("Cene") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRICES.forEach { p ->
                        OrangeChip(p, selected = price == p) { price = if (price == p) "" else p }
                    }
                }
            }

            SectionCard("Opis") {
                OrangeField(desc, { desc = it }, "Šta ga izdvaja?", singleLine = false, minLines = 3)
            }

            SectionCard("Tvoj utisak") {
                StarRatingInput(value = rating, onChange = { rating = it })
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (rating == 0) "Dodirni zvezdice da oceniš" else "Ocena: $rating/5",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                OrangeField(review, { review = it }, "Komentar", singleLine = false, minLines = 2)
            }

            SectionCard("Fotografija") {
                val uri = picked
                if (uri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OrangeSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = OrangeDark, modifier = Modifier.size(36.dp))
                            Text("Još nema fotografije", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ImagePickers(
                    onImagePicked = { picked = it },
                    onError = { msg -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() }
                )
            }

            Text(
                text = "Lokacija restorana je tvoja trenutna pozicija (ako nije dostupna, centar Niša).",
                color = TextMuted,
                fontSize = 12.sp
            )

            AuthError(error)

            AuthButton(
                text = if (busy) "Sačekaj..." else "Dodaj restoran",
                enabled = !busy,
                onClick = { if (!busy) submit() }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun addObjectToFirestore(
    ctx: Context,
    name: String,
    address: String,
    desc: String,
    type: String,
    price: String,
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
    fun proceed(lat: Double, lng: Double) {
        val ts = System.currentTimeMillis()
        val id = name.replace(" ", "_")

        val base = hashMapOf<String, Any>(
            "name" to name,
            "address" to address,
            "description" to desc,
            "type" to type,
            "price_range" to price,
            "rating" to rating.toDouble(),
            "latitude" to lat,
            "longitude" to lng,
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
                .addOnFailureListener { onError("Upload slike nije uspeo") }
        } else {
            save(base)
        }
    }

    val fine = ActivityCompat.checkSelfPermission(
        ctx, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ActivityCompat.checkSelfPermission(
        ctx, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fine && !coarse) {
        proceed(NIS_LAT, NIS_LNG)
        return
    }

    LocationServices.getFusedLocationProviderClient(ctx).lastLocation
        .addOnSuccessListener { l -> proceed(l?.latitude ?: NIS_LAT, l?.longitude ?: NIS_LNG) }
        .addOnFailureListener { proceed(NIS_LAT, NIS_LNG) }
}
