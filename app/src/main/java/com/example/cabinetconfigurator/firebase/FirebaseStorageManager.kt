package com.example.cabinetconfigurator.firebase

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseStorageManager(private val authManager: FirebaseAuthManager) {
    private val storage = FirebaseStorage.getInstance()
    private val pdfBucket = "pdfs"

    suspend fun uploadPdf(pdfFile: File, quoteId: Long): Boolean = runCatching {
        val uid = authManager.getUid() ?: return false
        val fileName = "quote_${uid}_${quoteId}_${System.currentTimeMillis()}.pdf"
        val ref = storage.reference.child(pdfBucket).child(uid).child(fileName)

        pdfFile.inputStream().use { stream ->
            ref.putStream(stream).await()
        }
        true
    }.onFailure {
        Log.e("FirebaseStorage", "Upload failed", it)
    }.getOrDefault(false)

    suspend fun listUserPdfs(): List<String> = runCatching {
        val uid = authManager.getUid() ?: return emptyList()
        val ref = storage.reference.child(pdfBucket).child(uid)
        val result = ref.listAll().await()
        result.items.map { it.name }
    }.onFailure {
        Log.e("FirebaseStorage", "List failed", it)
    }.getOrDefault(emptyList())

    suspend fun downloadPdf(fileName: String): ByteArray? = runCatching {
        val uid = authManager.getUid() ?: return null
        val ref = storage.reference.child(pdfBucket).child(uid).child(fileName)
        ref.getBytes(Long.MAX_VALUE).await()
    }.onFailure {
        Log.e("FirebaseStorage", "Download failed", it)
    }.getOrNull()

    suspend fun deletePdf(fileName: String): Boolean = runCatching {
        val uid = authManager.getUid() ?: return false
        val ref = storage.reference.child(pdfBucket).child(uid).child(fileName)
        ref.delete().await()
        true
    }.onFailure {
        Log.e("FirebaseStorage", "Delete failed", it)
    }.getOrDefault(false)

    suspend fun getPdfDownloadUrl(fileName: String): String? = runCatching {
        val uid = authManager.getUid() ?: return null
        val ref = storage.reference.child(pdfBucket).child(uid).child(fileName)
        ref.downloadUrl.await().toString()
    }.onFailure {
        Log.e("FirebaseStorage", "Get URL failed", it)
    }.getOrNull()
}
