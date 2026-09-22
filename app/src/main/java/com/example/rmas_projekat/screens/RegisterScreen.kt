package com.example.rmas_projekat.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(
    navController: NavController
) {
    val ctx = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun register() {
        error = null
        val e = email.trim()
        val p = pass

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            error = "Unesi ispravan email"; return
        }
        if (p.length < 6) {
            error = "Lozinka mora imati najmanje 6 karaktera"; return
        }

        busy = true
        auth.createUserWithEmailAndPassword(e, p)
            .addOnCompleteListener { t ->
                busy = false
                if (t.isSuccessful) {
                    Toast.makeText(ctx, "Uspešna registracija", Toast.LENGTH_SHORT).show()
                    ensureUserProfile()
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    error = t.exception?.localizedMessage ?: "Registracija nije uspela"
                }
            }
    }

    AuthScreenLayout(
        subtitle = "Napravi novi nalog",
        cardTitle = "Registracija",
        footer = {
            AuthLink("Već imaš nalog? Prijavi se") {
                navController.navigate("login") { popUpTo("login") { inclusive = true } }
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
            value = pass,
            onValueChange = { pass = it },
            label = "Lozinka",
            icon = Icons.Filled.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        AuthError(error)
        Spacer(Modifier.height(20.dp))
        AuthButton(
            text = if (busy) "Registrujem..." else "Registruj se",
            enabled = !busy,
            onClick = { if (!busy) register() }
        )
    }
}
