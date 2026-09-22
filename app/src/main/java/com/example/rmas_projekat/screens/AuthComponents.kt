package com.example.rmas_projekat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rmas_projekat.ui.theme.Orange
import com.example.rmas_projekat.ui.theme.OrangeDark
import com.example.rmas_projekat.ui.theme.OrangeLight
import com.example.rmas_projekat.ui.theme.OrangeText

@Composable
fun AuthScreenLayout(
    subtitle: String,
    cardTitle: String,
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Brush.verticalGradient(listOf(OrangeDark, Orange, OrangeLight)))
    ) {
        val minH = maxHeight

        // dekorativni krugovi
        Box(
            Modifier
                .offset(x = (-70).dp, y = (-70).dp)
                .size(230.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minH)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "RMAS",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Text(text = subtitle, color = Color.White.copy(alpha = 0.92f), fontSize = 16.sp)
            Spacer(Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = cardTitle,
                        color = OrangeDark,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))
                    content()
                }
            }

            Spacer(Modifier.height(16.dp))
            footer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun orangeFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    focusedTextColor = OrangeText,
    unfocusedTextColor = OrangeText,
    containerColor = Color.White,
    cursorColor = OrangeDark,
    focusedBorderColor = OrangeDark,
    unfocusedBorderColor = Color(0xFFBCAAA4),
    focusedLabelColor = OrangeDark,
    unfocusedLabelColor = Color(0xFF6D4C41),
    focusedLeadingIconColor = OrangeDark,
    unfocusedLeadingIconColor = Color(0xFF8D6E63)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = orangeFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AuthError(message: String?) {
    if (message != null) {
        Spacer(Modifier.height(10.dp))
        Text(text = message, color = Color(0xFFB3261E), fontSize = 14.sp)
    }
}

// Glavno dugme: popunjen narandžasti pravougaonik sa blago zaobljenim ivicama
@Composable
fun AuthButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangeDark,
            contentColor = Color.White,
            disabledContainerColor = OrangeDark.copy(alpha = 0.55f),
            disabledContentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AuthLink(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = text, color = OrangeText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
