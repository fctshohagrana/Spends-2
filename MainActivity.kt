package com.cardwise.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.cardwise.app.ui.navigation.CardWiseNavGraph
import com.cardwise.app.ui.theme.CardWiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var isBiometricAuthenticated by mutableStateOf(false)
    private var isBiometricEnabled by mutableStateOf(false) // Driven by DataStore/Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if biometric authentication is configured
        checkBiometricSettings()

        setContent {
            CardWiseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isBiometricEnabled && !isBiometricAuthenticated) {
                        BiometricLockScreen(onAuthenticate = { showBiometricPrompt() })
                    } else {
                        CardWiseNavGraph()
                    }
                }
            }
        }
    }

    private fun checkBiometricSettings() {
        val prefs = getSharedPreferences("cardwise_prefs", MODE_PRIVATE)
        isBiometricEnabled = prefs.getBoolean("biometric_lock_enabled", false)
        if (isBiometricEnabled) {
            showBiometricPrompt()
        } else {
            isBiometricAuthenticated = true
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isBiometricAuthenticated = true
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, "Auth Error: $errString", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock CardWise")
            .setSubtitle("Authenticate using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }
}

@Composable
fun BiometricLockScreen(onAuthenticate: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                "CardWise is Locked",
                style = MaterialTheme.typography.headlineMedium
            )
            androidx.compose.material3.Button(onClick = onAuthenticate) {
                androidx.compose.material3.Text("Unlock with Biometrics")
            }
        }
    }
}
