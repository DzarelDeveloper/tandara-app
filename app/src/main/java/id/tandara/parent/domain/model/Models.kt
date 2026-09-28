package id.tandara.parent.domain.model

import kotlin.math.round

data class Parent(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val role: String = "Orang Tua/Wali",
    val email: String? = null
)

data class Student(
    val id: String,
    val nisn: String,
    val name: String,
    val className: String,
    val photoUrl: String? = null
)

data class AttendanceEvent(
    val id: String,
    val studentId: String,
    val eventType: AttendanceEventType,
    val timestamp: String,
    val deviceName: String? = null,
    val note: String? = null
)

data class AttendanceSummary(
    val presentCount: Int = 0,
    val lateCount: Int = 0,
    val permissionCount: Int = 0,
    val unexcusedCount: Int = 0,
    val monthYear: String = ""
) {
    fun hasAnyAttendanceData(): Boolean {
        return presentCount > 0 || lateCount > 0 || permissionCount > 0 || unexcusedCount > 0
    }

    fun attendancePercentage(): Double? {
        val eligible = presentCount + lateCount + permissionCount + unexcusedCount
        if (eligible == 0) return null
        return (presentCount.toDouble() / eligible.toDouble()) * 100.0
    }

    fun attendancePercentageDisplay(): String {
        val percentage = attendancePercentage() ?: return "Belum ada data"
        return "${round(percentage).toInt()}%"
    }
}

data class AttendanceRecord(
    val id: String,
    val date: String,
    val dayName: String = "",
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val status: AttendanceStatus = AttendanceStatus.UNKNOWN,
    val note: String? = null,
    val approvalStatus: String? = null
) {
    val hasRecordedAttendance: Boolean
        get() = status != AttendanceStatus.UNKNOWN && (checkInTime != null || checkOutTime != null || id.isNotBlank())

    val hasRecordedCheckIn: Boolean
        get() = checkInTime != null && status != AttendanceStatus.UNKNOWN

    val punctualityLabel: String?
        get() = when {
            !hasRecordedCheckIn -> null
            status == AttendanceStatus.LATE -> "Terlambat"
            status == AttendanceStatus.PRESENT -> "Tepat waktu"
            else -> null
        }

    val isOnTimeCheckIn: Boolean
        get() = hasRecordedCheckIn && status == AttendanceStatus.PRESENT
}

data class LeaveAttachment(
    val fileName: String,
    val fileType: String,
    val fileSizeBytes: Long = 0L,
    val localUriString: String? = null
)

data class LeaveRequest(
    val id: String = "",
    val studentId: String,
    val type: LeaveType,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val attachment: LeaveAttachment? = null,
    val status: LeaveStatus = LeaveStatus.PENDING,
    val submittedAt: String = ""
)

data class ParentNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val relatedStudentId: String? = null,
    val relatedStudentName: String? = null,
    val type: String? = null
)
