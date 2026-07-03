package org.teamzemo.scarlet.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.teamzemo.scarlet.Constants
import org.teamzemo.scarlet.data.model.*
import org.teamzemo.scarlet.data.repository.AuthRepository
import java.security.SecureRandom
import android.util.Base64

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val gson = Gson()
    private val TAG = "AuthViewModel"

    var state by mutableStateOf(AuthState())
        private set

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    init {
        checkSession()
    }

    fun addLog(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _logs.value = _logs.value + "[$time] $msg"
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun checkSession() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val response = repository.getAuthMe()
                if (response.isSuccessful && response.body() != null) {
                    val userRes = response.body()!!
                    state = state.copy(user = userRes, isLoggedIn = true)
                    addLog("Session active for ${userRes.email}")
                    fetchUserProfile()
                    fetchSessions()
                } else {
                    state = state.copy(isLoggedIn = false, user = null)
                    addLog("No active session found")
                }
            } catch (e: Exception) {
                state = state.copy(isLoggedIn = false, error = e.localizedMessage)
                addLog("Session check failed: ${e.localizedMessage}")
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val response = repository.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    state = state.copy(profile = response.body())
                    addLog("Loaded profile for ${response.body()?.fullName}")
                }
            } catch (e: Exception) {
                addLog("Failed to fetch profile: ${e.localizedMessage}")
            }
        }
    }

    fun fetchSessions() {
        viewModelScope.launch {
            try {
                val response = repository.getSessions()
                if (response.isSuccessful && response.body() != null) {
                    state = state.copy(sessions = response.body()!!)
                    addLog("Fetched active sessions (${response.body()?.size})")
                }
            } catch (e: Exception) {
                addLog("Failed to fetch sessions: ${e.localizedMessage}")
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            addLog("Attempting password login for $email...")
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    addLog("Login response: ${body.message}")
                    if (body.mfaRequired) {
                        state = state.copy(
                            isMfaRequired = true,
                            mfaTempToken = body.tempToken,
                            mfaType = body.mfaType
                        )
                        addLog("MFA Required (${body.mfaType}). Please enter MFA code.")
                    } else {
                        state = state.copy(user = body.user, isLoggedIn = true)
                        addLog("Login success. Session created.")
                        fetchUserProfile()
                        fetchSessions()
                        onSuccess()
                    }
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Login failed"
                    state = state.copy(error = errMsg)
                    addLog("Login failed: $errMsg")
                }
            } catch (e: Exception) {
                state = state.copy(error = e.localizedMessage)
                addLog("Network error: ${e.localizedMessage}")
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun loginWithGoogle(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            addLog("Triggering Credential Manager Google Sign-In...")
            try {
                val credentialManager = CredentialManager.create(context)
                val nonce = generateSecureRandomNonce()
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(Constants.GOOGLE_CLIENT_ID)
                    .setNonce(nonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential && 
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleCredential.idToken
                    
                    addLog("Received Google ID Token. Authenticating with backend...")
                    val authResponse = repository.googleLogin(idToken)
                    
                    if (authResponse.isSuccessful && authResponse.body() != null) {
                        val body = authResponse.body()!!
                        state = state.copy(user = body.user, isLoggedIn = true)
                        addLog("Google Login Success.")
                        fetchUserProfile()
                        fetchSessions()
                        onSuccess()
                    } else {
                        val errorMsg = authResponse.errorBody()?.string() ?: "Backend Google login failed"
                        state = state.copy(error = errorMsg)
                        addLog("Google Auth Failed: $errorMsg")
                    }
                } else {
                    state = state.copy(error = "Unexpected credential type")
                    addLog("Error: Unexpected Google credential type")
                }
            } catch (e: Exception) {
                state = state.copy(error = e.localizedMessage)
                addLog("Google Login Error: ${e.localizedMessage}")
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun loginWithPasskey(context: Context, email: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            addLog("Fetching WebAuthn authentication options...")
            try {
                val optionsRes = repository.getPasskeyAuthOptions(email)
                if (!optionsRes.isSuccessful || optionsRes.body() == null) {
                    val err = optionsRes.errorBody()?.string() ?: "Failed to get auth options"
                    state = state.copy(error = err)
                    addLog("Passkey Auth Options Failed: $err")
                    return@launch
                }

                val optionsMap = optionsRes.body()!!
                val optionsJson = gson.toJson(optionsMap)
                addLog("Invoking Credential Manager Passkey Get flow...")

                val credentialManager = CredentialManager.create(context)
                val getPublicKeyOption = GetPublicKeyCredentialOption(optionsJson)

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(getPublicKeyOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == "androidx.credentials.TYPE_PUBLIC_KEY_CREDENTIAL") {
                    
                    val responseJson = credential.data.getString("androidx.credentials.BUNDLE_KEY_SUBTYPE_PUBLIC_KEY_CREDENTIAL_RESPONSE")!!
                    addLog("Received passkey assertion. Authenticating with backend...")
                    
                    val credentialMap = gson.fromJson(responseJson, Map::class.java) as Map<String, Any>
                    val verifyRes = repository.authenticatePasskey(credentialMap)
                    
                    if (verifyRes.isSuccessful) {
                        addLog("Passkey Authentication successful!")
                        checkSession()
                        onSuccess()
                    } else {
                        val verifyErr = verifyRes.errorBody()?.string() ?: "Verification failed"
                        state = state.copy(error = verifyErr)
                        addLog("Passkey Verification Failed: $verifyErr")
                    }
                } else {
                    state = state.copy(error = "Invalid passkey credential format")
                    addLog("Error: Unexpected passkey type")
                }
            } catch (e: Exception) {
                state = state.copy(error = e.localizedMessage)
                addLog("Passkey Login Failed: ${e.localizedMessage}")
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun registerPasskey(context: Context) {
        viewModelScope.launch {
            addLog("Fetching WebAuthn registration options...")
            try {
                val optionsRes = repository.getPasskeyRegisterOptions()
                if (!optionsRes.isSuccessful || optionsRes.body() == null) {
                    val err = optionsRes.errorBody()?.string() ?: "Failed to get registration options"
                    addLog("Passkey Register Options Failed: $err")
                    return@launch
                }

                val optionsMap = optionsRes.body()!!
                val optionsJson = gson.toJson(optionsMap)
                addLog("Invoking Credential Manager Passkey Creation flow...")

                val credentialManager = CredentialManager.create(context)
                val request = CreatePublicKeyCredentialRequest(optionsJson)

                val result = credentialManager.createCredential(context, request)
                val responseJson = result.data.getString("androidx.credentials.BUNDLE_KEY_SUBTYPE_PUBLIC_KEY_CREDENTIAL_RESPONSE")!!
                addLog("Received passkey registration response. Registering on backend...")

                val responseMap = gson.fromJson(responseJson, Map::class.java) as Map<String, Any>
                
                // Add default label
                val finalMap = responseMap.toMutableMap().apply {
                    put("label", "Android Device Passkey (${android.os.Build.MODEL})")
                }

                val verifyRes = repository.registerPasskey(finalMap)
                if (verifyRes.isSuccessful) {
                    addLog("Passkey registered and saved successfully!")
                    checkSession()
                } else {
                    val err = verifyRes.errorBody()?.string() ?: "Registration failed"
                    addLog("Backend Passkey Registration Failed: $err")
                }
            } catch (e: Exception) {
                addLog("Passkey Creation Failed: ${e.localizedMessage}")
            }
        }
    }

    fun register(request: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            addLog("Creating account for ${request.email}...")
            try {
                val response = repository.register(request)
                if (response.isSuccessful && response.body() != null) {
                    addLog("Account registration successful! ${response.body()?.message}")
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string() ?: "Registration failed"
                    state = state.copy(error = err)
                    addLog("Registration Failed: $err")
                }
            } catch (e: Exception) {
                state = state.copy(error = e.localizedMessage)
                addLog("Network error: ${e.localizedMessage}")
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            addLog("Logging out from application...")
            try {
                repository.logout()
                state = AuthState()
                addLog("Logged out successfully.")
                onSuccess()
            } catch (e: Exception) {
                addLog("Logout network call failed, clearing local session.")
                state = AuthState()
                onSuccess()
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            addLog("Changing account password...")
            try {
                val response = repository.changePassword(ChangePasswordRequest(current, new))
                if (response.isSuccessful) {
                    addLog("Password changed successfully!")
                } else {
                    val err = response.errorBody()?.string() ?: "Password change failed"
                    addLog("Password Change Failed: $err")
                }
            } catch (e: Exception) {
                addLog("Password Change Error: ${e.localizedMessage}")
            }
        }
    }

    fun updateProfileDetails(first: String, last: String) {
        viewModelScope.launch {
            addLog("Updating user profile names to: $first $last...")
            try {
                val response = repository.updateProfile(first, last)
                if (response.isSuccessful && response.body() != null) {
                    state = state.copy(profile = response.body())
                    addLog("Profile updated successfully.")
                    checkSession()
                } else {
                    addLog("Profile update failed.")
                }
            } catch (e: Exception) {
                addLog("Profile update error: ${e.localizedMessage}")
            }
        }
    }

    fun uploadAvatarFile(bytes: ByteArray, fileName: String, contentType: String) {
        viewModelScope.launch {
            addLog("Uploading avatar file (${bytes.size} bytes)...")
            try {
                val response = repository.uploadAvatar(bytes, fileName, contentType)
                if (response.isSuccessful && response.body() != null) {
                    state = state.copy(profile = response.body())
                    addLog("Avatar image uploaded successfully to MinIO S3!")
                } else {
                    val err = response.errorBody()?.string() ?: "Avatar upload failed"
                    addLog("Avatar Upload Failed: $err")
                }
            } catch (e: Exception) {
                addLog("Avatar Upload Error: ${e.localizedMessage}")
            }
        }
    }

    fun revokeSession(id: String) {
        viewModelScope.launch {
            addLog("Revoking remote session ID: $id...")
            try {
                val response = repository.deleteSession(id)
                if (response.isSuccessful) {
                    addLog("Session revoked.")
                    fetchSessions()
                } else {
                    addLog("Failed to revoke session.")
                }
            } catch (e: Exception) {
                addLog("Revoke session error: ${e.localizedMessage}")
            }
        }
    }

    fun sendEmailOtpCode() {
        val token = state.mfaTempToken ?: return
        viewModelScope.launch {
            addLog("Requesting email OTP code...")
            try {
                val response = repository.sendEmailOtp(token)
                if (response.isSuccessful) {
                    addLog("OTP code sent to email successfully.")
                } else {
                    addLog("Failed to send OTP code: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                addLog("Failed to send OTP code: ${e.localizedMessage}")
            }
        }
    }

    fun verifyMfaCode(code: String, onSuccess: () -> Unit) {
        val token = state.mfaTempToken ?: return
        val method = state.mfaType ?: return
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            addLog("Submitting MFA code ($code) via $method...")
            try {
                val response = repository.completeMfa(token, method, code)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    state = state.copy(
                        user = body.user,
                        isLoggedIn = true,
                        isMfaRequired = false,
                        mfaTempToken = null,
                        mfaType = null
                    )
                    addLog("MFA verification successful! Session established.")
                    fetchUserProfile()
                    fetchSessions()
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string() ?: "MFA verification failed"
                    state = state.copy(error = err)
                    addLog("MFA Verification Failed: $err")
                }
            } catch (e: Exception) {
                state = state.copy(error = e.localizedMessage)
                addLog("MFA Verification Error: ${e.localizedMessage}")
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    var mfaStatus by mutableStateOf<MfaStatusResponse?>(null)
        private set

    fun fetchMfaStatus() {
        viewModelScope.launch {
            try {
                val response = repository.getMfaStatus()
                if (response.isSuccessful && response.body() != null) {
                    mfaStatus = response.body()
                    addLog("Fetched MFA Status (Enabled: ${mfaStatus?.enabled}, Type: ${mfaStatus?.type})")
                }
            } catch (e: Exception) {
                addLog("Failed to fetch MFA status: ${e.localizedMessage}")
            }
        }
    }

    fun toggleEmailOtp(enable: Boolean) {
        viewModelScope.launch {
            try {
                val response = if (enable) repository.enableEmailOtp() else repository.disableEmailOtp()
                if (response.isSuccessful) {
                    addLog("Email OTP ${if (enable) "enabled" else "disabled"} successfully.")
                    fetchMfaStatus()
                } else {
                    addLog("Failed to toggle Email OTP: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                addLog("Email OTP toggle error: ${e.localizedMessage}")
            }
        }
    }

    fun startTotpSetup(onSetupReady: (TotpSetupResponse) -> Unit) {
        viewModelScope.launch {
            try {
                addLog("Initiating TOTP setup on backend...")
                val response = repository.setupTotp()
                if (response.isSuccessful && response.body() != null) {
                    onSetupReady(response.body()!!)
                } else {
                    addLog("Failed to initiate TOTP setup: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                addLog("TOTP setup error: ${e.localizedMessage}")
            }
        }
    }

    fun confirmTotpSetup(code: Int, onConfirmSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                addLog("Confirming TOTP setup with code: $code...")
                val response = repository.confirmTotp(code)
                if (response.isSuccessful) {
                    addLog("Authenticator app connected and confirmed successfully!")
                    fetchMfaStatus()
                    onConfirmSuccess()
                } else {
                    addLog("Failed to confirm TOTP setup: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                addLog("TOTP confirmation error: ${e.localizedMessage}")
            }
        }
    }

    fun disableTotp() {
        viewModelScope.launch {
            try {
                addLog("Disabling TOTP Authenticator...")
                val response = repository.disableTotp()
                if (response.isSuccessful) {
                    addLog("TOTP disabled successfully.")
                    fetchMfaStatus()
                } else {
                    addLog("Failed to disable TOTP: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                addLog("Disable TOTP error: ${e.localizedMessage}")
            }
        }
    }

    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: UserResponse? = null,
    val profile: UserProfile? = null,
    val sessions: List<UserSession> = emptyList(),
    val error: String? = null,
    val isMfaRequired: Boolean = false,
    val mfaTempToken: String? = null,
    val mfaType: String? = null
)

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
