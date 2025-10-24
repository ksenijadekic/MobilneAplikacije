package com.example.rmas_projekat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class UserScore(
    val username: String = "",
    val email: String = "",
    val points: Long = 0
)

@Composable
fun LeaderboardScreen(navController: NavController) {
    val db = remember { FirebaseFirestore.getInstance() }
    var items by remember { mutableStateOf(listOf<UserScore>()) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map {
                    UserScore(
                        username = it.getString("username") ?: "",
                        email = it.getString("email") ?: "",
                        points = it.getLong("points") ?: 0L
                    )
                } ?: emptyList()
                items = list
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Rang lista",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        HeaderRow()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(items) { index, item ->
                RowItem(position = index + 1, user = item)
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("#", fontWeight = FontWeight.SemiBold)
        Text("Korisnik", fontWeight = FontWeight.SemiBold)
        Text("Poeni", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RowItem(position: Int, user: UserScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(position.toString(), fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(user.username.ifBlank { user.email.substringBefore("@") })
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            Text(user.points.toString(), fontWeight = FontWeight.Bold)
        }
    }
}
