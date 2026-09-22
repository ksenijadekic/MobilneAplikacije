package com.example.rmas_projekat.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun login() {
        error = null
        val e = email.trim()
        if (e.isEmpty() || password.isEmpty()) {
            error = "Unesi email i lozinku"; return
        }

        loading = true
        auth.signInWithEmailAndPassword(e, password)
            .addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    ensureUserProfile()
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    error = task.exception?.localizedMessage ?: "Prijava nije uspela"
                }
            }
    }

    AuthScreenLayout(
        subtitle = "Dobrodošao nazad",
        cardTitle = "Prijavi se",
        footer = {
            AuthLink("Nemaš nalog? Registruj se") {
                navController.navigate("register") { launchSingleTop = true }
            }
        }
    ) {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            icon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Lozinka",
            icon = Icons.Filled.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        AuthError(error)
        Spacer(Modifier.height(20.dp))
        AuthButton(
            text = if (loading) "Prijavljujem..." else "Prijavi se",
            enabled = !loading,
            onClick = { if (!loading) login() }
        )
    }
}
