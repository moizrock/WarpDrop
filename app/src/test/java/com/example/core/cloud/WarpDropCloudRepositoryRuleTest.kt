package com.example.core.cloud

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WarpDropCloudRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createTransferRecord_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = WarpDropCloudRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTransferRecord(
                fileName = "WarpDrop_5GHz_Video.mp4",
                sizeBytes = 52_428_800L,
                category = "VIDEOS",
                direction = "SENT",
                peerDeviceName = "Xiaomi 12 Pro"
            )
        }
        assertTrue(createResult.isSuccess)
        val recordId = createResult.getOrThrow()
        assertTrue(recordId.isNotEmpty())
    }

    @Test
    fun getUserTransferRecords_authenticatedOwner_returnsMatchingRecords() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = WarpDropCloudRepository(firestore)
        val recordId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTransferRecord(
                fileName = "WarpDrop_Archive.zip",
                sizeBytes = 10_485_760L,
                category = "FILES",
                direction = "RECEIVED"
            ).getOrThrow()
        }

        val recordsResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserTransferRecords()
        }
        assertTrue(recordsResult.isSuccess)
        assertTrue(recordsResult.getOrThrow().map { it.id }.contains(recordId))
    }

    @Test
    fun observeTransferRecords_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = WarpDropCloudRepository(firestore)
        val recordId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTransferRecord(
                fileName = "WarpDrop_Music_Lossless.flac",
                sizeBytes = 31_457_280L,
                category = "MUSIC",
                direction = "SENT"
            ).getOrThrow()
        }

        val emittedRecords = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTransferRecords().first { list -> list.any { it.id == recordId } }
        }
        assertTrue(emittedRecords.map { it.id }.contains(recordId))
    }

    @Test
    fun getTransferRecordById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = WarpDropCloudRepository(firestore)
        val recordId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createTransferRecord(
                fileName = "Private_WhatsApp_Backup.crypt15",
                sizeBytes = 104_857_600L,
                category = "FILES",
                direction = "SENT"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = WarpDropCloudRepository(firestore)
        val crossUserResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getTransferRecordById(aliceUid, recordId)
        }
        assertTrue(crossUserResult.isFailure)
        val exception = crossUserResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(exception)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun observeTransferRecords_unauthenticatedUser_failsFast() = runBlocking {
        auth.signOut()
        val repository = WarpDropCloudRepository(firestore)

        var thrown: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTransferRecords().first()
            }
        } catch (e: Throwable) {
            thrown = e
        }
        assertNotNull(thrown)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@warpdrop.test"
        const val BOB_EMAIL = "bob@warpdrop.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
