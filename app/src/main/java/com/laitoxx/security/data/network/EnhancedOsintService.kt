package com.laitoxx.security.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import com.laitoxx.security.data.model.*

/**
 * Сервис для расширенного OSINT функционала
 * Реализует функции из Python скриптов на чистом Kotlin
 */
class EnhancedOsintService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ==================== IP Lookup ====================

    /**
     * Расширенный поиск информации об IP адресе (реализация ip.py)
     */
    suspend fun getEnhancedIPInfo(ip: String): Result<EnhancedIPInfo> = withContext(Dispatchers.IO) {
        try {
            // Локальные проверки
            val ipAddress = InetAddress.getByName(ip)
            val version = if (ipAddress.address.size == 4) "IPv4" else "IPv6"
            val isPrivate = ipAddress.isSiteLocalAddress
            val isLoopback = ipAddress.isLoopbackAddress
            val isMulticast = ipAddress.isMulticastAddress

            // Параллельный запрос к нескольким сервисам
            val sources = mutableListOf<String>()
            val rawDataMap = mutableMapOf<String, String>()

            val ipApiDeferred = async { fetchIPApi(ip) }
            val freeIpApiDeferred = async { fetchFreeIpApi(ip) }
            val ipApiCoDeferred = async { fetchIpApiCo(ip) }
            val ipWhoisDeferred = async { fetchIpWhois(ip) }

            val results = listOfNotNull(
                ipApiDeferred.await(),
                freeIpApiDeferred.await(),
                ipApiCoDeferred.await(),
                ipWhoisDeferred.await()
            )

            // Собираем данные из всех источников
            val countries = mutableSetOf<String>()
            val countryCodes = mutableSetOf<String>()
            val regions = mutableSetOf<String>()
            val cities = mutableSetOf<String>()
            val zipCodes = mutableSetOf<String>()
            val coordinates = mutableSetOf<String>()
            val timezones = mutableSetOf<String>()
            val providers = mutableSetOf<String>()
            val asns = mutableSetOf<String>()
            val organizations = mutableSetOf<String>()

            var isProxy = false
            var isVpn = false
            var isTor = false

            results.forEach { (source, data) ->
                sources.add(source)
                rawDataMap[source] = data.toString()

                data.jsonObject.let { obj ->
                    // Страна
                    obj["country"]?.jsonPrimitive?.content?.let { countries.add(it) }
                    obj["countryName"]?.jsonPrimitive?.content?.let { countries.add(it) }
                    obj["countryCode"]?.jsonPrimitive?.content?.let { countryCodes.add(it) }
                    obj["country_code"]?.jsonPrimitive?.content?.let { countryCodes.add(it) }

                    // Регион
                    obj["regionName"]?.jsonPrimitive?.content?.let { regions.add(it) }
                    obj["region_name"]?.jsonPrimitive?.content?.let { regions.add(it) }
                    obj["region"]?.jsonPrimitive?.content?.let { regions.add(it) }

                    // Город
                    obj["city"]?.jsonPrimitive?.content?.let { cities.add(it) }
                    obj["cityName"]?.jsonPrimitive?.content?.let { cities.add(it) }

                    // Индекс
                    obj["zip"]?.jsonPrimitive?.content?.let { zipCodes.add(it) }
                    obj["zipCode"]?.jsonPrimitive?.content?.let { zipCodes.add(it) }
                    obj["postal"]?.jsonPrimitive?.content?.let { zipCodes.add(it) }

                    // Координаты
                    val lat = obj["lat"]?.jsonPrimitive?.content ?: obj["latitude"]?.jsonPrimitive?.content
                    val lon = obj["lon"]?.jsonPrimitive?.content ?: obj["longitude"]?.jsonPrimitive?.content
                    if (lat != null && lon != null) {
                        coordinates.add("$lat, $lon")
                    }

                    // Часовой пояс
                    obj["timezone"]?.jsonPrimitive?.content?.let { timezones.add(it) }
                    obj["time_zone"]?.jsonPrimitive?.content?.let { timezones.add(it) }

                    // Провайдер
                    obj["isp"]?.jsonPrimitive?.content?.let { providers.add(it) }
                    obj["org"]?.jsonPrimitive?.content?.let { providers.add(it) }
                    obj["organization"]?.jsonPrimitive?.content?.let { organizations.add(it) }
                    obj["asnOrganization"]?.jsonPrimitive?.content?.let { organizations.add(it) }
                    obj["asname"]?.jsonPrimitive?.content?.let { organizations.add(it) }

                    // ASN
                    obj["as"]?.jsonPrimitive?.content?.let { if (it.contains("AS")) asns.add(it) }
                    obj["asn"]?.jsonPrimitive?.content?.let { asns.add("AS$it") }

                    // Proxy/VPN/Tor
                    obj["isProxy"]?.jsonPrimitive?.content?.let { if (it == "true") isProxy = true }
                    obj["proxy"]?.jsonPrimitive?.content?.let { if (it == "true") isProxy = true }
                    obj["vpn"]?.jsonPrimitive?.content?.let { if (it == "true") isVpn = true }
                    obj["tor"]?.jsonPrimitive?.content?.let { if (it == "true") isTor = true }
                }
            }

            Result.success(EnhancedIPInfo(
                ip = ip,
                version = version,
                isPrivate = isPrivate,
                isLoopback = isLoopback,
                isReserved = false,
                isMulticast = isMulticast,
                country = countries,
                countryCode = countryCodes,
                region = regions,
                city = cities,
                zipCode = zipCodes,
                coordinates = coordinates,
                timezone = timezones,
                provider = providers,
                asn = asns,
                organization = organizations,
                isProxy = isProxy,
                isVpn = isVpn,
                isTor = isTor,
                rawData = rawDataMap,
                sources = sources
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchIPApi(ip: String): Pair<String, JsonObject>? {
        return try {
            val request = Request.Builder()
                .url("http://ip-api.com/json/$ip")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return null
                    "ip-api.com" to json.parseToJsonElement(body).jsonObject
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFreeIpApi(ip: String): Pair<String, JsonObject>? {
        return try {
            val request = Request.Builder()
                .url("https://freeipapi.com/api/json/$ip")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return null
                    "freeipapi.com" to json.parseToJsonElement(body).jsonObject
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchIpApiCo(ip: String): Pair<String, JsonObject>? {
        return try {
            val request = Request.Builder()
                .url("https://ipapi.co/$ip/json/")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return null
                    "ipapi.co" to json.parseToJsonElement(body).jsonObject
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchIpWhois(ip: String): Pair<String, JsonObject>? {
        return try {
            val request = Request.Builder()
                .url("https://ipwhois.io/json/$ip")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return null
                    "ipwhois.io" to json.parseToJsonElement(body).jsonObject
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== MAC Lookup ====================

    /**
     * Поиск информации о MAC адресе (реализация mac.py)
     */
    suspend fun getMACInfo(mac: String): Result<MACInfo> = withContext(Dispatchers.IO) {
        try {
            val normalized = normalizeMac(mac) ?: return@withContext Result.failure(
                IllegalArgumentException("Некорректный формат MAC адреса")
            )

            val oui = normalized.substring(0, 8) // XX:XX:XX
            val firstOctet = normalized.substring(0, 2).toInt(16)
            val isMulticast = (firstOctet and 1) == 1
            val isLocal = (firstOctet and 2) == 2

            val sources = mutableListOf<String>()
            val rawDataMap = mutableMapOf<String, String>()

            // Запрос к API
            val macLookupData = fetchMacLookupApp(mac)

            val vendors = mutableSetOf<String>()
            val companies = mutableSetOf<String>()
            val addresses = mutableSetOf<String>()
            val countries = mutableSetOf<String>()
            val blockTypes = mutableSetOf<String>()
            val blockRanges = mutableSetOf<String>()

            macLookupData?.let { (source, data) ->
                sources.add(source)
                rawDataMap[source] = data.toString()

                data.jsonObject.let { obj ->
                    obj["company"]?.jsonPrimitive?.content?.let { companies.add(it) }
                    obj["address"]?.jsonPrimitive?.content?.let { addresses.add(it.replace("\n", ", ")) }
                    obj["country"]?.jsonPrimitive?.content?.let { countries.add(it) }
                    obj["type"]?.jsonPrimitive?.content?.let { blockTypes.add(it) }

                    val blockStart = obj["blockStart"]?.jsonPrimitive?.content
                    val blockEnd = obj["blockEnd"]?.jsonPrimitive?.content
                    if (blockStart != null && blockEnd != null) {
                        blockRanges.add("$blockStart — $blockEnd")
                    }
                }
            }

            Result.success(MACInfo(
                mac = mac,
                normalized = normalized,
                oui = oui,
                isLocal = isLocal,
                isMulticast = isMulticast,
                vendor = vendors,
                company = companies,
                address = addresses,
                country = countries,
                blockType = blockTypes,
                blockRange = blockRanges,
                rawData = rawDataMap,
                sources = sources
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeMac(mac: String): String? {
        val clean = mac.replace(Regex("[^0-9A-Fa-f]"), "").uppercase()
        if (clean.length != 12) return null

        return clean.chunked(2).joinToString(":")
    }

    private suspend fun fetchMacLookupApp(mac: String): Pair<String, JsonObject>? {
        return try {
            val cleanMac = mac.replace(Regex("[^0-9A-Fa-f]"), "").uppercase()
            val request = Request.Builder()
                .url("https://api.maclookup.app/v2/macs/$cleanMac")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return null
                    val jsonData = json.parseToJsonElement(body).jsonObject
                    if (jsonData["found"]?.jsonPrimitive?.content == "true") {
                        "maclookup.app" to jsonData
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== WHOIS Lookup ====================

    /**
     * WHOIS поиск (реализация whois.py)
     */
    suspend fun getWhoisInfo(resource: String): Result<WhoisInfo> = withContext(Dispatchers.IO) {
        try {
            val type = detectResourceType(resource)
            val cleanResource = when {
                resource.startsWith("as", ignoreCase = true) -> resource.substring(2)
                else -> resource
            }

            val sources = mutableListOf<String>()
            val rawDataMap = mutableMapOf<String, String>()

            // Запрос к RDAP
            val rdapData = fetchRdap(type, cleanResource)

            var network: String? = null
            var registrar: String? = null
            var registrant: String? = null
            var country: String? = null
            var registrationDate: String? = null
            var updateDate: String? = null
            var expirationDate: String? = null
            val statuses = mutableListOf<String>()
            val nameservers = mutableListOf<String>()
            var abuseContact: String? = null

            rdapData?.let { (source, data) ->
                sources.add(source)
                rawDataMap[source] = data.toString()

                data.jsonObject.let { obj ->
                    // Страна
                    obj["country"]?.jsonPrimitive?.content?.let { country = it }

                    // Сеть (для IP)
                    val startAddr = obj["startAddress"]?.jsonPrimitive?.content
                    val endAddr = obj["endAddress"]?.jsonPrimitive?.content
                    if (startAddr != null && endAddr != null) {
                        network = "$startAddr - $endAddr"
                    }

                    // Статусы (для доменов)
                    obj["status"]?.let { statusArray ->
                        // TODO: parse array
                    }

                    // События (даты)
                    obj["events"]?.let { eventsArray ->
                        // TODO: parse events
                    }

                    // Entities (регистратор, регистрант)
                    obj["entities"]?.let { entitiesArray ->
                        // TODO: parse entities
                    }
                }
            }

            Result.success(WhoisInfo(
                resource = resource,
                type = type,
                network = network,
                registrar = registrar,
                registrant = registrant,
                country = country,
                registrationDate = registrationDate,
                updateDate = updateDate,
                expirationDate = expirationDate,
                statuses = statuses,
                nameservers = nameservers,
                abuseContact = abuseContact,
                rawData = rawDataMap,
                sources = sources
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun detectResourceType(resource: String): String {
        return when {
            resource.startsWith("as", ignoreCase = true) -> "autnum"
            resource.contains(".") && !resource.all { it.isDigit() || it == '.' || it == ':' } -> "domain"
            else -> "ip"
        }
    }

    private suspend fun fetchRdap(type: String, resource: String): Pair<String, JsonObject>? {
        return try {
            val request = Request.Builder()
                .url("https://rdap.org/$type/$resource")
                .addHeader("Accept", "application/rdap+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return null
                    "rdap.org" to json.parseToJsonElement(body).jsonObject
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
