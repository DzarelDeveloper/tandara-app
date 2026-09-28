package id.tandara.parent.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val localeId = Locale("id", "ID")

    fun formatToIndonesianDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", localeId)
        return sdf.format(Date(millis))
    }

    fun formatToShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", localeId)
        return sdf.format(Date(millis))
    }

    fun formatToApiDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(millis))
    }

    fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", localeId)
        return sdf.format(Date())
    }

    fun getIndonesianGreeting(name: String = ""): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..18 -> "Selamat sore"
            else -> "Selamat malam"
        }
        return if (name.isNotBlank()) "$greeting, $name" else greeting
    }

    fun isDateRangeValid(startMillis: Long?, endMillis: Long?): Boolean {
        if (startMillis == null || endMillis == null) return false
        return endMillis >= startMillis
    }
}
