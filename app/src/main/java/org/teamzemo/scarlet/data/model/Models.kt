package org.teamzemo.scarlet.data.model

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val emailVerified: Boolean,
    val mfaEnabled: Boolean,
    val mfaType: String?,
    val passkeyRegistered: Boolean
)

data class AuthResponse(
    val message: String?,
    val user: UserResponse?,
    val mfaRequired: Boolean = false,
    val tempToken: String? = null,
    val mfaType: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class CompleteMfaRequest(
    val pendingToken: String,
    val method: String,
    val code: String
)

data class TotpSetupResponse(
    val secret: String,
    val qrCodeUrl: String
)

data class UserProfile(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val role: String,
    val profilePictureUrl: String?
)

data class UserSession(
    val id: String,
    val ipAddress: String,
    val deviceDetails: String?,
    val browser: String?,
    val os: String?,
    val createdAt: String?,
    val lastActiveAt: String?,
    val current: Boolean
)

data class MfaStatusResponse(
    val enabled: Boolean,
    val type: String?,
    val secret: String?,
    val qrCodeUrl: String?
)

data class MessageResponse(
    val message: String
)
