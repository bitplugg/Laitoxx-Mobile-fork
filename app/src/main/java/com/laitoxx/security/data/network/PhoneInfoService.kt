package com.laitoxx.security.data.network

import com.laitoxx.security.data.model.PhoneInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Сервис для получения информации о телефонных номерах
 * Реализует функционал из phone.py без использования Selenium
 */
class PhoneInfoService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    )

    /**
     * Получить информацию о телефонном номере
     */
    suspend fun getPhoneInfo(phoneNumber: String): Result<PhoneInfo> = withContext(Dispatchers.IO) {
        try {
            // Базовая валидация номера
            val cleanNumber = phoneNumber.trim()
            if (!cleanNumber.matches(Regex("^\\+[1-9]\\d{1,14}$"))) {
                return@withContext Result.failure(
                    IllegalArgumentException("Неверный формат номера. Используйте международный формат: +1234567890")
                )
            }

            val sources = mutableListOf<String>()
            val rawDataMap = mutableMapOf<String, String>()

            // Базовый парсинг номера (без библиотеки phonenumbers, упрощенная версия)
            val basicInfo = parsePhoneNumber(cleanNumber)

            // Параллельный запрос репутации из разных источников
            val reputationDeferred = async { checkReputation(cleanNumber) }

            val reputationData = reputationDeferred.await()

            var reputation: String? = null
            var isSpam = false
            var viewCount: String? = null
            val additionalInfo = mutableMapOf<String, String>()

            reputationData.forEach { (source, data) ->
                sources.add(source)
                rawDataMap[source] = data

                when (source) {
                    "spamcalls.net" -> {
                        if (data.contains("СПАМ", ignoreCase = true)) {
                            isSpam = true
                        }
                    }
                    "free-lookup.net" -> {
                        // Парсим дополнительную информацию
                        val viewMatch = Regex("(\\d+)\\s*раз").find(data)
                        viewCount = viewMatch?.groupValues?.get(1)
                    }
                }
            }

            Result.success(PhoneInfo(
                number = cleanNumber,
                isPossible = true,
                isValid = true,
                countryCode = basicInfo["countryCode"],
                regionCode = basicInfo["regionCode"],
                carrier = basicInfo["carrier"],
                timezones = basicInfo["timezones"]?.split(",") ?: emptyList(),
                internationalFormat = cleanNumber,
                reputation = reputation,
                isSpam = isSpam,
                viewCount = viewCount,
                additionalInfo = additionalInfo,
                rawData = rawDataMap,
                sources = sources
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Парсинг номера телефона с использованием libphonenumber
     */
    private fun parsePhoneNumber(number: String): Map<String, String> {
        val result = mutableMapOf<String, String>()

        try {
            val phoneUtil = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance()
            val parsed = phoneUtil.parse(number, null)

            // Код страны
            result["countryCode"] = parsed.countryCode.toString()

            // Регион
            val regionCode = phoneUtil.getRegionCodeForNumber(parsed)
            result["regionCode"] = regionCode ?: "Unknown"

            // Оператор
            val carrier = com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper.getInstance()
                .getNameForNumber(parsed, java.util.Locale.ENGLISH)
            if (carrier.isNotEmpty()) {
                result["carrier"] = carrier
            }

            // Часовые пояса
            val timeZoneMapper = com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper.getInstance()
            val timezones = timeZoneMapper.getTimeZonesForNumber(parsed)
            if (timezones.isNotEmpty()) {
                result["timezones"] = timezones.joinToString(", ")
            }

            // Валидация
            result["isPossible"] = phoneUtil.isPossibleNumber(parsed).toString()
            result["isValid"] = phoneUtil.isValidNumber(parsed).toString()

        } catch (e: Exception) {
            // Fallback к упрощенному парсингу
            val countryCodeMatch = Regex("^\\+(\\d{1,3})").find(number)
            if (countryCodeMatch != null) {
                result["countryCode"] = countryCodeMatch.groupValues[1]
            }
        }

        return result
    }

    /**
     * Проверка репутации номера через веб-парсинг
     */
    private suspend fun checkReputation(number: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        val numWithoutPlus = number.removePrefix("+")

        // Проверка через spamcalls.net
        try {
            val spamRequest = Request.Builder()
                .url("https://spamcalls.net/en/number/$numWithoutPlus")
                .header("User-Agent", userAgents.random())
                .build()

            client.newCall(spamRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    val doc = Jsoup.parse(html)

                    val isSpam = doc.select("div.report-body").isNotEmpty()
                    val resultText = if (isSpam) {
                        "⚠️ Отмечен как СПАМ (spamcalls.net)"
                    } else {
                        "✓ Явных признаков спама не найдено (spamcalls.net)"
                    }

                    results.add("spamcalls.net" to resultText)
                }
            }
        } catch (e: Exception) {
            // Игнорируем ошибки
        }

        // Проверка через free-lookup.net
        try {
            val lookupRequest = Request.Builder()
                .url("https://free-lookup.net/$numWithoutPlus")
                .header("User-Agent", userAgents.random())
                .build()

            client.newCall(lookupRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    val doc = Jsoup.parse(html)

                    val reportList = doc.select("ul.report-summary__list")
                    if (reportList.isNotEmpty()) {
                        val info = mutableListOf<String>()

                        reportList.select("div").forEach { div ->
                            val text = div.text().trim()
                            if (text.isNotEmpty() && text != "Not found") {
                                info.add(text)
                            }
                        }

                        if (info.isNotEmpty()) {
                            results.add("free-lookup.net" to info.joinToString("\n"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Игнорируем ошибки
        }

        return results
    }

    /**
     * Поиск информации в социальных сетях (опционально)
     */
    suspend fun searchSocialMedia(phoneNumber: String): Map<String, String> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, String>()

        // Генерируем ссылки для поиска
        val numWithoutPlus = phoneNumber.removePrefix("+")

        results["Google"] = "https://www.google.com/search?q=$phoneNumber"
        results["DuckDuckGo"] = "https://duckduckgo.com/?q=$phoneNumber"
        results["Truecaller"] = "https://www.truecaller.com/search/phone/$phoneNumber"

        results
    }
}
