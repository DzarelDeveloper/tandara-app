package id.tandara.parent.core.common

object PhoneUtils {
    /**
     * Normalizes Indonesian phone numbers for comparison.
     * E.g., "+6281234567890", "0812-3456-7890", "6281234567890" -> "081234567890"
     */
    fun normalizeIndonesianPhoneNumber(raw: String): String {
        val digitsOnly = raw.replace(Regex("[^0-9+]"), "")
        return when {
            digitsOnly.startsWith("+62") -> "0" + digitsOnly.substring(3)
            digitsOnly.startsWith("62") -> "0" + digitsOnly.substring(2)
            digitsOnly.startsWith("0") -> digitsOnly
            digitsOnly.isNotEmpty() -> "0$digitsOnly"
            else -> ""
        }
    }

    /**
     * Masks an Indonesian phone number for privacy display.
     * E.g., "081234567890" -> "0812••••7890"
     */
    fun maskPhoneNumber(phone: String): String {
        val clean = phone.replace(Regex("[^0-9]"), "")
        if (clean.length < 8) return phone
        val prefix = clean.take(4)
        val suffix = clean.takeLast(4)
        return "$prefix••••$suffix"
    }

    /**
     * Basic validation for Indonesian phone numbers.
     * Must be 10-15 digits after normalization and start with "08".
     */
    fun isValidIndonesianPhoneNumber(raw: String): Boolean {
        val normalized = normalizeIndonesianPhoneNumber(raw)
        return normalized.startsWith("08") && normalized.length in 10..15
    }
}
