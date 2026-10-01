package id.tandara.parent.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.tandara.parent.core.network.NetworkDiagnostics
import id.tandara.parent.core.network.NetworkConfig
import id.tandara.parent.data.session.SessionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(sessionManager: SessionStore) {
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

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        getRetrofit().create(AuthApiService::class.java)
    }

    val parentApiService: ParentApiService by lazy {
        getRetrofit().create(ParentApiService::class.java)
    }

    val attendanceApiService: AttendanceApiService by lazy {
        getRetrofit().create(AttendanceApiService::class.java)
    }

    val permissionApiService: PermissionApiService by lazy {
        getRetrofit().create(PermissionApiService::class.java)
    }
}
