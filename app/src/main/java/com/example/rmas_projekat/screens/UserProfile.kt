package com.example.rmas_projekat.screens

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Rang lista i poeni (addPoints) rade nad dokumentom users/{uid}; ako ne postoji, pravi ga.
// Postojeći dokument (i poeni u njemu) se nikad ne prepisuje.
fun ensureUserProfile() {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val email = user.email ?: return
    val ref = FirebaseFirestore.getInstance().collection("users").document(user.uid)
    ref.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            ref.set(
                mapOf(
                    "username" to email.substringBefore("@"),
                    "email" to email,
                    "points" to 0L
                )
            )
        }
    }
}
