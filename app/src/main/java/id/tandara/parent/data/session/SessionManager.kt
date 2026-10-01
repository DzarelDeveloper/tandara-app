package id.tandara.parent.data.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tandara_session_prefs")

data class SessionUser(
    val isAuthenticated: Boolean = false,
    val displayName: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val role: String = "PARENT",
    val parentId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val parentPhotoUrl: String = "",
    val studentPhotoUrl: String = ""
)

interface SessionStore {
    val sessionFlow: Flow<SessionUser>
    fun getAccessToken(): String?
    suspend fun saveSession(
        accessToken: String,
        parentId: String,
        displayName: String,
        phoneNumber: String,
        username: String,
        role: String,
        studentId: String,
        studentName: String,
        studentNis: String,
        studentClass: String,
        parentPhotoUrl: String = "",
        studentPhotoUrl: String = ""
    )
    suspend fun clearSession()
}

class SessionManager(private val context: Context) : SessionStore {

    companion object {
        private val KEY_IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_PARENT_ID = stringPreferencesKey("parent_id")
        private val KEY_STUDENT_ID = stringPreferencesKey("student_id")
        private val KEY_STUDENT_NAME = stringPreferencesKey("student_name")
        private val KEY_STUDENT_NIS = stringPreferencesKey("student_nis")
        private val KEY_STUDENT_CLASS = stringPreferencesKey("student_class")
        private val KEY_PARENT_PHOTO_URL = stringPreferencesKey("parent_photo_url")
        private val KEY_STUDENT_PHOTO_URL = stringPreferencesKey("student_photo_url")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        private val KEY_APPEARANCE = stringPreferencesKey("app_appearance")

        private const val SECURE_PREFS = "tandara_auth_secure"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    private val securePrefs by lazy {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(context, SECURE_PREFS, key, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    override val sessionFlow: Flow<SessionUser> = context.dataStore.data.map { prefs ->
        val isAuth = prefs[KEY_IS_AUTHENTICATED] ?: false
        val name = prefs[KEY_DISPLAY_NAME] ?: ""
        val phone = prefs[KEY_PHONE_NUMBER] ?: ""
        val username = prefs[KEY_USERNAME] ?: ""
        val role = prefs[KEY_ROLE] ?: "PARENT"
        SessionUser(
            isAuthenticated = isAuth,
            displayName = name,
            phoneNumber = phone,
            username = username,
            role = role,
            parentId = prefs[KEY_PARENT_ID] ?: username,
            studentId = prefs[KEY_STUDENT_ID] ?: "",
            studentName = prefs[KEY_STUDENT_NAME] ?: "",
            studentNis = prefs[KEY_STUDENT_NIS] ?: "",
            studentClass = prefs[KEY_STUDENT_CLASS] ?: "",
            parentPhotoUrl = prefs[KEY_PARENT_PHOTO_URL] ?: "",
            studentPhotoUrl = prefs[KEY_STUDENT_PHOTO_URL] ?: ""
        )
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: false
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAS_COMPLETED_ONBOARDING] ?: false
    }

    val appearanceFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APPEARANCE] ?: "light"
    }

    suspend fun setAppearance(appearance: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APPEARANCE] = appearance
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_COMPLETED_ONBOARDING] = true
        }
    }

    override suspend fun saveSession(accessToken: String, parentId: String, displayName: String, phoneNumber: String, username: String, role: String, studentId: String, studentName: String, studentNis: String, studentClass: String, parentPhotoUrl: String, studentPhotoUrl: String) {
        securePrefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_AUTHENTICATED] = true
            prefs[KEY_DISPLAY_NAME] = displayName
            prefs[KEY_PHONE_NUMBER] = phoneNumber
            prefs[KEY_USERNAME] = username
            prefs[KEY_ROLE] = role
            prefs[KEY_PARENT_ID] = parentId
            prefs[KEY_STUDENT_ID] = studentId
            prefs[KEY_STUDENT_NAME] = studentName
            prefs[KEY_STUDENT_NIS] = studentNis
            prefs[KEY_STUDENT_CLASS] = studentClass
            prefs[KEY_PARENT_PHOTO_URL] = parentPhotoUrl
            prefs[KEY_STUDENT_PHOTO_URL] = studentPhotoUrl
        }
    }

    override suspend fun clearSession() {
        securePrefs.edit().remove(KEY_ACCESS_TOKEN).apply()
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_IS_AUTHENTICATED)
            prefs.remove(KEY_DISPLAY_NAME)
            prefs.remove(KEY_PHONE_NUMBER)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_PARENT_ID)
            prefs.remove(KEY_STUDENT_ID)
            prefs.remove(KEY_STUDENT_NAME)
            prefs.remove(KEY_STUDENT_NIS)
            prefs.remove(KEY_STUDENT_CLASS)
            prefs.remove(KEY_PARENT_PHOTO_URL)
            prefs.remove(KEY_STUDENT_PHOTO_URL)
            // Note: harmless preferences like notification toggle are preserved as required
        }
    }

    override fun getAccessToken(): String? = securePrefs.getString(KEY_ACCESS_TOKEN, null)

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
