package org.teamzemo.scarlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.teamzemo.scarlet.data.repository.AuthRepository
import org.teamzemo.scarlet.ui.screens.HomeScreen
import org.teamzemo.scarlet.ui.screens.LoginScreen
import org.teamzemo.scarlet.ui.screens.MfaScreen
import org.teamzemo.scarlet.ui.screens.RegisterScreen
import org.teamzemo.scarlet.ui.theme.ScarletTheme
import org.teamzemo.scarlet.ui.viewmodel.AuthViewModel
import org.teamzemo.scarlet.ui.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AuthRepository(applicationContext)
        val factory = AuthViewModelFactory(repository)

        setContent {
            ScarletTheme {
                val navController = rememberNavController()
                val viewModel: AuthViewModel = viewModel(factory = factory)
                val state = viewModel.state

                LaunchedEffect(state.isLoggedIn, state.isMfaRequired) {
                    if (state.isLoggedIn) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            popUpTo("register") { inclusive = true }
                        }
                    } else if (state.isMfaRequired) {
                        navController.navigate("mfa") {
                            popUpTo("login") { inclusive = false }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (state.isLoggedIn) "home" else if (state.isMfaRequired) "mfa" else "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = {}
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }

                    composable("mfa") {
                        MfaScreen(
                            viewModel = viewModel,
                            onMfaVerified = {}
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}