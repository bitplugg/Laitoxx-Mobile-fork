package com.laitoxx.security.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.laitoxx.security.ui.components.*
import com.laitoxx.security.ui.theme.*
import com.laitoxx.security.utils.TextUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(navController: NavController) {
    var length by remember { mutableStateOf(16f) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Length: ${length.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentRed,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = length,
                            onValueChange = { length = it },
                            valueRange = 8f..32f,
                            steps = 23,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentRed,
                                activeTrackColor = AccentRed,
                                inactiveTrackColor = LightGray
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Character Types",
                            style = MaterialTheme.typography.titleMedium,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeUppercase,
                                onCheckedChange = { includeUppercase = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentRed,
                                    uncheckedColor = LightGray
                                )
                            )
                            Text("Uppercase (A-Z)", color = DarkOnSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeLowercase,
                                onCheckedChange = { includeLowercase = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentRed,
                                    uncheckedColor = LightGray
                                )
                            )
                            Text("Lowercase (a-z)", color = DarkOnSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeNumbers,
                                onCheckedChange = { includeNumbers = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentRed,
                                    uncheckedColor = LightGray
                                )
                            )
                            Text("Numbers (0-9)", color = DarkOnSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeSymbols,
                                onCheckedChange = { includeSymbols = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentRed,
                                    uncheckedColor = LightGray
                                )
                            )
                            Text("Symbols (!@#$...)", color = DarkOnSurface)
                        }
                    }
                }
            }

            item {
                ActionButton(
                    text = "Generate Password",
                    onClick = {
                        generatedPassword = TextUtils.generatePassword(
                            length.toInt(),
                            includeUppercase,
                            includeLowercase,
                            includeNumbers,
                            includeSymbols
                        )
                    },
                    icon = Icons.Default.Refresh
                )
            }

            if (generatedPassword.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Generated Password",
                                style = MaterialTheme.typography.titleMedium,
                                color = Green,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                generatedPassword,
                                style = MaterialTheme.typography.headlineSmall,
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedPassword))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentRed
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy to Clipboard")
                            }
                        }
                    }
                }
            }
        }
    }
}
