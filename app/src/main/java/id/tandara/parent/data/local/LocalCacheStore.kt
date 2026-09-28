package id.tandara.parent.data.local

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.model.Student

data class CachedValue<T>(val value: T, val fetchedAt: Long)

class LocalCacheStore(private val dao: CacheDao) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val studentAdapter = moshi.adapter(Student::class.java)
    private val attendanceAdapter = moshi.adapter(AttendanceRecord::class.java)
    private val attendanceListAdapter: JsonAdapter<List<AttendanceRecord>> = moshi.adapter(Types.newParameterizedType(List::class.java, AttendanceRecord::class.java))
    private val leaveListAdapter: JsonAdapter<List<LeaveRequest>> = moshi.adapter(Types.newParameterizedType(List::class.java, LeaveRequest::class.java))
    private val notificationListAdapter: JsonAdapter<List<ParentNotification>> = moshi.adapter(Types.newParameterizedType(List::class.java, ParentNotification::class.java))

    private fun id(account: String, kind: String, key: String) = "$account|$kind|$key"
    private suspend fun put(account: String, student: String?, kind: String, key: String, payload: String) {
        dao.put(CacheEntry(id(account, kind, key), account, student, kind, key, payload, System.currentTimeMillis()))
    }
    private suspend fun <T> get(account: String, kind: String, key: String, adapter: JsonAdapter<T>): CachedValue<T>? {
        val entry = dao.get(id(account, kind, key)) ?: return null
        return adapter.fromJson(entry.payload)?.let { CachedValue(it, entry.fetchedAt) }
    }

    suspend fun putStudent(account: String, value: Student) = put(account, value.id, "student", "assigned", studentAdapter.toJson(value))
    suspend fun student(account: String) = get(account, "student", "assigned", studentAdapter)
    suspend fun putToday(account: String, student: String, date: String, value: AttendanceRecord?) {
        val cacheId = id(account, "attendance_today", "$student:$date")
        if (value == null) dao.delete(cacheId) else put(account, student, "attendance_today", "$student:$date", attendanceAdapter.toJson(value))
    }
    suspend fun today(account: String, student: String, date: String) = get(account, "attendance_today", "$student:$date", attendanceAdapter)
    suspend fun putReports(account: String, student: String, period: String, values: List<AttendanceRecord>) =
        put(account, student, "reports", "$student:$period", attendanceListAdapter.toJson(values.distinctBy { it.id.ifBlank { it.date } }))
    suspend fun reports(account: String, student: String, period: String) = get(account, "reports", "$student:$period", attendanceListAdapter)
    suspend fun putLeave(account: String, student: String, values: List<LeaveRequest>) =
        put(account, student, "leave", student, leaveListAdapter.toJson(values.map { it.copy(attachment = null) }.distinctBy { it.id.ifBlank { "${it.startDate}|${it.type}|${it.reason}" } }))
    suspend fun leave(account: String, student: String) = get(account, "leave", student, leaveListAdapter)
    suspend fun putNotifications(account: String, values: List<ParentNotification>) =
        put(account, null, "notifications", "all", notificationListAdapter.toJson(values.distinctBy { it.id }))
    suspend fun notifications(account: String) = get(account, "notifications", "all", notificationListAdapter)
    suspend fun clearAccount(account: String) = dao.clearAccount(account)
}
