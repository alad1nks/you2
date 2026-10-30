package com.alad1nks.you2.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher? {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return { complete ->
        scope.launch {
            val result =
                try {
                    val clientIdResource =
                        context.resources.getIdentifier(
                            "default_web_client_id",
                            "string",
                            context.packageName,
                        )
                    check(clientIdResource != 0)
                    val option = GetSignInWithGoogleOption.Builder(context.getString(clientIdResource)).build()
                    val response =
                        CredentialManager.create(context).getCredential(
                            context,
                            GetCredentialRequest.Builder().addCredentialOption(option).build(),
                        )
                    val credential = response.credential
                    if (
                        credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        GoogleSignInResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                    } else {
                        GoogleSignInResult.Failed
                    }
                } catch (_: GetCredentialCancellationException) {
                    GoogleSignInResult.Cancelled
                } catch (cancelled: CancellationException) {
                    complete(GoogleSignInResult.Cancelled)
                    throw cancelled
                } catch (_: Exception) {
                    GoogleSignInResult.Failed
                }
            complete(result)
        }
    }
}
