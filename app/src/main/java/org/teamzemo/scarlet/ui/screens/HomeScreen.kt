package org.teamzemo.scarlet.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.teamzemo.scarlet.ui.components.AccountPopup
import org.teamzemo.scarlet.ui.components.ScarletTopBar
import org.teamzemo.scarlet.ui.viewmodel.AuthViewModel

// Custom modifier to draw a dashed border around cards to match the web design
fun Modifier.dashedBorder(
    width: Float = 2f,
    color: Color = Color.LightGray,
    cornerRadius: Float = 48f,
    dashLength: Float = 16f,
    gapLength: Float = 12f
): Modifier = this.drawWithContent {
    drawContent()
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = size.let { androidx.compose.ui.geometry.Rect(0f, 0f, it.width, it.height) },
                radiusX = cornerRadius,
                radiusY = cornerRadius
            )
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = width,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    var showAccountPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    Scaffold(
        topBar = {
            ScarletTopBar(
                profile = state.profile,
                onAvatarClick = { showAccountPopup = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main clean card mimicking web homepage body
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .dashedBorder(
                        width = 2.5f,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                        cornerRadius = 60f,
                        dashLength = 14f,
                        gapLength = 10f
                    )
                    .background(Color.Transparent)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // User Check Icon Box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .dashedBorder(
                            width = 2f,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            cornerRadius = 120f
                        )
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to /home",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This page is empty for now. Click your profile picture in the top right to configure your security profiles, update details, or manage credentials.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Animated Google-style account dialog popup overlay
        if (showAccountPopup) {
            AccountPopup(
                profile = state.profile,
                onDismiss = { showAccountPopup = false },
                onNavigateToSettings = onNavigateToSettings,
                onLogout = { viewModel.logout(onLogout) }
            )
        }
    }
}
