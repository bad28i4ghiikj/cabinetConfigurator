package com.example.cabinetconfigurator.firebase

import android.util.Log
import com.example.cabinetconfigurator.domain.model.Quote
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreManager(private val authManager: FirebaseAuthManager) {
    private val db = FirebaseFirestore.getInstance()
    private val quotesCollection = "quotes"

    suspend fun uploadQuote(quote: Quote): Boolean = runCatching {
        val uid = authManager.getUid() ?: return false
        val data = quote.toMap()
        db.collection(quotesCollection)
            .document("${uid}_${quote.id}")
            .set(data)
            .await()
        true
    }.onFailure {
        Log.e("Firestore", "Upload failed", it)
    }.getOrDefault(false)

    suspend fun deleteQuoteFromCloud(quoteId: Long): Boolean = runCatching {
        val uid = authManager.getUid() ?: return false
        db.collection(quotesCollection)
            .document("${uid}_$quoteId")
            .delete()
            .await()
        true
    }.onFailure {
        Log.e("Firestore", "Delete failed", it)
    }.getOrDefault(false)

    fun observeCloudQuotes(): Flow<List<Quote>> = callbackFlow {
        val uid = authManager.getUid()
        if (uid == null) {
            close()
            return@callbackFlow
        }

        val listener = db.collection(quotesCollection)
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed", error)
                    close(error)
                    return@addSnapshotListener
                }

                val quotes = snapshot?.documents?.mapNotNull { doc ->
                    runCatching { doc.toObject(Quote::class.java) }.getOrNull()
                } ?: emptyList()

                try {
                    trySend(quotes)
                } catch (e: Exception) {
                    Log.e("Firestore", "Send failed", e)
                }
            }

        awaitClose { listener.remove() }
    }

    private fun Quote.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to authManager.getUid(),
        "name" to name,
        "cabinetType" to cabinetType,
        "elementType" to elementType,
        "widthMm" to widthMm,
        "heightMm" to heightMm,
        "depthMm" to depthMm,
        "totalNet" to totalNet,
        "totalGross" to totalGross,
        "createdAt" to createdAt,
        "pricingSnapshot" to pricingSnapshot
    )
}
