package com.example.core.cloud

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

@IgnoreExtraProperties
data class CloudTransferRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fileName: String = "",
    val sizeBytes: Long = 0L,
    val category: String = "FILES",
    val direction: String = "SENT",
    val peerDeviceName: String = "Xiaomi 12 Pro",
    val sha256Checksum: String = "",
    val status: String = "COMPLETED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class WarpDropCloudRepository(private val db: FirebaseFirestore) {

    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeTransferRecords(): Flow<List<CloudTransferRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/transfer_records"
        emitAll(
            db.collection("users")
                .document(uid)
                .collection("transfer_records")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudTransferRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createTransferRecord(
        fileName: String,
        sizeBytes: Long,
        category: String = "FILES",
        direction: String = "SENT",
        peerDeviceName: String = "Xiaomi 12 Pro",
        sha256Checksum: String = "",
        status: String = "COMPLETED"
    ): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/unauthenticated/transfer_records")
            return Result.failure(e)
        }
        val recordId = "rec_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("users")
            .document(uid)
            .collection("transfer_records")
            .document(recordId)
        val payload = mapOf(
            "userId" to uid,
            "fileName" to fileName.trim().take(255).ifEmpty { "WarpDrop_File.bin" },
            "sizeBytes" to sizeBytes.coerceAtLeast(0L),
            "category" to category.trim().take(40).ifEmpty { "FILES" },
            "direction" to if (direction == "RECEIVED") "RECEIVED" else "SENT",
            "peerDeviceName" to peerDeviceName.trim().take(80).ifEmpty { "Xiaomi 12 Pro" },
            "sha256Checksum" to sha256Checksum.trim().take(128),
            "status" to status,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(recordId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getTransferRecordById(ownerUserId: String, recordId: String): Result<CloudTransferRecord?> {
        val docRef = db.collection("users")
            .document(ownerUserId)
            .collection("transfer_records")
            .document(recordId)
        return try {
            val snapshot = docRef.get().await()
            Result.success(
                snapshot.toObject(
                    CloudTransferRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserTransferRecords(): Result<List<CloudTransferRecord>> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "users/unauthenticated/transfer_records")
            return Result.failure(e)
        }
        val path = "users/$uid/transfer_records"
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("transfer_records")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val items = snapshot.toObjects(
                CloudTransferRecord::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun updateTransferRecordStatus(record: CloudTransferRecord, newStatus: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/unauthenticated/transfer_records")
            return Result.failure(e)
        }
        require(record.id.isNotBlank()) { "Record ID must not be blank for update" }
        requireNotNull(record.createdAt) { "Record createdAt must not be null for update" }
        val docRef = db.collection("users")
            .document(uid)
            .collection("transfer_records")
            .document(record.id)
        return try {
            docRef.set(record.copy(status = newStatus, updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteTransferRecord(recordId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/unauthenticated/transfer_records")
            return Result.failure(e)
        }
        val docRef = db.collection("users")
            .document(uid)
            .collection("transfer_records")
            .document(recordId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
