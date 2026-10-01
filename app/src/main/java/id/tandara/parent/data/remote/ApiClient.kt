package id.tandara.parent.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.tandara.parent.core.network.NetworkConfigManager
import id.tandara.parent.core.network.NetworkDiagnostics
import id.tandara.parent.data.session.SessionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

open class ApiClient(
    private val sessionManager: SessionStore,
    private val networkConfigManager: NetworkConfigManager
) {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = sessionManager.getAccessToken()
                val request = if (token.isNullOrBlank()) chain.request() else chain.request().newBuilder()
                    .header("Authorization", "Bearer $token").build()
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (!response.isSuccessful) {
                    NetworkDiagnostics.logHttpStatus(
                        chain.request().method,
                        chain.request().url.encodedPath,
                        response.code
                    )
                }
                response
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Volatile private var cachedRetrofitKey: String? = null
    @Volatile private var cachedRetrofit: Retrofit? = null

    private fun getRetrofit(): Retrofit {
        val key = networkConfigManager.activeConfig.value.normalizedKey()
        val current = cachedRetrofit
        if (current != null && cachedRetrofitKey == key) return current
        synchronized(this) {
            val secondCheck = cachedRetrofit
            val secondKey = cachedRetrofitKey
            if (secondCheck != null && secondKey == key) return secondCheck
            val fresh = Retrofit.Builder()
                .baseUrl(networkConfigManager.baseHttpUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            cachedRetrofit = fresh
            cachedRetrofitKey = key
            return fresh
        }
    }

    fun invalidateServices() {
        synchronized(this) {
            cachedRetrofit = null
            cachedRetrofitKey = null
        }
    }

    open val authApiService: AuthApiService
        get() = getRetrofit().create(AuthApiService::class.java)

    open val parentApiService: ParentApiService
        get() = getRetrofit().create(ParentApiService::class.java)

    open val attendanceApiService: AttendanceApiService
        get() = getRetrofit().create(AttendanceApiService::class.java)

    open val permissionApiService: PermissionApiService
        get() = getRetrofit().create(PermissionApiService::class.java)
}
