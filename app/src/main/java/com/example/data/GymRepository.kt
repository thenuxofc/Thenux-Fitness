package com.example.data

import kotlinx.coroutines.flow.Flow

class GymRepository(private val gymDao: GymDao) {

    // --- Gym Days (Calendar) ---
    val allGymDays: Flow<List<GymDay>> = gymDao.getAllGymDays()

    suspend fun insertGymDay(gymDay: GymDay) = gymDao.insertGymDay(gymDay)

    suspend fun deleteGymDay(gymDay: GymDay) = gymDao.deleteGymDay(gymDay)

    suspend fun deleteGymDayByDate(dateString: String) = gymDao.deleteGymDayByDate(dateString)

    suspend fun getGymDayByDate(dateString: String): GymDay? = gymDao.getGymDayByDate(dateString)

    // --- Reminders ---
    val allReminders: Flow<List<GymReminder>> = gymDao.getAllReminders()

    suspend fun insertReminder(reminder: GymReminder) = gymDao.insertReminder(reminder)

    suspend fun deleteReminder(reminder: GymReminder) = gymDao.deleteReminder(reminder)

    suspend fun updateReminderStatus(id: Int, isEnabled: Boolean) = gymDao.updateReminderStatus(id, isEnabled)

    // --- Workout Tasks ---
    fun getTasksForDate(dateString: String): Flow<List<WorkoutTask>> = gymDao.getTasksForDate(dateString)

    suspend fun insertTask(task: WorkoutTask) = gymDao.insertTask(task)

    suspend fun deleteTask(task: WorkoutTask) = gymDao.deleteTask(task)

    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) = gymDao.updateTaskStatus(id, isCompleted)

    suspend fun clearTasksForDate(dateString: String) = gymDao.clearTasksForDate(dateString)

    // --- Users ---
    val allUsers: Flow<List<User>> = gymDao.getAllUsers()
    val selectedUser: Flow<User?> = gymDao.getSelectedUser()

    suspend fun insertUser(user: User) = gymDao.insertUser(user)
    suspend fun deleteUser(user: User) = gymDao.deleteUser(user)

    suspend fun selectUser(userId: Int) {
        gymDao.deselectAllUsers()
        gymDao.selectUserProfile(userId)
    }

    suspend fun getSelectedUserDirectly(): User? = gymDao.getSelectedUserDirectly()
}
