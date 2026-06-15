package com.example.cabinetconfigurator.firebase

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

object FirebaseModule {
    private var authManager: FirebaseAuthManager? = null
    private var firestoreManager: FirebaseFirestoreManager? = null
    private var storageManager: FirebaseStorageManager? = null

    fun initialize() {
        Firebase.analytics.logEvent("firebase_initialized") {
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun getAuthManager(): FirebaseAuthManager {
        if (authManager == null) {
            authManager = FirebaseAuthManager()
        }
        return authManager!!
    }

    fun getFirestoreManager(): FirebaseFirestoreManager {
        if (firestoreManager == null) {
            firestoreManager = FirebaseFirestoreManager(getAuthManager())
        }
        return firestoreManager!!
    }

    fun getStorageManager(): FirebaseStorageManager {
        if (storageManager == null) {
            storageManager = FirebaseStorageManager(getAuthManager())
        }
        return storageManager!!
    }
}
