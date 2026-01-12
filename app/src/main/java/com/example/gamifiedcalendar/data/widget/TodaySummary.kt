package com.example.gamifiedcalendar.data.widget

data class TodaySummary(
    val progressPercent: Int,
    val items: List<TodayTodoItem>
)

data class TodayTodoItem(
    val title: String,
    val description: String? = null,
    val done: Boolean = false
)
