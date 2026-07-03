package org.teamzemo.scarlet.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import org.teamzemo.scarlet.data.model.UserProfile
import org.teamzemo.scarlet.ui.theme.SilkBase100
import org.teamzemo.scarlet.ui.theme.SilkBase200
import org.teamzemo.scarlet.ui.theme.SilkPrimary
import org.teamzemo.scarlet.ui.theme.SilkPrimaryContent

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AccountPopup(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Semi-transparent overlay backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.TopCenter
        ) {
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -150 }) + fadeIn() + scaleIn(initialScale = 0.92f),
                exit = slideOutVertically(targetOffsetY = { -150 }) + fadeOut() + scaleOut(targetScale = 0.92f),
                modifier = Modifier.padding(top = 64.dp, start = 16.dp, end = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}, // Prevent dismiss click from propagating
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header: Close button and Brand Logo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(0.2f))
                            
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)) {
                                        append("Project")
                                    }
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = SilkPrimary)) {
                                        append("Scarlet")
                                    }
                                },
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.weight(0.2f))
                            Spacer(modifier = Modifier.width(36.dp)) // Equalizer space for balancing the Close button
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Inner Card containing Profile info (mimicking Google Account switcher)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = SilkBase200),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Large Profile Photo
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val imageUrl = profile?.profilePictureUrl?.let { url ->
                                        url.replace("http://localhost:9000/", org.teamzemo.scarlet.Constants.BASE_URL)
                                           .replace("http://127.0.0.1:9000/", org.teamzemo.scarlet.Constants.BASE_URL)
                                    }
                                    if (imageUrl != null) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Large Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val initials = if (profile != null) {
                                            "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}".uppercase()
                                        } else "U"
                                        Text(
                                            text = initials,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Name
                                Text(
                                    text = profile?.fullName ?: "Uday Kiran",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Email
                                Text(
                                    text = profile?.email ?: "uday@example.com",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // "Manage your Account" outline button
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onNavigateToSettings()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Manage your Account",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Options
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Settings Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onDismiss()
                                        onNavigateToSettings()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Account Settings",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Sign Out Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onDismiss()
                                        onLogout()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Sign Out Session",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer links
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Privacy Policy",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "Terms of Service",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
