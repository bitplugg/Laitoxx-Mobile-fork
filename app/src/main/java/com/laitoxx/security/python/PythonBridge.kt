package com.laitoxx.security.python

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Bridge between Kotlin and Python modules
 * Provides easy access to all Python OSINT and security tools
 */
object PythonBridge {

    private const val TAG = "PythonBridge"
    private var isInitialized = false
    private lateinit var python: Python

    /**
     * Initialize Python runtime
     * Must be called once before using any Python functions
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            return
        }

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }

            python = Python.getInstance()

            isInitialized = true
            Log.d(TAG, "PythonBridge initialized")
        } catch (e: Exception) {
            Log.e(TAG, "PythonBridge init failed", e)
            throw e
        }
    }

    /**
     * Execute Python function and return JSON result
     */
    private suspend fun executePythonFunction(
        moduleName: String,
        functionName: String,
        vararg args: Any
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val module = python.getModule(moduleName)
            val result = module.callAttr(functionName, *args).toString()
            Result.success(JSONObject(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== OSINT Tools ====================

    /**
     * WHOIS domain lookup
     */
    suspend fun whoisLookup(domain: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "whois_lookup", domain)
    }

    /**
     * Advanced phone number lookup
     */
    suspend fun phoneLookup(phoneNumber: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "phone_lookup", phoneNumber)
    }

    /**
     * Advanced DNS lookup (all record types)
     */
    suspend fun dnsLookup(domain: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "dns_lookup", domain)
    }

    /**
     * Email OSINT investigation
     */
    suspend fun emailOsint(email: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "email_osint", email)
    }

    /**
     * Reverse IP lookup
     */
    suspend fun reverseIP(ip: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "reverse_ip", ip)
    }

    /**
     * Website technology stack detection
     */
    suspend fun techStack(url: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "tech_stack", url)
    }

    /**
     * Social media username search
     */
    suspend fun socialSearch(username: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "social_search", username)
    }

    /**
     * Enhanced IP lookup with multiple geolocation sources
     */
    suspend fun enhancedIPLookup(ip: String): Result<JSONObject> {
        return executePythonFunction("osint_tools", "enhanced_ip_info", ip)
    }

    // ==================== Extended OSINT Tools ====================

    /**
     * Gmail OSINT investigation
     */
    suspend fun gmailOsint(emailPrefix: String): Result<JSONObject> {
        return executePythonFunction("extended_osint", "gmail_osint", emailPrefix)
    }

    /**
     * MAC Address vendor lookup
     */
    suspend fun macLookup(macAddress: String): Result<JSONObject> {
        return executePythonFunction("extended_osint", "mac_lookup", macAddress)
    }

    /**
     * Username checker across 29 platforms
     */
    suspend fun usernameCheck(username: String): Result<JSONObject> {
        return executePythonFunction("extended_osint", "username_check", username)
    }

    /**
     * Google Dork query generator
     */
    suspend fun googleDork(keyword: String, operators: String): Result<JSONObject> {
        return executePythonFunction("extended_osint", "google_dork", keyword, operators)
    }

    /**
     * Web crawler (enhanced)
     */
    suspend fun webCrawlEnhanced(startUrl: String, maxPages: Int = 10): Result<JSONObject> {
        return executePythonFunction("extended_osint", "web_crawl", startUrl, maxPages)
    }

    /**
     * Enhanced subdomain enumeration
     */
    suspend fun subdomainFind(domain: String): Result<JSONObject> {
        return executePythonFunction("extended_osint", "subdomain_find", domain)
    }

    /**
     * WhatsApp account checker (without Selenium)
     * Multiple methods to verify if phone number has WhatsApp account
     */
    suspend fun whatsappCheck(phoneNumber: String): Result<JSONObject> {
        return executePythonFunction("extended_osint", "whatsapp_check", phoneNumber)
    }

    // ==================== Security Tools ====================

    /**
     * SQL injection vulnerability scanner
     */
    suspend fun sqlScan(url: String): Result<JSONObject> {
        return executePythonFunction("security_tools", "sql_scan", url)
    }

    /**
     * XSS vulnerability scanner
     */
    suspend fun xssScan(url: String): Result<JSONObject> {
        return executePythonFunction("security_tools", "xss_scan", url)
    }

    /**
     * SSL certificate analysis
     */
    suspend fun sslCheck(domain: String): Result<JSONObject> {
        return executePythonFunction("security_tools", "ssl_check", domain)
    }

    /**
     * Security headers checker
     */
    suspend fun securityHeaders(url: String): Result<JSONObject> {
        return executePythonFunction("security_tools", "security_headers", url)
    }

    /**
     * Robots.txt analyzer
     */
    suspend fun robotsAnalyzer(url: String): Result<JSONObject> {
        return executePythonFunction("security_tools", "robots_analyzer", url)
    }

    /**
     * Directory fuzzing
     */
    suspend fun directoryFuzz(url: String): Result<JSONObject> {
        return executePythonFunction("security_tools", "dir_fuzz", url)
    }

    // ==================== Web Tools ====================

    /**
     * Web crawler
     */
    suspend fun webCrawl(url: String, maxPages: Int = 20): Result<JSONObject> {
        return executePythonFunction("web_tools", "crawl", url, maxPages)
    }

    /**
     * Extract emails from webpage
     */
    suspend fun extractEmails(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "extract_emails", url)
    }

    /**
     * Extract phone numbers from webpage
     */
    suspend fun extractPhones(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "extract_phones", url)
    }

    /**
     * Extract social media links
     */
    suspend fun extractSocial(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "extract_social", url)
    }

    /**
     * Extract forms from webpage
     */
    suspend fun extractForms(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "extract_forms", url)
    }

    /**
     * Extract JavaScript files
     */
    suspend fun extractJS(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "extract_js", url)
    }

    /**
     * Extract CSS files
     */
    suspend fun extractCSS(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "extract_css", url)
    }

    /**
     * Comprehensive page analysis
     */
    suspend fun analyzePage(url: String): Result<JSONObject> {
        return executePythonFunction("web_tools", "analyze_page", url)
    }
}
