package com.laitoxx.security.data.exceptions

/**
 * Custom exception hierarchy for better error handling
 *
 * Benefits:
 * - Specific error types for different failure scenarios
 * - Better user experience with targeted error messages
 * - Easier debugging and logging
 * - Proper separation of concerns
 */

/**
 * Base class for all network-related exceptions
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * No internet connection available
 */
class NoInternetException(cause: Throwable? = null) :
    NetworkException("No internet connection. Please check your network settings.", cause)

/**
 * Request timeout
 */
class TimeoutException(cause: Throwable? = null) :
    NetworkException("Request timed out. Please try again.", cause)

/**
 * Server returned an error response
 */
class ServerException(val code: Int, message: String) :
    NetworkException("Server error [$code]: $message")

/**
 * Failed to parse API response
 */
class ParseException(message: String, cause: Throwable? = null) :
    NetworkException("Failed to parse response: $message", cause)

/**
 * Input validation failed
 */
class ValidationException(message: String) :
    NetworkException("Invalid input: $message")

/**
 * Rate limit exceeded
 */
class RateLimitException(message: String = "Too many requests. Please wait before trying again.") :
    NetworkException(message)

/**
 * API returned empty or null data
 */
class EmptyResponseException(message: String = "Server returned empty response") :
    NetworkException(message)

/**
 * DNS resolution failed
 */
class DnsException(message: String, cause: Throwable? = null) :
    NetworkException("DNS resolution failed: $message", cause)

/**
 * SSL/TLS certificate error
 */
class SslException(message: String, cause: Throwable? = null) :
    NetworkException("SSL/TLS error: $message", cause)

/**
 * Unauthorized access (401)
 */
class UnauthorizedException(message: String = "Unauthorized access") :
    NetworkException(message)

/**
 * Forbidden access (403)
 */
class ForbiddenException(message: String = "Access forbidden") :
    NetworkException(message)

/**
 * Resource not found (404)
 */
class NotFoundException(message: String = "Resource not found") :
    NetworkException(message)

/**
 * Helper function to create appropriate exception from HTTP status code
 */
fun createHttpException(code: Int, message: String): NetworkException {
    return when (code) {
        401 -> UnauthorizedException(message)
        403 -> ForbiddenException(message)
        404 -> NotFoundException(message)
        408 -> TimeoutException()
        429 -> RateLimitException(message)
        in 500..599 -> ServerException(code, message)
        else -> ServerException(code, message)
    }
}
