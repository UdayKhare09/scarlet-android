package org.teamzemo.scarlet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.teamzemo.scarlet.data.model.RegisterRequest
import org.teamzemo.scarlet.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val state = viewModel.state

    Scaffold(
        containerColor = Color(0xFFF2F2F2)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6F2)),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Person Add Icon Box
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE5E1DA)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFF2D2D2D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Join Us",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2D2D2D),
                            fontSize = 28.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Register details to set up authentication profiles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // First Name Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "FIRST NAME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = { Text("John", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Last Name Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "LAST NAME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = { Text("Doe", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EMAIL ADDRESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("name@example.com", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentType = ContentType.Username
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "PASSWORD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentType = ContentType.Password
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )
                    }

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Sign Up Button
                    Button(
                        onClick = {
                            viewModel.register(
                                RegisterRequest(firstName, lastName, email, password),
                                onSuccess = onNavigateToLogin
                            )
                        },
                        enabled = !state.isLoading && firstName.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1B2E),
                            disabledContainerColor = Color(0xFF1A1B2E).copy(alpha = 0.6f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFE2FF00),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Sign Up",
                                color = Color(0xFFE2FF00),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Login Link
                    Row(
                        modifier = Modifier.clickable { onNavigateToLogin() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF2D2D2D)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back to Sign In",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                color = Color(0xFF2D2D2D)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

