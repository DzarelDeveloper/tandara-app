package id.tandara.parent.core.network

import id.tandara.parent.BuildConfig

data class ServerConfig(
    val host: String,
    val port: Int
) {
    val resolvedHttpBaseUrl: String
        get() = "http://${host.trim()}:${port.coerceIn(1, 65535)}/"

    val resolvedWebSocketBaseUrl: String
        get() = "ws://${host.trim()}:${port.coerceIn(1, 65535)}/"

    fun normalizedKey(): String = "${host.trim().lowercase()}:${port.coerceIn(1, 65535)}"

    companion object {
        const val DEFAULT_PORT = 8000

        private val IPV4_REGEX =
            Regex("""^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""")

        fun validateHost(raw: String): Boolean {
            val host = raw.trim()
            if (host.isBlank()) return false
            return IPV4_REGEX.matches(host) || host.equals("localhost", ignoreCase = true)
        }

        fun validatePort(raw: String): Int? {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) return DEFAULT_PORT
            val value = trimmed.toIntOrNull() ?: return null
            if (value !in 1..65535) return null
            return value
        }

        fun fromBuildConfigFallback(): ServerConfig {
            val raw = BuildConfig.TANDARA_API_BASE_URL.trim().trimEnd('/')
                .removePrefix("http://").removePrefix("https://")
            val parts = raw.split(':', limit = 2)
            val host = parts[0].ifBlank { "192.168.110.101" }
            val port = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 65535) ?: DEFAULT_PORT
            return ServerConfig(host, port)
        }
    }
}
