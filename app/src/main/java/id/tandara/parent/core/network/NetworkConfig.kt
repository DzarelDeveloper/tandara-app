package id.tandara.parent.core.network

import id.tandara.parent.BuildConfig

object NetworkConfig {
    val baseUrl: String = BuildConfig.TANDARA_API_BASE_URL.let {
        require(it.startsWith("http://") || it.startsWith("https://")) { "TANDARA_API_BASE_URL must be an HTTP(S) URL" }
        if (it.endsWith('/')) it else "$it/"
    }
}
