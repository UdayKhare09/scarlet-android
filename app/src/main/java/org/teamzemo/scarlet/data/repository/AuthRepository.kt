package org.teamzemo.scarlet.data.repository

import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.teamzemo.scarlet.data.api.ScarletApi
import org.teamzemo.scarlet.data.model.*
import org.teamzemo.scarlet.data.network.ScarletClient
import retrofit2.Response

class AuthRepository(private val context: Context) {
    private val api: ScarletApi
        get() = ScarletClient.getApi(context)

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }

    suspend fun googleLogin(idToken: String): Response<AuthResponse> {
        return api.googleLogin(GoogleLoginRequest(idToken))
    }

    suspend fun logout(): Response<MessageResponse> {
        return try {
            api.logout()
        } finally {
            ScarletClient.cookieJarInstance?.clear()
        }
    }

    suspend fun getAuthMe(): Response<UserResponse> {
        return api.getAuthMe()
    }

    suspend fun changePassword(request: ChangePasswordRequest): Response<AuthResponse> {
        return api.changePassword(request)
    }

    suspend fun getSessions(): Response<List<UserSession>> {
        return api.getSessions()
    }

    suspend fun deleteSession(id: String): Response<MessageResponse> {
        return api.deleteSession(id)
    }

    suspend fun forgotPassword(email: String): Response<AuthResponse> {
        return api.forgotPassword(email)
    }

    suspend fun resetPassword(token: String, newPassword: String): Response<AuthResponse> {
        return api.resetPassword(token, newPassword)
    }

    // User Profile
    suspend fun getUserProfile(): Response<UserProfile> {
        return api.getUserProfile()
    }

    suspend fun updateProfile(firstName: String, lastName: String): Response<UserProfile> {
        return api.updateProfile(mapOf("firstName" to firstName, "lastName" to lastName))
    }

    suspend fun uploadAvatar(bytes: ByteArray, fileName: String, contentType: String): Response<UserProfile> {
        val requestFile = bytes.toRequestBody(contentType.toMediaTypeOrNull(), 0, bytes.size)
        val body = MultipartBody.Part.createFormData("file", fileName, requestFile)
        return api.uploadAvatar(body)
    }

    // WebAuthn Passkeys Options
    suspend fun getPasskeyRegisterOptions(): Response<Map<String, Any>> {
        return api.getPasskeyRegisterOptions()
    }

    suspend fun registerPasskey(response: Map<String, Any>): Response<Map<String, Any>> {
        return api.registerPasskey(response)
    }

    suspend fun getPasskeyAuthOptions(email: String?): Response<Map<String, Any>> {
        return api.getPasskeyAuthOptions(email)
    }

    suspend fun authenticatePasskey(response: Map<String, Any>): Response<Map<String, Any>> {
        return api.authenticatePasskey(response)
    }

    // MFA Methods
    suspend fun getMfaStatus(): Response<MfaStatusResponse> = api.getMfaStatus()
    suspend fun enableEmailOtp(): Response<AuthResponse> = api.enableEmailOtp()
    suspend fun disableEmailOtp(): Response<AuthResponse> = api.disableEmailOtp()
    suspend fun setupTotp(): Response<TotpSetupResponse> = api.setupTotp()
    suspend fun confirmTotp(code: Int): Response<AuthResponse> = api.confirmTotp(code)
    suspend fun disableTotp(): Response<AuthResponse> = api.disableTotp()
    suspend fun sendEmailOtp(pendingToken: String): Response<AuthResponse> = api.sendEmailOtp(pendingToken)
    suspend fun completeMfa(pendingToken: String, method: String, code: String): Response<AuthResponse> {
        return api.completeMfa(CompleteMfaRequest(pendingToken, method, code))
    }

    suspend fun getPasskeys(): Response<List<PasskeyResponse>> = api.getPasskeys()
    suspend fun deletePasskey(credentialId: String): Response<MessageResponse> = api.deletePasskey(credentialId)
}
