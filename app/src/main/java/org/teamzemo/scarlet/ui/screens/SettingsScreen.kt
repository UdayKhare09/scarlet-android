package org.teamzemo.scarlet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.teamzemo.scarlet.data.model.TotpSetupResponse
import org.teamzemo.scarlet.ui.components.SessionCard
import org.teamzemo.scarlet.ui.components.SettingsCard
import org.teamzemo.scarlet.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    val mfa = viewModel.mfaStatus
    var activeTab by remember { mutableStateOf("profile") }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        viewModel.fetchMfaStatus()
        viewModel.fetchSessions()
        viewModel.fetchPasskeys()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal Tab Selector mimicking the web sidebar tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("profile", "Profile", Icons.Default.Person),
                    Triple("security", "Security", Icons.Default.Lock),
                    Triple("sessions", "Sessions", Icons.Default.List)
                ).forEach { (tabId, label, icon) ->
                    val isSelected = activeTab == tabId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { activeTab = tabId }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Tab Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "profile" -> ProfileSettingsTab(viewModel)
                    "security" -> SecuritySettingsTab(viewModel)
                    "sessions" -> SessionsSettingsTab(viewModel, onLogout)
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsTab(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val state = viewModel.state
    val profile = state.profile

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        if (profile != null) {
            firstName = profile.firstName
            lastName = profile.lastName
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val cr = context.contentResolver
                val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val mime = cr.getType(uri) ?: "image/png"
                    viewModel.uploadAvatarFile(bytes, "avatar.png", mime)
                }
            } catch (e: Exception) {
                viewModel.addLog("File read failed: ${e.localizedMessage}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsCard(
            title = "Profile Settings",
            description = "Configure your personal name and identification parameters"
        ) {
            // Avatar upload container mimicking web settings hover style
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imageLauncher.launch("image/*") }
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = profile?.profilePictureUrl?.let { url ->
                        url.replace("http://localhost:9000/", org.teamzemo.scarlet.Constants.BASE_URL)
                           .replace("http://127.0.0.1:9000/", org.teamzemo.scarlet.Constants.BASE_URL)
                    }
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = if (profile != null) {
                            "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}".uppercase()
                        } else "U"
                        Text(
                            text = initials,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tap circle to change profile picture",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.updateProfileDetails(firstName, lastName) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Profile Names", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SecuritySettingsTab(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val state = viewModel.state
    val mfa = viewModel.mfaStatus

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var totpSetupResponse by remember { mutableStateOf<TotpSetupResponse?>(null) }
    var totpCodeConfirm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Passkey Setup
        SettingsCard(
            title = "On-Device Passkeys",
            description = "Register a passkey to sign into ProjectScarlet passwordless using biometric or device locks."
        ) {
            Button(
                onClick = { viewModel.registerPasskey(context) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                    Text("Register Android Passkey", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Registered Passkeys",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (state.passkeys.isEmpty()) {
                Text(
                    text = "No on-device passkeys registered.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.passkeys.forEach { passkey ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = passkey.label ?: "Unnamed Passkey",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "ID: ${passkey.credentialId.take(16)}...",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(onClick = { viewModel.deletePasskey(passkey.credentialId) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Passkey",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Multi-Factor Authentication
        SettingsCard(
            title = "Multi-Factor Authentication (MFA)",
            description = "Add an extra layer of security to block unauthorized sessions."
        ) {
            // Email OTP Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Email OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "Receive single-use codes in your mailbox to sign in",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                val isEmailEnabled = mfa?.emailOtpEnabled == true
                Switch(
                    checked = isEmailEnabled,
                    onCheckedChange = { viewModel.toggleEmailOtp(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // TOTP Authenticator app Row
            val isTotpEnabled = mfa?.totpConfirmed == true
            if (isTotpEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Authenticator App MFA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Fully enabled and configured", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Button(
                        onClick = { viewModel.disableTotp() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Disable")
                    }
                }
            } else {
                if (totpSetupResponse == null) {
                    Button(
                        onClick = {
                            viewModel.startTotpSetup { res ->
                                totpSetupResponse = res
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enable Authenticator App")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Setup Authenticator App", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("1. Scan this QR Code or enter the secret below in your Authenticator app:", fontSize = 12.sp)
                            
                            val qrBitmap = remember(totpSetupResponse) {
                                totpSetupResponse?.qrCodeBase64?.let { decodeBase64ToBitmap(it) }
                            }
                            if (qrBitmap != null) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "TOTP QR Code",
                                        modifier = Modifier.size(180.dp)
                                    )
                                }
                            }

                            SelectionContainer {
                                Text(
                                    text = totpSetupResponse?.secret ?: "",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text("2. Enter the code generated by your app below:", fontSize = 12.sp)

                            OutlinedTextField(
                                value = totpCodeConfirm,
                                onValueChange = { totpCodeConfirm = it },
                                label = { Text("Verification Code") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    totpCodeConfirm.toIntOrNull()?.let { codeVal ->
                                        viewModel.confirmTotpSetup(codeVal) {
                                            totpSetupResponse = null
                                            totpCodeConfirm = ""
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirm Setup")
                            }
                        }
                    }
                }
            }
        }

        // Change Password Card
        SettingsCard(
            title = "Change Password",
            description = "Update your account's entry password credentials"
        ) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    viewModel.changePassword(currentPassword, newPassword)
                    currentPassword = ""
                    newPassword = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Change Password", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SessionsSettingsTab(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val state = viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Active Sessions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            if (state.sessions.isEmpty()) {
                Text(
                    text = "No active sessions found.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                state.sessions.forEach { session ->
                    SessionCard(
                        session = session,
                        onRevoke = { viewModel.revokeSession(session.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign out button
        Button(
            onClick = { viewModel.logout(onLogout) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                Text("Sign Out Current Session", fontWeight = FontWeight.Bold)
            }
    }
}
}

private fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    return try {
        val cleanString = if (base64Str.startsWith("data:image/")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val decodedBytes = Base64.decode(cleanString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

