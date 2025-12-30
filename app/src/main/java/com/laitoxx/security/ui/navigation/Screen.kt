package com.laitoxx.security.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Tools : Screen("tools")
    object Settings : Screen("settings")
    object ThemeEditor : Screen("theme_editor")

    // OSINT Tools
    object IPInfo : Screen("ip_info")
    object EnhancedIPInfo : Screen("enhanced_ip_info")
    object SubdomainFinder : Screen("subdomain_finder")
    object EmailValidator : Screen("email_validator")
    object PhoneLookup : Screen("phone_lookup")
    object GmailOsint : Screen("gmail_osint")
    object MacLookup : Screen("mac_lookup")
    object WhoisLookup : Screen("whois_lookup")
    object UsernameChecker : Screen("username_checker")
    object GoogleDork : Screen("google_dork")
    object WebCrawler : Screen("web_crawler")

    // Network Tools
    object PortScanner : Screen("port_scanner")
    object DNSLookup : Screen("dns_lookup")
    object Ping : Screen("ping")

    // Web Security
    object URLChecker : Screen("url_checker")
    object AdminFinder : Screen("admin_finder")

    // Utilities
    object TextTransformer : Screen("text_transformer")
    object HashGenerator : Screen("hash_generator")
    object Base64Encoder : Screen("base64_encoder")
    object PasswordGenerator : Screen("password_generator")
}
