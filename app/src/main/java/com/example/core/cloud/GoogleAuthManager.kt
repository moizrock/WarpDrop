package com.example.core.cloud

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object GoogleAuthManager {

    private const val TAG = "Auth"
    private const val PREFS_NAME = "warpdrop_auth_session_prefs"
    private const val KEY_AUTHORIZED_OR_SKIPPED = "authorized_or_skipped"
    private const val KEY_PROFILE_LABEL = "saved_profile_label"

    fun saveLocalOrSkippedSession(context: Context, profileLabel: String = "Offline Mode") {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTHORIZED_OR_SKIPPED, true)
            .putString(KEY_PROFILE_LABEL, profileLabel)
            .apply()
    }

    fun isSessionAuthorizedOrSkipped(context: Context): Boolean {
        val hasFirebaseUser = runCatching { Firebase.auth.currentUser != null }.getOrDefault(false)
        if (hasFirebaseUser) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTHORIZED_OR_SKIPPED, false)
    }

    fun getSavedProfileLabel(context: Context): String {
        val firebaseEmail = runCatching { Firebase.auth.currentUser?.email }.getOrNull()
        if (!firebaseEmail.isNullOrBlank()) return firebaseEmail
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE_LABEL, "Offline Mode") ?: "Offline Mode"
    }

    fun clearLocalSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTHORIZED_OR_SKIPPED, false)
            .remove(KEY_PROFILE_LABEL)
            .apply()
    }

    fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        addAuthStateListener(listener)
        awaitClose { removeAuthStateListener(listener) }
    }

    /**
     * Silent Auto-Sign-In on App Startup using GetGoogleIdOption in a single-option GetCredentialRequest.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (isSessionAuthorizedOrSkipped(context)) {
            onAuthSuccess()
            return
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    saveLocalOrSkippedSession(context, Firebase.auth.currentUser?.email ?: "Google Account")
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive Sign-In using GetSignInWithGoogleOption in a single-option GetCredentialRequest.
     * If Firebase/Play Services credentials are absent on the emulator or offline device,
     * gracefully completes sign-in with a local profile so it never blocks or crashes.
     */
    fun onGoogleSignInClicked(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit = {},
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            saveLocalOrSkippedSession(context, "WarpDrop Local Profile")
            onAuthSuccess()
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val activity = context as? Activity
                if (activity == null) {
                    saveLocalOrSkippedSession(context, "WarpDrop Local Profile")
                    onAuthSuccess()
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    saveLocalOrSkippedSession(context, Firebase.auth.currentUser?.email ?: "Google Account")
                    onAuthSuccess()
                } else {
                    saveLocalOrSkippedSession(context, "WarpDrop Local Profile")
                    onAuthSuccess()
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.w(TAG, "Google Play Services / Credentials unavailable; falling back to local profile", e)
                saveLocalOrSkippedSession(context, "WarpDrop Local Profile")
                onAuthSuccess()
            }
        }
    }

    /**
     * Signs out of FirebaseAuth, clears local session preferences, and clears Jetpack CredentialManager state.
     */
    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        clearLocalSession(context)
        runCatching { Firebase.auth.signOut() }
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
