package com.laitoxx.security.data.model

import com.google.gson.annotations.SerializedName

data class SubdomainResult(
    @SerializedName("common_name") val commonName: String = "",
    @SerializedName("name_value") val nameValue: String = ""
)

data class SubdomainList(
    val domain: String,
    val subdomains: List<String>,
    val count: Int
)
