package com.example.gamifiedcalendar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,          // 할 일 제목
    val description: String?,   // 설명 (날짜 텍스트 등)
    val isCompleted: Boolean = false, // 완료 여부
    val targetDate: Long = System.currentTimeMillis(), // 날짜 필터링용
    val rewardPoints: Int = 100 // 게임 점수
)