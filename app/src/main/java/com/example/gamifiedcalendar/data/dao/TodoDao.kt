package com.example.gamifiedcalendar.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.gamifiedcalendar.data.entity.TodoEntity

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("SELECT * FROM todos WHERE targetDate BETWEEN :start AND :end ORDER BY isCompleted ASC, targetDate DESC")
    fun getTodosByDate(start: Long, end: Long): Flow<List<TodoEntity>>

    @Query("SELECT SUM(rewardPoints) FROM todos WHERE isCompleted = 1")
    fun getTotalScore(): Flow<Int?>
}