package com.example.gamifiedcalendar.widget

data class TodayTodoItem(
    val label: String,
    val done: Boolean
)

data class TodaySummary(
    val progressPercent: Int,
    val items: List<TodayTodoItem>
)
