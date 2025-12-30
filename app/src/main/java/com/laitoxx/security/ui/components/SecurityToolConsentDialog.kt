package com.laitoxx.security.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Security Tool Consent Dialog
 *
 * CRITICAL: This dialog MUST be shown before executing potentially dangerous security tools
 * such as SQL injection scanners, XSS scanners, and directory fuzzers.
 *
 * Legal Compliance:
 * - Computer Fraud and Abuse Act (CFAA) compliance
 * - Unauthorized access prevention
 * - User responsibility acknowledgment
 * - Google Play Store policy compliance
 *
 * @param toolName Name of the security tool being used
 * @param targetUrl Target URL that will be scanned
 * @param onConfirm Callback when user accepts responsibility
 * @param onDismiss Callback when user declines
 */
@Composable
fun SecurityToolConsentDialog(
    toolName: String,
    targetUrl: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var hasReadWarning by remember { mutableStateOf(false) }
    var hasAcceptedTerms by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Warning Icon and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "⚠️ LEGAL WARNING",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Security Tool: $toolName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable warning content
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Target:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = targetUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "⚖️ LEGAL NOTICE",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = """
                                Unauthorized security scanning is ILLEGAL and may violate:

                                • Computer Fraud and Abuse Act (CFAA) - USA
                                • Computer Misuse Act - UK
                                • Council of Europe Convention on Cybercrime
                                • Local cybercrime laws in your jurisdiction

                                PENALTIES may include:
                                • Criminal prosecution
                                • Fines up to $250,000 USD
                                • Imprisonment up to 20 years
                                • Civil liability for damages

                                ✅ AUTHORIZED USE ONLY:
                                • Your own websites/applications
                                • Systems with written permission
                                • Authorized penetration testing engagements
                                • Educational environments with consent
                                • Bug bounty programs with proper authorization

                                ❌ PROHIBITED:
                                • Scanning third-party websites without permission
                                • Unauthorized vulnerability testing
                                • Exploiting discovered vulnerabilities
                                • Using this tool for malicious purposes

                                📋 YOUR RESPONSIBILITIES:
                                • Obtain written authorization before scanning
                                • Document your authorization
                                • Use this tool ethically and legally
                                • Immediately stop if not authorized
                                • Report findings responsibly

                                ⚠️ DISCLAIMER:
                                The developers of Laitoxx are NOT responsible for:
                                • Misuse of this tool
                                • Legal consequences of unauthorized use
                                • Damages caused by improper use
                                • Your actions or their consequences

                                By proceeding, you acknowledge that:
                                1. You have legal authorization to scan the target
                                2. You understand the legal risks
                                3. You accept full responsibility for your actions
                                4. You will use this tool ethically and legally
                                5. Developers bear NO liability for your actions
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkboxes for consent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasReadWarning,
                        onCheckedChange = { hasReadWarning = it }
                    )
                    Text(
                        text = "I have read and understood the warning above",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasAcceptedTerms,
                        onCheckedChange = { hasAcceptedTerms = it }
                    )
                    Text(
                        text = "I have legal authorization to scan this target",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = hasReadWarning && hasAcceptedTerms,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("I Accept Responsibility")
                    }
                }
            }
        }
    }
}

/**
 * Helper function to determine if a tool requires consent
 */
fun requiresSecurityConsent(toolName: String): Boolean {
    val dangerousTools = setOf(
        "SQL Injection Scanner",
        "XSS Scanner",
        "Directory Fuzzer",
        "Security Headers",
        "Admin Finder",
        "Web Crawler Enhanced"
    )
    return dangerousTools.contains(toolName)
}
