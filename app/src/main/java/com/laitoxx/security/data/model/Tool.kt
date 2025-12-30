package com.laitoxx.security.data.model

data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val icon: Int = 0
)

enum class ToolCategory {
    OSINT,
    NETWORK,
    WEB_SECURITY,
    UTILITIES
}

data class ToolResult(
    val success: Boolean,
    val data: String,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
