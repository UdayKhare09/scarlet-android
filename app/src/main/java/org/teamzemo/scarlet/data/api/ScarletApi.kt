package org.teamzemo.scarlet.data.api

import okhttp3.MultipartBody
import org.teamzemo.scarlet.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ScarletApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @GET("api/auth/me")
    suspend fun getAuthMe(): Response<UserResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>

    @GET("api/auth/sessions")
    suspend fun getSessions(): Response<List<UserSession>>

    @DELETE("api/auth/sessions/{id}")
    suspend fun deleteSession(@Path("id") id: String): Response<MessageResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String): Response<AuthResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Query("token") token: String,
        @Query("newPassword") newPassword: String
    ): Response<AuthResponse>

    // Profile Service
    @GET("api/users/me")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: Map<String, String>): Response<UserProfile>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<UserProfile>

    // WebAuthn Passkeys Options & Verification
    @GET("api/webauthn/register/options")
    suspend fun getPasskeyRegisterOptions(): Response<Map<String, Any>>

    @POST("api/webauthn/register")
    suspend fun registerPasskey(@Body response: @JvmSuppressWildcards Map<String, Any>): Response<Map<String, Any>>

    @GET("api/webauthn/authenticate/options")
    suspend fun getPasskeyAuthOptions(@Query("email") email: String?): Response<Map<String, Any>>

    @POST("api/webauthn/authenticate")
    suspend fun authenticatePasskey(@Body response: @JvmSuppressWildcards Map<String, Any>): Response<Map<String, Any>>

    @GET("api/webauthn/passkeys")
    suspend fun getPasskeys(): Response<List<PasskeyResponse>>

    @DELETE("api/webauthn/passkeys/{credentialId}")
    suspend fun deletePasskey(@Path("credentialId") credentialId: String): Response<MessageResponse>


    // MFA Management
    @GET("api/mfa/status")
    suspend fun getMfaStatus(): Response<MfaStatusResponse>

    @POST("api/mfa/email-otp/enable")
    suspend fun enableEmailOtp(): Response<AuthResponse>

    @POST("api/mfa/email-otp/disable")
    suspend fun disableEmailOtp(): Response<AuthResponse>

    @POST("api/mfa/totp/setup")
    suspend fun setupTotp(): Response<TotpSetupResponse>

    @POST("api/mfa/totp/confirm")
    suspend fun confirmTotp(@Query("code") code: Int): Response<AuthResponse>

    @POST("api/mfa/totp/disable")
    suspend fun disableTotp(): Response<AuthResponse>

    @POST("api/mfa/send-email-otp")
    suspend fun sendEmailOtp(@Query("pendingToken") pendingToken: String): Response<AuthResponse>

    @POST("api/mfa/complete")
    suspend fun completeMfa(@Body request: CompleteMfaRequest): Response<AuthResponse>
}
