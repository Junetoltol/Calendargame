package com.example.gamifiedcalendar.data.calendar

import android.content.ContentResolver
import java.util.Calendar

class CalendarRepository(
    contentResolver: ContentResolver
) {
    private val dataSource = CalendarProviderDataSource(contentResolver)

    /** dayMillis가 속한 날짜의 일정 목록 반환 */
    fun getEventsForDay(dayMillis: Long): List<CalendarEvent> {
        val (startMillis, endMillis) = dayRangeMillis(dayMillis)
        return dataSource.getEventsBetween(startMillis, endMillis)
    }

    /** [00:00, 다음날 00:00) 범위(ms) */
    private fun dayRangeMillis(dayMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }
}
