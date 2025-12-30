package com.laitoxx.security.data.repository

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.laitoxx.security.data.exceptions.*
import com.laitoxx.security.data.model.IPInfo
import com.laitoxx.security.data.model.SubdomainList
import com.laitoxx.security.data.model.ToolResult
import com.laitoxx.security.data.network.RetrofitClient
import com.laitoxx.security.utils.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class OsintRepository {

    companion object {
        private const val TAG = "OsintRepository"
    }

    suspend fun getIPInfo(target: String): Result<IPInfo> = withContext(Dispatchers.IO) {
        try {
            // Input validation
            val sanitizedTarget = target.trim()
            if (sanitizedTarget.isEmpty()) {
                return@withContext Result.failure(ValidationException("Target cannot be empty"))
            }

            // Check for suspicious patterns
            if (InputValidator.containsSuspiciousPatterns(sanitizedTarget)) {
                Log.w(TAG, "Suspicious pattern detected in target: $sanitizedTarget")
                return@withContext Result.failure(
                    ValidationException("Invalid characters detected in target")
                )
            }

            val ip = resolveHostToIP(sanitizedTarget)
            val response = RetrofitClient.apiService.getIPInfo(ip)

            when {
                !response.isSuccessful -> {
                    Result.failure(createHttpException(response.code(), response.message()))
                }
                response.body() == null -> {
                    Result.failure(EmptyResponseException("IP info service returned empty response"))
                }
                else -> {
                    val ipInfo = response.body()!!
                    if (ipInfo.success) {
                        Result.success(ipInfo)
                    } else {
                        Result.failure(ServerException(400, ipInfo.message ?: "Unknown error"))
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout in getIPInfo", e)
            Result.failure(TimeoutException(e))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Unknown host in getIPInfo", e)
            Result.failure(DnsException("Cannot resolve host: ${e.message}", e))
        } catch (e: SSLException) {
            Log.e(TAG, "SSL error in getIPInfo", e)
            Result.failure(SslException(e.message ?: "SSL handshake failed", e))
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parsing error in getIPInfo", e)
            Result.failure(ParseException("Invalid JSON response", e))
        } catch (e: IOException) {
            Log.e(TAG, "IO error in getIPInfo", e)
            Result.failure(NoInternetException(e))
        } catch (e: ValidationException) {
            Result.failure(e)
        } catch (e: NetworkException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getIPInfo", e)
            Result.failure(e)
        }
    }

    suspend fun findSubdomains(domain: String): Result<SubdomainList> = withContext(Dispatchers.IO) {
        try {
            // SECURITY: Sanitize and validate domain to prevent SSRF and injection attacks
            val cleanDomain = InputValidator.sanitizeDomain(domain)
                ?: return@withContext Result.failure(
                    ValidationException("Invalid domain format. Please enter a valid domain name.")
                )

            // Additional validation: domain length check (RFC 1035)
            if (cleanDomain.length > 253) {
                return@withContext Result.failure(
                    ValidationException("Domain name is too long (max 253 characters)")
                )
            }

            // Construct safe URL with validated domain
            val url = "https://crt.sh/?q=%.${cleanDomain}&output=json"

            val customService = RetrofitClient.createCustomService("https://crt.sh/")
            val response = customService.getSubdomains(url)

            when {
                !response.isSuccessful -> {
                    Result.failure(
                        createHttpException(
                            response.code(),
                            "Failed to fetch subdomains: ${response.message()}"
                        )
                    )
                }
                response.body() == null -> {
                    Result.failure(EmptyResponseException("Subdomain service returned empty response"))
                }
                else -> {
                    val results = response.body()!!
                    val subdomains = results.map { it.commonName }.distinct().sorted()

                    Result.success(
                        SubdomainList(
                            domain = cleanDomain,
                            subdomains = subdomains,
                            count = subdomains.size
                        )
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout in findSubdomains", e)
            Result.failure(TimeoutException(e))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Unknown host in findSubdomains", e)
            Result.failure(NoInternetException(e))
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parsing error in findSubdomains", e)
            Result.failure(ParseException("Invalid JSON response from subdomain service", e))
        } catch (e: IOException) {
            Log.e(TAG, "IO error in findSubdomains", e)
            Result.failure(NoInternetException(e))
        } catch (e: ValidationException) {
            Result.failure(e)
        } catch (e: NetworkException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in findSubdomains", e)
            Result.failure(e)
        }
    }

    suspend fun validateEmail(email: String): Result<ToolResult> = withContext(Dispatchers.IO) {
        try {
            // Use InputValidator for proper email validation
            val sanitizedEmail = InputValidator.sanitizeEmail(email)

            val message = if (sanitizedEmail != null) {
                "✓ Email format is valid\n\nSanitized: $sanitizedEmail"
            } else {
                "✗ Email format is invalid\n\nReasons:\n" +
                "- Must be a valid email address\n" +
                "- Max length: 320 characters\n" +
                "- Cannot contain suspicious patterns"
            }

            Result.success(ToolResult(
                success = sanitizedEmail != null,
                data = message
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error in validateEmail", e)
            Result.failure(e)
        }
    }

    private suspend fun resolveHostToIP(host: String): String = withContext(Dispatchers.IO) {
        try {
            // SECURITY FIX: Use InputValidator for proper IP validation
            if (InputValidator.isValidIP(host)) {
                return@withContext host
            }

            // Not an IP, try to resolve as domain
            val address = InetAddress.getByName(host)
            address.hostAddress ?: host
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Cannot resolve host to IP: $host", e)
            throw DnsException("Cannot resolve host: $host", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving host to IP", e)
            host
        }
    }

    suspend fun lookupPhone(phoneNumber: String): Result<ToolResult> = withContext(Dispatchers.IO) {
        try {
            // SECURITY: Use InputValidator for phone sanitization
            val cleaned = InputValidator.sanitizePhone(phoneNumber)
                ?: return@withContext Result.failure(
                    ValidationException(
                        "Invalid phone number format. Must be 7-20 digits, optionally starting with +"
                    )
                )

            val info = buildString {
                appendLine("Phone Number: $cleaned")
                appendLine("Length: ${cleaned.length} digits")
                appendLine()

                // Country code detection
                when {
                    cleaned.startsWith("+1") -> appendLine("Country: United States/Canada (+1)")
                    cleaned.startsWith("+7") -> appendLine("Country: Russia/Kazakhstan (+7)")
                    cleaned.startsWith("+44") -> appendLine("Country: United Kingdom (+44)")
                    cleaned.startsWith("+49") -> appendLine("Country: Germany (+49)")
                    cleaned.startsWith("+33") -> appendLine("Country: France (+33)")
                    cleaned.startsWith("+86") -> appendLine("Country: China (+86)")
                    cleaned.startsWith("+91") -> appendLine("Country: India (+91)")
                    cleaned.startsWith("+81") -> appendLine("Country: Japan (+81)")
                    cleaned.startsWith("+82") -> appendLine("Country: South Korea (+82)")
                    cleaned.startsWith("+55") -> appendLine("Country: Brazil (+55)")
                    else -> appendLine("Country: Unknown or no country code")
                }

                appendLine()
                if (cleaned.length >= 10) {
                    appendLine("✓ Format: Valid")
                } else {
                    appendLine("✗ Format: Invalid (too short, minimum 7 digits)")
                }
            }

            Result.success(ToolResult(
                success = cleaned.length >= 10,
                data = info
            ))
        } catch (e: ValidationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error in lookupPhone", e)
            Result.failure(e)
        }
    }
}
