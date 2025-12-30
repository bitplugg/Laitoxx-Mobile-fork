package com.laitoxx.security.utils

import java.security.MessageDigest
import java.util.Base64

object TextUtils {

    fun encodeBase64(text: String): String {
        return Base64.getEncoder().encodeToString(text.toByteArray())
    }

    fun decodeBase64(text: String): String {
        return try {
            String(Base64.getDecoder().decode(text))
        } catch (e: Exception) {
            "Invalid Base64"
        }
    }

    fun generateHash(text: String, algorithm: String = "MD5"): String {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            val hash = digest.digest(text.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Error generating hash"
        }
    }

    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val chars = buildString {
            if (includeUppercase) append(uppercase)
            if (includeLowercase) append(lowercase)
            if (includeNumbers) append(numbers)
            if (includeSymbols) append(symbols)
        }

        if (chars.isEmpty()) return ""

        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun transformText(text: String, transformation: TextTransformation): String {
        return when (transformation) {
            TextTransformation.UPPERCASE -> text.uppercase()
            TextTransformation.LOWERCASE -> text.lowercase()
            TextTransformation.REVERSE -> text.reversed()
            TextTransformation.CAPITALIZE -> text.split(" ").joinToString(" ") { it.capitalize() }
            TextTransformation.LEETSPEAK -> toLeetSpeak(text)
            TextTransformation.REMOVE_SPACES -> text.replace(" ", "")
            TextTransformation.URL_ENCODE -> java.net.URLEncoder.encode(text, "UTF-8")
            TextTransformation.URL_DECODE -> java.net.URLDecoder.decode(text, "UTF-8")
        }
    }

    private fun toLeetSpeak(text: String): String {
        val leetMap = mapOf(
            'a' to '4', 'A' to '4',
            'e' to '3', 'E' to '3',
            'i' to '1', 'I' to '1',
            'o' to '0', 'O' to '0',
            's' to '5', 'S' to '5',
            't' to '7', 'T' to '7',
            'l' to '1', 'L' to '1'
        )
        return text.map { leetMap[it] ?: it }.joinToString("")
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

enum class TextTransformation {
    UPPERCASE,
    LOWERCASE,
    REVERSE,
    CAPITALIZE,
    LEETSPEAK,
    REMOVE_SPACES,
    URL_ENCODE,
    URL_DECODE
}
