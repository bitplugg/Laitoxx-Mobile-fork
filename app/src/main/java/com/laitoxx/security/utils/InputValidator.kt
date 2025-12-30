package com.laitoxx.security.utils

import android.util.Log
import java.net.IDN

/**
 * Input validation and sanitization utilities for security
 *
 * SECURITY: Protection against injection attacks, SSRF, and malformed input
 * OWASP Mobile Top 10 - M7: Poor Code Quality
 */
object InputValidator {

    private const val TAG = "InputValidator"

    // RFC 1035: Maximum domain name length
    private const val MAX_DOMAIN_LENGTH = 253

    // RFC 5321: Maximum email length
    private const val MAX_EMAIL_LENGTH = 320

    // Reasonable limits for security
    private const val MAX_PHONE_LENGTH = 20
    private const val MAX_IP_LENGTH = 45  // IPv6 max length

    /**
     * Sanitize and validate domain name
     *
     * @param input Raw domain input from user
     * @return Sanitized domain or null if invalid
     */
    fun sanitizeDomain(input: String): String? {
        return try {
            val cleaned = input.trim()
                .lowercase()
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .split("/")[0]
                .split(":")[0]  // Remove port if present
                .takeIf { it.isNotBlank() }
                ?: return null

            // Check length constraints (RFC 1035)
            if (cleaned.length > MAX_DOMAIN_LENGTH) {
                Log.w(TAG, "Domain exceeds maximum length: ${cleaned.length}")
                return null
            }

            // Validate domain format: only alphanumeric, dots, and hyphens
            if (!cleaned.matches(Regex("^[a-zA-Z0-9.-]+$"))) {
                Log.w(TAG, "Domain contains invalid characters: $cleaned")
                return null
            }

            // Check for consecutive dots
            if (cleaned.contains("..")) {
                Log.w(TAG, "Domain contains consecutive dots: $cleaned")
                return null
            }

            // Check if starts or ends with dot or hyphen
            if (cleaned.startsWith(".") || cleaned.endsWith(".") ||
                cleaned.startsWith("-") || cleaned.endsWith("-")) {
                Log.w(TAG, "Domain has invalid start/end character: $cleaned")
                return null
            }

            // Validate domain has at least one dot (TLD required)
            if (!cleaned.contains(".")) {
                Log.w(TAG, "Domain missing TLD: $cleaned")
                return null
            }

            // Convert to ASCII (handles internationalized domains)
            try {
                IDN.toASCII(cleaned)
            } catch (e: Exception) {
                Log.w(TAG, "Invalid internationalized domain: $cleaned", e)
                return null
            }

            cleaned
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing domain", e)
            null
        }
    }

    /**
     * Validate IP address (both IPv4 and IPv6)
     *
     * @param ip IP address string
     * @return true if valid IP address
     */
    fun isValidIP(ip: String): Boolean {
        if (ip.isBlank() || ip.length > MAX_IP_LENGTH) {
            return false
        }

        return isValidIPv4(ip) || isValidIPv6(ip)
    }

    /**
     * Validate IPv4 address
     *
     * SECURITY FIX: Proper validation to prevent invalid IPs like 999.999.999.999
     */
    private fun isValidIPv4(ip: String): Boolean {
        val ipv4Regex = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$".toRegex()
        val match = ipv4Regex.matchEntire(ip) ?: return false

        // Validate each octet is 0-255
        return match.groupValues.drop(1).all { octet ->
            val value = octet.toIntOrNull() ?: return false
            value in 0..255
        }
    }

    /**
     * Validate IPv6 address
     */
    private fun isValidIPv6(ip: String): Boolean {
        // Basic IPv6 validation (simplified)
        if (!ip.contains(":")) return false

        val parts = ip.split(":")
        if (parts.size < 3 || parts.size > 8) return false

        // Each part should be valid hex (0-4 chars)
        return parts.all { part ->
            if (part.isEmpty()) return@all true  // Allow :: notation
            if (part.length > 4) return@all false
            part.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
        }
    }

    /**
     * Sanitize and validate email address
     *
     * @param email Email address
     * @return Sanitized email or null if invalid
     */
    fun sanitizeEmail(email: String): String? {
        val cleaned = email.trim().lowercase()

        if (cleaned.isEmpty() || cleaned.length > MAX_EMAIL_LENGTH) {
            Log.w(TAG, "Email length invalid: ${cleaned.length}")
            return null
        }

        // RFC 5322 simplified email regex
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        if (!emailRegex.matches(cleaned)) {
            Log.w(TAG, "Email format invalid: $cleaned")
            return null
        }

        // Check for suspicious patterns
        if (cleaned.contains("..") || cleaned.startsWith(".") || cleaned.contains("@.")) {
            Log.w(TAG, "Email contains suspicious patterns: $cleaned")
            return null
        }

        return cleaned
    }

    /**
     * Sanitize phone number
     *
     * @param phone Phone number
     * @return Sanitized phone or null if invalid
     */
    fun sanitizePhone(phone: String): String? {
        // Remove all non-digit and non-plus characters
        val cleaned = phone.replace(Regex("[^0-9+]"), "")

        if (cleaned.isEmpty() || cleaned.length > MAX_PHONE_LENGTH) {
            Log.w(TAG, "Phone length invalid: ${cleaned.length}")
            return null
        }

        // Basic validation: starts with + or digit, minimum 7 digits
        if (!cleaned.matches(Regex("^\\+?[0-9]{7,}$"))) {
            Log.w(TAG, "Phone format invalid: $cleaned")
            return null
        }

        return cleaned
    }

    /**
     * Sanitize URL for safe usage
     *
     * @param url URL string
     * @return Sanitized URL or null if invalid
     */
    fun sanitizeUrl(url: String): String? {
        val cleaned = url.trim()

        if (cleaned.isEmpty() || cleaned.length > 2048) {
            Log.w(TAG, "URL length invalid: ${cleaned.length}")
            return null
        }

        // Must start with http:// or https://
        if (!cleaned.matches(Regex("^https?://.*", RegexOption.IGNORE_CASE))) {
            Log.w(TAG, "URL missing protocol: $cleaned")
            return null
        }

        // Check for suspicious characters
        if (cleaned.contains("\n") || cleaned.contains("\r") || cleaned.contains("\t")) {
            Log.w(TAG, "URL contains suspicious whitespace: $cleaned")
            return null
        }

        return cleaned
    }

    /**
     * Validate if string contains only alphanumeric and safe characters
     *
     * @param input Input string
     * @param allowedChars Additional allowed characters
     * @return true if safe
     */
    fun isSafeString(input: String, allowedChars: String = ""): Boolean {
        if (input.isEmpty()) return false

        val safePattern = "^[a-zA-Z0-9$allowedChars]+$".toRegex()
        return safePattern.matches(input)
    }

    /**
     * Check if input contains potential injection patterns
     *
     * @param input Input to check
     * @return true if suspicious patterns detected
     */
    fun containsSuspiciousPatterns(input: String): Boolean {
        val suspiciousPatterns = listOf(
            "<script",
            "javascript:",
            "onerror=",
            "onclick=",
            "onload=",
            "../",
            "..\\",
            "\${",
            "SELECT.*FROM",
            "UNION.*SELECT",
            "DROP.*TABLE",
            "INSERT.*INTO",
            "UPDATE.*SET",
            "DELETE.*FROM"
        )

        return suspiciousPatterns.any { pattern ->
            input.contains(pattern, ignoreCase = true)
        }
    }
}
