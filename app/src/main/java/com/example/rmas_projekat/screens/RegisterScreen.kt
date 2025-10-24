package com.example.rmas_projekat.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
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
                    navController.navigate("map") {
                        popUpTo("register") { inclusive = true }
                    }
                } else {
                    error = t.exception?.localizedMessage ?: "Registracija nije uspela"
                }
            }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Registracija") }) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Lozinka") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { if (!busy) register() },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "Registrujem..." else "Registruj se")
            }
        }
    }
}