package com.example.gamifiedcalendar.data.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.CalendarContract

class CalendarProviderDataSource(
    private val contentResolver: ContentResolver
) {
    fun getEventsBetween(startMillis: Long, endMillis: Long): List<CalendarEvent> {
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        val uri = builder.build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION
        )

        val out = mutableListOf<CalendarEvent>()

        contentResolver.query(
            uri,
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )?.use { c ->
            val idxId = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val idxTitle = c.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val idxBegin = c.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val idxEnd = c.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val idxAllDay = c.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val idxLoc = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)

            while (c.moveToNext()) {
                out += CalendarEvent(
                    id = c.getLong(idxId),
                    title = c.getString(idxTitle).orEmpty(),
                    begin = c.getLong(idxBegin),
                    end = c.getLong(idxEnd),
                    allDay = c.getInt(idxAllDay) == 1,
                    location = c.getString(idxLoc)
                )
            }
        }

        return out
    }
}
