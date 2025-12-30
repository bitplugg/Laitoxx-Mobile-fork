package com.laitoxx.security.data.model

import kotlinx.serialization.Serializable

/**
 * Расширенная информация об IP адресе (объединение данных от нескольких источников)
 */
@Serializable
data class EnhancedIPInfo(
    val ip: String,
    val version: String = "IPv4",
    val isPrivate: Boolean = false,
    val isLoopback: Boolean = false,
    val isReserved: Boolean = false,
    val isMulticast: Boolean = false,

    // Геолокация
    val country: Set<String> = emptySet(),
    val countryCode: Set<String> = emptySet(),
    val region: Set<String> = emptySet(),
    val city: Set<String> = emptySet(),
    val zipCode: Set<String> = emptySet(),
    val coordinates: Set<String> = emptySet(),
    val timezone: Set<String> = emptySet(),

    // Провайдер и сеть
    val provider: Set<String> = emptySet(),
    val asn: Set<String> = emptySet(),
    val organization: Set<String> = emptySet(),

    // Безопасность
    val isProxy: Boolean = false,
    val isVpn: Boolean = false,
    val isTor: Boolean = false,

    // Raw данные от источников
    val rawData: Map<String, String> = emptyMap(),
    val sources: List<String> = emptyList()
) {
    fun toReadableString(): String = buildString {
        appendLine("🌐 Расширенная информация об IP: $ip")
        appendLine()

        appendLine("📋 Локальные проверки:")
        appendLine("   IP адрес: $ip")
        appendLine("   Версия: $version")
        appendLine("   Приватный: ${if (isPrivate) "Да" else "Нет"}")
        appendLine("   Loopback: ${if (isLoopback) "Да" else "Нет"}")
        appendLine("   Зарезервирован: ${if (isReserved) "Да" else "Нет"}")
        appendLine("   Multicast: ${if (isMulticast) "Да" else "Нет"}")
        appendLine()

        appendLine("─".repeat(40))
        appendLine()

        appendLine("🌍 Геолокация (от ${sources.size} источников):")
        appendLine("   Страна: ${country.joinToString(", ").ifEmpty { "—" }}")
        if (countryCode.isNotEmpty()) {
            appendLine("   Код страны: ${countryCode.joinToString(", ")}")
        }
        appendLine("   Регион: ${region.joinToString(", ").ifEmpty { "—" }}")
        appendLine("   Город: ${city.joinToString(", ").ifEmpty { "—" }}")
        if (zipCode.isNotEmpty()) {
            appendLine("   Индекс: ${zipCode.joinToString(", ")}")
        }
        appendLine("   Координаты: ${coordinates.joinToString(", ").ifEmpty { "—" }}")
        appendLine("   Часовой пояс: ${timezone.joinToString(", ").ifEmpty { "—" }}")
        appendLine()

        appendLine("─".repeat(40))
        appendLine()

        appendLine("🔌 Сеть и провайдер:")
        appendLine("   Провайдер: ${provider.joinToString(", ").ifEmpty { "—" }}")
        if (organization.isNotEmpty()) {
            appendLine("   Организация: ${organization.joinToString(", ")}")
        }
        if (asn.isNotEmpty()) {
            appendLine("   AS: ${asn.joinToString(", ")}")
        }
        appendLine()

        if (isProxy || isVpn || isTor) {
            appendLine("─".repeat(40))
            appendLine()
            appendLine("⚠️ Безопасность:")
            if (isProxy) appendLine("   ⚡ Прокси обнаружен")
            if (isVpn) appendLine("   🔒 VPN обнаружен")
            if (isTor) appendLine("   🧅 Tor обнаружен")
            appendLine()
        }

        appendLine("─".repeat(40))
        appendLine("📊 Источники данных: ${sources.joinToString(", ")}")
    }
}

/**
 * MAC адрес информация
 */
@Serializable
data class MACInfo(
    val mac: String,
    val normalized: String,
    val oui: String,
    val isLocal: Boolean = false,
    val isMulticast: Boolean = false,

    // Производитель
    val vendor: Set<String> = emptySet(),
    val company: Set<String> = emptySet(),
    val address: Set<String> = emptySet(),
    val country: Set<String> = emptySet(),
    val blockType: Set<String> = emptySet(),
    val blockRange: Set<String> = emptySet(),

    // Raw данные
    val rawData: Map<String, String> = emptyMap(),
    val sources: List<String> = emptyList()
) {
    fun toReadableString(): String = buildString {
        appendLine("🔍 Информация о MAC адресе")
        appendLine()

        appendLine("📋 Локальные проверки:")
        appendLine("   MAC адрес: $normalized")
        appendLine("   OUI (первые 3 октета): $oui")
        appendLine("   Тип: ${if (isLocal) "Локально администрируемый" else "Глобально уникальный"}")
        appendLine("   Multicast: ${if (isMulticast) "Да" else "Нет"}")
        appendLine()

        appendLine("─".repeat(40))
        appendLine()

        appendLine("🏭 Производитель (от ${sources.size} источников):")
        val vendors = (vendor + company).filter { it.isNotBlank() }
        appendLine("   Производитель: ${vendors.joinToString(", ").ifEmpty { "—" }}")

        if (address.isNotEmpty()) {
            appendLine("   Адрес: ${address.joinToString(", ")}")
        }
        if (country.isNotEmpty()) {
            appendLine("   Страна: ${country.joinToString(", ")}")
        }
        if (blockRange.isNotEmpty()) {
            appendLine("   Диапазон OUI: ${blockRange.joinToString(", ")}")
        }
        if (blockType.isNotEmpty()) {
            appendLine("   Тип блока: ${blockType.joinToString(", ")}")
        }
        appendLine()

        appendLine("─".repeat(40))
        appendLine("📊 Источники: ${sources.joinToString(", ")}")
    }
}

/**
 * WHOIS информация
 */
@Serializable
data class WhoisInfo(
    val resource: String,
    val type: String, // "ip", "domain", "asn"

    // Основные данные
    val network: String? = null,
    val registrar: String? = null,
    val registrant: String? = null,
    val country: String? = null,

    // Даты
    val registrationDate: String? = null,
    val updateDate: String? = null,
    val expirationDate: String? = null,

    // Статусы и серверы
    val statuses: List<String> = emptyList(),
    val nameservers: List<String> = emptyList(),

    // Контакты
    val abuseContact: String? = null,

    // Raw данные
    val rawData: Map<String, String> = emptyMap(),
    val sources: List<String> = emptyList()
) {
    fun toReadableString(): String = buildString {
        appendLine("📋 WHOIS / RDAP информация")
        appendLine()

        appendLine("Тип: ${type.uppercase()}")
        appendLine("Ресурс: ${resource.uppercase()}")
        appendLine()

        appendLine("─".repeat(40))
        appendLine()

        if (type == "domain") {
            registrar?.let { appendLine("🏢 Регистратор: $it") }
            registrant?.let { appendLine("👤 Владелец: $it") }
            country?.let { appendLine("🌍 Страна: $it") }
            appendLine()

            if (statuses.isNotEmpty()) {
                appendLine("📊 Статусы: ${statuses.joinToString(", ")}")
                appendLine()
            }

            if (nameservers.isNotEmpty()) {
                appendLine("🌐 DNS серверы:")
                nameservers.forEach { appendLine("   • $it") }
                appendLine()
            }
        } else {
            network?.let { appendLine("🔢 Сеть/Диапазон: $it") }
            registrar?.let { appendLine("🏢 Организация: $it") }
            country?.let { appendLine("🌍 Страна: $it") }
            appendLine()
        }

        appendLine("─".repeat(40))
        appendLine()

        appendLine("📅 Даты:")
        registrationDate?.let { appendLine("   Регистрация: $it") }
        updateDate?.let { appendLine("   Обновление: $it") }
        expirationDate?.let { appendLine("   Истечение: $it") }
        appendLine()

        abuseContact?.let {
            appendLine("─".repeat(40))
            appendLine()
            appendLine("🚨 Abuse контакт: $it")
            appendLine()
        }

        appendLine("─".repeat(40))
        appendLine("📊 Источники: ${sources.joinToString(", ")}")
    }
}

/**
 * Информация о телефонном номере
 */
@Serializable
data class PhoneInfo(
    val number: String,
    val isPossible: Boolean = false,
    val isValid: Boolean = false,

    val countryCode: String? = null,
    val regionCode: String? = null,
    val carrier: String? = null,
    val timezones: List<String> = emptyList(),
    val internationalFormat: String? = null,

    // Репутация
    val reputation: String? = null,
    val isSpam: Boolean = false,
    val viewCount: String? = null,

    // Дополнительные данные
    val additionalInfo: Map<String, String> = emptyMap(),

    // Raw данные
    val rawData: Map<String, String> = emptyMap(),
    val sources: List<String> = emptyList()
) {
    fun toReadableString(): String = buildString {
        appendLine("📞 Информация о номере телефона")
        appendLine()

        appendLine("📋 Основные данные:")
        appendLine("   Номер: $number")
        appendLine("   Возможный: ${if (isPossible) "Да" else "Нет"}")
        appendLine("   Валидный: ${if (isValid) "Да" else "Нет"}")
        appendLine()

        countryCode?.let { appendLine("   Код страны: $it") }
        regionCode?.let { appendLine("   Код региона: $it") }
        carrier?.let { appendLine("   Оператор: $it") }

        if (timezones.isNotEmpty()) {
            appendLine("   Часовой пояс: ${timezones.joinToString(", ")}")
        }

        internationalFormat?.let { appendLine("   Международный формат: $it") }
        appendLine()

        if (reputation != null || isSpam || viewCount != null) {
            appendLine("─".repeat(40))
            appendLine()
            appendLine("📊 Репутация:")
            reputation?.let { appendLine("   $it") }
            if (isSpam) appendLine("   ⚠️ Отмечен как СПАМ")
            viewCount?.let { appendLine("   👁️ Просмотров: $it") }
            appendLine()
        }

        if (additionalInfo.isNotEmpty()) {
            appendLine("─".repeat(40))
            appendLine()
            appendLine("ℹ️ Дополнительно:")
            additionalInfo.forEach { (key, value) ->
                appendLine("   $key: $value")
            }
            appendLine()
        }

        appendLine("─".repeat(40))
        appendLine("📊 Источники: ${sources.joinToString(", ")}")
    }
}
