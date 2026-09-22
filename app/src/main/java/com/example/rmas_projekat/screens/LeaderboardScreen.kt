package com.example.rmas_projekat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rmas_projekat.ui.theme.Orange
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeLight
import com.example.rmas_projekat.ui.theme.OrangeSoft
import com.example.rmas_projekat.ui.theme.OrangeText
import com.example.rmas_projekat.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class UserScore(
    val username: String = "",
    val email: String = "",
    val points: Long = 0
)

private fun UserScore.displayName() = username.ifBlank { email.substringBefore("@") }

@Composable
fun LeaderboardScreen(navController: NavController) {
    var items by remember { mutableStateOf(listOf<UserScore>()) }

    DisposableEffect(Unit) {
        val reg = FirebaseFirestore.getInstance().collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                items = snap?.documents?.map {
                    UserScore(
                        username = it.getString("username") ?: "",
                        email = it.getString("email") ?: "",
                        points = it.getLong("points") ?: 0L
                    )
                } ?: emptyList()
            }
        onDispose { reg.remove() }
    }

    val myEmail = FirebaseAuth.getInstance().currentUser?.email

    AppScaffold(nav = navController, current = "leaderboard", title = "Rang lista") { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (items.isEmpty()) {
                item {
                    Text(
                        text = "Još nema poena.\nDodaj restoran i uđi na rang listu!",
                        color = TextMuted,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                item { Podium(items.take(3), myEmail) }
                itemsIndexed(items.drop(3)) { index, user ->
                    LeaderRow(position = index + 4, user = user, isMe = user.email == myEmail)
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = OrangeSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Kako do poena?", fontWeight = FontWeight.Bold, color = OrangeText)
                        Spacer(Modifier.height(4.dp))
                        Text("Dodaj novi restoran: +5 poena", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Podium(top: List<UserScore>, myEmail: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // redosled prikaza: 2. mesto, 1. mesto, 3. mesto
        listOf(1, 0, 2).forEach { idx ->
            val user = top.getOrNull(idx)
            if (user != null) {
                PodiumSlot(place = idx + 1, user = user, isMe = user.email == myEmail)
            } else {
                Spacer(Modifier.width(96.dp))
            }
        }
    }
}

@Composable
private fun PodiumSlot(place: Int, user: UserScore, isMe: Boolean) {
    val medal = when (place) {
        1 -> Color(0xFFFFB300)
        2 -> Color(0xFF90A4AE)
        else -> Color(0xFFBF7A4A)
    }
    val avatar = if (place == 1) 72.dp else 58.dp
    val block = when (place) {
        1 -> 110.dp
        2 -> 80.dp
        else -> 60.dp
    }
    Column(
        modifier = Modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(user.displayName(), avatar, medal)
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isMe) "${user.displayName()} (ti)" else user.displayName(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = OrangeText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text("${user.points} p.", fontSize = 12.sp, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(block)
                .background(
                    Brush.verticalGradient(listOf(Orange, OrangeDark)),
                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(place.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LeaderRow(position: Int, user: UserScore, isMe: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isMe) OrangeSoft else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = position.toString(),
                fontWeight = FontWeight.Bold,
                color = OrangeDark,
                modifier = Modifier.width(32.dp)
            )
            Avatar(user.displayName(), 40.dp, OrangeLight)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = if (isMe) "${user.displayName()} (ti)" else user.displayName(),
                    fontWeight = FontWeight.SemiBold,
                    color = OrangeText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(user.email, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                text = "${user.points} p.",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .background(OrangeDark, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
