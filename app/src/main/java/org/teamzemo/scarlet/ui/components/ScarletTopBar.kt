package org.teamzemo.scarlet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.teamzemo.scarlet.data.model.UserProfile
import org.teamzemo.scarlet.ui.theme.SilkPrimary
import org.teamzemo.scarlet.ui.theme.SilkPrimaryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScarletTopBar(
    profile: UserProfile?,
    onAvatarClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Branded S Box
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = SilkPrimaryContent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                
                // Branded Title text
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)) {
                            append("Project")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = SilkPrimary)) {
                            append("Scarlet")
                        }
                    },
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
            }
        },
        navigationIcon = navigationIcon ?: {},
        actions = {
            // Profile Avatar Button on the right
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onAvatarClick() },
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
