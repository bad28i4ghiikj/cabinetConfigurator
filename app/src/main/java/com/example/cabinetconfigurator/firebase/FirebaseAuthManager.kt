package com.example.cabinetconfigurator.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser
    val isLoggedIn = auth.currentUser != null

    suspend fun registerWithEmail(email: String, password: String, displayName: String): Boolean = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
        )?.await()
        true
    }.onFailure {
        Log.e("FirebaseAuth", "Registration failed", it)
    }.getOrDefault(false)

    suspend fun loginWithEmail(email: String, password: String): Boolean = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        true
    }.onFailure {
        Log.e("FirebaseAuth", "Login failed", it)
    }.getOrDefault(false)

    fun logout() {
        auth.signOut()
    }

    fun getUid(): String? = auth.currentUser?.uid

    fun getEmail(): String? = auth.currentUser?.email

    fun getDisplayName(): String? = auth.currentUser?.displayName
}
