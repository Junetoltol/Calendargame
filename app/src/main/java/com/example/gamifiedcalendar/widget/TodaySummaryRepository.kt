package com.example.gamifiedcalendar.widget

import android.content.Context

class TodaySummaryRepository {

    fun load(context: Context): TodaySummary {
        // TODO: 나중에 Room/DataStore/암호화 저장소에서 "오늘 ToDo"를 읽어오도록 교체
        val items = listOf(
            TodayTodoItem("Label", done = true),
            TodayTodoItem("Label", done = false),
            TodayTodoItem("Label", done = false)
        )

        val doneCount = items.count { it.done }
        val percent = if (items.isEmpty()) 0 else (doneCount * 100) / items.size

        return TodaySummary(
            progressPercent = percent,
            items = items.take(6) // 위젯 공간 고려해서 최대 6개만
        )
    }
}
