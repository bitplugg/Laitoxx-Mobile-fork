package com.laitoxx.security.data.repository

import com.laitoxx.security.data.model.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class NetworkRepository {

    suspend fun scanPort(host: String, port: Int, timeout: Int = 2000): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeout)
                    Result.success(true)
                }
            } catch (e: Exception) {
                Result.success(false)
            }
        }

    suspend fun scanPortRange(host: String, startPort: Int, endPort: Int): Result<List<Int>> =
        withContext(Dispatchers.IO) {
            try {
                val openPorts = mutableListOf<Int>()

                for (port in startPort..endPort) {
                    val result = scanPort(host, port, 1000)
                    if (result.isSuccess && result.getOrNull() == true) {
                        openPorts.add(port)
                    }
                }

                Result.success(openPorts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun dnsLookup(domain: String): Result<ToolResult> = withContext(Dispatchers.IO) {
        try {
            val addresses = InetAddress.getAllByName(domain)

            val info = buildString {
                appendLine("Domain: $domain")
                appendLine("Number of addresses: ${addresses.size}")
                appendLine()
                appendLine("IP Addresses:")
                addresses.forEach { addr ->
                    appendLine("  - ${addr.hostAddress}")
                }
            }

            Result.success(ToolResult(
                success = true,
                data = info
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ping(host: String, timeout: Int = 5000): Result<ToolResult> =
        withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val address = InetAddress.getByName(host)
                val reachable = address.isReachable(timeout)
                val endTime = System.currentTimeMillis()
                val responseTime = endTime - startTime

                val info = if (reachable) {
                    """
                    ✓ Host is reachable
                    Host: $host
                    IP: ${address.hostAddress}
                    Response time: ${responseTime}ms
                    """.trimIndent()
                } else {
                    """
                    ✗ Host is not reachable
                    Host: $host
                    Timeout: ${timeout}ms
                    """.trimIndent()
                }

                Result.success(ToolResult(
                    success = reachable,
                    data = info
                ))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getCommonPorts(): Map<Int, String> = mapOf(
        21 to "FTP",
        22 to "SSH",
        23 to "Telnet",
        25 to "SMTP",
        53 to "DNS",
        80 to "HTTP",
        110 to "POP3",
        143 to "IMAP",
        443 to "HTTPS",
        445 to "SMB",
        3306 to "MySQL",
        3389 to "RDP",
        5432 to "PostgreSQL",
        5900 to "VNC",
        8080 to "HTTP-Proxy",
        8443 to "HTTPS-Alt"
    )
}
