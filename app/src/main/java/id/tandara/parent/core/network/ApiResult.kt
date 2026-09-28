package id.tandara.parent.core.network

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
    data class BackendUnavailable(
        val message: String = "Backend belum terhubung. Data belum dapat disimpan."
    ) : ApiResult<Nothing>
}
