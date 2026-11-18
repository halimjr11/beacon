package com.halimjr11.beacon.common

private val MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val ISO_DATE = Regex("""^(\d{4})-(\d{2})-(\d{2})""")

/**
 * "2026-07-24T10:15:30Z" -> "24 Jul 2026". Only the date part is read (no time zone shift).
 * Null/blank -> "", unparseable or impossible dates -> the raw input.
 */
fun formatPublishedAt(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val (y, m, d) = ISO_DATE.find(raw)?.destructured ?: return raw
    val year = y.toInt()
    val month = m.toInt()
    val day = d.toInt()
    if (month !in 1..12 || day !in 1..daysIn(year, month)) return raw
    return "%02d %s %d".format(day, MONTHS[month - 1], year)
}

private fun daysIn(year: Int, month: Int): Int = when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
