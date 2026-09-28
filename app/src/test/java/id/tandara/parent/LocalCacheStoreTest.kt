package id.tandara.parent

import id.tandara.parent.data.local.CacheDao
import id.tandara.parent.data.local.CacheEntry
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.domain.model.ParentNotification
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalCacheStoreTest {
    private class MemoryDao : CacheDao {
        val values = linkedMapOf<String, CacheEntry>()
        override suspend fun get(id: String) = values[id]
        override suspend fun put(entry: CacheEntry) { values[entry.cacheId] = entry }
        override suspend fun delete(id: String) { values.remove(id) }
        override suspend fun clearAccount(accountId: String) { values.entries.removeAll { it.value.accountId == accountId } }
    }

    @Test fun todayCacheIsDateAndAccountScoped() = runTest {
        val store = LocalCacheStore(MemoryDao())
        val record = AttendanceRecord("a1", "29 Sep 2026", checkInTime = "07:00", status = AttendanceStatus.PRESENT)
        store.putToday("parent-1", "student-1", "2026-09-29", record)

        assertEquals(record, store.today("parent-1", "student-1", "2026-09-29")?.value)
        assertNull(store.today("parent-1", "student-1", "2026-09-30"))
        assertNull(store.today("parent-2", "student-1", "2026-09-29"))
    }

    @Test fun successfulEmptyTodayRemovesOldRecord() = runTest {
        val store = LocalCacheStore(MemoryDao())
        store.putToday("parent", "student", "2026-09-29", AttendanceRecord("a1", "29 Sep 2026"))
        store.putToday("parent", "student", "2026-09-29", null)
        assertNull(store.today("parent", "student", "2026-09-29"))
    }

    @Test fun repeatedListSynchronizationDoesNotDuplicateIds() = runTest {
        val store = LocalCacheStore(MemoryDao())
        val report = AttendanceRecord("r1", "29 Sep 2026")
        store.putReports("parent", "student", "2026-09", listOf(report))
        val currentReport = report.copy(id = "r2")
        store.putReports("parent", "student", "2026-09", listOf(currentReport, currentReport))
        assertEquals(listOf(currentReport), store.reports("parent", "student", "2026-09")?.value)

        val leave = LeaveRequest(
            "l1", "student", LeaveType.SICK, "2026-09-29", "2026-09-29", "Demam tinggi",
            attachment = id.tandara.parent.domain.model.LeaveAttachment(
                "surat.pdf", "application/pdf", 1024, "content://private/document/1"
            )
        )
        store.putLeave("parent", "student", listOf(leave))
        val currentLeave = leave.copy(id = "l2")
        store.putLeave("parent", "student", listOf(currentLeave, currentLeave))
        val cachedLeave = store.leave("parent", "student")?.value?.single()
        assertEquals("l2", cachedLeave?.id)
        assertNull(cachedLeave?.attachment)

        val notification = ParentNotification("n1", "Info", "Pesan", "2026-09-29T07:00:00")
        store.putNotifications("parent", listOf(notification))
        val currentNotification = notification.copy(id = "n2")
        store.putNotifications("parent", listOf(currentNotification, currentNotification))
        assertEquals(listOf(currentNotification), store.notifications("parent")?.value)
    }

    @Test fun logoutClearOnlyRemovesCurrentAccount() = runTest {
        val dao = MemoryDao()
        val store = LocalCacheStore(dao)
        val notification = ParentNotification("n1", "Info", "Pesan", "now")
        store.putNotifications("parent-1", listOf(notification))
        store.putNotifications("parent-2", listOf(notification))
        store.clearAccount("parent-1")
        assertNull(store.notifications("parent-1"))
        assertTrue(store.notifications("parent-2")?.value?.isNotEmpty() == true)
    }

    @Test fun noAttendanceNeverHasPunctualityLabel() {
        val noAttendance = AttendanceRecord("", "29 Sep 2026", status = AttendanceStatus.UNKNOWN)
        assertNull(noAttendance.punctualityLabel)
        assertTrue(!noAttendance.hasRecordedAttendance)
    }
}
