package id.tandara.parent.domain.model

enum class AttendanceEventType {
    CHECK_IN,
    CHECK_OUT
}

enum class AttendanceStatus(val labelId: String) {
    PRESENT("Hadir"),
    LATE("Terlambat"),
    SICK("Sakit"),
    PERMISSION("Izin"),
    UNEXCUSED("Alfa"),
    UNKNOWN("Belum Tercatat");

    companion object {
        fun fromBackend(value: String?): AttendanceStatus = when ((value ?: "").uppercase()) {
            "PRESENT" -> PRESENT
            "LATE" -> LATE
            "SICK" -> SICK
            "EXCUSED", "PERMISSION" -> PERMISSION
            "UNEXCUSED", "ALFA" -> UNEXCUSED
            else -> UNKNOWN
        }
    }
}

enum class LeaveType(val displayName: String) {
    SICK("Sakit"),
    PERMISSION("Izin"),
    OTHER("Lainnya");

    companion object {
        fun fromBackend(value: String?): LeaveType = when ((value ?: "").uppercase()) {
            "SICK" -> SICK
            "PERMISSION", "IZIN", "EXCUSED" -> PERMISSION
            else -> OTHER
        }
    }
}

enum class LeaveStatus(val displayName: String) {
    PENDING("Menunggu Persetujuan"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak");

    companion object {
        fun fromBackend(value: String?): LeaveStatus = when ((value ?: "").uppercase()) {
            "APPROVED" -> APPROVED
            "REJECTED" -> REJECTED
            "PENDING" -> PENDING
            else -> PENDING
        }
    }
}
