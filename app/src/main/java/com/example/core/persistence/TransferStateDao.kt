package com.example.core.persistence

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Phase 2: Stateful Pause, Seamless Resume & 30-Min Crash Recovery Persistence Layer.
 * Stores a compact bit-level chunk completion bitmap (`chunkBitmap`) in SQLite WAL (Write-Ahead Logging)
 * mode so paused or interrupted transfers (including network switches or unexpected reboots within
 * 30 minutes) resume from the exact unverified 64 KB chunk offset.
 */
@Entity(tableName = "transfer_checkpoints")
data class TransferCheckpointEntity(
    @PrimaryKey
    @ColumnInfo(name = "transfer_id")
    val transferId: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "source_uri_or_path")
    val sourceUriOrPath: String,

    @ColumnInfo(name = "target_file_path")
    val targetFilePath: String,

    @ColumnInfo(name = "total_bytes")
    val totalBytes: Long,

    @ColumnInfo(name = "chunk_size_bytes")
    val chunkSizeBytes: Int = ChunkBitmapTracker.DEFAULT_CHUNK_SIZE_BYTES,

    @ColumnInfo(name = "total_chunks")
    val totalChunks: Int,

    @ColumnInfo(name = "verified_chunks")
    val verifiedChunks: Int,

    @ColumnInfo(name = "chunk_bitmap", typeAffinity = ColumnInfo.BLOB)
    val chunkBitmap: ByteArray,

    @ColumnInfo(name = "file_sha256_expected")
    val fileSha256Expected: String,

    @ColumnInfo(name = "last_verified_chunk_hash")
    val lastVerifiedChunkHash: String,

    @ColumnInfo(name = "status")
    val status: String, // STREAMING, PAUSED, INTERRUPTED_RECOVERY_READY, COMPLETED, FAILED

    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long,

    @ColumnInfo(name = "expires_at_epoch_ms")
    val expiresAtEpochMs: Long
) {
    val resumeChunkIndex: Int
        get() = ChunkBitmapTracker.findFirstUnverifiedChunkIndex(chunkBitmap, totalChunks)

    val resumeByteOffset: Long
        get() = resumeChunkIndex.toLong() * chunkSizeBytes.toLong()

    val completionFraction: Float
        get() = if (totalChunks <= 0) 1f else (verifiedChunks.toFloat() / totalChunks.toFloat()).coerceIn(0f, 1f)

    fun isWithin30MinRecoveryWindow(nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        return nowEpochMs <= expiresAtEpochMs
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransferCheckpointEntity) return false
        return transferId == other.transferId &&
            verifiedChunks == other.verifiedChunks &&
            status == other.status &&
            updatedAtEpochMs == other.updatedAtEpochMs &&
            chunkBitmap.contentEquals(other.chunkBitmap)
    }

    override fun hashCode(): Int {
        var result = transferId.hashCode()
        result = 31 * result + verifiedChunks
        result = 31 * result + chunkBitmap.contentHashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}

@Dao
interface TransferStateDao {

    @Query("SELECT * FROM transfer_checkpoints ORDER BY updated_at_epoch_ms DESC")
    fun observeAllCheckpoints(): Flow<List<TransferCheckpointEntity>>

    @Query(
        """
        SELECT * FROM transfer_checkpoints 
        WHERE expires_at_epoch_ms >= :nowEpochMs 
        AND status IN ('PAUSED', 'INTERRUPTED_RECOVERY_READY', 'STREAMING')
        ORDER BY updated_at_epoch_ms DESC
        """
    )
    fun observeRecoverableCheckpoints(nowEpochMs: Long): Flow<List<TransferCheckpointEntity>>

    @Query("SELECT * FROM transfer_checkpoints WHERE transfer_id = :transferId LIMIT 1")
    suspend fun getCheckpointById(transferId: String): TransferCheckpointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckpoint(checkpoint: TransferCheckpointEntity)

    @Query(
        """
        UPDATE transfer_checkpoints 
        SET verified_chunks = :verifiedChunks,
            chunk_bitmap = :chunkBitmap,
            last_verified_chunk_hash = :lastChunkHash,
            status = :status,
            updated_at_epoch_ms = :updatedAtMs,
            expires_at_epoch_ms = :expiresAtMs
        WHERE transfer_id = :transferId
        """
    )
    suspend fun updateBitmapProgress(
        transferId: String,
        verifiedChunks: Int,
        chunkBitmap: ByteArray,
        lastChunkHash: String,
        status: String,
        updatedAtMs: Long,
        expiresAtMs: Long
    )

    @Query(
        """
        UPDATE transfer_checkpoints 
        SET status = :newStatus,
            updated_at_epoch_ms = :updatedAtMs,
            expires_at_epoch_ms = :expiresAtMs
        WHERE transfer_id = :transferId
        """
    )
    suspend fun updateTransferStatus(
        transferId: String,
        newStatus: String,
        updatedAtMs: Long,
        expiresAtMs: Long
    )

    @Query("DELETE FROM transfer_checkpoints WHERE expires_at_epoch_ms < :nowEpochMs AND status != 'COMPLETED'")
    suspend fun purgeExpiredCheckpoints(nowEpochMs: Long): Int

    @Query("DELETE FROM transfer_checkpoints WHERE transfer_id = :transferId")
    suspend fun deleteCheckpointById(transferId: String)
}

@Database(
    entities = [TransferCheckpointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VayuTransferDatabase : RoomDatabase() {
    abstract fun transferStateDao(): TransferStateDao

    companion object {
        private const val DB_NAME = "vayudrop_transfer_wal.db"

        @Volatile
        private var INSTANCE: VayuTransferDatabase? = null

        fun getInstance(context: Context): VayuTransferDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VayuTransferDatabase::class.java,
                    DB_NAME
                )
                    // Explicitly enforce SQLite WAL (Write-Ahead Logging) mode for atomic crash-safe commits
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

/**
 * Bit-packed Chunk Completion Bitmap utility.
 * Even a 100 GB file at 64 KB chunks (1,638,400 chunks) requires only 200 KB of bitmap memory!
 */
object ChunkBitmapTracker {
    const val DEFAULT_CHUNK_SIZE_BYTES = 64 * 1024 // 64 KB (65,536 bytes)
    const val CRASH_RECOVERY_TTL_MS = 30L * 60L * 1000L // 30 minutes (1,800,000 ms)

    fun calculateTotalChunks(totalBytes: Long, chunkSizeBytes: Int = DEFAULT_CHUNK_SIZE_BYTES): Int {
        if (totalBytes <= 0L) return 1
        return ((totalBytes + chunkSizeBytes - 1L) / chunkSizeBytes.toLong()).toInt().coerceAtLeast(1)
    }

    fun allocateBitmap(totalChunks: Int): ByteArray {
        val byteLen = ((totalChunks + 7) / 8).coerceAtLeast(1)
        return ByteArray(byteLen)
    }

    fun markChunkVerified(bitmap: ByteArray, chunkIndex: Int) {
        val byteIndex = chunkIndex ushr 3
        val bitMask = 1 shl (chunkIndex and 7)
        if (byteIndex in bitmap.indices) {
            bitmap[byteIndex] = (bitmap[byteIndex].toInt() or bitMask).toByte()
        }
    }

    fun isChunkVerified(bitmap: ByteArray, chunkIndex: Int): Boolean {
        val byteIndex = chunkIndex ushr 3
        val bitMask = 1 shl (chunkIndex and 7)
        if (byteIndex !in bitmap.indices) return false
        return (bitmap[byteIndex].toInt() and bitMask) != 0
    }

    fun countVerifiedChunks(bitmap: ByteArray, totalChunks: Int): Int {
        var count = 0
        for (i in 0 until totalChunks) {
            if (isChunkVerified(bitmap, i)) count++
        }
        return count
    }

    /**
     * Scans the completion bitmap to locate the exact first unverified 64 KB chunk index.
     * Returns `totalChunks` if 100% of chunks are verified.
     */
    fun findFirstUnverifiedChunkIndex(bitmap: ByteArray, totalChunks: Int): Int {
        for (i in 0 until totalChunks) {
            if (!isChunkVerified(bitmap, i)) {
                return i
            }
        }
        return totalChunks
    }

    /**
     * Renders a compact ASCII/Binary preview of the bitmap window around the resume cursor for HUD inspection.
     */
    fun formatBitmapWindowPreview(bitmap: ByteArray, totalChunks: Int, windowSize: Int = 32): String {
        val limit = totalChunks.coerceAtMost(windowSize)
        val sb = StringBuilder(limit)
        for (i in 0 until limit) {
            sb.append(if (isChunkVerified(bitmap, i)) '1' else '0')
        }
        if (totalChunks > windowSize) {
            sb.append("…")
        }
        return sb.toString()
    }
}

/**
 * Repository abstracting Room WAL `TransferStateDao` operations for the ViewModel and StreamEngine.
 */
class TransferCheckpointRepository(
    private val dao: TransferStateDao
) {
    val allCheckpoints: Flow<List<TransferCheckpointEntity>> = dao.observeAllCheckpoints()

    fun observeRecoverableCheckpoints(nowEpochMs: Long = System.currentTimeMillis()): Flow<List<TransferCheckpointEntity>> {
        return dao.observeRecoverableCheckpoints(nowEpochMs)
    }

    suspend fun getCheckpoint(transferId: String): TransferCheckpointEntity? {
        return dao.getCheckpointById(transferId)
    }

    suspend fun createOrGetCheckpoint(
        transferId: String,
        fileName: String,
        sourceUriOrPath: String,
        targetFilePath: String,
        totalBytes: Long,
        expectedFileSha256: String,
        chunkSizeBytes: Int = ChunkBitmapTracker.DEFAULT_CHUNK_SIZE_BYTES
    ): TransferCheckpointEntity {
        val now = System.currentTimeMillis()
        dao.purgeExpiredCheckpoints(now)

        val existing = dao.getCheckpointById(transferId)
        if (existing != null && existing.isWithin30MinRecoveryWindow(now)) {
            return existing
        }

        val totalChunks = ChunkBitmapTracker.calculateTotalChunks(totalBytes, chunkSizeBytes)
        val freshBitmap = ChunkBitmapTracker.allocateBitmap(totalChunks)
        val entity = TransferCheckpointEntity(
            transferId = transferId,
            fileName = fileName,
            sourceUriOrPath = sourceUriOrPath,
            targetFilePath = targetFilePath,
            totalBytes = totalBytes,
            chunkSizeBytes = chunkSizeBytes,
            totalChunks = totalChunks,
            verifiedChunks = 0,
            chunkBitmap = freshBitmap,
            fileSha256Expected = expectedFileSha256,
            lastVerifiedChunkHash = "NONE",
            status = "STREAMING",
            updatedAtEpochMs = now,
            expiresAtEpochMs = now + ChunkBitmapTracker.CRASH_RECOVERY_TTL_MS
        )
        dao.upsertCheckpoint(entity)
        return entity
    }

    suspend fun recordVerifiedChunk(
        transferId: String,
        chunkIndex: Int,
        chunkSha256Hex: String,
        bitmap: ByteArray,
        totalChunks: Int,
        status: String = "STREAMING"
    ) {
        ChunkBitmapTracker.markChunkVerified(bitmap, chunkIndex)
        val verifiedCount = ChunkBitmapTracker.countVerifiedChunks(bitmap, totalChunks)
        val now = System.currentTimeMillis()
        dao.updateBitmapProgress(
            transferId = transferId,
            verifiedChunks = verifiedCount,
            chunkBitmap = bitmap.copyOf(),
            lastChunkHash = chunkSha256Hex,
            status = if (verifiedCount >= totalChunks) "COMPLETED" else status,
            updatedAtMs = now,
            expiresAtMs = now + ChunkBitmapTracker.CRASH_RECOVERY_TTL_MS
        )
    }

    suspend fun markPausedOrInterrupted(transferId: String, pausedByUser: Boolean) {
        val now = System.currentTimeMillis()
        dao.updateTransferStatus(
            transferId = transferId,
            newStatus = if (pausedByUser) "PAUSED" else "INTERRUPTED_RECOVERY_READY",
            updatedAtMs = now,
            expiresAtMs = now + ChunkBitmapTracker.CRASH_RECOVERY_TTL_MS
        )
    }

    suspend fun deleteCheckpoint(transferId: String) {
        dao.deleteCheckpointById(transferId)
    }
}
