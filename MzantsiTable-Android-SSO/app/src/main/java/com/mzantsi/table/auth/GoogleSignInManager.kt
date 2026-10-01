package com.mzantsi.table.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

data class GoogleAccount(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String
)

sealed interface GoogleSignInResult {
    data class Success(val account: GoogleAccount) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failure(val message: String) : GoogleSignInResult
}

object GoogleSignInManager {

    private fun webClientId(context: Context): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId == 0) return null
        return context.getString(resId).takeIf { it.isNotBlank() }
    }

    fun isConfigured(context: Context): Boolean = webClientId(context) != null


    suspend fun signIn(context: Context): GoogleSignInResult {
        val clientId = webClientId(context)
            ?: return GoogleSignInResult.Failure("Google Sign-In is not configured (google-services.json missing).")

        return try {
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // show every Google account, so new users can sign up too
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val response = CredentialManager.create(context).getCredential(context, request)

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(google.idToken, null)
                val firebaseUser = FirebaseAuth.getInstance()
                    .signInWithCredential(firebaseCredential)
                    .await()
                    .user
                    ?: return GoogleSignInResult.Failure("Firebase did not return a user. Please try again.")

                GoogleSignInResult.Success(
                    GoogleAccount(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: google.displayName ?: "",
                        email = firebaseUser.email ?: google.id,
                        photoUrl = (firebaseUser.photoUrl ?: google.profilePictureUri)?.toString().orEmpty()
                    )
                )
            } else {
                GoogleSignInResult.Failure("Unexpected sign-in response. Please try again.")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleSignInResult.Failure(
                "No Google account found on this device. Add one under Settings > Passwords & accounts, then try again."
            )
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Failure(e.message ?: "Google Sign-In failed. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            GoogleSignInResult.Failure(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
        }
    }

    suspend fun signOut(context: Context) {
        if (!isConfigured(context)) return
        runCatching { FirebaseAuth.getInstance().signOut() }
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }
}
