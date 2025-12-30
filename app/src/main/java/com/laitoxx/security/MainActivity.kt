package com.laitoxx.security

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.laitoxx.security.ui.navigation.Screen
import com.laitoxx.security.ui.screens.*
import com.laitoxx.security.ui.theme.LAITOXXTheme
import com.laitoxx.security.utils.CrashHandler
import com.laitoxx.security.utils.CrashDialog

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity.onCreate() started")

        // Initialize crash handler
        CrashHandler.init(applicationContext)

        // Initialize Python runtime
        try {
            com.laitoxx.security.python.PythonBridge.initialize(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Python init failed", e)
            e.printStackTrace()
        }

        // Setup UI
        setContent {
            LAITOXXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }

        Log.d(TAG, "MainActivity.onCreate() finished")
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val crashInfo = CrashHandler.getCrashInfo()

    // Показываем диалог краша если есть информация
    CrashDialog(
        crashInfo = crashInfo,
        onDismiss = { CrashHandler.clearCrashInfo() }
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        // OSINT Tools
        composable(Screen.IPInfo.route) {
            IPInfoScreen(navController)
        }
        composable(Screen.EnhancedIPInfo.route) {
            EnhancedIPInfoScreen(navController)
        }
        composable(Screen.SubdomainFinder.route) {
            SubdomainFinderScreen(navController)
        }
        composable(Screen.EmailValidator.route) {
            EmailValidatorScreen(navController)
        }
        composable(Screen.PhoneLookup.route) {
            PhoneLookupScreen(navController)
        }
        composable(Screen.WhoisLookup.route) {
            WhoisLookupScreen(navController)
        }

        // Network Tools
        composable(Screen.PortScanner.route) {
            PortScannerScreen(navController)
        }
        composable(Screen.DNSLookup.route) {
            DNSLookupScreen(navController)
        }
        composable(Screen.Ping.route) {
            PingScreen(navController)
        }

        // Web Security
        composable(Screen.URLChecker.route) {
            URLCheckerScreen(navController)
        }
        composable(Screen.AdminFinder.route) {
            AdminFinderScreen(navController)
        }

        // Utilities
        composable(Screen.TextTransformer.route) {
            TextTransformerScreen(navController)
        }
        composable(Screen.HashGenerator.route) {
            HashGeneratorScreen(navController)
        }
        composable(Screen.Base64Encoder.route) {
            Base64EncoderScreen(navController)
        }
        composable(Screen.PasswordGenerator.route) {
            PasswordGeneratorScreen(navController)
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.ThemeEditor.route) {
            ThemeEditorFullScreen(navController)
        }

        // New OSINT Tools
        composable(Screen.GmailOsint.route) {
            GmailOsintScreen(navController)
        }
        composable(Screen.MacLookup.route) {
            MacLookupScreen(navController)
        }
        composable(Screen.UsernameChecker.route) {
            UsernameCheckerScreen(navController)
        }
        composable(Screen.GoogleDork.route) {
            GoogleDorkScreen(navController)
        }
        composable(Screen.WebCrawler.route) {
            WebCrawlerScreen(navController)
        }
    }
}
