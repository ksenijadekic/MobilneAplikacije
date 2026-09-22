package com.example.rmas_projekat.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rmas_projekat.ui.theme.Cream
import com.example.rmas_projekat.ui.theme.Orange
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeText
import com.example.rmas_projekat.ui.theme.StarEmpty
import com.example.rmas_projekat.ui.theme.TextMuted
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// Okvir ekrana posle prijave: narandžasta gornja traka + donja navigacija
// ---------------------------------------------------------------------------

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("main", "Restorani", Icons.Filled.Home),
    Tab("maps", "Mapa", Icons.Filled.Place),
    Tab("addObject", "Dodaj", Icons.Filled.Add),
    Tab("leaderboard", "Rang", Icons.Filled.Star),
    Tab("profile", "Profil", Icons.Filled.Person)
)

@Composable
fun AppScaffold(
    nav: NavController,
    current: String,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { OrangeTopBar(title, actions) },
        bottomBar = { AppBottomBar(nav, current) },
        containerColor = Cream,
        content = content
    )
}

@Composable
fun OrangeTopBar(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.horizontalGradient(listOf(OrangeDark, Orange)))
            .height(64.dp)
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun AppBottomBar(nav: NavController, current: String) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = current == tab.route,
                onClick = {
                    if (current != tab.route) {
                        nav.navigate(tab.route) {
                            popUpTo("main")
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = OrangeDark,
                    indicatorColor = OrangeDark,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Zajedničke komponente
// ---------------------------------------------------------------------------

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = title, color = OrangeDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun RatingStars(rating: Double, size: Dp = 16.dp) {
    Row {
        repeat(5) { i ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (i < rating.roundToInt()) Orange else StarEmpty,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
fun StarRatingInput(value: Int, onChange: (Int) -> Unit, size: Dp = 40.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Ocena $i",
                tint = if (i <= value) Orange else StarEmpty,
                modifier = Modifier
                    .size(size)
                    .clickable { onChange(i) }
            )
        }
    }
}

@Composable
fun OrangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) OrangeDark else Color.White)
            .border(BorderStroke(1.dp, if (selected) OrangeDark else Color(0xFFE0C9B8)), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else OrangeText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun Avatar(text: String, size: Dp, color: Color = OrangeDark) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.trim().take(1).uppercase().ifBlank { "?" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}

@Composable
fun OrangeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = if (icon != null) ({ Icon(icon, contentDescription = null) }) else null,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = orangeFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}
