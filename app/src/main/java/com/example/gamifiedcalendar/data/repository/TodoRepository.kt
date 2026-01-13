package com.example.gamifiedcalendar.data.repository

import java.util.Calendar
import com.example.gamifiedcalendar.data.dao.TodoDao
import com.example.gamifiedcalendar.data.entity.TodoEntity

class TodoRepository(private val todoDao: TodoDao) {
    suspend fun saveTodo(todo: TodoEntity) = todoDao.insertTodo(todo)
    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)
    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)

    fun getTodayTodos() = todoDao.getTodosByDate(getStartOfDay(), getEndOfDay())
    fun getCurrentScore() = todoDao.getTotalScore()

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}