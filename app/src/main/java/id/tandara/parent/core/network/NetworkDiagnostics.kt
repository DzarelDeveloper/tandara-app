package id.tandara.parent.core.network

import android.util.Log
import com.squareup.moshi.JsonDataException
import id.tandara.parent.BuildConfig
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException

object NetworkDiagnostics {
    private const val TAG = "TandaraNetwork"

    fun logHttpStatus(method: String, path: String, status: Int) {
        if (!BuildConfig.DEBUG) return
        val category = if (status == 401 || status == 403) "AUTH ERROR" else "HTTP STATUS"
        Log.w(TAG, "$category: $method $path returned HTTP $status")
    }

    fun logInvalidServerResponse(context: String) {
        if (!BuildConfig.DEBUG) return
        Log.w(TAG, "INVALID SERVER RESPONSE: $context")
    }

    fun logFailure(context: String, error: Throwable) {
        if (!BuildConfig.DEBUG) return
        val category = when (error) {
            is SocketTimeoutException -> "TIMEOUT"
            is JsonDataException, is EOFException -> "INVALID SERVER RESPONSE"
            is IOException -> "NETWORK ERROR"
            else -> "INVALID SERVER RESPONSE"
        }
        Log.w(TAG, "$category: $context (${error.javaClass.simpleName})")
    }
}