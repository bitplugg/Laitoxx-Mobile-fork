package com.laitoxx.security.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.laitoxx.security.ui.components.CategoryHeader
import com.laitoxx.security.ui.components.ToolCard
import com.laitoxx.security.ui.navigation.Screen
import com.laitoxx.security.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "LAITOXX",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Security & OSINT Toolkit",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Warning Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Educational & Authorized Testing Only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // OSINT Tools
            item {
                CategoryHeader(
                    title = "OSINT Tools",
                    icon = Icons.Default.Search
                )
            }
            item {
                ToolCard(
                    title = "IP Lookup",
                    description = "Поиск информации по IP адресу",
                    icon = Icons.Default.Public,
                    onClick = { navController.navigate(Screen.EnhancedIPInfo.route) }
                )
            }
            item {
                ToolCard(
                    title = "WHOIS Lookup",
                    description = "WHOIS/RDAP информация по IP, домену или ASN",
                    icon = Icons.Default.Info,
                    onClick = { navController.navigate(Screen.WhoisLookup.route) }
                )
            }
            item {
                ToolCard(
                    title = "Subdomain Finder",
                    description = "Discover subdomains of a target domain",
                    icon = Icons.Default.Dns,
                    onClick = { navController.navigate(Screen.SubdomainFinder.route) }
                )
            }
            item {
                ToolCard(
                    title = "Email Validator",
                    description = "Validate email address format",
                    icon = Icons.Default.Email,
                    onClick = { navController.navigate(Screen.EmailValidator.route) }
                )
            }
            item {
                ToolCard(
                    title = "Phone Lookup",
                    description = "Analyze phone number information",
                    icon = Icons.Default.Phone,
                    onClick = { navController.navigate(Screen.PhoneLookup.route) }
                )
            }
            item {
                ToolCard(
                    title = "Gmail OSINT",
                    description = "Investigate Gmail accounts",
                    icon = Icons.Default.Email,
                    onClick = { navController.navigate(Screen.GmailOsint.route) }
                )
            }
            item {
                ToolCard(
                    title = "MAC Address Lookup",
                    description = "Find manufacturer by MAC address",
                    icon = Icons.Default.DeviceHub,
                    onClick = { navController.navigate(Screen.MacLookup.route) }
                )
            }
            item {
                ToolCard(
                    title = "Username Checker",
                    description = "Check username across 29+ platforms",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate(Screen.UsernameChecker.route) }
                )
            }
            item {
                ToolCard(
                    title = "Google Dork Generator",
                    description = "Generate advanced Google search queries",
                    icon = Icons.Default.Search,
                    onClick = { navController.navigate(Screen.GoogleDork.route) }
                )
            }
            item {
                ToolCard(
                    title = "Web Crawler",
                    description = "Crawl and discover website pages",
                    icon = Icons.Default.Web,
                    onClick = { navController.navigate(Screen.WebCrawler.route) }
                )
            }

            // Network Tools
            item {
                CategoryHeader(
                    title = "Network Tools",
                    icon = Icons.Default.NetworkCheck
                )
            }
            item {
                ToolCard(
                    title = "Port Scanner",
                    description = "Scan open ports on target hosts",
                    icon = Icons.Default.Shield,
                    onClick = { navController.navigate(Screen.PortScanner.route) }
                )
            }
            item {
                ToolCard(
                    title = "DNS Lookup",
                    description = "Resolve domain names to IP addresses",
                    icon = Icons.Default.Dns,
                    onClick = { navController.navigate(Screen.DNSLookup.route) }
                )
            }
            item {
                ToolCard(
                    title = "Ping",
                    description = "Test host reachability and latency",
                    icon = Icons.Default.SignalCellularAlt,
                    onClick = { navController.navigate(Screen.Ping.route) }
                )
            }

            // Web Security
            item {
                CategoryHeader(
                    title = "Web Security",
                    icon = Icons.Default.Security
                )
            }
            item {
                ToolCard(
                    title = "URL Checker",
                    description = "Check URL status and security",
                    icon = Icons.Default.Link,
                    onClick = { navController.navigate(Screen.URLChecker.route) }
                )
            }
            item {
                ToolCard(
                    title = "Admin Finder",
                    description = "Find admin panels on websites",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = { navController.navigate(Screen.AdminFinder.route) }
                )
            }

            // Utilities
            item {
                CategoryHeader(
                    title = "Utilities",
                    icon = Icons.Default.Build
                )
            }
            item {
                ToolCard(
                    title = "Text Transformer",
                    description = "Transform text with various methods",
                    icon = Icons.Default.Transform,
                    onClick = { navController.navigate(Screen.TextTransformer.route) }
                )
            }
            item {
                ToolCard(
                    title = "Hash Generator",
                    description = "Generate MD5, SHA-1, SHA-256 hashes",
                    icon = Icons.Default.Tag,
                    onClick = { navController.navigate(Screen.HashGenerator.route) }
                )
            }
            item {
                ToolCard(
                    title = "Base64 Encoder",
                    description = "Encode and decode Base64",
                    icon = Icons.Default.Code,
                    onClick = { navController.navigate(Screen.Base64Encoder.route) }
                )
            }
            item {
                ToolCard(
                    title = "Password Generator",
                    description = "Generate secure random passwords",
                    icon = Icons.Default.Key,
                    onClick = { navController.navigate(Screen.PasswordGenerator.route) }
                )
            }
        }
    }
}
