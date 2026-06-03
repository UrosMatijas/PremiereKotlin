package rs.edu.raf.premiereuros.auth.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import org.koin.compose.koinInject
import rs.edu.raf.premiereuros.core.auth.AuthState
import rs.edu.raf.premiereuros.core.auth.SessionManager

@Composable
fun AuthLandingRoute(
    onOpenLogin: () -> Unit,
    onOpenSignup: () -> Unit,
    onAlreadyLoggedIn: () -> Unit,
    sessionManager: SessionManager = koinInject()
) {
    LaunchedEffect(sessionManager) {
        sessionManager.authStateFlow.collect { authState ->
            if (authState is AuthState.LoggedIn) {
                onAlreadyLoggedIn()
            }
        }
    }

    AuthLandingScreen(
        onOpenLogin = onOpenLogin,
        onOpenSignup = onOpenSignup
    )
}

@Composable
fun AuthLandingScreen(
    onOpenLogin: () -> Unit,
    onOpenSignup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Showtime",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Login or create an account to continue.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onOpenLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Button(
            onClick = onOpenSignup,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Sign up")
        }
    }
}
