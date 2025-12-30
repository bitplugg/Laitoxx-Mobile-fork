package com.laitoxx.security.data.model

import com.google.gson.annotations.SerializedName

data class IPInfo(
    @SerializedName("ip") val ip: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("continent") val continent: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("region") val region: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("postal") val postal: String = "",
    @SerializedName("capital") val capital: String = "",
    @SerializedName("connection") val connection: Connection? = null,
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null
)

data class Connection(
    @SerializedName("asn") val asn: Int = 0,
    @SerializedName("org") val org: String = "",
    @SerializedName("isp") val isp: String = "",
    @SerializedName("domain") val domain: String = ""
)
