package id.tandara.parent

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.tandara.parent.core.network.NetworkConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
  }

  @Test
  fun configuredBackendHealthIsReachable() {
    val request = Request.Builder()
      .url("${NetworkConfig.baseUrl}api/health")
      .build()
    val client = OkHttpClient.Builder()
      .callTimeout(10, TimeUnit.SECONDS)
      .build()

    client.newCall(request).execute().use { response ->
      assertEquals(200, response.code)
      assertTrue(response.body?.string()?.contains("\"status\":\"ok\"") == true)
    }
  }
}
