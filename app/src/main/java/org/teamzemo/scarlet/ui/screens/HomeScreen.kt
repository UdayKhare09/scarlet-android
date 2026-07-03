package org.teamzemo.scarlet.ui.screens

import android.net.Uri
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.flow.asStateFlow
import org.teamzemo.scarlet.data.model.TotpSetupResponse
import org.teamzemo.scarlet.ui.viewmodel.AuthViewModel

private fun formatIsoTimestamp(isoString: String?): String {
    if (isoString == null) return "Unknown"
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss a", Locale.US)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        isoString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val logs by viewModel.logs.collectAsState()
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        viewModel.fetchMfaStatus()
        viewModel.fetchSessions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Text("ProjectScarlet", fontWeight = FontWeight.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Profile", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sessions", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Security", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> ProfileTab(viewModel)
                    1 -> SessionsTab(viewModel)
                    2 -> SecurityTab(viewModel)
                }
            }

            // Terminal/Console Logs Area at the bottom
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIAGNOSTIC TERMINAL LOGS",
                            color = Color.Green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "clear",
                            color = Color.Green.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable { viewModel.clearLogs() }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true
                    ) {
                        items(logs.reversed()) { log ->
                            Text(
                                text = log,
                                color = Color.Green,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(viewModel: AuthViewModel) {
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar picker
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { imageLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = profile?.profilePictureUrl?.let { url ->
                val host = android.net.Uri.parse(org.teamzemo.scarlet.Constants.BASE_URL).host ?: "10.241.197.190"
                url.replace("localhost", host).replace("127.0.0.1", host)
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = "Tap circle to change profile picture",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Button(
            onClick = { viewModel.updateProfileDetails(firstName, lastName) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Update Profile Names", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SessionsTab(viewModel: AuthViewModel) {
    val state = viewModel.state

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.sessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Box matching React's grey square
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val isMobile = session.os?.contains("Android", ignoreCase = true) == true || 
                                           session.os?.contains("iOS", ignoreCase = true) == true
                            Icon(
                                imageVector = if (isMobile) Icons.Default.Smartphone else Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            val displayName = "${session.os ?: "Unknown OS"} • ${session.browser ?: "Unknown Browser"}"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (session.current) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CURRENT",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = session.id.take(8).uppercase(),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "IP Address: ${session.ipAddress}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Logged In: ${formatIsoTimestamp(session.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Last Active: ${formatIsoTimestamp(session.lastActiveAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (!session.current) {
                        OutlinedButton(
                            onClick = { viewModel.revokeSession(session.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Revoke", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityTab(viewModel: AuthViewModel) {
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Passkey setup card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("On-Device Passkeys", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Register a passkey to sign into ProjectScarlet passwordless using biometric or device locks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Button(
                    onClick = { viewModel.registerPasskey(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register Android Passkey", fontWeight = FontWeight.Bold)
                }
            }
        }

        // MFA management card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Multi-Factor Authentication (MFA)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                // Email OTP Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Email OTP", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Receive codes in your inbox to sign in", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    val isEmailEnabled = mfa?.enabled == true && mfa.type == "EMAIL"
                    Switch(
                        checked = isEmailEnabled,
                        onCheckedChange = { viewModel.toggleEmailOtp(it) }
                    )
                }

                HorizontalDivider()

                // Authenticator App TOTP configuration
                val isTotpEnabled = mfa?.enabled == true && mfa.type == "TOTP"

                if (isTotpEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Authenticator App MFA", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Fully enabled and configured", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
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
                        // TOTP confirmation screen
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Setup Authenticator App", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("1. Enter this secret in your Authenticator app manually:", fontSize = 12.sp)
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
                                    label = { Text("Authenticator Code") },
                                    modifier = Modifier.fillMaxWidth()
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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Confirm Setup")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Change Password Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Change Password", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
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
}

// Simple SelectionContainer for secret copying
@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    Box { content() }
}
