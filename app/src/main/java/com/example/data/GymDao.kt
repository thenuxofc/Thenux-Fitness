package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    // --- Gym Day Queries (Calendar tracking) ---
    @Query("SELECT * FROM gym_days ORDER BY dateString DESC")
    fun getAllGymDays(): Flow<List<GymDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymDay(gymDay: GymDay)

    @Delete
    suspend fun deleteGymDay(gymDay: GymDay)

    @Query("DELETE FROM gym_days WHERE dateString = :dateString")
    suspend fun deleteGymDayByDate(dateString: String)

    @Query("SELECT * FROM gym_days WHERE dateString = :dateString LIMIT 1")
    suspend fun getGymDayByDate(dateString: String): GymDay?

    // --- Gym Reminders Queries ---
    @Query("SELECT * FROM gym_reminders ORDER BY timeString ASC")
    fun getAllReminders(): Flow<List<GymReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: GymReminder)

    @Delete
    suspend fun deleteReminder(reminder: GymReminder)

    @Query("UPDATE gym_reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, isEnabled: Boolean)

    // --- Workout Checklist Tasks Queries (Daily tickers) ---
    @Query("SELECT * FROM workout_tasks WHERE dateString = :dateString ORDER BY id ASC")
    fun getTasksForDate(dateString: String): Flow<List<WorkoutTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: WorkoutTask)

    @Delete
    suspend fun deleteTask(task: WorkoutTask)

    @Query("UPDATE workout_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM workout_tasks WHERE dateString = :dateString")
    suspend fun clearTasksForDate(dateString: String)

    // --- User Profiles Queries ---
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET isSelectedUserProfile = 0")
    suspend fun deselectAllUsers()

    @Query("UPDATE users SET isSelectedUserProfile = 1 WHERE id = :userId")
    suspend fun selectUserProfile(userId: Int)

    @Query("SELECT * FROM users WHERE isSelectedUserProfile = 1 LIMIT 1")
    suspend fun getSelectedUserDirectly(): User?

    @Query("SELECT * FROM users WHERE isSelectedUserProfile = 1")
    fun getSelectedUser(): Flow<User?>
}
